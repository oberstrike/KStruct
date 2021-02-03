package com.maju.utils

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import kotlin.reflect.KClass

data class CKType(
    val className: ClassName,
    val isNullable: Boolean = false,
    val arguments: List<CKType> = listOf(),
)

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
