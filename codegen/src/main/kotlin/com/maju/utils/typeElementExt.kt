package com.maju.utils

import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import javax.lang.model.element.TypeElement

@KotlinPoetMetadataPreview
fun TypeElement.toType(): CKType {
    val kmClassName = toImmutableKmClass()
    return kmClassName.toType()
}

@KotlinPoetMetadataPreview
fun TypeElement.isSubType(target: CKType): Boolean {
    val kmClass = toImmutableKmClass()
    return kmClass.supertypes.map { it.toType().className }.contains(target.className)
}
