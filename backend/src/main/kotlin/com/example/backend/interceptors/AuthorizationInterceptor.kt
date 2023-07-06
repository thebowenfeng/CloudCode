package com.example.backend.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthorizationInterceptor : HandlerInterceptor {
    private val apiKey = System.getenv("CODECLOUD_API_KEY")
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (request.getHeader("Authorization") != "Bearer $apiKey")
            throw Exception("Unauthorized request")
        return true
    }
}