package com.maju.utils

import com.squareup.kotlinpoet.TypeSpec

interface IGenerator<T> {
    fun generate(): T

}
