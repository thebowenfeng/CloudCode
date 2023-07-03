package com.example.backend.misc

import java.net.InetSocketAddress
import java.net.Socket

class DockerBridge(host: String, val port: Int, val containerId: String) {
    private val socket = Socket(host, port)
    private val output = socket.getOutputStream()
    private val input = socket.getInputStream()
    val created = System.currentTimeMillis()
    var lastInteracted = System.currentTimeMillis()
    init {
        socket.soTimeout = 3000
    }

    fun sendMsg(msg: String){
        if (msg == "ENTER"){
            output.write("\n".toByteArray())
        }else{
            output.write(msg.toByteArray())
        }
        lastInteracted = System.currentTimeMillis()
    }

    fun receiveMsg(size: Int): String{
        val resp = ByteArray(size)
        input.read(resp, 0, size)
        lastInteracted = System.currentTimeMillis()
        return String(resp.filter {
            it.toInt() != 0
        }.toByteArray())
    }

    fun dispose(){
        input.close()
        output.close()
        socket.close()
    }
}