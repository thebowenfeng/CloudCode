package com.example.backend.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UserInputDto(
        @JsonProperty("user_id")
        val userId: String,
        @JsonProperty("input")
        val input: String
)
