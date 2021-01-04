package com.maju.generators.repository

import com.maju.entities.ConverterEntity
import com.maju.utils.IGenerator
import com.maju.utils.CKType

class ConverterEntityGenerator(
    private val type: CKType
) : IGenerator<ConverterEntity> {

    override fun generate(): ConverterEntity {
        return ConverterEntity(
            type = type
        )
    }

}
