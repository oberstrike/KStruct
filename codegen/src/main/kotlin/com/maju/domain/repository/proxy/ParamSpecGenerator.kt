package com.maju.domain.repository.proxy

import com.maju.domain.generator.ParameterEntity
import com.maju.domain.repository.IGenerator
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