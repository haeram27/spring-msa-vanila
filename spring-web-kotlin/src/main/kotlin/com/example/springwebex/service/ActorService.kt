package com.example.springwebex.service

import org.springframework.stereotype.Service
import com.example.springwebex.dao.DvdRentalDao
import com.example.springwebex.model.rdb.ActorDto

@Service
class ActorService(private val dao: DvdRentalDao) {

    fun getActors(): List<ActorDto> {
        return dao.selectActorsByPage(pageSize = 100, offset = 0)
    }

    fun getActorById(id: Long): ActorDto {
        return dao.selectActorById(id.toInt()) ?: ActorDto()
    }

    fun setActor(firstName: String, lastName: String) {
        val actor = ActorDto(firstName = firstName, lastName = lastName)
    }
}
