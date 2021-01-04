package com.maju.generators.repository.proxy.dependency

import com.squareup.kotlinpoet.*

interface DependencyGenerator {
    fun applyDependency(typeSpecBuilder: TypeSpec.Builder)
}

abstract class AbstractDependencyGenerator(
    protected val repositoryClassName: ClassName,
    protected val converterClassName: ClassName
) : DependencyGenerator {

}

