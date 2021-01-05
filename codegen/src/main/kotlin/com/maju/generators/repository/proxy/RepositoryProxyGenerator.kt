package com.maju.generators.repository.proxy


import com.maju.annotations.InjectionStrategy
import com.maju.entities.MethodEntity
import com.maju.entities.ParameterEntity
import com.maju.entities.RepositoryEntity
import com.maju.entities.RepositoryType
import com.maju.generators.repository.ParameterEntityGenerator
import com.maju.generators.repository.proxy.dependency.ConstructorDependencyGenerator
import com.maju.generators.repository.proxy.dependency.DefaultDependencyGenerator
import com.maju.generators.repository.proxy.dependency.PropertyDependencyGenerator
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.maju.utils.*
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import kotlinx.metadata.KmClassifier

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

        val abstractRepositoryProxyClassName = IRepositoryProxy::class.toType().className.parameterizedBy(
            modelClass.toParameterizedTypeName(),
            dtoClass.toParameterizedTypeName()
        )

        val converterClassName = repository.converter.type.className

        val repositoryClassName = repository.type.className
        val repositorySimpleName = repositoryClassName.simpleName

        val typeSpecBuilder = TypeSpec.classBuilder("${repositorySimpleName}Proxy")
        typeSpecBuilder.addSuperinterface(abstractRepositoryProxyClassName)

        //Dependency Injection
        val dependencyGenerator = if (componentModel == "cdi" && injectionStrategy == InjectionStrategy.CONSTRUCTOR) {
            ConstructorDependencyGenerator(repositoryClassName, converterClassName)
        } else if (componentModel == "cdi" && injectionStrategy == InjectionStrategy.PROPERTY) {
            PropertyDependencyGenerator(repositoryClassName, converterClassName)
        } else {
            DefaultDependencyGenerator(repositoryClassName, converterClassName)
        }

        val proxyHelperClass = RepositoryProxyHelper::class.toType().className.parameterizedBy(
            modelClass.toParameterizedTypeName(),
            dtoClass.toParameterizedTypeName()
        )

        //Helper property
        typeSpecBuilder.addProperty(
            PropertySpec.builder("helper", proxyHelperClass)
                .addModifiers(KModifier.PRIVATE)
                .mutable(false)
                .initializer("RepositoryProxyHelper(converter)")
                .build()
        )


        dependencyGenerator.applyDependency(typeSpecBuilder)


        for (method in repository.methods) {
            val parameters = method.parameters.map { it.type }
            val returnType = method.returnType
            val isReturnTypeDTO = returnType.className == dtoClass.className
            val isReturnTypeListOfDTO = returnType.className == LIST && returnType.hasArgument(dtoClass)

            if (parameters.contains(dtoClass) || isReturnTypeDTO || isReturnTypeListOfDTO) {
                typeSpecBuilder.addFunction(
                    generateFunSpec(method)
                )
            }
        }

        when (repository.repositoryType) {
            RepositoryType.PANACHE_ENTITY -> generatePanacheFunctions(typeSpecBuilder, dtoClass)

        }

        return FileSpec.get(packageName, typeSpecBuilder.build())

    }

    @KotlinPoetMetadataPreview
    private fun generatePanacheFunctions(typeSpecBuilder: TypeSpec.Builder, dtoClass: CKType) {
        val panacheEntityKmClass = PanacheRepositoryBase::class.toImmutableKmClass()
        val functions = panacheEntityKmClass.functions

        for (function in functions) {
            val name = function.name
            val rType = if (function.returnType.classifier is KmClassifier.TypeParameter) {
                dtoClass
            } else {
                if (function.returnType.arguments.isNotEmpty()) {
                    function.returnType.toType(dtoClass)
                } else {
                    function.returnType.toType()
                }
            }

            val parameters = function.valueParameters
            val mParameters = mutableListOf<ParameterEntity>()
            if(parameters.map { it.type }.any { it!!.arguments.isNotEmpty() }){
                continue
            }

            if(rType.arguments.isNotEmpty())
                continue

            for (parameter in parameters) {
                val pType = parameter.type ?: continue
                val pName = parameter.name


                val valParameter = if (parameter.type!!.classifier is KmClassifier.TypeParameter) {
                    if (pName == "id") {
                        LONG.toType()
                    } else {
                        dtoClass
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
                        val statements = generateStatement(mParameters, rType, name)
                        for (statement in statements) {
                            addStatement(statement)
                        }
                    }

                    .build()
            )

        }


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
        val otherParams = params.filterNot { it.type.className == dtoClass.className || it.type.hasArgument(dtoClass) }
            .map { it.name }
        val dtoParams = params.filter { it.type.className == dtoClass.className }.map { "${it.name}Model" }

        val dtoListParams = params.filter { it.type.hasArgument(dtoClass) }
            .map { "${it.name}Models" }

        for (dtoParam in dtoParams) {
            val toModel = "val $dtoParam = helper.toModel { ${dtoParam.substring(0, dtoParam.length - 1 - 4)} } "
            statements.add(toModel)
        }

        for (dtoListParam in dtoListParams) {
            val toModel =
                "val $dtoListParam = helper.toModels { ${dtoListParam.subSequence(0, dtoListParam.length - 1 - 5)} }"
            statements.add(toModel)
        }

        val allParams = dtoParams.plus(otherParams).plus(dtoListParams)
        val paramsAsString = allParams.joinToString()

        val computeStatement = "helper.compute { repository.$methodName( $paramsAsString ) }"
        if (returnType.className == dtoClass.className) {
            statements.add("return helper.toDTO { $computeStatement } ")
        } else if (returnType.hasArgument(dtoClass)) {
            statements.add("return helper.toDTOs {  helper.compute { repository.$methodName ( $paramsAsString ) }  }")
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
