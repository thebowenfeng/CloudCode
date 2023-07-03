package com.example.backend.data

import com.example.backend.misc.DockerBridge
import org.springframework.stereotype.Repository
import org.springframework.web.context.annotation.ApplicationScope

@Repository
@ApplicationScope
class UserSession {
    val sessionMap: HashMap<String, DockerBridge> = HashMap()

    fun createSession(userId: String, bridge: DockerBridge){
        sessionMap[userId]?.dispose()
        sessionMap[userId] = bridge
    }

    fun deleteSession(userId: String){
        val oldBridge = sessionMap.remove(userId)
        oldBridge?.dispose()
    }

    fun getSession(userId: String): DockerBridge{
        return sessionMap[userId] ?: throw Exception("No session associated with userID")
    }

    fun checkSessionExist(userId: String): Boolean = userId in sessionMap

    fun getSessionCount(): Int = sessionMap.size
}