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

@KotlinPoetMetadataPreview
fun <T : Any> KClass<T>.toType(): CKType {
    return ClassInspectorUtil.createClassName(toImmutableKmClass().name).toType()
}

fun CKType.hasArgument(argument: ClassName): Boolean {
    return arguments.map { it.className }.contains(argument)
}

fun CKType.hasArgument(argument: CKType): Boolean {
    return arguments.contains(argument)
}

fun CKType.toParameterizedTypeName(): TypeName {
    val rValue = if (arguments.isEmpty()) className
    else className.parameterizedBy(arguments.map { each -> each.toParameterizedTypeName() })
    return rValue.copy(nullable = isNullable)
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
fun ImmutableKmType.toType(): CKType {
    return CKType(
        className = className(),
        isNullable = isNullable,
        arguments = arguments.filter { it.type != null }.map { it.type!!.toType() }
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

fun ClassName.toType(): CKType {
    return CKType(
        className = this,
        isNullable = isNullable
    )
}

fun ClassName.parameterizedToType(type: CKType): CKType {
    return CKType(
        className = this,
        isNullable = isNullable,
        arguments = listOf(type)
    )
}

fun ClassName.parameterizedToType(className: ClassName): CKType {
    return CKType(
        className = this,
        isNullable = isNullable,
        arguments = listOf(className.toType())
    )
}

@KotlinPoetMetadataPreview
fun TypeElement.isSubType(target: CKType): Boolean {
    val kmClass = toImmutableKmClass()
    return kmClass.supertypes.map { it.toType().className }.contains(target.className)
}

@KotlinPoetMetadataPreview
fun TypeElement.toType(): CKType {
    val kmClassName = toImmutableKmClass()
    return kmClassName.toType()
}

