package com.maju.domain.repository.proxy.statement

import com.maju.utils.CKType
import com.maju.utils.PANACHE_QUERY
import com.squareup.kotlinpoet.ClassName

class PanacheStatementGenerator(
    private val targetType: CKType
) : StatementGenerator() {

    override val supportedTypes: List<ClassName>
        get() = listOf(PANACHE_QUERY.topLevelClassName())

    override fun createStatement(paramName: String): String {
        return "$paramName.project(${targetType.className.simpleName}::class.java)"
    }

}