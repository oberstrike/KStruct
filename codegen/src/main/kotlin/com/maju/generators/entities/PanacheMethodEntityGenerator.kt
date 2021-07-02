package com.maju.generators.entities

import com.maju.entities.MethodEntity
import com.maju.entities.ParameterEntity
import com.maju.generators.repository.IGenerator
import com.maju.utils.CKType
import com.maju.utils.STREAM
import com.maju.utils.toType
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.metadata.ImmutableKmFunction
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isNullable
import com.squareup.kotlinpoet.metadata.isSuspend
import kotlinx.metadata.KmClassifier

@KotlinPoetMetadataPreview
class PanacheMethodEntityGenerator(
    private val function: ImmutableKmFunction,
    private val targetType: CKType,
    private val idType: CKType
) : IGenerator<MethodEntity?> {

    override fun generate(): MethodEntity? {

        val functionName = function.name

        val originReturnType = function.returnType

        val isSuspend = function.isSuspend

        val targetReturnType = if (originReturnType.classifier is KmClassifier.TypeParameter) {
            targetType.copy(isNullable = originReturnType.isNullable)
        } else {
            if (originReturnType.arguments.isNotEmpty()) {
                originReturnType.toType(targetType)
            } else {
                originReturnType.toType()
            }
        }

        val parameters = function.valueParameters
        val mParameters = mutableListOf<ParameterEntity>()

        if (parameters.map { it.type }.any { it!!.arguments.isNotEmpty() }) {
            return null
        }

        if (targetReturnType.arguments.isNotEmpty()
            && targetReturnType.className != STREAM
            && targetReturnType.className != LIST
        ) return null

        for (parameter in parameters) {
            val parameterType = parameter.type ?: continue
            val parameterName = parameter.name

            val targetParameterType = if (parameter.type!!.classifier is KmClassifier.TypeParameter) {
                if (parameterName == "id") {
                    idType.copy(isNullable = false)
                } else {
                    if (parameterType.isNullable) targetType.copy(isNullable = true)
                    else targetType
                }
            } else {
                parameterType.toType()
            }
            val parameterEntity = ParameterEntityGenerator(parameterName, targetParameterType).generate()
            mParameters.add(parameterEntity)
        }

        return MethodEntityGenerator(functionName, mParameters, targetReturnType, isSuspend).generate();


    }
}