package com.example.backend.services

import com.example.backend.data.UserSession
import com.example.backend.dto.Language
import org.assertj.core.api.Assertions.*
import org.http4k.client.ApacheClient
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class DockerExecutorServiceTest @Autowired constructor(
        private val dockerService: DockerExecutorService,
        private val session: UserSession
){
    private val client: HttpHandler = ApacheClient()

    @Order(1)
    @Test
    fun `Verify host env variable`(){
        val host: String = System.getenv("CODECLOUD_DOCKER_HOST")

        assertThat(host).isNotEmpty
    }

    @Order(2)
    @Test
    fun `create docker container`(){
        val host: String = System.getenv("CODECLOUD_DOCKER_HOST")
        dockerService.createDocker("test", Language.IPYTHON)
        val containerId = session.getSession("test").containerId
        val getContainerResp = JSONArray(client(Request(Method.GET,"http://$host:4243/containers/json")).body.toString())

        assertThatIterable(getContainerResp).isNotEmpty
        val jsonList = ArrayList<JSONObject>()
        for (i in 0 until getContainerResp.length()){
            jsonList.add(getContainerResp.getJSONObject(i))
        }
        assertThatIterable(jsonList).anyMatch{
            it.get("Id") == containerId
        }
    }

    @Order(3)
    @Test
    fun `read docker output`(){
        val out = dockerService.readDockerOutput("test")
        assertThat(out).isNotEmpty
    }

    @Order(4)
    @Test
    fun `create two containers at once`(){
        val host: String = System.getenv("CODECLOUD_DOCKER_HOST")
        dockerService.createDocker("test2", Language.IPYTHON)
        val containerId = session.getSession("test2").containerId
        val getContainerResp = JSONArray(client(Request(Method.GET,"http://$host:4243/containers/json")).body.toString())

        assertThatIterable(getContainerResp).isNotEmpty
        val jsonList = ArrayList<JSONObject>()
        for (i in 0 until getContainerResp.length()){
            jsonList.add(getContainerResp.getJSONObject(i))
        }
        assertThatIterable(jsonList).anyMatch{
            it.get("Id") == containerId
        }
    }

    @Order(5)
    @Test
    fun `docker input`(){
        dockerService.inputDocker("test", "blah")
        val res = dockerService.readDockerOutput("test")
        println(res)
        assertThat(res).isNotEmpty
    }

    @Order(6)
    @Test
    fun `delete docker`(){
        val host: String = System.getenv("CODECLOUD_DOCKER_HOST")
        dockerService.deleteDocker("test")
        dockerService.deleteDocker("test2")

        Thread.sleep(100) // Sometimes killed containers does not immediately reflect in get all containers

        val getContainerResp = JSONArray(client(Request(Method.GET,"http://$host:4243/containers/json")).body.toString())
        assertThatIterable(getContainerResp).isEmpty()
    }
}