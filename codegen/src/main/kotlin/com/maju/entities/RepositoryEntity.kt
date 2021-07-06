package com.maju.entities

import com.maju.utils.CKType
import com.maju.utils.parameterizedToType
import com.squareup.kotlinpoet.LIST
import java.util.*


data class RepositoryEntity(
    val name: String,
    val type: CKType,
    val converters: List<ConverterEntity>,
    val methods: List<MethodEntity>,
    val panacheEntity: ExtensionEntity? = null
)


data class ExtensionEntity(
    val type: CKType,
    val idType: CKType
)

data class ConverterEntity(
    val type: CKType,
    val originType: CKType,
    val targetType: CKType,
    val originToTargetFunctionName: String,
    val targetToOriginFunctionName: String
) {

    fun getName() = type.className.simpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) }

    fun convert(type: CKType): CKType? {
        val listOfModelType = LIST.parameterizedToType(originType)
        val listOfDTOType = LIST.parameterizedToType(targetType)
        val modelTypeNullable = originType.copy(isNullable = true)
        val dtoTypeNullable = targetType.copy(isNullable = true)

        return when (type) {
            originType -> {
                targetType
            }
            listOfModelType -> {
                listOfDTOType
            }
            modelTypeNullable -> {
                dtoTypeNullable
            }
            else -> {
                null
            }
        }
    }

}


data class MethodEntity(
    val name: String,
    val parameters: List<ParameterEntity>,
    val returnType: CKType,
    val isSuspend: Boolean
)

data class ParameterEntity(
    val name: String,
    val type: CKType
)
