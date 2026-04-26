package com.amro.domain.result

sealed class DomainError(
    open val cause: Throwable? = null,
) {
    abstract val isRetryable: Boolean

    data class Network(
        override val cause: Throwable? = null,
    ) : DomainError(cause = cause) {
        override val isRetryable: Boolean = true
    }

    data class Http(
        val code: Int,
        val errorBody: String? = null,
        override val cause: Throwable? = null,
    ) : DomainError(cause = cause) {
        override val isRetryable: Boolean = code >= 500
    }

    data class Serialization(
        override val cause: Throwable? = null,
    ) : DomainError(cause = cause) {
        override val isRetryable: Boolean = false
    }

    data class Unknown(
        override val cause: Throwable? = null,
    ) : DomainError(cause = cause) {
        override val isRetryable: Boolean = false
    }

    data class Empty(
        val what: String,
    ) : DomainError() {
        override val isRetryable: Boolean = false
    }
}

