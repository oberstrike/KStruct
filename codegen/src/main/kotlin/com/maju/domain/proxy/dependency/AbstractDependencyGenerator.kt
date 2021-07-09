package com.maju.domain.proxy.dependency

import com.maju.cli.InjectionStrategy
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec

abstract class AbstractDependencyGenerator(

) : IDependencyGenerator {


    protected fun annotations(block: AnnotationCreator.() -> Unit): List<AnnotationSpec> {
        val annotationCreator = AnnotationCreator()
        annotationCreator.block()
        return annotationCreator.annotations
    }

    protected fun property(name: String, type: ClassName, block: PropertySpec.Builder.() -> Unit): PropertySpec {
        val builder = PropertySpec.builder(name, type)
        builder.block()
        return builder.build()
    }

    class AnnotationCreator {

        val annotations: MutableList<AnnotationSpec> = mutableListOf()

        fun add(name: ClassName): AnnotationCreator = apply {
            annotations.add(
                AnnotationSpec.builder(name)
                    .build()
            )
        }
    }


}

