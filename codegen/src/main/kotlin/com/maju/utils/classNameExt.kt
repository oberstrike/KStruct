package com.maju.utils

import com.squareup.kotlinpoet.ClassName
import io.quarkus.hibernate.orm.panache.kotlin.PanacheQuery
import org.springframework.beans.factory.annotation.Autowired

fun ClassName.toType(): CKType {
    return CKType(
        className = this,
        isNullable = isNullable
    )
}

fun ClassName.parameterizedToType(type: CKType): CKType {
    return CKType(
        className = this,
        isNullable = isNullable,
        arguments = listOf(type)
    )
}

fun ClassName.parameterizedToType(className: ClassName): CKType {
    return CKType(
        className = this,
        isNullable = isNullable,
        arguments = listOf(className.toType())
    )
}

@JvmField
val STREAM = ClassName("java.util.stream", "Stream")

@JvmField
val PANACHE_QUERY = ClassName("io.quarkus.hibernate.orm.panache.kotlin", "PanacheQuery")

@JvmField
val APPLICATION_SCOPED = ClassName("javax.enterprise.context", "ApplicationScoped")

@Autowired
val AUTOWIRED = ClassName("org.springframework.beans.factory.annotation", "Autowired")