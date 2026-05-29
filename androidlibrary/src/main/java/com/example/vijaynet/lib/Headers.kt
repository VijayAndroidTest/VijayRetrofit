package com.example.vijaynet.lib

@Target(AnnotationTarget.FUNCTION)
annotation class Headers(
    vararg val value: String
)
