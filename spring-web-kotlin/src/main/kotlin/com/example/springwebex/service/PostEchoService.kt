package com.example.springwebex.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import com.example.springwebex.model.restreq.BasicReqDto
import com.example.springwebex.model.restresp.ResponseJsonDto

@Service
class PostEchoService {
    private val log = KotlinLogging.logger {}
    fun <T : Any> echo(request: T): ResponseJsonDto<T> {
        val response = ResponseJsonDto<T>()
        response.response = request
        return response
    }
}
