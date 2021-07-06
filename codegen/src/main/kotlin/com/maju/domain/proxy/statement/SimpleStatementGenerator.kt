package com.maju.domain.proxy.statement

import com.squareup.kotlinpoet.ClassName

class SimpleStatementGenerator(
    private val converterName: String,
    private val convertExpressions: String
): StatementGenerator() {


    override val supportedTypes: List<ClassName>
        get() = listOf()

    override fun createStatement(paramName: String): String {
        return "$converterName.$convertExpressions($paramName)"
    }
}