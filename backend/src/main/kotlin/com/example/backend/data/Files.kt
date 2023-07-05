package com.example.backend.data

import org.springframework.stereotype.Repository
import org.springframework.web.context.annotation.ApplicationScope
import java.util.Base64

@Repository
@ApplicationScope
class Files {
    val fileMap = HashMap<String, Pair<Long, ByteArray>>()

    fun putFile(userId: String, byteData: ByteArray){
        fileMap[userId] = Pair(System.currentTimeMillis(), byteData)
    }

    fun getFile(userId: String): ByteArray {
        return fileMap.getOrDefault(userId, null)?.second ?: throw Exception("No file associated with user ID")
    }
}