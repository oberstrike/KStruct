package com.maju.generators.repository.proxy.dependency

import com.squareup.kotlinpoet.*
import javax.enterprise.context.ApplicationScoped

class DefaultDependencyGenerator : DependencyGenerator {

    override fun applyDependency(
        typeSpecBuilder: TypeSpec.Builder,
        repositoryClassName: ClassName,
        converterClassName: ClassName,
        componentModel: String
    ) {
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

        if (componentModel == "cdi") typeSpecBuilder.addAnnotation(ApplicationScoped::class)

    }
}
