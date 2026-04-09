package com.example.httpclient.util

import java.nio.file.Path
import java.nio.file.Paths

object PathUtil {
    fun processWorkingDirectory(): Path = Paths.get(System.getProperty("user.dir"))
}
