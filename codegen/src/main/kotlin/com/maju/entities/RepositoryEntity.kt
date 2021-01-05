package com.maju.entities

import com.maju.utils.CKType


data class RepositoryEntity(
    val name: String,
    val dtoClass: CKType,
    val type: CKType,
    val modelClass: CKType,
    val converter: ConverterEntity,
    val methods: List<MethodEntity>,
    val repositoryType: RepositoryType = RepositoryType.DEFAULT_ENTITY
)


enum class RepositoryType {
    PANACHE_ENTITY, DEFAULT_ENTITY
}

data class ConverterEntity(
    val type: CKType
)


data class MethodEntity(
    val name: String,
    val parameters: List<ParameterEntity>,
    val returnType: CKType
)

data class ParameterEntity(
    val name: String,
    val type: CKType
)
