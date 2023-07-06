package com.example.backend.misc

import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

class DockerBridge(private val host: String, val port: Int, val containerId: String) {
    private lateinit var socket: Socket
    private lateinit var output: OutputStream
    private lateinit var input: InputStream
    val created = System.currentTimeMillis()
    var lastInteracted = System.currentTimeMillis()
    fun connect(){
        socket = Socket(host, port)
        socket.soTimeout = 1000
        output = socket.getOutputStream()
        input = socket.getInputStream()
    }

    fun sendMsg(msg: String){
        if (!::socket.isInitialized) throw Exception("Socket is not connected")

        if (msg == "ENTER"){
            output.write("\n".toByteArray())
        }else{
            output.write(msg.toByteArray())
        }
        lastInteracted = System.currentTimeMillis()
    }

    fun receiveMsg(size: Int): String{
        if (!::socket.isInitialized) throw Exception("Socket is not connected")

        val resp = ByteArray(size)
        input.read(resp, 0, size)
        lastInteracted = System.currentTimeMillis()
        return String(resp.filter {
            it.toInt() != 0
        }.toByteArray())
    }

    fun dispose(){
        if (::socket.isInitialized){
            input.close()
            output.close()
            socket.close()
        }
    }
}