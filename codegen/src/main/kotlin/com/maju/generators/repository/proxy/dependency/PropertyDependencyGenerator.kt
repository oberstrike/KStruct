package com.maju.generators.repository.proxy.dependency

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

class PropertyDependencyGenerator(
    repositoryClassName: ClassName, converterClassName: ClassName
) : AbstractDependencyGenerator(repositoryClassName, converterClassName) {

    override fun applyDependency(typeSpecBuilder: TypeSpec.Builder) {
        //Dependency Injection - InjectionStrategy.PROPERTIES
        typeSpecBuilder.addProperty(
            PropertySpec.builder("converter", converterClassName)
                .addModifiers(KModifier.OVERRIDE, KModifier.LATEINIT)
                .addAnnotation(Inject::class)
                .mutable(true)
                .build()
        )

        typeSpecBuilder.addProperty(
            PropertySpec.builder("repository", repositoryClassName)
                .addModifiers(KModifier.PRIVATE, KModifier.LATEINIT)
                .addAnnotation(Inject::class)
                .mutable(true)
                .build()
        )

        typeSpecBuilder.addAnnotation(ApplicationScoped::class.java)
    }

}
