package com.example.vijaynet.exceptions

import java.io.IOException

// Base exception for anything that goes wrong in VijayNet
open class VijayNetException(message: String, cause: Throwable? = null) : IOException(message, cause)

// For non-2xx HTTP responses (e.g., 404, 500)
class HttpException(val code: Int, val errorBody: String?) :
    VijayNetException("HTTP Error status code: $code\nBody: $errorBody")

// For network dropouts / no internet
class NetworkConnectivityException(cause: Throwable) :
    VijayNetException("No internet connection or timeout occurred.", cause)