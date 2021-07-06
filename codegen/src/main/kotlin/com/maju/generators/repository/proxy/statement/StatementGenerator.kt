package com.maju.generators.repository.proxy.statement

import com.squareup.kotlinpoet.ClassName

abstract class StatementGenerator : IStatementGenerator {
    abstract val supportedTypes: List<ClassName>
}