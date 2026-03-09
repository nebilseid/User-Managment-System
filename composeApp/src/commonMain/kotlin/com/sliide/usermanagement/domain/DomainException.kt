package com.sliide.usermanagement.domain

sealed class DomainException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** No network connectivity. */
    class NetworkException(cause: Throwable? = null) : DomainException("No internet connection", cause)

    /** Server returned a 422 Unprocessable Entity (e.g. duplicate email). */
    class ConflictException(message: String, cause: Throwable? = null) : DomainException(message, cause)

    /** Any other server-side failure (5xx, unexpected response). */
    class ServerException(message: String, cause: Throwable? = null) : DomainException(message, cause)
}
