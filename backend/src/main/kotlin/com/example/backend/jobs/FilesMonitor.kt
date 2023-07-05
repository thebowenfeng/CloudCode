package com.example.backend.jobs

import com.example.backend.data.Files
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class FilesMonitor(@Autowired private val files: Files) {
    private val MAX_FILE_TIME = 60000
    @Scheduled(fixedRate = 1000)
    fun pruneStaleFiles(){
        val filesToDelete = files.fileMap.filter {
            System.currentTimeMillis() - it.value.first > MAX_FILE_TIME
        }

        filesToDelete.forEach{
            files.fileMap.remove(it.key)
        }
    }
}