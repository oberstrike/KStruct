package com.maju.domain.entities

import com.maju.domain.generator.ParameterEntity
import com.maju.utils.IGenerator
import com.maju.utils.CKType

class ParameterEntityGenerator(private val parameterName: String, private val ckType: CKType) :
    IGenerator<ParameterEntity> {
    override fun generate(): ParameterEntity {
        return ParameterEntity(parameterName, ckType)
    }
}
