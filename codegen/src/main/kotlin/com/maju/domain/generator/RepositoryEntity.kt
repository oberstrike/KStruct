package com.maju.domain.generator

import com.maju.domain.generator.ConverterEntity
import com.maju.domain.generator.ExtensionEntity
import com.maju.domain.generator.MethodEntity
import com.maju.utils.CKType


data class RepositoryEntity(
    val name: String,
    val type: CKType,
    val converters: List<ConverterEntity>,
    val methods: List<MethodEntity>,
    val panacheEntity: ExtensionEntity? = null
)


