package com.maju.generators.repository.proxy.statement

import com.maju.entities.ConverterEntity
import com.maju.entities.MethodEntity
import com.maju.entities.ParameterEntity
import com.maju.generators.repository.IGenerator
import com.maju.utils.CKType
import com.maju.utils.Constants.createStatementGeneratorByType

class StatementsGenerator(
    private val methodEntity: MethodEntity,
    private val converterEntities: List<ConverterEntity>
) : IGenerator<List<String>> {

    private val statements = mutableListOf<String>()

    private val resultVariableName = "result"


    override fun generate(): List<String> {
        val returnType = methodEntity.returnType

        val allParams = methodEntity.parameters.joinToString(",·") { param -> analyseParam(param) }

        var resultStatement = "val $resultVariableName = repository.·${methodEntity.name}·(·$allParams·)"

        if (returnType.isNullable) {
            resultStatement += "?: return null"
        }

        statements.add(resultStatement)
        statements.add(analyseReturnType(returnType))
        return statements
    }

    private fun getConverterByTargetType(target: CKType): ConverterEntity? {
        val arguments = target.arguments
        if (arguments.isNotEmpty()) {
            return getConverterByTargetType(arguments.first())
        }

        return converterEntities.find { converter ->
            converter.targetType.className == target.className
        }

    }

    private fun analyseParam(param: ParameterEntity): String {
        val paramType = param.type
        val converterEntity = getConverterByTargetType(paramType)

        var targetName = param.name
        val originName = param.name

        val targetType = converterEntity?.targetType ?: return targetName

        targetName = "${param.name}Model"
        val converterName = converterEntity.getName()

        val generator: StatementGenerator = createStatementGeneratorByType(
            paramType,
            targetType,
            converterName,
            converterEntity.targetToOriginFunctionName
        )

        statements.add("val $targetName = ${generator.createStatement(originName)}")

        return targetName
    }

    private fun analyseReturnType(returnType: CKType): String {
        val converterEntity = getConverterByTargetType(returnType)
        val targetName = "result"
        val targetType = converterEntity?.targetType ?: return "return $targetName"

        val generator: StatementGenerator = createStatementGeneratorByType(
            returnType,
            targetType,
            converterEntity.getName(),
            converterEntity.originToTargetFunctionName
        )

        return "return ${generator.createStatement(targetName)}"
    }

}