package com.example.httpclient.config

import com.example.httpclient.model.RestServerConfigDto
import com.example.httpclient.util.PathUtil
import tools.jackson.dataformat.yaml.YAMLMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Files
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RestServerConfig(
    private val yamlMapper: YAMLMapper,
) {
    private val log = KotlinLogging.logger {}

    @Bean
    fun restServerInfo(): RestServerConfigDto {
        val path = PathUtil.processWorkingDirectory()
            .resolve("rest-server.yaml")
            .normalize()

        log.info { path.toString() }

        if (!(Files.exists(path) && Files.isRegularFile(path))) {
            log.error { "can NOT find configuration file" }
        }

        return try {
            yamlMapper.readValue(path.toFile(), RestServerConfigDto::class.java)
        } catch (e: Exception) {
            log.error(e) { e.message ?: "failed to read rest-server.yaml" }
            RestServerConfigDto()
        }
    }
}
