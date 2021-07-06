package com.maju.domain.proxy.dependency

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec

data class Dependency(
    val constructor: FunSpec?,
    val properties: List<PropertySpec> = listOf(),
    val annotations: List<AnnotationSpec> = listOf()
)