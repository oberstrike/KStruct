package com.maju.domain.proxy.statement

interface IStatementGenerator {
    fun createStatement(paramName: String): String

}