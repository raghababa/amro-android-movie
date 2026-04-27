package com.amro.domain.result

sealed interface DomainResult<out T> {
    data class Success<T>(val value: T) : DomainResult<T>
    data class Error(val error: DomainError) : DomainResult<Nothing>
}

inline fun <T, R> DomainResult<T>.map(transform: (T) -> R): DomainResult<R> =
    when (this) {
        is DomainResult.Success -> DomainResult.Success(transform(value))
        is DomainResult.Error -> this
    }

inline fun <T, R> DomainResult<T>.flatMap(
    transform: (T) -> DomainResult<R>,
): DomainResult<R> =
    when (this) {
        is DomainResult.Success -> transform(value)
        is DomainResult.Error -> this
    }

inline fun <T> DomainResult<T>.onSuccess(action: (T) -> Unit): DomainResult<T> {
    if (this is DomainResult.Success) action(value)
    return this
}

inline fun <T> DomainResult<T>.onError(action: (DomainError) -> Unit): DomainResult<T> {
    if (this is DomainResult.Error) action(error)
    return this
}

fun <T> DomainResult<T>.getOrNull(): T? =
    (this as? DomainResult.Success)?.value

inline fun <T> DomainResult<T>.getOrElse(default: () -> T): T =
    when (this) {
        is DomainResult.Success -> value
        is DomainResult.Error -> default()
    }

