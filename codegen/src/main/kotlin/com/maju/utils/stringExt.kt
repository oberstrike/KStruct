package com.maju.utils

import java.util.*

fun String.firstCharToLower(): String = replaceFirstChar { it.lowercase(Locale.getDefault()) }
