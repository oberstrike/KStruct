package com.maju.domain.proxy


import com.maju.cli.*
import com.maju.domain.generator.RepositoryEntity
import com.maju.domain.proxy.dependency.DefaultIDependencyGenerator
import com.maju.domain.proxy.dependency.PropertyDependencyGenerator
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


        val (repositoryClassName, repositoryProxyTypeSpecBuilder) = generateRepositoryProxyTypeSpec()
        val converterClassNames = repositoryEntity.converters.map { it.type.className }

        val dependencyGeneratorClass = when (injectionStrategy) {
            InjectionStrategy.DEFAULT -> DefaultIDependencyGenerator::class.java
            InjectionStrategy.CONSTRUCTOR -> DefaultIDependencyGenerator::class.java
            InjectionStrategy.PROPERTY -> PropertyDependencyGenerator::class.java
            else -> DefaultIDependencyGenerator::class.java
        }

        val dependencyGenerator = dependencyGeneratorClass
            .getConstructor(ClassName::class.java, List::class.java, ComponentModel::class.java)
            .newInstance(repositoryClassName, converterClassNames, componentModel)


        val dependency = dependencyGenerator.getDependency()

        if (dependency.constructor != null) {
            repositoryProxyTypeSpecBuilder.primaryConstructor(dependency.constructor)
        }

        for (property in dependency.properties) {
            repositoryProxyTypeSpecBuilder.addProperty(property)
        }

        for (annotation in dependency.annotations) {
            repositoryProxyTypeSpecBuilder.addAnnotation(annotation)
        }



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
