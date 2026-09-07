package com.example.springwebex.mongo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.example.springwebex.model.mongo.MongoSampleDoc;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
//@DataMongoTest
@Slf4j
public class MongoInsertSampleDocTests {
    private final String COLLECTION_NAME = "samples";

    @Autowired
    private MongoTemplate mongoTemplate; // mongoTemplate is overrided by @TestConfiguration

    @TestConfiguration
    static class TestConfig {
        // https://www.baeldung.com/spring-beans-integration-test-override
        // https://howtodoinjava.com/spring-boot2/testing/springboot-test-configuration/

        @Primary
        @Bean("mongoTemplate.stub")
        public MongoTemplate mongoTemplate() throws Exception {
            ConnectionString connectionString = new ConnectionString(
                    "mongodb://localhost:27017/test");

            MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
            return new MongoTemplate(MongoClients.create(mongoClientSettings), "test");
        }
    }

    private MongoSampleDoc generateSampleDoc(int i) {
        MongoSampleDoc doc = new MongoSampleDoc();
        ObjectId id = new ObjectId();
        doc.setId(id);
        doc.setStringField("Sample String " + i);
        doc.setIntField((int) (Math.random() * 100));
        doc.setDoubleField(Math.random() * 100);
        doc.setDateField(new Date());
        doc.setBooleanField(i % 2 == 0);
        doc.setGroupField(doc.getIntField() % 2 == 0 ? "x" : "y");
        doc.setArrayField(Arrays.asList("value" + i, "value" + (i + 1), "value" + (i + 2)));
        doc.setEmbeddedDocumentField(Map.of("embeddedString", "Embedded Sample " + i, "embeddedInt",
                String.valueOf((int) (Math.random() * 1000))));
        doc.setObjectIdField(id);
        doc.setDecimalField(BigDecimal.valueOf(Math.random() * 100));
        doc.setBinaryField(new Binary("This is a sample binary data".getBytes()));
        doc.setCreatedAt(Date.from(Instant.now()));
        doc.setNullableField(null);
        return doc;
    }

    @BeforeEach
    public void DropCollectionTest() {
        mongoTemplate.dropCollection(COLLECTION_NAME);
    }

    @Test
    public void insertTest() {
        List<MongoSampleDoc> docs = new ArrayList<>();
        System.out.println("Start to insert new docs");
        for (int i = 0; i < 10000; i++) {
            docs.add(generateSampleDoc(i));
            try {
                if (i % 1000 == 0) {
                    System.out.println(i + " docs are created, sleep 500 ms");
                    Thread.sleep(500);
                }
            } catch (Exception e) {

            }
        }

        mongoTemplate.insertAll(docs);
        System.out.println("10,000 documents inserted into the collection.");

        Query query = new Query();
        long totalSizeOfDocs = mongoTemplate.count(query, COLLECTION_NAME);
        System.out.println(totalSizeOfDocs);
    }
}
