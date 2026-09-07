package com.example.springwebex.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.springwebex.dao.DvdRentalDao;
import com.example.springwebex.model.rdb.ActorDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActorService {
    private final DvdRentalDao dao;

    public List<ActorDto> getActors() {
        return dao.selectActors();
    }

    public ActorDto getActorById(Long id) {
        return dao.selectActorById(id);
    }

    public void setActor(String firstName, String lastName) {
        var actor = new ActorDto();
        actor.setFirstName(firstName);
        actor.setLastName(lastName);
        dao.insertActor(actor);
    }
}
