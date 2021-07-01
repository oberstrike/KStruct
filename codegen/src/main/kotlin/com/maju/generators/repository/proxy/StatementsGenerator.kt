package com.maju.generators.repository.proxy

import com.maju.entities.ConverterEntity
import com.maju.entities.ParameterEntity
import com.maju.generators.repository.IGenerator
import com.maju.utils.CKType
import com.maju.utils.STREAM
import com.maju.utils.hasArgument
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.UNIT

class StatementsGenerator(
    private val params: List<ParameterEntity>,
    private val returnType: CKType,
    private val methodName: String,
    private val converterEntity: ConverterEntity
) : IGenerator<List<String>> {

    override fun generate(): List<String> {
        val targetType = converterEntity.targetType
        val converterName = converterEntity.type.className.simpleName.decapitalize()
        val returnTypeIsNullable = returnType.isNullable

        val statements = mutableListOf<String>()
        val convertToModel = "convertDTOToModel"
        val convertToModels = "convertDTOsToModels"
        val convertToDTO = "convertModelToDTO"
        val convertToDTOs = "convertModelsToDTOs"
        val convertToDTOStream = "convertStreamModelsToDTOs"


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
            val toModel =
                "val $dtoListParam =·$converterName.$convertToModels·(${
                    dtoListParam.subSequence(
                        0,
                        dtoListParam.length - 1 - 5
                    )
                })"
            statements.add(toModel)
        }

        val allParams = dtoParams.plus(otherParams).plus(dtoListParams)
        val paramsAsString = allParams.joinToString(",·")

        val computeStatement = "repository.$methodName·($paramsAsString)"

        val convertStatement = if (returnType.className == targetType.className) {
            //ReturnType is targetType
            "$converterName.$convertToDTO"
        } else if (returnType.hasArgument(targetType) && returnType.className == LIST) {
            //ReturnType is List with targetType as param
            "$converterName.$convertToDTOs"
        } else if (returnType.hasArgument(targetType) && returnType.className == STREAM.topLevelClassName())
            "$converterName.$convertToDTOStream"
        else {
            ""
        }


        if (returnType.className != UNIT) {
            statements.add("val result = $computeStatement")
            if (returnTypeIsNullable) {
                statements.add("return·if(result != null)·$convertStatement·(result) else null")
            } else {
                statements.add("return·$convertStatement·(result)")
            }
        } else {
            statements.add(computeStatement)
        }

        return statements
    }

}