package com.example.backend.controllers

import com.example.backend.data.UserSession
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/metrics")
class MetricsController(@Autowired private val session: UserSession) {
    @GetMapping("/session/count")
    fun getNumSessions(request: HttpServletRequest): Int = session.getSessionCount()

    @GetMapping("/session/has")
    fun hasSession(request: HttpServletRequest, @RequestParam userId: String): ResponseEntity<Any>{
        return if (session.checkSessionExist(userId)) {
            ResponseEntity(HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }
}