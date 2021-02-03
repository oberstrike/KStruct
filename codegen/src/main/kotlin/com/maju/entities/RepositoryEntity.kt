package com.maju.entities

import com.maju.utils.CKType
import com.maju.utils.parameterizedToType
import com.squareup.kotlinpoet.LIST


data class RepositoryEntity(
    val name: String,
    val type: CKType,
    val converter: ConverterEntity,
    val methods: List<MethodEntity>,
    val repositoryType: RepositoryType = RepositoryType.DEFAULT_ENTITY
)


enum class RepositoryType {
    PANACHE_ENTITY, DEFAULT_ENTITY
}

data class ConverterEntity(
    val type: CKType,
    val modelType: CKType,
    val dtoType: CKType
) {
    fun convert(type: CKType): CKType {
        val listOfModelType = LIST.parameterizedToType(modelType)
        val listOfDTOType = LIST.parameterizedToType(dtoType)
        val modelTypeNullable = modelType.copy(isNullable = true)
        val dtoTypeNullable = dtoType.copy(isNullable = true)

        return when (type) {
            modelType -> {
                dtoType
            }
            listOfModelType -> {
                listOfDTOType
            }
            modelTypeNullable -> {
                dtoTypeNullable
            }
            else -> {
                type
            }
        }
    }

}


data class MethodEntity(
    val name: String,
    val parameters: List<ParameterEntity>,
    val returnType: CKType
)

data class ParameterEntity(
    val name: String,
    val type: CKType
)
