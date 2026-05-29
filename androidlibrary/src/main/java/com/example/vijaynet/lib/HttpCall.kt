package com.example.vijaynet.lib

interface HttpCall<T> {

    suspend fun execute(): T
}