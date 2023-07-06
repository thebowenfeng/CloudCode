package com.example.backend.controllers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class SessionControllerTest @Autowired constructor(val mockMvc: MockMvc){
    @Test
    fun `disallow 0 length user id`(){
        mockMvc.post("/api/session/create"){
            contentType = MediaType.APPLICATION_JSON
            content = "{\"user_id\": \"\", \"language\": \"IPYTHON\"}"
        }.andExpect {
            status { is5xxServerError() }
        }.andDo {
            this.print()
        }
    }

    @Test
    fun `valid language`(){
        mockMvc.post("/api/session/create"){
            contentType = MediaType.APPLICATION_JSON
            content = "{\"user_id\": \"abc\", \"language\": \"invalidlang\"}"
        }.andExpect {
            status { is5xxServerError() }
        }.andDo {
            this.print()
        }

        mockMvc.post("/api/session/create"){
            contentType = MediaType.APPLICATION_JSON
            content = "{\"user_id\": \"abc\", \"language\": \"IPYTHON\"}"
        }.andExpect {
            status { isOk() }
        }
    }
}