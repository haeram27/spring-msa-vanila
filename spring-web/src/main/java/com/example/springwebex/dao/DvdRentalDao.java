package com.example.springwebex.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.example.springwebex.model.rdb.ActorDto;

@Mapper
public interface DvdRentalDao {

    List<ActorDto> selectActors();

    @Select("SELECT * FROM actor WHERE actor_id = #{id}")
    ActorDto selectActorById(@Param("id") Long id);

    void insertActor(ActorDto actorDto);
}
