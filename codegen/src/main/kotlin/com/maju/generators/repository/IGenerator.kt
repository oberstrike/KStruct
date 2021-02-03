package com.maju.generators.repository

interface IGenerator<T> {
    fun generate(): T

}
