package com.maju.domain.proxy.statement


import com.maju.utils.STREAM
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ITERABLE
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MUTABLE_LIST

class CollectionStatementGenerator(
    private val converterName: String,
    private val convertExpression: String
): StatementGenerator() {

    override val supportedTypes: List<ClassName>
        get() = listOf(
            LIST,
            STREAM.topLevelClassName(),
            ITERABLE.topLevelClassName(),
            MUTABLE_LIST.topLevelClassName()
        )

    override fun createStatement(paramName: String): String{
        return  "${paramName}.map(·$converterName::$convertExpression) "
    }
}