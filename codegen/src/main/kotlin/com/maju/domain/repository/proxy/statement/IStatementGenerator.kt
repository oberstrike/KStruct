package com.maju.domain.repository.proxy.statement

interface IStatementGenerator {
    fun createStatement(paramName: String): String

}