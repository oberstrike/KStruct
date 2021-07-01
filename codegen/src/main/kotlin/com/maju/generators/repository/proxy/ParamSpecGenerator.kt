package com.maju.generators.repository.proxy

import com.maju.entities.ParameterEntity
import com.maju.generators.repository.IGenerator
import com.maju.utils.toParameterizedTypeName
import com.squareup.kotlinpoet.ParameterSpec

class ParamSpecGenerator(
    private val parameterEntity: ParameterEntity
) : IGenerator<ParameterSpec> {

    override fun generate(): ParameterSpec {
        val parameterName = parameterEntity.name
        val parameterType = parameterEntity.type.toParameterizedTypeName()
        return ParameterSpec.builder(parameterName, parameterType).build()
    }

}