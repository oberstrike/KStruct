package com.maju.generators.repository.proxy


import com.maju.annotations.IRepositoryProxy
import com.maju.annotations.InjectionStrategy
import com.maju.annotations.RepositoryProxyHelper
import com.maju.entities.MethodEntity
import com.maju.entities.ParameterEntity
import com.maju.entities.RepositoryEntity
import com.maju.entities.RepositoryType
import com.maju.generators.repository.IGenerator
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.maju.utils.*
import com.squareup.kotlinpoet.metadata.isNullable
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import kotlinx.metadata.KmClassifier

class RepositoryProxyGenerator(
    private val packageName: String,
    private val repository: RepositoryEntity,
    private val injectionStrategy: InjectionStrategy = InjectionStrategy.CONSTRUCTOR,
    private val componentModel: String = "default"
) : IGenerator<FileSpec> {

    private val modelCKType by lazy { repository.converter.modelType }

    private val dtoCKType by lazy { repository.converter.dtoType }

    private val converterClassName by lazy { repository.converter.type.className }

    private val repositoryHelperPropertyName = "helper"

    private val modelTypeName = modelCKType.toParameterizedTypeName()

    private val dtoTypeName = dtoCKType.toParameterizedTypeName()

    @KotlinPoetMetadataPreview
    override fun generate(): FileSpec {


        val (repositoryClassName, repositoryProxyTypeSpecBuilder) = generateRepositoryProxyTypeSpec(
            modelTypeName,
            dtoTypeName
        )

        val repositoryProxyHelperClass = RepositoryProxyHelper::class.toType().className.parameterizedBy(
            modelTypeName,
            dtoTypeName
        )

        //Helper property
        repositoryProxyTypeSpecBuilder.addProperty(
            PropertySpec.builder(repositoryHelperPropertyName, repositoryProxyHelperClass)
                .addModifiers(KModifier.PRIVATE)
                .mutable(false)
                .initializer("RepositoryProxyHelper(converter)")
                .build()
        )

        injectionStrategy.dependencyGenerator.applyDependency(
            typeSpecBuilder = repositoryProxyTypeSpecBuilder,
            repositoryClassName = repositoryClassName,
            converterClassName = converterClassName,
            componentModel = componentModel
        )

        for (methodEntity in repository.methods) {
            val parameters = methodEntity.parameters.map { it.type }
            val returnType = methodEntity.returnType
            val isReturnTypeDTO = returnType.className == dtoCKType.className
            val isReturnTypeListOfDTO = returnType.className == LIST && returnType.hasArgument(dtoCKType)

            if (parameters.contains(dtoCKType) || isReturnTypeDTO || isReturnTypeListOfDTO) {
                repositoryProxyTypeSpecBuilder.addFunction(
                    generateFunSpec(methodEntity)
                )
            }
        }

        when (repository.repositoryType) {
            RepositoryType.PANACHE_ENTITY -> generatePanacheFunctions(repositoryProxyTypeSpecBuilder, dtoCKType)
        }

        return FileSpec.get(packageName, repositoryProxyTypeSpecBuilder.build())

    }

    @KotlinPoetMetadataPreview
    private fun generateRepositoryProxyTypeSpec(
        modelClassTypeName: TypeName,
        dtoClassTypeName: TypeName
    ): Pair<ClassName, TypeSpec.Builder> {
        val abstractRepositoryProxyClassName = IRepositoryProxy::class.toType().className.parameterizedBy(
            modelClassTypeName,
            dtoClassTypeName
        )
        val repositoryClassName = repository.type.className
        val repositorySimpleName = repositoryClassName.simpleName

        val typeSpecBuilder = TypeSpec.classBuilder("${repositorySimpleName}Proxy")
        typeSpecBuilder.addSuperinterface(abstractRepositoryProxyClassName)
        return Pair(repositoryClassName, typeSpecBuilder)
    }

    @KotlinPoetMetadataPreview
    private fun generatePanacheFunctions(typeSpecBuilder: TypeSpec.Builder, dtoClass: CKType) {
        val panacheEntityKmClass = PanacheRepositoryBase::class.toImmutableKmClass()
        val functions = panacheEntityKmClass.functions

        for (function in functions) {
            val name = function.name
            val fReturnType = function.returnType
            val rType = if (fReturnType.classifier is KmClassifier.TypeParameter) {
                dtoClass.copy(isNullable = fReturnType.isNullable)
            } else {
                if (fReturnType.arguments.isNotEmpty()) {
                    fReturnType.toType(dtoClass)
                } else {
                    fReturnType.toType()
                }
            }

            val parameters = function.valueParameters
            val mParameters = mutableListOf<ParameterEntity>()


            if (parameters.map { it.type }.any { it!!.arguments.isNotEmpty() }) {
                continue
            }

            if (rType.arguments.isNotEmpty() && rType.className != STREAM && rType.className != LIST)
                continue

            for (parameter in parameters) {
                val pType = parameter.type ?: continue
                val pName = parameter.name


                val valParameter = if (parameter.type!!.classifier is KmClassifier.TypeParameter) {
                    if (pName == "id") {
                        LONG.toType()
                    } else {
                        if (pType.isNullable) {
                            dtoClass.copy(isNullable = true)
                        } else {
                            dtoClass
                        }
                    }
                } else {
                    pType.toType()
                }
                val parameterEntity = ParameterEntity(pName, valParameter)
                mParameters.add(parameterEntity)
            }


            typeSpecBuilder.addFunction(
                FunSpec.builder(name)
                    .apply {
                        for (parameter in mParameters) {
                            addParameter(generateParamSpec(parameter))
                        }
                    }.returns(rType.toParameterizedTypeName())

                    .apply {
                        val statements = generateStatements(mParameters, rType, name)
                        for (statement in statements) {
                            addStatement(statement)
                        }
                    }

                    .build()
            )

        }


    }

    private fun generateFunSpec(methodEntity: MethodEntity): FunSpec {
        val methodName = methodEntity.name
        val methodReturnType = methodEntity.returnType
        val methodParameters = methodEntity.parameters
        return FunSpec.builder(methodName)
            .returns(methodReturnType.toParameterizedTypeName())
            .apply {
                val statements = generateStatements(methodParameters, methodReturnType, methodName)
                for (statement in statements) {
                    addStatement(statement)
                }
            }
            .addParameters(methodParameters.map { generateParamSpec(it) })
            .build()
    }

    private fun generateStatements(
        params: List<ParameterEntity>,
        returnType: CKType,
        methodName: String
    ): List<String> {
        val statements = mutableListOf<String>()

        val otherParams = params
            .filterNot { it.type.className == dtoCKType.className || it.type.hasArgument(dtoCKType) }
            .map { it.name }

        val dtoParams = params
            .filter { it.type.className == dtoCKType.className }
            .map { "${it.name}Model" }

        val dtoListParams = params.filter { it.type.hasArgument(dtoCKType) }
            .map { "${it.name}Models" }

        for (dtoParam in dtoParams) {
            val toModel = "val $dtoParam = $repositoryHelperPropertyName.toModel { ${
                dtoParam.substring(
                    0,
                    dtoParam.length - 1 - 4
                )
            } } "
            statements.add(toModel)
        }

        for (dtoListParam in dtoListParams) {
            val toModel =
                "val $dtoListParam = $repositoryHelperPropertyName.toModels { ${
                    dtoListParam.subSequence(
                        0,
                        dtoListParam.length - 1 - 5
                    )
                } }"
            statements.add(toModel)
        }

        val allParams = dtoParams.plus(otherParams).plus(dtoListParams)
        val paramsAsString = allParams.joinToString()

        val computeStatement = "repository.$methodName( $paramsAsString )"
        if (returnType.className == dtoCKType.className) {
            statements.add("return $repositoryHelperPropertyName.toDTO·{ $computeStatement } ")
        } else if (returnType.hasArgument(dtoCKType) && returnType.className == LIST) {
            statements.add("return $repositoryHelperPropertyName.toDTOs·{ repository.$methodName ( $paramsAsString )  }")
        } else if (returnType == UNIT.toType()) {
            statements.add(computeStatement)
        } else if (returnType.className == STREAM.topLevelClassName())
            statements.add("return $repositoryHelperPropertyName.toStreamDTOs·{ repository.$methodName($paramsAsString) }")
        else {
            statements.add("return $computeStatement")
        }
        return statements
    }

    private fun generateParamSpec(parameterEntity: ParameterEntity): ParameterSpec {
        val parameterName = parameterEntity.name
        val parameterType = parameterEntity.type.toParameterizedTypeName()
        return ParameterSpec.builder(parameterName, parameterType).build()
    }

}
