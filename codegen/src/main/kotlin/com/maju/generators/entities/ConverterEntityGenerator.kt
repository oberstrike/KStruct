package com.maju.generators.entities

import com.maju.entities.ConverterEntity
import com.maju.generators.repository.IGenerator
import com.maju.utils.CKType

class ConverterEntityGenerator(
    private val type: CKType,
    private val originType: CKType,
    private val targetType: CKType
) : IGenerator<ConverterEntity> {

    override fun generate(): ConverterEntity {
        return ConverterEntity(
            type = type,
            originType = originType,
            targetType = targetType
        )
    }
}


