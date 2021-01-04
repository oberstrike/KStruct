package com.maju.generators.repository.proxy


import com.maju.annotations.InjectionStrategy
import com.maju.entities.MethodEntity
import com.maju.entities.ParameterEntity
import com.maju.entities.RepositoryEntity
import com.maju.generators.repository.proxy.dependency.ConstructorDependencyGenerator
import com.maju.generators.repository.proxy.dependency.DefaultDependencyGenerator
import com.maju.generators.repository.proxy.dependency.PropertyDependencyGenerator
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import core.util.AbstractRepositoryProxy
import com.maju.utils.*

class RepositoryProxyGenerator(
    private val packageName: String,
    private val repository: RepositoryEntity,
    private val injectionStrategy: InjectionStrategy = InjectionStrategy.CONSTRUCTOR,
    private val componentModel: String = "default"
) : IGenerator<FileSpec> {


    private val modelClass by lazy { repository.modelClass }

    private val dtoClass by lazy { repository.dtoClass }

    @KotlinPoetMetadataPreview
    override fun generate(): FileSpec {

        val abstractRepositoryProxyClassName = AbstractRepositoryProxy::class.toType().className.parameterizedBy(
            modelClass.toParameterizedTypeName(),
            dtoClass.toParameterizedTypeName()
        )

        val converterClassName = repository.converter.type.className

        val repositoryClassName = repository.type.className
        val repositorySimpleName = repositoryClassName.simpleName

        val typeSpecBuilder = TypeSpec.classBuilder("${repositorySimpleName}Proxy")
        typeSpecBuilder.superclass(abstractRepositoryProxyClassName)

        //Dependency Injection
        val dependencyGenerator = if (componentModel == "cdi" && injectionStrategy == InjectionStrategy.CONSTRUCTOR) {
            ConstructorDependencyGenerator(repositoryClassName, converterClassName)
        } else if (componentModel == "cdi" && injectionStrategy == InjectionStrategy.PROPERTY) {
            PropertyDependencyGenerator(repositoryClassName, converterClassName)
        } else {
            DefaultDependencyGenerator(repositoryClassName, converterClassName)
        }

        dependencyGenerator.applyDependency(typeSpecBuilder)


        for (method in repository.methods) {
            val parameters = method.parameters.map { it.type }
            val returnType = method.returnType
            val isReturnTypeDTO = returnType == dtoClass
            val isReturnTypeListOfDTO = returnType.className == LIST && returnType.hasArgument(dtoClass)

            if (parameters.contains(dtoClass) || isReturnTypeDTO || isReturnTypeListOfDTO) {
                typeSpecBuilder.addFunction(
                    generateFunSpec(method)
                )
            }
        }

        return FileSpec.get(packageName, typeSpecBuilder.build())

    }

    private fun generateFunSpec(methodEntity: MethodEntity): FunSpec {

        val name = methodEntity.name
        val returnType = methodEntity.returnType
        val parameters = methodEntity.parameters

        return FunSpec.builder(name)
            .returns(returnType.toParameterizedTypeName())
            .apply {
                for (parameter in parameters) {
                    addParameter(generateParamSpec(parameter))
                }

                val statements = generateStatement(parameters, returnType, name)
                for (statement in statements) {
                    addStatement(statement)
                }


            }
            .build()
    }

    private fun generateStatement(params: List<ParameterEntity>, returnType: CKType, methodName: String): List<String> {
        val statements = mutableListOf<String>()
        val otherParams = params.filterNot { it.type == dtoClass || it.type.hasArgument(dtoClass) }.map { it.name }
        val dtoParams = params.filter { it.type == dtoClass }.map { "${it.name}Model" }

        val dtoListParams = params.filter { it.type.hasArgument(dtoClass) }
            .map { "${it.name}Models" }

        for (dtoParam in dtoParams) {
            val toModel = "val $dtoParam = toModel { ${dtoParam.substring(0, dtoParam.length - 1 - 4)} } "
            statements.add(toModel)
        }

        for (dtoListParam in dtoListParams) {
            val toModel =
                "val $dtoListParam = toModels { ${dtoListParam.subSequence(0, dtoListParam.length - 1 - 5)} }"
            statements.add(toModel)
        }

        val allParams = dtoParams.plus(otherParams).plus(dtoListParams)
        val paramsAsString = allParams.joinToString()

        val computeStatement = "compute { repository.$methodName( $paramsAsString ) }"
        if (returnType == dtoClass) {
            statements.add("return toDTO { $computeStatement } ")
        } else if (returnType.hasArgument(dtoClass)) {
            statements.add("return toDTOs {  compute { repository.$methodName ( $paramsAsString ) }  }")
        } else if (returnType == UNIT.toType()) {
            statements.add(computeStatement)
        } else {
            statements.add("return $computeStatement")
        }
        return statements
    }

    private fun generateParamSpec(parameterEntity: ParameterEntity): ParameterSpec {
        val name = parameterEntity.name
        var type = parameterEntity.type


        return ParameterSpec.builder(name, type.toParameterizedTypeName()).build()
    }

}
