package com.maju.domain.generator

import com.maju.utils.CKType

data class MethodEntity(
    val name: String,
    val parameters: List<ParameterEntity>,
    val returnType: CKType,
    val isSuspend: Boolean
)