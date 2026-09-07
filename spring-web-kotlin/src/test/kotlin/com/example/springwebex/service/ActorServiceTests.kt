package com.example.springwebex.service

import com.example.springwebex.model.ActorDto
import com.example.springwebex.model.Client
import com.example.springwebex.model.RequestBody
import com.example.springwebex.model.ResponseBody
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ActorServiceTests {

    @Autowired
    private lateinit var actorService: ActorService

    @Test
    fun testGetActor() {
        val actor = actorService.getActor(1)
        println("Actor: $actor")
    }

    @Test
    fun testSaveActor() {
        val actorDto = ActorDto(0, "John", "Doe", null)
        val result = actorService.saveActor(actorDto)
        println("SaveActor result: $result")
    }

    @Test
    fun testUpdateActor() {
        val actorDto = ActorDto(1, "Jane", "Smith", null)
        val result = actorService.updateActor(actorDto)
        println("UpdateActor result: $result")
    }

    @Test
    fun testDeleteActor() {
        val result = actorService.deleteActor(1)
        println("DeleteActor result: $result")
    }
}
