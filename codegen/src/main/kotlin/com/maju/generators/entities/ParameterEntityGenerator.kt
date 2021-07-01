package com.maju.generators.entities

import com.maju.entities.ParameterEntity
import com.maju.generators.repository.IGenerator
import com.maju.utils.CKType

class ParameterEntityGenerator(private val parameterName: String, private val ckType: CKType) :
    IGenerator<ParameterEntity> {
    override fun generate(): ParameterEntity {
        return ParameterEntity(parameterName, ckType)
    }
}
