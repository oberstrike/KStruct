package com.maju.generators.repository.proxy.dependency

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

class PropertyDependencyGenerator : DependencyGenerator {

    override fun applyDependency(
        typeSpecBuilder: TypeSpec.Builder,
        repositoryClassName: ClassName,
        converterClassName: ClassName,
        componentModel: String
    ) {
        //Dependency Injection - InjectionStrategy.PROPERTIES
        typeSpecBuilder.addProperty(
            PropertySpec.builder("converter", converterClassName)
                .addModifiers(KModifier.OVERRIDE, KModifier.LATEINIT)
                .apply {
                    if (componentModel == "cdi") addAnnotation(Inject::class)
                }
                .mutable(true)
                .build()
        )

        typeSpecBuilder.addProperty(
            PropertySpec.builder("repository", repositoryClassName)
                .addModifiers(KModifier.PRIVATE, KModifier.LATEINIT)
                .apply {
                    if (componentModel == "cdi") addAnnotation(Inject::class)
                }
                .mutable(true)
                .build()
        )
        if (componentModel == "cdi") typeSpecBuilder.addAnnotation(ApplicationScoped::class.java)
    }

}
