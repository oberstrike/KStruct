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
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.classinspector.elements.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import com.maju.utils.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
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
            val repositoryProxyAnnotation = repository.getAnnotation(RepositoryProxy::class.java)
            val componentModel = repositoryProxyAnnotation.componentModel
            val injectionStrategy = repositoryProxyAnnotation.injectionStrategy

            val converter = repository.getAnnotationClassValue<RepositoryProxy> { converter }
            val element = processingEnv.typeUtils.asElement(converter)
            val converterType = IConverter::class.toType()
            val implementedConverterElement = element as TypeElement

            val isSubTypeOfConverter = implementedConverterElement.isSubType(converterType)

            if(!isSubTypeOfConverter){
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

            val functions = kmClazz.functions
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
                name = "${kmClazz.name}Proxy"
            ).generate()

            val packageName = processingEnv.elementUtils.getPackageOf(repository).toString()

            val fileSpec =
                RepositoryProxyGenerator(packageName, repositoryEntity, injectionStrategy, componentModel).generate()
            generateClass(fileSpec, "${repository.simpleName}Proxy")
        }

        return true
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

        return when (rType) {
            modelType -> {
                dtoType
            }
            listOfModelType -> {
                listOfDTOType
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
}

inline fun <reified T : Annotation> Element.getAnnotationClassValue(f: T.() -> KClass<*>): TypeMirror = try {
    getAnnotation(T::class.java).f()
    throw Exception("Expected to get a MirroredTypeException")
} catch (e: MirroredTypeException) {
    e.typeMirror
}
