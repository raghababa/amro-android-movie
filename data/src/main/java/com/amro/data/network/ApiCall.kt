package com.amro.data.network

import com.amro.domain.result.DomainError
import com.amro.domain.result.DomainResult
import com.amro.data.network.security.MissingTmdbTokenException
import kotlinx.serialization.SerializationException
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
                DomainResult.Error(DomainError.UnexpectedEmpty("Response body"))
            }
        } else {
            DomainResult.Error(
                response.code().toDomainError()
            )
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: MissingTmdbTokenException) {
        DomainResult.Error(DomainError.Configuration(cause = e))
    } catch (e: IOException) {
        DomainResult.Error(DomainError.Network(cause = e))
    } catch (e: SerializationException) {
        DomainResult.Error(DomainError.Serialization(cause = e))
    } catch (e: Exception) {
        DomainResult.Error(DomainError.Unknown(cause = e))
    }
}

private fun Int.toDomainError(): DomainError =
    when (this) {
        401 -> DomainError.Unauthorized
        404 -> DomainError.NotFound
        429 -> DomainError.RateLimited
        in 500..599 -> DomainError.Server
        else -> DomainError.Unknown()
    }

