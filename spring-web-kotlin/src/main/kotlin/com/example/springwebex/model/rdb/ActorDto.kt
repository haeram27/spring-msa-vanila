package com.example.springwebex.model.rdb

import java.time.LocalDateTime

data class ActorDto(
    var actorId: Int? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var lastUpdate: LocalDateTime? = null
)
