package com.maju.utils

import com.maju.domain.repository.proxy.statement.CollectionStatementGenerator
import com.maju.domain.repository.proxy.statement.PanacheStatementGenerator
import com.maju.domain.repository.proxy.statement.SimpleStatementGenerator
import com.maju.domain.repository.proxy.statement.StatementGenerator
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ITERABLE
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MUTABLE_LIST

object Constants {
    val collectionTypes: List<ClassName> = listOf(
        LIST,
        STREAM.topLevelClassName(),
        ITERABLE.topLevelClassName(),
        MUTABLE_LIST.topLevelClassName()
    )

    val panacheTypes: List<ClassName> = listOf(
        PANACHE_QUERY.topLevelClassName()
    )


    fun createStatementGeneratorByType(
        upperType: CKType,
        targetType: CKType,
        converterName: String,
        convertExpression: String
    ): StatementGenerator {
        return if (panacheTypes.contains(upperType.className)) {
            PanacheStatementGenerator(targetType)
        } else if (collectionTypes.contains(upperType.className)) {
            CollectionStatementGenerator(converterName, convertExpression)
        } else {
            SimpleStatementGenerator(converterName, convertExpression)
        }

    }


}