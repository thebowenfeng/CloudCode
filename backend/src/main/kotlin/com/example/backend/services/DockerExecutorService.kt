package com.example.backend.services

import com.example.backend.data.UserSession
import com.example.backend.dto.Language
import com.example.backend.misc.DockerBridge
import org.http4k.client.ApacheClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.json.JSONObject

@Service
class DockerExecutorService (@Autowired private val session: UserSession) {
    private val MAX_CONCURRENT_SESSION = 5
    private val host: String = System.getenv("CODECLOUD_DOCKER_HOST")
    private val client: HttpHandler = ApacheClient()

    fun createDocker(userId: String, language: Language){
        if (session.checkSessionExist(userId)) throw Exception("Close existing session before opening new one")
        if (session.getSessionCount() == MAX_CONCURRENT_SESSION)
            throw Exception("Limit reached on concurrent sessions. Max is $MAX_CONCURRENT_SESSION")

        var nextAvailPort: Int = -1
        for (currPort in 10000..11000){
            if(session.sessionMap.filter { it.value.port == currPort }.isEmpty()){
                nextAvailPort = currPort
                break
            }
        }

        if (nextAvailPort == -1) throw Exception("Cannot obtain available port for docker service")

        val imageName = when(language){
            Language.IPYTHON -> "ipython:latest"
            else -> throw Exception("$language is not currently supported")
        }

        // Create container
        val createContainerReq = client(Request(Method.POST, "http://$host:4243/containers/create")
                .header("content-type", "application/json")
                .body("{\n" +
                        "    \"Image\": \"$imageName\",\n" +
                        "    \"Cmd\": [\"$nextAvailPort\"],\n" +
                        "    \"ExposedPorts\": {\n" +
                        "        \"$nextAvailPort/tcp\": {}\n" +
                        "    },\n" +
                        "    \"HostConfig\": {\n" +
                        "        \"Memory\": 536870912,\n" +
                        "        \"MemorySwap\": 536870912,\n" +
                        "        \"NanoCpus\": 1000000000,\n" +
                        "        \"PortBindings\": {\n" +
                        "            \"$nextAvailPort/tcp\": [{\"HostIp\": \"\", \"HostPort\": \"$nextAvailPort\"}]\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"))
        val containerId: String = if (createContainerReq.status.code != 201){
            throw Exception("Unable to create docker container")
        } else {
            JSONObject(createContainerReq.body.toString()).get("Id").toString()
        }

        val startContainerReq = client(Request(Method.POST, "http://$host:4243/containers/$containerId/start"))
        if (startContainerReq.status.code != 204) throw Exception("Unable to start docker container")

        // Check in case a lot of containers were created in the meantime
        if (session.getSessionCount() == MAX_CONCURRENT_SESSION){
            shutdownDocker(containerId)
            throw Exception("Limit reached on concurrent sessions. Max is $MAX_CONCURRENT_SESSION")
        }

        // Try connecting for 5 seconds before giving up
        val currTime = System.currentTimeMillis()
        while(System.currentTimeMillis() - currTime <= 5000){
            try {
                session.createSession(userId, DockerBridge(host, nextAvailPort, containerId))
                return
            } catch(e: Exception){
                continue
            }
        }
        session.createSession(userId, DockerBridge(host, nextAvailPort, containerId))
    }

    fun readDockerOutput(userId: String): String = session.getSession(userId).receiveMsg(1024)

    fun inputDocker(userId: String, input: String){
        session.getSession(userId).sendMsg(input)
    }

    fun shutdownDocker(containerId: String){
        val deleteContainerReq = client(Request(Method.POST, "http://$host:4243/containers/$containerId/kill"))
        if (deleteContainerReq.status.code != 204) throw Exception("Cannot kill container")
    }

    fun deleteDocker(userId: String){
        val containerId = session.getSession(userId).containerId
        shutdownDocker(containerId)
        session.deleteSession(userId)
    }
}