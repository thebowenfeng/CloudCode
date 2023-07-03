package com.example.backend.jobs

import com.example.backend.data.UserSession
import com.example.backend.services.DockerExecutorService
import org.http4k.client.ApacheClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DockerMonitor @Autowired constructor(
        private val session: UserSession,
        private val dockerService: DockerExecutorService
) {
    private val client: HttpHandler = ApacheClient()
    private val host: String = System.getenv("CODECLOUD_DOCKER_HOST")
    private val MAX_AFK_TIME = 60000
    private val MAX_LIFETIME = 600000
    @Scheduled(fixedRate = 1000)
    fun pruneSessions(){
        val listContainersReq = client(Request(Method.GET, "http://$host:4243/containers/json"))
        if (listContainersReq.status.code != 200){
            println("Unable to query for containers")
        } else {
            val containerList = JSONArray(listContainersReq.body.toString()).map { JSONObject(it.toString()) }
            val containersToShutdown = containerList.filter { jsonEntry ->
                session.sessionMap.none { mapEntry -> mapEntry.value.containerId == jsonEntry.get("Id").toString() }
            }
            val sessionToRemove = session.sessionMap.filter { mapEntry ->
                containerList.none { jsonEntry -> jsonEntry.get("Id").toString() == mapEntry.value.containerId }
            }

            containersToShutdown.forEach {
                dockerService.shutdownDocker(it.get("Id").toString())
            }
            sessionToRemove.forEach {
                session.deleteSession(it.key)
            }
        }
    }

    @Scheduled(fixedRate = 1000)
    fun pruneStaleSessions(){
        val staleSessions = session.sessionMap.filter {
            System.currentTimeMillis() - it.value.lastInteracted > MAX_AFK_TIME ||
                    System.currentTimeMillis() - it.value.created > MAX_LIFETIME
        }
        staleSessions.forEach {
            dockerService.deleteDocker(it.key)
            session.deleteSession(it.key)
        }
    }
}