package com.example.backend.controllers

import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class RootControllerTest @Autowired constructor(val mockMvc: MockMvc) {
    @Test
    fun `Returns valid version`() {
        mockMvc.get("/").andExpect {
            status { isOk() }
            content {
                string(Matchers.not(""))
            }
        }
    }
}