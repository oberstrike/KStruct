package com.maju.generators.repository.proxy


import com.maju.annotations.*
import com.maju.entities.*
import com.maju.generators.repository.IGenerator
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.maju.utils.*
import com.squareup.kotlinpoet.metadata.isNullable
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import io.quarkus.hibernate.orm.panache.kotlin.PanacheQuery
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import kotlinx.metadata.KmClassifier

class RepositoryProxyGenerator(
    private val packageName: String,
    private val repositoryEntity: RepositoryEntity,
    private val injectionStrategy: InjectionStrategy = InjectionStrategy.CONSTRUCTOR,
    private val componentModel: String = "default"
) : IGenerator<FileSpec> {

    @KotlinPoetMetadataPreview
    override fun generate(): FileSpec {


        val (repositoryClassName, repositoryProxyTypeSpecBuilder) = generateRepositoryProxyTypeSpec()

        injectionStrategy.dependencyGenerator.applyDependency(
            typeSpecBuilder = repositoryProxyTypeSpecBuilder,
            repositoryClassName = repositoryClassName,
            converterClassNames = repositoryEntity.converters.map { it.type.className },
            componentModel = componentModel
        )

        for (methodEntity in repositoryEntity.methods) {
            for (converterEntity in repositoryEntity.converters) {
                val targetType = converterEntity.targetType

                val parameters = methodEntity.parameters.map { it.type }
                val returnType = methodEntity.returnType
                val isReturnTypeDTO = returnType.className == targetType.className
                val isReturnTypeListOfDTO = returnType.className == LIST && returnType.hasArgument(targetType)

                if (parameters.contains(targetType) || isReturnTypeDTO || isReturnTypeListOfDTO) {
                    repositoryProxyTypeSpecBuilder.addFunction(
                        generateFunSpec(methodEntity, converterEntity)
                    )
                }
            }
        }

        if (repositoryEntity.panacheEntity != null) {
            val panacheConverter =
                repositoryEntity.converters.firstOrNull { it.originType == repositoryEntity.panacheEntity.type }
                    ?: throw Exception("No suitable converter for the Panache entity was found! ")
            generatePanacheFunctions(
                repositoryProxyTypeSpecBuilder,
                //Take the first converter to generate Panache Functions
                panacheConverter
            )
        }


        return FileSpec.get(packageName, repositoryProxyTypeSpecBuilder.build())

    }

    @KotlinPoetMetadataPreview
    private fun generateRepositoryProxyTypeSpec(): Pair<ClassName, TypeSpec.Builder> {

        val repositoryClassName = repositoryEntity.type.className
        val repositorySimpleName = repositoryClassName.simpleName

        val typeSpecBuilder = TypeSpec.classBuilder("${repositorySimpleName}Proxy")

        return Pair(repositoryClassName, typeSpecBuilder)
    }

    @KotlinPoetMetadataPreview
    private fun generatePanacheFunctions(
        typeSpecBuilder: TypeSpec.Builder,
        converterEntity: ConverterEntity
    ) {
        val panacheRepositoryKmClass = PanacheRepositoryBase::class.toImmutableKmClass()
        val functions = panacheRepositoryKmClass.functions
        val targetType = converterEntity.targetType

        for (function in functions) {
            val functionName = function.name

            val originReturnType = function.returnType

            val targetReturnType = if (originReturnType.classifier is KmClassifier.TypeParameter) {
                targetType.copy(isNullable = originReturnType.isNullable)
            } else {
                if (originReturnType.arguments.isNotEmpty()) {
                    originReturnType.toType(targetType)
                } else {
                    originReturnType.toType()
                }
            }

            val parameters = function.valueParameters
            val mParameters = mutableListOf<ParameterEntity>()

            if (parameters.map { it.type }.any { it!!.arguments.isNotEmpty() }) {
                continue
            }

            if (targetReturnType.arguments.isNotEmpty()
                && targetReturnType.className != STREAM
                && targetReturnType.className != LIST
            ) continue

            for (parameter in parameters) {
                val parameterType = parameter.type ?: continue
                val parameterName = parameter.name

                val targetParameterType = if (parameter.type!!.classifier is KmClassifier.TypeParameter) {
                    if (parameterName == "id") {
                        LONG.toType()
                    } else {
                        if (parameterType.isNullable) targetType.copy(isNullable = true)
                        else targetType
                    }
                } else {
                    parameterType.toType()
                }
                val parameterEntity = ParameterEntity(parameterName, targetParameterType)
                mParameters.add(parameterEntity)
            }

            typeSpecBuilder.addFunction(
                FunSpec.builder(functionName)
                    .apply {
                        for (parameter in mParameters) {
                            addParameter(generateParamSpec(parameter))
                        }
                    }.returns(targetReturnType.toParameterizedTypeName())
                    .apply {
                        val statements =
                            generateStatements(mParameters, targetReturnType, functionName, converterEntity)
                        for (statement in statements) {
                            addStatement(statement)
                        }
                    }
                    .build()
            )
        }
    }

    private fun generateFunSpec(methodEntity: MethodEntity, converterEntity: ConverterEntity): FunSpec {
        val methodName = methodEntity.name
        val methodReturnType = methodEntity.returnType
        val methodParameters = methodEntity.parameters
        val isSuspend = methodEntity.isSuspend

        return FunSpec.builder(methodName)
            .returns(methodReturnType.toParameterizedTypeName())
            .apply {
                val statements = generateStatements(
                    methodParameters,
                    methodReturnType,
                    methodName,
                    converterEntity
                )
                for (statement in statements) {
                    addStatement(statement)
                }
                if (isSuspend) addModifiers(KModifier.SUSPEND)
            }
            .addParameters(methodParameters.map { generateParamSpec(it) })
            .build()
    }

    private fun generateStatements(
        params: List<ParameterEntity>,
        returnType: CKType,
        methodName: String,
        converterEntity: ConverterEntity
    ): List<String> {
        val targetType = converterEntity.targetType
        val converterName = converterEntity.type.className.simpleName.decapitalize()
        val returnTypeIsNullable = returnType.isNullable

        val statements = mutableListOf<String>()
        val convertToModel = "convertDTOToModel"
        val convertToModels = "convertDTOsToModels"
        val convertToDTO = "convertModelToDTO"
        val convertToDTOs = "convertModelsToDTOs"
        val convertToDTOStream = "convertStreamModelsToDTOs"


        val otherParams = params
            .filterNot { it.type.className == targetType.className || it.type.hasArgument(targetType) }
            .map { it.name }

        val dtoParams = params
            .filter { it.type.className == targetType.className }
            .map { "${it.name}Model" }

        val dtoListParams = params.filter { it.type.hasArgument(targetType) }
            .map { "${it.name}Models" }

        //Create variable for dto params
        for (dtoParam in dtoParams) {
            val toModel = "val $dtoParam =·$converterName.$convertToModel(${
                dtoParam.substring(
                    0,
                    dtoParam.length - 1 - 4
                )
            }) "
            statements.add(toModel)
        }

        //Create variable for List with dto as argument params
        for (dtoListParam in dtoListParams) {
            val toModel =
                "val $dtoListParam =·$converterName.$convertToModels·(${
                    dtoListParam.subSequence(
                        0,
                        dtoListParam.length - 1 - 5
                    )
                })"
            statements.add(toModel)
        }

        val allParams = dtoParams.plus(otherParams).plus(dtoListParams)
        val paramsAsString = allParams.joinToString(",·")

        val computeStatement = "repository.$methodName·($paramsAsString)"

        val convertStatement = if (returnType.className == targetType.className) {
            //ReturnType is targetType
            "$converterName.$convertToDTO"
        } else if (returnType.hasArgument(targetType) && returnType.className == LIST) {
            //ReturnType is List with targetType as param
            "$converterName.$convertToDTOs"
        } else if (returnType.hasArgument(targetType) && returnType.className == STREAM.topLevelClassName())
            "$converterName.$convertToDTOStream"
        else {
            ""
        }


        if (returnType.className != UNIT) {
            statements.add("val result = $computeStatement")
            if (returnType.isNullable) {
                statements.add("return·if(result != null)·$convertStatement·(result) else null")
            } else {
                statements.add("return·$convertStatement·(result)")
            }
        } else {
            statements.add(computeStatement)
        }

        return statements
    }

    private fun generateParamSpec(parameterEntity: ParameterEntity): ParameterSpec {
        val parameterName = parameterEntity.name
        val parameterType = parameterEntity.type.toParameterizedTypeName()
        return ParameterSpec.builder(parameterName, parameterType).build()
    }

}
