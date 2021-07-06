package com.maju.domain.repository.proxy.dependency

import com.maju.cli.ComponentModel
import com.squareup.kotlinpoet.*
import javax.enterprise.context.ApplicationScoped

class DefaultDependencyGenerator : DependencyGenerator {

    override fun applyDependency(
        typeSpecBuilder: TypeSpec.Builder,
        repositoryClassName: ClassName,
        converterClassNames: List<ClassName>,
        componentModel: ComponentModel
    ) {
        typeSpecBuilder.primaryConstructor(
            FunSpec.constructorBuilder()
                .apply {
                    for (converterClassName in converterClassNames) {
                        addParameter(
                            ParameterSpec.builder(
                                converterClassName.simpleName.decapitalize(),
                                converterClassName
                            )
                                .build()
                        )
                    }
                }.addParameter(
                    ParameterSpec.builder("repository", repositoryClassName)
                        .build()
                )
                .build()
        )

        typeSpecBuilder.apply {
            for (converterClassName in converterClassNames) {
                val converterName = converterClassName.simpleName.decapitalize()
                addProperty(
                    PropertySpec.builder(converterName, converterClassName)
                        .mutable(false)
                        .initializer(converterName)
                        .build()
                )
            }

        }

        typeSpecBuilder.addProperty(
            PropertySpec.builder("repository", repositoryClassName)
                .mutable(false)
                .initializer("repository")
                .build()
        )

        if (componentModel == ComponentModel.CDI) typeSpecBuilder.addAnnotation(ApplicationScoped::class)

    }
}
