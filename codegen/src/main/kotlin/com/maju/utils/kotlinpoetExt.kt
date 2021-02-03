package com.maju.utils

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.metadata.*
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass
import kotlin.reflect.KType

@KotlinPoetMetadataPreview
fun <T : Any> KClass<T>.toType(): CKType {
    return ClassInspectorUtil.createClassName(toImmutableKmClass().name).toType()
}




@KotlinPoetMetadataPreview
fun ImmutableKmType.className(): ClassName {
    return ClassInspectorUtil.createClassName((classifier as KmClassifier.Class).name)
}

@KotlinPoetMetadataPreview
fun ImmutableKmClass.className(): ClassName {
    return ClassInspectorUtil.createClassName(name)
}

@KotlinPoetMetadataPreview
fun ImmutableKmType.toType(argument: CKType? = null): CKType {
    return CKType(
        className = className(),
        isNullable = isNullable,
        arguments = if (argument == null) {
            arguments.filter { it.type != null && it.type?.classifier is KmClassifier.Class }.map { it.type!!.toType() }
        } else {
            listOf(argument)
        }
    )
}

@KotlinPoetMetadataPreview
fun ImmutableKmClass.toType(): CKType {
    return CKType(
        className = className(),
        isNullable = false,
        arguments = listOf()
    )
}






