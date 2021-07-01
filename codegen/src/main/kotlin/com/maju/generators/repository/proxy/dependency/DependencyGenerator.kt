package com.maju.generators.repository.proxy.dependency

import com.maju.annotations.ComponentModel
import com.squareup.kotlinpoet.*

interface DependencyGenerator {
    fun applyDependency(
        typeSpecBuilder: TypeSpec.Builder,
        repositoryClassName: ClassName,
        converterClassNames: List<ClassName>,
        componentModel: ComponentModel
    )
}


