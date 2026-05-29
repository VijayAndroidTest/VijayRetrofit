package com.example.vijaynet.lib



@Target(AnnotationTarget.VALUE_PARAMETER) // This MUST be VALUE_PARAMETER
@Retention(AnnotationRetention.RUNTIME)
annotation class Header(val value: String)
