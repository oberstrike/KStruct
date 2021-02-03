package com.maju.utils

import com.squareup.kotlinpoet.ClassName

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

@JvmField
val STREAM = ClassName("java.util.stream", "Stream")

