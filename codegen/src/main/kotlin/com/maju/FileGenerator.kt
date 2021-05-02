package com.maju

import com.maju.annotations.RepositoryProxy
import com.maju.entities.MethodEntity
import com.maju.entities.ParameterEntity
import com.maju.generators.entities.ConverterEntityGenerator
import com.maju.generators.entities.MethodEntityGenerator
import com.maju.generators.entities.RepositoryEntityGenerator
import com.maju.generators.repository.proxy.RepositoryProxyGenerator
import com.google.auto.service.AutoService
import com.maju.annotations.IConverter
import com.maju.entities.ConverterEntity
import com.maju.entities.PanacheEntity
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.classinspector.elements.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.maju.utils.*
import com.squareup.kotlinpoet.metadata.*
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(FileGenerator.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class FileGenerator : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    @KotlinPoetMetadataPreview
    private lateinit var elementClassInspector: ClassInspector

    @ExperimentalStdlibApi
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(RepositoryProxy::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    @KotlinPoetMetadataPreview
    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        val elementUtils = processingEnv.elementUtils
        val types = processingEnv.typeUtils
        elementClassInspector = ElementsClassInspector.create(elementUtils, types)

        val repositories = roundEnv.getElementsAnnotatedWith(RepositoryProxy::class.java) ?: mutableSetOf()

        for (repository in repositories) {
            val repositoryKmClazz = (repository as TypeElement).toImmutableKmClass()
            val repositoryName = repositoryKmClazz.name

            val inheritedInterfacesKmClasses = roundEnv.getAllSuperTypesOfElementRecursive(repository)
            printNote("Generating the Proxy of $repositoryName")
            printNote("The class $repositoryName inherits from the interfaces: ${inheritedInterfacesKmClasses.map { it.name }}")

            val inheritedFunctions = inheritedInterfacesKmClasses.flatMap { it.functions }
            printNote("The class $repositoryName owns the inherited functions: ${
                inheritedFunctions.joinToString { it.name }
            }"
            )

            val panacheKmType =
                repositoryKmClazz.supertypes
                    .findLast { (PanacheRepository::class.qualifiedName) == it.className().canonicalName }

            var panacheEntity: PanacheEntity? = null

            if (panacheKmType != null) {
                printNote("The class $repositoryName inherits the Repository: ${PanacheRepository::class.qualifiedName}")
                panacheEntity = PanacheEntity(panacheKmType.arguments.first().type!!.toType())
            }

            val repositoryProxyAnnotation = repository.getAnnotation(RepositoryProxy::class.java)

            val componentModel = repositoryProxyAnnotation.componentModel
            printNote("The proxy of the class: $repositoryName will use the component-model: $componentModel")

            val injectionStrategy = repositoryProxyAnnotation.injectionStrategy
            printNote("The proxy of the class: $repositoryName will use the injection-strategy: $injectionStrategy")
            println("test")


            //Get the converter of the Repository
            val converterTypeMirrors = repository.getAnnotationClassValues<RepositoryProxy> { converters }
            val converterType = IConverter::class.toType()

            val converterEntities = converterTypeMirrors.asSequence().map(processingEnv.typeUtils::asElement)
                .map { it as TypeElement }
                .filter { it.isSubType(converterType) }
                .map {
                    it.toImmutableKmClass().supertypes
                        .map { supertype -> supertype.toType() }
                        .findLast { supertype -> supertype.className == converterType.className } to it
                }.map {
                    ConverterEntityGenerator(
                        it.second.toType(),
                        it.first!!.arguments[0],
                        it.first!!.arguments[1]
                    ).generate()
                }.toList()

            val methodEntities = mutableListOf<MethodEntity>()

            val kmFunctions = repositoryKmClazz.functions.plus(inheritedFunctions)

            for (function in kmFunctions) {
                val isProtected = function.isPrivate || function.isProtected
                if (isProtected) continue

                val methodName = function.name
                val methodReturnType = function.returnType.toType()
                val methodConvertedReturnType = convert(converterEntities, methodReturnType)
                val methodIsSuspend = function.isSuspend

                val methodParameters = function.valueParameters
                    .map { parameter ->
                        val parameterName = parameter.name
                        val parameterCKType = parameter.type?.toType()!!
                        val parameterType = convert(converterEntities, parameterCKType)
                        ParameterEntity(parameterName, parameterType ?: parameterCKType)
                    }


                val methodEntity = MethodEntityGenerator(
                    name = methodName,
                    parameters = methodParameters,
                    returnType = methodConvertedReturnType ?: methodReturnType,
                    isSuspend = methodIsSuspend
                ).generate()

                methodEntities.add(methodEntity)
            }


            val repositoryEntity = RepositoryEntityGenerator(
                type = repositoryKmClazz.toType(),
                converters = converterEntities,
                methods = methodEntities,
                name = "${repositoryName}Proxy",
                panacheEntity = panacheEntity
            ).generate()

            val targetPackageName = processingEnv.elementUtils.getPackageOf(repository).toString()

            val fileSpec =
                RepositoryProxyGenerator(
                    targetPackageName,
                    repositoryEntity,
                    injectionStrategy,
                    componentModel
                ).generate()

            printNote("Writing the file: ${repositoryName}Proxy.kt")
            generateClass(fileSpec, "${repositoryName}Proxy")
        }

        return true
    }

    private fun convert(converters: List<ConverterEntity>, originType: CKType): CKType? {
        return converters.mapNotNull { it.convert(originType) }.firstOrNull()
    }

    private fun RoundEnvironment.getInterfaceByName(name: String): TypeElement? {
        return rootElements.filter { it.kind == ElementKind.INTERFACE }.map { it as TypeElement }
            .find { it.qualifiedName.toString() == name }
    }

    @KotlinPoetMetadataPreview
    private fun RoundEnvironment.getAllSuperTypesOfElementRecursive(typeElement: TypeElement): List<ImmutableKmClass> {
        val mutableList = mutableListOf<ImmutableKmClass>()
        val kmClass = typeElement.toImmutableKmClass()
        val superTypeElements =
            kmClass.supertypes.mapNotNull { getInterfaceByName(it.className().canonicalName) }
        val superClasses =
            superTypeElements.mapNotNull { getInterfaceByName(it.qualifiedName.toString())?.toImmutableKmClass() }
        mutableList.addAll(superClasses)

        for (superTypeElement in superTypeElements) {
            mutableList.addAll(getAllSuperTypesOfElementRecursive(superTypeElement))
        }

        return mutableList
    }

    @KotlinPoetMetadataPreview
    private fun generateClass(fileSpec: FileSpec, className: String) {

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        val fileName = "${className}Generated"
        fileSpec.writeTo(File("${kaptKotlinGeneratedDir}/$fileName.kt"))
    }


    private fun printError(msg: String) {
        processingEnv.messager.printMessage(
            Diagnostic.Kind.ERROR,
            msg
        )
    }

    private fun printNote(msg: String) {
        processingEnv.messager.printMessage(
            Diagnostic.Kind.NOTE,
            msg
        )
    }
}

