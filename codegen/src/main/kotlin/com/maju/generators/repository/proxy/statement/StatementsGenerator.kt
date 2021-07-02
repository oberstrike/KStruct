package com.maju.generators.repository.proxy.statement

import com.maju.entities.ConverterEntity
import com.maju.entities.MethodEntity
import com.maju.entities.ParameterEntity
import com.maju.generators.repository.IGenerator
import com.maju.utils.CKType
import com.maju.utils.STREAM
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ITERABLE
import com.squareup.kotlinpoet.LIST

class StatementsGenerator(
    private val methodEntity: MethodEntity,
    private val converterEntities: List<ConverterEntity>
) : IGenerator<List<String>> {

    private val supportedTypes: List<ClassName> = listOf(LIST, STREAM.topLevelClassName(), ITERABLE.topLevelClassName())
    private val convertToModel = "convertDTOToModel"
    private val convertToDTO = "convertModelToDTO"

    override fun generate(): List<String> {
        val returnType = methodEntity.returnType
        val params = methodEntity.parameters
        val methodName = methodEntity.name
        val statements = mutableListOf<String>()
        val isNullable = methodEntity.returnType.isNullable


        val allParams = params.map { analyseParam(it, statements) }
        val paramsAsString = allParams.joinToString(",·")
        val computeStatement = "val result = repository.·$methodName·(·$paramsAsString·)"
        statements.add(computeStatement)
        if(isNullable){
            statements.add("if(result·==·null)·return·null ")
        }

        val returnStatement = analyseReturnType(returnType)
        statements.add(returnStatement)
        return statements
    }

    private fun analyseParam(param: ParameterEntity, statements: MutableList<String>): String {
        var paramName = param.name
        val paramType = param.type
        val isSupportedType = supportedTypes.contains(paramType.className)
        val targetConverterEntity = converterEntities.find { converter -> converter.targetType.className == paramType.className }

        if (isSupportedType) {
            val argument = paramType.arguments.firstOrNull()
            if (argument != null) {
                val converterEntity = converterEntities.find { converter ->
                    converter.targetType.className == argument.className
                }
                if (converterEntity != null) {
                    val converterName = converterEntity.getName()
                    paramName = "${param.name}Model"
                    val statement = "val $paramName = ${param.name}.map(·$converterName::$convertToModel) "
                    statements.add(statement)
                }
            }
        }else if(targetConverterEntity != null) {
            val converterName = targetConverterEntity.getName()
            paramName = "${param.name}Model"
            val statement = "val $paramName =·$converterName.$convertToModel(${param.name})"
            statements.add(statement)
        }

        return paramName
    }

    private fun analyseReturnType(returnType: CKType): String {
        var returnStatement = "return result"
        val isSupportedType = supportedTypes.contains(returnType.className)
        val targetConverterEntity = converterEntities.find { converter -> converter.targetType.className == returnType.className }

        if(isSupportedType){
            val argument = returnType.arguments.firstOrNull()
            if (argument != null) {
                val converterEntity = converterEntities.find { converter ->
                    converter.targetType.className == argument.className
                }
                if (converterEntity != null) {
                    val converterName = converterEntity.getName()
                    return "return result.map($converterName::$convertToDTO)"
                }
            }
        }else if(targetConverterEntity != null){
            val converterName = targetConverterEntity.getName()
            return "return $converterName.$convertToDTO(result)"

        }

        return returnStatement
    }

}