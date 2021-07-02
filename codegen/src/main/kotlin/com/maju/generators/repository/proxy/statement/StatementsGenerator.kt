package com.maju.generators.repository.proxy.statement

import com.maju.entities.ConverterEntity
import com.maju.entities.MethodEntity
import com.maju.generators.repository.IGenerator
import com.maju.utils.STREAM
import com.maju.utils.hasArgument
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ITERABLE
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.UNIT
import java.util.*

class StatementsGenerator(
    private val methodEntity: MethodEntity,
    private val converterEntity: ConverterEntity
) : IGenerator<List<String>> {

    private val supportedTypes: List<ClassName> = listOf(LIST, STREAM.topLevelClassName(), ITERABLE.topLevelClassName())

    override fun generate(): List<String> {
        val targetType = converterEntity.targetType
        val converterName =
            converterEntity.type.className.simpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) }
        val returnType = methodEntity.returnType
        val params = methodEntity.parameters
        val methodName = methodEntity.name

        val returnTypeIsNullable = returnType.isNullable

        val statements = mutableListOf<String>()
        val convertToModel = "convertDTOToModel"
        val convertToDTO = "convertModelToDTO"

        val otherParams = params
            .filterNot { it.type.className == targetType.className || it.type.hasArgument(targetType) }
            .map { it.name }

        val dtoParams = params
            .filter { it.type.className == targetType.className }
            .map { "${it.name}Model" }

        val dtoListParams = params.filter { it.type.hasArgument(targetType) }
            .map { "${it.name}Models" }

        //Create variable for dto params
        for (dtoParam in dtoParams) {
            val toModel = "val $dtoParam =·$converterName.$convertToModel(${
                dtoParam.substring(
                    0,
                    dtoParam.length - 1 - 4
                )
            }) "
            statements.add(toModel)
        }

        //Create variable for List with dto as argument params
        for (dtoListParam in dtoListParams) {
            val originName = dtoListParam.subSequence(0, dtoListParam.length - 1 - 5)
            val toModel =
                "val $dtoListParam =·$originName.map($converterName::$convertToModel)"
            statements.add(toModel)
        }

        val allParams = dtoParams.plus(otherParams).plus(dtoListParams)
        val paramsAsString = allParams.joinToString(",·")

        val computeStatement = "repository.$methodName·($paramsAsString)"

        val isTargetType = returnType.className == targetType.className
        val hasTargetTypeAsArgument = returnType.hasArgument(targetType)
        val isSupportedType = supportedTypes.contains(returnType.className) && hasTargetTypeAsArgument

        val convertStatement = if (isTargetType) {
            //ReturnType is targetType
            "$converterName.$convertToDTO·(result)"
        } else {
            if (isSupportedType) {
                "result.map·($converterName::$convertToDTO)"
            } else {
                "result"
            }
        }


        if (returnType.className != UNIT) {
            statements.add("val result = $computeStatement")
            if (returnTypeIsNullable) {
                statements.add("return·if(result != null)·$convertStatement·else·null")
            } else {
                statements.add("return·$convertStatement·")
            }
        } else {
            statements.add(computeStatement)
        }

        return statements
    }

}