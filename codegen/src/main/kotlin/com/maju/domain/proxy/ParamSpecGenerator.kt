package com.maju.domain.proxy

import com.maju.domain.generator.ParameterEntity
import com.maju.utils.IGenerator
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