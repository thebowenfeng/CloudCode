package com.example.backend.jobs

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DockerMonitor {
    @Scheduled(fixedRate = 1000)
    fun checkCrashes(){
        //println("test")
    }
}