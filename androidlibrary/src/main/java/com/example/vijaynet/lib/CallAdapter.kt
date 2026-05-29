package com.example.vijaynet.lib

interface CallAdapter<R, T> {

    fun adapt(call: HttpCall<R>): T
}