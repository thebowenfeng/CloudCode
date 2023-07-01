package com.example.backend.misc

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class DockerBridge(val host: String, val port: Int, val containerId: String) {
    private val socket = Socket(host, port)
    private val output = socket.getOutputStream()
    private val input = socket.getInputStream()

    init {
        socket.soTimeout = 3000
    }

    fun sendMsg(msg: String){
        if (msg == "ENTER"){
            output.write("\n".toByteArray())
        }else{
            output.write(msg.toByteArray())
        }
    }

    fun receiveMsg(size: Int): String{
        val resp = ByteArray(size)
        input.read(resp, 0, size)
        resp.filter {
            it.toInt() != 0
        }
        return String(resp)
    }

    fun dispose(){
        input.close()
        output.close()
        socket.close()
    }
}