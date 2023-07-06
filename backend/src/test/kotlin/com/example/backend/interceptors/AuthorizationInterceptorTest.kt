package com.example.backend.interceptors

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@AutoConfigureMockMvc
class AuthorizationInterceptorTest(@Autowired val mockMvc: MockMvc){
    private lateinit var apiKey: String
    @Order(1)
    @Test
    fun `load api key`(){
        apiKey = System.getenv("CODECLOUD_API_KEY")
    }
    @Order(2)
    @Test
    fun `Wrong token`(){
        mockMvc.post("/api/session/create"){
            header("Authorization", "abcd")
        }.andExpect {
            status { is5xxServerError() }
        }

        mockMvc.post("/api/session/create").andExpect {
            status { is5xxServerError() }
        }
    }
    @Order(3)
    @Test
    fun `correct token`(){
        apiKey = System.getenv("CODECLOUD_API_KEY")
        mockMvc.post("/api/session/create"){
            contentType = MediaType.APPLICATION_JSON
            content = "{\"user_id\": \"abc\", \"language\": \"IPYTHON\"}"
            header("Authorization", "Bearer $apiKey")
        }.andExpect {
            status { isOk() }
        }

        mockMvc.post("/api/session/delete"){
            contentType = MediaType.APPLICATION_JSON
            content = "{\"user_id\": \"abc\"}"
            header("Authorization", "Bearer $apiKey")
        }.andExpect {
            status { isOk() }
        }
    }
}