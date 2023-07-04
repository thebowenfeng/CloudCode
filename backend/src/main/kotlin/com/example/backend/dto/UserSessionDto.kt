package com.example.backend.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue

enum class Language{
        @JsonValue
        IPYTHON, PYTHON
}

data class UserSessionDto (
        @JsonProperty("user_id")
        val userId: String,
        @JsonProperty("language")
        val language: Language?,
        @JsonProperty("file_url")
        val fileURL: String?
)