package com.maju.domain.proxy


import com.maju.cli.*
import com.maju.domain.generator.RepositoryEntity
import com.maju.domain.proxy.dependency.DependencyGeneratorFactory
import com.maju.utils.IGenerator
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

class RepositoryProxyGenerator(
    private val packageName: String,
    private val repositoryEntity: RepositoryEntity,
    private val injectionStrategy: InjectionStrategy = InjectionStrategy.CONSTRUCTOR,
    private val componentModel: ComponentModel = ComponentModel.DEFAULT
) : IGenerator<FileSpec> {

    @KotlinPoetMetadataPreview
    override fun generate(): FileSpec {
        val repositoryClassName = repositoryEntity.type.className
        val repositorySimpleName = repositoryClassName.simpleName
        val repositoryProxyTypeSpecBuilder = TypeSpec.classBuilder("${repositorySimpleName}Proxy")
        val converterClassNames = repositoryEntity.converters.map { it.type.className }

        val dependency = DependencyGeneratorFactory.aDependencyGenerator(injectionStrategy)
            .getDependency(repositoryClassName, converterClassNames, componentModel)

        with(dependency) {
            repositoryProxyTypeSpecBuilder.primaryConstructor(constructor)
            properties.forEach { repositoryProxyTypeSpecBuilder.addProperty(it) }
            annotations.forEach { repositoryProxyTypeSpecBuilder.addAnnotation(it) }
        }

        for (methodEntity in repositoryEntity.methods) {
            val functionGenerator = FunctionSpecGenerator(methodEntity, repositoryEntity.converters)
            repositoryProxyTypeSpecBuilder.addFunction(
                functionGenerator.generate()
            )

        }

        return FileSpec.get(packageName, repositoryProxyTypeSpecBuilder.build())

    }
}
