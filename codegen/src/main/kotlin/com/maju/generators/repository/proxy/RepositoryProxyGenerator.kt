package com.maju.generators.repository.proxy


import com.maju.annotations.*
import com.maju.entities.*
import com.maju.generators.repository.IGenerator
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.maju.utils.*

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
            val functionGenerator = FunctionSpecGenerator(methodEntity, repositoryEntity.converters)
            repositoryProxyTypeSpecBuilder.addFunction(
                functionGenerator.generate()
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

}
