package com.maju.domain.proxy.statement

import com.squareup.kotlinpoet.ClassName

abstract class StatementGenerator : IStatementGenerator {
    abstract val supportedTypes: List<ClassName>
}