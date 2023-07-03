package com.example.backend.controllers

import com.example.backend.dto.UserInputDto
import com.example.backend.dto.UserSessionDto
import com.example.backend.services.DockerExecutorService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.lang.IllegalArgumentException

@RestController
@RequestMapping("/api/session")
class SessionController (@Autowired private val dockerExecutor: DockerExecutorService) {
    @PostMapping("/create")
    fun createSession(request: HttpServletRequest, @RequestBody newUser: UserSessionDto): ResponseEntity<Any>{
        newUser.language ?: throw Exception("Language cannot be null")
        if (newUser.userId.isEmpty()) throw IllegalArgumentException("User ID cannot be length 0")

        dockerExecutor.createDocker(newUser.userId, newUser.language)
        return ResponseEntity<Any>(HttpStatus.OK)
    }

    @GetMapping("/output")
    fun getSessionOutput(request: HttpServletRequest, @RequestParam userId: String): String =
            dockerExecutor.readDockerOutput(userId)

    @PostMapping("/input")
    fun sessionInput(request: HttpServletRequest, @RequestBody input: UserInputDto){
        dockerExecutor.inputDocker(input.userId, input.input)
    }

    @PostMapping("/delete")
    fun deleteSession(request: HttpServletRequest, @RequestBody user : UserSessionDto): ResponseEntity<Any>{
        if (user.userId.isEmpty()) throw IllegalArgumentException("User ID cannot be length 0")
        dockerExecutor.deleteDocker(user.userId)
        return ResponseEntity(HttpStatus.OK)
    }
}