package com.assignment.user.application.port.out

interface TokenProvider {
    fun createToken(email: String, role: String): String
    fun getExpirationSeconds(): Long
}
