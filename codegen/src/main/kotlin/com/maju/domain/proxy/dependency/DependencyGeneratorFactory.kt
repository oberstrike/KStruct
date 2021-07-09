package com.maju.domain.proxy.dependency

import com.maju.cli.InjectionStrategy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

@KotlinPoetMetadataPreview
object DependencyGeneratorFactory {

    private val generators: List<IDependencyGenerator> = listOf(
        DefaultDependencyGenerator(), PropertyDependencyGenerator()
    )


    fun aDependencyGenerator(injectionStrategy: InjectionStrategy): IDependencyGenerator {
        return generators.first { it.injectionStrategies.contains(injectionStrategy) }
    }


}