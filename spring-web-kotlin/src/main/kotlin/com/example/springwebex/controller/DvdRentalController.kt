package com.example.springwebex.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.example.springwebex.model.rdb.ActorDto
import com.example.springwebex.service.ActorService

@RestController
@RequestMapping("/dvdrental")
class DvdRentalController(private val actorService: ActorService) {

    private val log = KotlinLogging.logger {}

    @GetMapping("/actors")
    fun getActors(): List<ActorDto> {
        val result = actorService.getActors()
        log.info { "$result" }
        return result
    }

    @GetMapping("/actor/{id}")
    fun getActorById(@PathVariable("id") id: Long): ActorDto {
        val result = actorService.getActorById(id)
        log.info { "getActors$result" }
        return result
    }

    @GetMapping("/actor/insert/{firstName}/{lastName}")
    fun setActor(
        @PathVariable("firstName") firstName: String,
        @PathVariable("lastName") lastName: String
    ) {
        actorService.setActor(firstName, lastName)
        log.info { "insertActor()" }
    }
}
