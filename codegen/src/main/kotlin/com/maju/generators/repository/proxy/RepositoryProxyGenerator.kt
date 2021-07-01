package com.maju.generators.repository.proxy


import com.maju.annotations.*
import com.maju.entities.*
import com.maju.generators.repository.IGenerator
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.maju.utils.*
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase

class RepositoryProxyGenerator(
    private val packageName: String,
    private val repositoryEntity: RepositoryEntity,
    private val injectionStrategy: InjectionStrategy = InjectionStrategy.CONSTRUCTOR,
    private val componentModel: ComponentModel = ComponentModel.DEFAULT
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
                val converterTargetType = converterEntity.targetType

                val methodParameters = methodEntity.parameters.map { it.type }
                val methodReturnType = methodEntity.returnType
                val isReturnTypeConverterTargetType = methodReturnType.className == converterTargetType.className
                val isReturnTypeListOfConverterTargetType =
                    methodReturnType.className == LIST && methodReturnType.hasArgument(converterTargetType)

                if (methodParameters.contains(converterTargetType) || isReturnTypeConverterTargetType || isReturnTypeListOfConverterTargetType) {
                    val functionGenerator = FunctionSpecGenerator(methodEntity, converterEntity)
                    repositoryProxyTypeSpecBuilder.addFunction(
                        functionGenerator.generate()
                    )
                }
            }
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

}
