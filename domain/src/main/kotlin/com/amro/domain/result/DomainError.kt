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

    data class Configuration(
        override val cause: Throwable? = null,
    ) : DomainError(cause = cause) {
        override val isRetryable: Boolean = false
    }

    data object Unauthorized : DomainError() {
        override val isRetryable: Boolean = false
    }

    data object NotFound : DomainError() {
        override val isRetryable: Boolean = false
    }

    data object RateLimited : DomainError() {
        override val isRetryable: Boolean = true
    }

    data object Server : DomainError() {
        override val isRetryable: Boolean = true
    }

    data class InvalidInput(
        val field: String,
    ) : DomainError() {
        override val isRetryable: Boolean = false
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

    /**
     * Represents an unexpectedly empty result where the use case requires data.
     *
     * Valid empty states should remain successful empty collections and be interpreted by the UI.
     */
    data class UnexpectedEmpty(
        val what: String,
    ) : DomainError() {
        override val isRetryable: Boolean = false
    }
}

