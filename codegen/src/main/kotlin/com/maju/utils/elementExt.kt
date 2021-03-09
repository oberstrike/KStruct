package com.maju.utils

import javax.lang.model.element.Element
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

inline fun <reified T : Annotation> Element.getAnnotationClassValue(f: T.() -> KClass<*>): TypeMirror = try {
    getAnnotation(T::class.java).f()
    throw Exception("Expected to get a MirroredTypeException")
} catch (e: MirroredTypeException) {
    e.typeMirror
}

inline fun <reified T : Annotation> Element.getAnnotationClassValues(f: T.() -> Array<KClass<*>>): List<TypeMirror> {
    println("test2")
    try {
        getAnnotation(T::class.java).f()
        throw Exception("Expected to get a MirroredTypeException")
    } catch (e: MirroredTypesException) {
        return e.typeMirrors
    }
}

