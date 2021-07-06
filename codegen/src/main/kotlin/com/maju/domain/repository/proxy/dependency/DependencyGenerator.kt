package com.maju.domain.repository.proxy.dependency

import com.maju.cli.ComponentModel
import com.squareup.kotlinpoet.*

interface DependencyGenerator {
    fun applyDependency(
        typeSpecBuilder: TypeSpec.Builder,
        repositoryClassName: ClassName,
        converterClassNames: List<ClassName>,
        componentModel: ComponentModel
    )
}


