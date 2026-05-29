package com.example.vijaynet.converter

import java.lang.reflect.Type

interface Converter {
    fun fromJson(json: String, type: Type): Any
    fun toJson(obj: Any): String

    interface Factory {
        fun create(): Converter
    }
}