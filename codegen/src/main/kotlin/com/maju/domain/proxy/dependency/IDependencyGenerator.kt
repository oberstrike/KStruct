package com.maju.domain.proxy.dependency

import com.maju.cli.ComponentModel
import com.squareup.kotlinpoet.*

interface IDependencyGenerator {

    fun getDependency(): Dependency
}


