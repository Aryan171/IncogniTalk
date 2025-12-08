package com.incognitalk.app.data.exception

import java.io.IOException

class WebSocketConnectionException(message: String, cause: Throwable? = null) : IOException(message, cause)
