package com.example.vijaynet.lib

import com.google.gson.Gson
import java.lang.reflect.Type

class GsonConverterFactory : Converter.Factory {
    private val gson = Gson()

    override fun create(): Converter {
        return object : Converter {
            override fun fromJson(json: String, type: Type): Any {
                // If Gson encounters a map and we expect a class,
                // this forces re-serialization/deserialization to bind to the class
                val result = gson.fromJson<Any>(json, type)

                if (result is com.google.gson.internal.LinkedTreeMap<*, *>) {
                    val jsonString = gson.toJson(result)
                    return gson.fromJson(jsonString, type)
                }
                return result
            }
            override fun toJson(obj: Any): String = gson.toJson(obj)
        }
    }
}