package com.maju.generators.repository.proxy.statement

interface IStatementGenerator {
    fun createStatement(paramName: String): String

}