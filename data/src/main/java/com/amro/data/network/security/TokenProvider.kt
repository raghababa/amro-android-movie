package com.amro.data.network.security

fun interface TokenProvider {
    fun getToken(): String
}
