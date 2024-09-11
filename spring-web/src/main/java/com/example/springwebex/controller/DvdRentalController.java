package com.example.springwebex.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springwebex.model.rdb.ActorDto;
import com.example.springwebex.service.ActorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dvdrental")
@Slf4j
public class DvdRentalController {
    private final ActorService actorService;

    @GetMapping("/actors")
    public List<ActorDto> getActors() {
        var result = actorService.getActors();
        log.info("{}", result);
        return result;
    }

    @GetMapping("/actor/{id}")
    public ActorDto getActorById(@PathVariable("id") Long id) {
        var result = actorService.getActorById(id);
        log.info("getActors{}", result);
        return result;
    }

    @GetMapping("/actor/insert/{firstName}/{lastName}")
    public void setActor(@PathVariable("firstName") String firstName,
            @PathVariable("lastName") String lastName) {
        actorService.setActor(firstName, lastName);
        log.info("insertActor()");
    }
}
