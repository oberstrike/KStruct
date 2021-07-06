package com.maju.domain.proxy.dependency

import com.maju.cli.InjectionStrategy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

@KotlinPoetMetadataPreview
object DependencyGeneratorFactory {
    fun aDependencyGenerator(injectionStrategy: InjectionStrategy): IDependencyGenerator {
        return when (injectionStrategy) {
            InjectionStrategy.PROPERTY -> property()
            else -> default()
        }
    }


    private fun default(): DefaultDependencyGenerator {
        return DefaultDependencyGenerator()
    }

    private fun property(): PropertyDependencyGenerator {
        return PropertyDependencyGenerator()
    }

}