package com.amro.data.network

import com.amro.domain.result.DomainError
import com.amro.domain.result.DomainResult
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException
import kotlinx.coroutines.CancellationException

internal suspend inline fun <T> apiCall(
    crossinline block: suspend () -> Response<T>,
): DomainResult<T> {
    return try {
        val response = block()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                DomainResult.Success(body)
            } else {
                DomainResult.Error(DomainError.Unknown(cause = NullPointerException("Body was null")))
            }
        } else {
            DomainResult.Error(
                DomainError.Http(
                    code = response.code(),
                    errorBody = response.errorBody()?.safeString(),
                )
            )
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        DomainResult.Error(DomainError.Network(cause = e))
    } catch (e: SerializationException) {
        DomainResult.Error(DomainError.Serialization(cause = e))
    } catch (e: Exception) {
        DomainResult.Error(DomainError.Unknown(cause = e))
    }
}

internal fun ResponseBody.safeString(): String? =
    try {
        string()
    } catch (_: Exception) {
        null
    }

