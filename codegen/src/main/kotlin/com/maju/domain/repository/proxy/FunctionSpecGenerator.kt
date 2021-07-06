package com.maju.domain.repository.proxy

import com.maju.domain.generator.ConverterEntity
import com.maju.domain.generator.MethodEntity
import com.maju.domain.repository.IGenerator
import com.maju.domain.repository.proxy.statement.StatementsGenerator
import com.maju.utils.toParameterizedTypeName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

class FunctionSpecGenerator(
    private val methodEntity: MethodEntity,
    private val converterEntities: List<ConverterEntity>
) : IGenerator<FunSpec> {


    override fun generate(): FunSpec {
        val methodName = methodEntity.name
        val methodReturnType = methodEntity.returnType
        val methodParameters = methodEntity.parameters
        val isSuspend = methodEntity.isSuspend

        return FunSpec.builder(methodName)
            .returns(methodReturnType.toParameterizedTypeName())
            .apply {
                val statementGenerator = StatementsGenerator(
                    methodEntity,
                    converterEntities
                )

                val statements = statementGenerator.generate()
                for (statement in statements) {
                    addStatement(statement)
                }
                if (isSuspend) addModifiers(KModifier.SUSPEND)
            }
            .addParameters(methodParameters.map { param -> ParamSpecGenerator(param).generate() })
            .build()
    }

}