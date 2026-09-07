package com.example.httpclient.service

import com.example.httpclient.SpringApplication
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.springframework.stereotype.Service

@Service
class FileReadService {
    private val log = KotlinLogging.logger {}

    fun readFromProcessWorkingDir() {
        val p = Paths.get(System.getProperty("user.dir"))
        log.info { "process working dir:\n${p}" }
    }

    fun readFromExecDir() {
        try {
            val uri: URI = SpringApplication::class.java.protectionDomain.codeSource.location.toURI()
            val execPath = Paths.get(uri)
            val execDir = if (Files.isRegularFile(execPath)) execPath.parent else execPath
            log.info { "class file dir:\n${execDir}" }
        } catch (e: Exception) {
            log.error(e) { "Error reading file from exec dir" }
        }
    }

    fun readFromJarInnerResource() {
        try {
            SpringApplication::class.java.getResourceAsStream("/application.yml").use { input: InputStream? ->
                if (input == null) {
                    throw IOException("resource not found")
                }
                val text = String(input.readAllBytes(), StandardCharsets.UTF_8)
                log.info { "read from jar inner resource:\n$text" }
            }
        } catch (e: IOException) {
            log.error(e) { "Error reading resource from jar" }
        }
    }

    fun getExecutableFilePath(): Path {
        try {
            val uri = SpringApplication::class.java.protectionDomain.codeSource.location.toURI()
            try {
                val execPath = Paths.get(uri)
                if (Files.isRegularFile(execPath) && execPath.toString().endsWith(".jar")) {
                    return execPath
                }
            } catch (_: Exception) {
            }

            val uriStr = uri.toString()
            val idx = uriStr.indexOf(".jar!")
            if (idx != -1) {
                val jarPart = uriStr.substring(0, idx + 4)
                try {
                    val jarUri = URI(jarPart)
                    val jarPath = Paths.get(jarUri)
                    if (Files.exists(jarPath)) {
                        return jarPath
                    }
                } catch (_: Exception) {
                }
            }
        } catch (e: Exception) {
            log.debug(e) { "codeSource lookup failed" }
        }

        try {
            val cmd = System.getProperty("sun.java.command")
            log.debug { "sun.java.command: $cmd" }
            if (cmd != null) {
                val first = cmd.split(" ").firstOrNull().orEmpty()
                if (first.endsWith(".jar")) {
                    var jarPath = Paths.get(first)
                    if (!jarPath.isAbsolute) {
                        jarPath = Paths.get(System.getProperty("user.dir")).resolve(jarPath).normalize()
                    }
                    if (Files.exists(jarPath)) {
                        return jarPath
                    }
                }
            }
        } catch (e: Exception) {
            log.debug(e) { "sun.java.command lookup failed" }
        }

        log.warn { "Unable to determine executable jar path; falling back to user.dir" }
        return Paths.get(System.getProperty("user.dir"))
    }
}
