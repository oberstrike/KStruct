package com.maju.domain.entities

import com.maju.domain.generator.MethodEntity
import com.maju.domain.generator.ParameterEntity
import com.maju.utils.IGenerator
import com.maju.utils.CKType

class MethodEntityGenerator(
    private val name: String,
    private val parameters: List<ParameterEntity>,
    private val returnType: CKType,
    private val isSuspend: Boolean
) : IGenerator<MethodEntity> {

    override fun generate(): MethodEntity {
        return MethodEntity(
            name = name,
            parameters = parameters,
            returnType = returnType,
            isSuspend = isSuspend
        )
    }
}
