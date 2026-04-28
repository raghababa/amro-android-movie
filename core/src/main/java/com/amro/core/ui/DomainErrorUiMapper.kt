package com.amro.core.ui

import androidx.annotation.StringRes
import com.amro.core.R
import com.amro.domain.result.DomainError

data class DomainErrorUi(
    val message: UiText,
    val isRetryable: Boolean,
)

object DomainErrorUiMapper {
    fun map(
        error: DomainError,
        @StringRes notFoundMessageRes: Int = R.string.error_not_found,
        @StringRes invalidInputMessageRes: Int = R.string.error_something_went_wrong,
    ): DomainErrorUi {
        val message = when (error) {
            is DomainError.Network -> UiText.StringRes(R.string.error_network)
            is DomainError.Configuration -> UiText.StringRes(R.string.error_configuration)
            DomainError.Unauthorized -> UiText.StringRes(R.string.error_unauthorized)
            DomainError.NotFound -> UiText.StringRes(notFoundMessageRes)
            DomainError.RateLimited -> UiText.StringRes(R.string.error_rate_limited)
            DomainError.Server -> UiText.StringRes(R.string.error_server)
            is DomainError.InvalidInput -> UiText.StringRes(invalidInputMessageRes)
            is DomainError.UnexpectedEmpty -> UiText.StringRes(R.string.error_empty_result)
            is DomainError.Serialization -> UiText.StringRes(R.string.error_unexpected_response)
            is DomainError.Unknown -> UiText.StringRes(R.string.error_something_went_wrong)
        }
        return DomainErrorUi(
            message = message,
            isRetryable = error.isRetryable,
        )
    }
}
