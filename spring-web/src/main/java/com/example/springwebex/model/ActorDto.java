package com.example.springwebex.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ActorDto {
    Integer actorId;
    String firstName;
    String lastName;
    LocalDateTime lastUpdate;
}
