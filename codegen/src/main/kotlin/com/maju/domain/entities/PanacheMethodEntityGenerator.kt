package com.maju.domain.entities

import com.maju.domain.generator.MethodEntity
import com.maju.domain.generator.ParameterEntity
import com.maju.utils.IGenerator
import com.maju.utils.CKType
import com.maju.utils.Constants
import com.maju.utils.toType
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
            && !Constants.collectionTypes.contains(targetReturnType.className)
            && !Constants.panacheTypes.contains(targetReturnType.className)
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