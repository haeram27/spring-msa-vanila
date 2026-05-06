package com.example.springwebex.h2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Sql(scripts = {"classpath:sql/h2.schema.sql", "classpath:sql/h2.data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class JpaH2Tests {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void selectMemberTableWithJpa() {
        List<Object[]> rows = entityManager.createNativeQuery("SELECT id, name, age FROM member ORDER BY id")
            .getResultList();

        assertEquals(4, rows.size());

        Object[] first = rows.get(0);
        assertEquals("bob", first[1]);
        assertEquals(12, ((Number) first[2]).intValue());
        assertTrue(((Number) first[0]).longValue() > 0L);
    }
}
