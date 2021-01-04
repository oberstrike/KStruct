package com.maju.generators.repository.proxy.dependency

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import javax.enterprise.context.ApplicationScoped

class ConstructorDependencyGenerator(
    repositoryClassName: ClassName, converterClassName: ClassName
) : AbstractDependencyGenerator(repositoryClassName, converterClassName) {

    private val defaultDependencyGenerator = DefaultDependencyGenerator(repositoryClassName, converterClassName)

    override fun applyDependency(typeSpecBuilder: TypeSpec.Builder) {
        defaultDependencyGenerator.applyDependency(typeSpecBuilder)
        typeSpecBuilder.addAnnotation(ApplicationScoped::class)
    }
}
