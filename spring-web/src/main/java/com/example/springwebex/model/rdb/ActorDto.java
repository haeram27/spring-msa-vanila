package com.example.springwebex.model.rdb;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ActorDto {
    Integer actorId;
    String firstName;
    String lastName;
    LocalDateTime lastUpdate;
}
