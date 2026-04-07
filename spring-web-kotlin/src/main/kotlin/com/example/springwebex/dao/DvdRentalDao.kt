package com.example.springwebex.dao

import com.example.springwebex.model.rdb.ActorDto
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select

@Mapper
interface DvdRentalDao {

    @Select("""
        SELECT 
            actor_id, 
            first_name, 
            last_name, 
            last_update
        FROM actor
        WHERE actor_id = #{actorId}
    """)
    fun selectActorById(@Param("actorId") actorId: Int): ActorDto?

    @Select("""
        SELECT 
            actor_id, 
            first_name, 
            last_name, 
            last_update
        FROM actor
        LIMIT #{pageSize} 
        OFFSET #{offset}
    """)
    fun selectActorsByPage(
        @Param("pageSize") pageSize: Int,
        @Param("offset") offset: Int
    ): List<ActorDto>

    @Select("SELECT COUNT(*) FROM actor")
    fun countActors(): Int
}
