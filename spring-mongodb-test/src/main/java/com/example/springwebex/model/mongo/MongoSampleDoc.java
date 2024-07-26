package com.example.springwebex.model.mongo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.Nullable;

import lombok.Data;

@Data
@Document(collection = "samples")
public class MongoSampleDoc {

    @Id
    private ObjectId id;

    private String stringField;
    private int intField;
    private double doubleField;
    private Date dateField;
    private boolean booleanField;
    private List<String> arrayField;
    private Map<String, String> embeddedDocumentField;
    private ObjectId objectIdField;
    private BigDecimal decimalField;
    private Binary binaryField;
    private LocalDateTime createdAt;

    @Nullable
    private ObjectId nullableField;
}