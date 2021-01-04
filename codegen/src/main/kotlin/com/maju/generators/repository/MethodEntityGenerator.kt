package com.maju.generators.repository

import com.maju.entities.MethodEntity
import com.maju.entities.ParameterEntity
import com.maju.utils.IGenerator
import com.maju.utils.CKType

class MethodEntityGenerator(
    private val name: String,
    private val parameters: List<ParameterEntity>,
    private val returnType: CKType
) : IGenerator<MethodEntity> {

    override fun generate(): MethodEntity {
        return MethodEntity(
            name = name,
            parameters = parameters,
            returnType = returnType
        )
    }
}
