package com.maju

import com.maju.annotations.Converter
import com.maju.annotations.RepositoryProxy
import com.maju.entities.MethodEntity
import com.maju.entities.ParameterEntity
import com.maju.generators.repository.ConverterEntityGenerator
import com.maju.generators.repository.MethodEntityGenerator
import com.maju.generators.repository.RepositoryEntityGenerator
import com.maju.generators.repository.proxy.RepositoryProxyGenerator
import com.google.auto.service.AutoService
import com.maju.entities.RepositoryType
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.classinspector.elements.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import com.maju.utils.*
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.tools.Diagnostic
import kotlin.reflect.KClass

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
        return mutableSetOf(Converter::class.java.name, RepositoryProxy::class.java.name)
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
            val kmClazz = (repository as TypeElement).toImmutableKmClass()
            printNote("Generating the Proxy of $kmClazz")

            val inheritedInterfacesKmClasses = roundEnv.getAllSuperTypes(repository)
            printNote("The class ${kmClazz.name} inherit from the interfaces: ${inheritedInterfacesKmClasses.map { it.name }}")

            val inheritedFunctions = inheritedInterfacesKmClasses.flatMap { it.functions }
            printNote("The class ${kmClazz.name} owns the inheritted functions: ${inheritedFunctions.joinToString()}")

            val isPanacheEntity = kmClazz.supertypes.map { it.className().canonicalName }.contains(PanacheRepository::class.qualifiedName)

            val repositoryProxyAnnotation = repository.getAnnotation(RepositoryProxy::class.java)
            val componentModel = repositoryProxyAnnotation.componentModel
            val injectionStrategy = repositoryProxyAnnotation.injectionStrategy

            val converter = repository.getAnnotationClassValue<RepositoryProxy> { converter }
            val element = processingEnv.typeUtils.asElement(converter)
            val converterType = IConverter::class.toType()
            val implementedConverterElement = element as TypeElement

            val isSubTypeOfConverter = implementedConverterElement.isSubType(converterType)

            if (!isSubTypeOfConverter) {
                printError("The converter: ${converterType.className.simpleName} has to implement the interface IConverter")
                continue
            }

            val iConverter = implementedConverterElement.toImmutableKmClass().supertypes.map { it.toType() }
                .findLast { it.className == converterType.className }

            if (iConverter == null) {
                printError("The $repository::class doesn't inherit from the interface IConverter")
                continue
            }

            val modelType = iConverter.arguments[0]
            val dtoType = iConverter.arguments[1]

            val methods = mutableListOf<MethodEntity>()

            val functions = kmClazz.functions.plus(inheritedFunctions)

            for (function in functions) {
                val fName = function.name

                val fReturnType = function.returnType.toType()

                val fConvertedReturnType = getType(fReturnType, modelType, dtoType)

                val fParameters = function.valueParameters
                    .map {
                        ParameterEntity(it.name, getType(it.type!!.toType(), modelType, dtoType))
                    }

                val method = MethodEntityGenerator(
                    name = fName,
                    parameters = fParameters,
                    returnType = fConvertedReturnType
                ).generate()

                methods.add(method)
            }

            val converterEntity = ConverterEntityGenerator(
                implementedConverterElement.toType()
            ).generate()


            val repositoryEntity = RepositoryEntityGenerator(
                modelClass = modelType,
                dtoClass = dtoType,
                type = kmClazz.toType(),
                converter = converterEntity,
                methods = methods,
                name = "${kmClazz.name}Proxy",
                repositoryType = if (isPanacheEntity) RepositoryType.PANACHE_ENTITY else RepositoryType.DEFAULT_ENTITY
            ).generate()

            val packageName = processingEnv.elementUtils.getPackageOf(repository).toString()

            val fileSpec =
                RepositoryProxyGenerator(packageName, repositoryEntity, injectionStrategy, componentModel).generate()

            printNote("Writng the file: ${repository.simpleName}Proxy.kt")
            generateClass(fileSpec, "${repository.simpleName}Proxy")
        }

        return true
    }

    private fun RoundEnvironment.getInterfaceByName(name: String): TypeElement? {
        return rootElements.filter { it.kind == ElementKind.INTERFACE }.map { it as TypeElement }
            .find { it.qualifiedName.toString() == name }
    }

    @KotlinPoetMetadataPreview
    private fun RoundEnvironment.getAllSuperTypes(typeElement: TypeElement): List<ImmutableKmClass> {
        val mutableList = mutableListOf<ImmutableKmClass>()
        val kmClass = typeElement.toImmutableKmClass()
        val superTypeElements = kmClass.supertypes.mapNotNull { getInterfaceByName(it.className().canonicalName) }
        val superClasses =
            superTypeElements.mapNotNull { getInterfaceByName(it.qualifiedName.toString())?.toImmutableKmClass() }
        mutableList.addAll(superClasses)

        for (superTypeElement in superTypeElements) {
            mutableList.addAll(getAllSuperTypes(superTypeElement))
        }

        return mutableList
    }

    @KotlinPoetMetadataPreview
    private fun generateClass(fileSpec: FileSpec, className: String) {

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        val fileName = "${className}Generated"
        fileSpec.writeTo(File("${kaptKotlinGeneratedDir}/$fileName.kt"))
    }

    private fun getType(
        rType: CKType,
        modelType: CKType,
        dtoType: CKType,
    ): CKType {
        val listOfModelType = LIST.parameterizedToType(modelType)
        val listOfDTOType = LIST.parameterizedToType(dtoType)
        val modelTypeNullable = modelType.copy(isNullable = true)
        val dtoTypeNullable = dtoType.copy(isNullable = true)

        return when (rType) {
            modelType -> {
                dtoType
            }
            listOfModelType -> {
                listOfDTOType
            }
            modelTypeNullable -> {
                dtoTypeNullable
            }
            else -> {
                rType
            }
        }
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
        println(msg)
    }
}

inline fun <reified T : Annotation> Element.getAnnotationClassValue(f: T.() -> KClass<*>): TypeMirror = try {
    getAnnotation(T::class.java).f()
    throw Exception("Expected to get a MirroredTypeException")
} catch (e: MirroredTypeException) {
    e.typeMirror
}
