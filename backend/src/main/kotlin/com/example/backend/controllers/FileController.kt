package com.example.backend.controllers

import com.example.backend.data.Files
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/files")
class FileController(@Autowired private val files: Files) {
    @GetMapping("/get")
    fun getFile(request: HttpServletRequest, @RequestParam userId: String): ResponseEntity<Resource> =
        ResponseEntity.ok().contentLength(files.getFile(userId).size.toLong())
            .contentType(MediaType.APPLICATION_OCTET_STREAM).body(ByteArrayResource(files.getFile(userId)))

    @PostMapping("/add/{userId}")
    fun addFile(request: HttpServletRequest, @PathVariable("userId") userId: String, @RequestBody file: MultipartFile): String{
        if (userId.isEmpty()) throw Exception("User ID cannot be empty")

        files.putFile(userId, file.bytes)
        return "http://${request.getHeader("host")}/api/files/get?userId=${userId}"
    }
}