package com.example.springwebex.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DvdRentalDao {
    @Select("SELECT * FROM actor WHERE actor_id < #{idLimit}")
    void selectActor(@Param("idLimit") Integer idLimit);
}
