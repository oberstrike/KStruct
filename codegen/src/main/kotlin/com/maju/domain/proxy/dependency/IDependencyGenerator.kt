package com.maju.domain.proxy.dependency

import com.maju.cli.ComponentModel
import com.maju.cli.InjectionStrategy
import com.squareup.kotlinpoet.*

interface IDependencyGenerator {

    val injectionStrategies: List<InjectionStrategy>


    fun getDependency(
        repositoryClassName: ClassName,
        converterClassNames: List<ClassName>,
        componentModel: ComponentModel
    ): Dependency
}


