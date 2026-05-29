package com.example.vijaynet.converter

import com.google.gson.Gson
import java.lang.reflect.Type

class GsonConverterFactory : Converter.Factory {
    private val gson = Gson()

    override fun create(): Converter {
        return object : Converter {
            override fun fromJson(json: String, type: Type): Any {
                return gson.fromJson(json, type)
            }
            override fun toJson(obj: Any): String {
                return gson.toJson(obj)
            }
        }
    }
}