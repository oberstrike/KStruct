package com.maju.domain.generator

import com.maju.utils.CKType
import com.maju.utils.firstCharToLower
import com.maju.utils.parameterizedToType
import com.squareup.kotlinpoet.LIST
import java.util.*

data class ConverterEntity(
    val type: CKType,
    val originType: CKType,
    val targetType: CKType,
    val originToTargetFunctionName: String,
    val targetToOriginFunctionName: String
) {

    fun getName() = type.className.simpleName.firstCharToLower()

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