package com.maju.utils

import com.squareup.kotlinpoet.ClassName

data class CKType(
    val className: ClassName,
    val isNullable: Boolean = false,
    val arguments: List<CKType> = listOf(),
)

