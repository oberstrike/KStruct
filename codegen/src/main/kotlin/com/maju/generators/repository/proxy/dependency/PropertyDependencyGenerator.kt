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
        converterClassNames: List<ClassName>,
        componentModel: String
    ) {
        //Dependency Injection - InjectionStrategy.PROPERTIES
        val componentModelIsCDI = componentModel == "cdi"

        typeSpecBuilder.apply {
            for (converterClassName in converterClassNames) {
                addProperty(
                    PropertySpec.builder(converterClassName.simpleName.decapitalize(), converterClassName)
                        .addModifiers(KModifier.LATEINIT)
                        .apply {
                            if (componentModelIsCDI) addAnnotation(Inject::class)
                        }
                        .mutable(true)
                        .build()
                )

            }
        }

        typeSpecBuilder.addProperty(
            PropertySpec.builder("repository", repositoryClassName)
                .addModifiers(KModifier.PRIVATE, KModifier.LATEINIT)
                .apply {
                    if (componentModelIsCDI) addAnnotation(Inject::class)
                }
                .mutable(true)
                .build()
        )
        if (componentModelIsCDI) typeSpecBuilder.addAnnotation(ApplicationScoped::class.java)
    }

}
