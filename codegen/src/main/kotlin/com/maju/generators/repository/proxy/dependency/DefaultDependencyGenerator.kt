package com.maju.generators.repository.proxy.dependency

import com.squareup.kotlinpoet.*

class DefaultDependencyGenerator(
    repositoryClassName: ClassName,
    converterClassName: ClassName
) : AbstractDependencyGenerator(repositoryClassName, converterClassName) {

    override fun applyDependency(typeSpecBuilder: TypeSpec.Builder) {
        typeSpecBuilder.primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(
                    ParameterSpec.builder("converter", converterClassName)
                        .build()
                ).addParameter(
                    ParameterSpec.builder("repository", repositoryClassName)
                        .build()
                )
                .build()
        )

        typeSpecBuilder.addProperty(
            PropertySpec.builder("converter", converterClassName)
                .mutable(false)
                .addModifiers(KModifier.OVERRIDE)
                .initializer("converter")
                .build()
        )

        typeSpecBuilder.addProperty(
            PropertySpec.builder("repository", repositoryClassName)
                .mutable(false)
                .initializer("repository")
                .build()
        )


    }
}
