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
/*
    {
    "_id": {
        "$oid": "66d831dddf02490e65ae6f4b"
    },
    "stringField": "Sample String 9000",
    "intField": 30,
    "doubleField": 83.38548462283282,
    "dateField": {
        "$date": "2024-09-04T10:09:33.175Z"
    },
    "booleanField": true,
    "groupField": "x",
    "arrayField": [
        "value9000",
        "value9001",
        "value9002"
    ],
    "embeddedDocumentField": {
        "embeddedInt": "666",
        "embeddedString": "Embedded Sample 9000"
    },
    "objectIdField": {
        "$oid": "66d831dddf02490e65ae6f4b"
    },
    "decimalField": "37.590286644441015",
    "binaryField": {
        "$binary": {
        "base64": "VGhpcyBpcyBhIHNhbXBsZSBiaW5hcnkgZGF0YQ==",
        "subType": "00"
        }
    },
    "createdAt": {
        "$date": "2024-09-04T10:09:33.175Z"
    },
    "_class": "com.example.springwebex.model.mongo.MongoSampleDoc"
    }
 */

    @Id
    private ObjectId id;

    private String stringField;
    private int intField;
    private double doubleField;
    private Date dateField;
    private boolean booleanField;
    private String groupField;
    private List<String> arrayField;
    private Map<String, String> embeddedDocumentField;
    private ObjectId objectIdField;
    private BigDecimal decimalField;
    private Binary binaryField;
    private Date createdAt;

    @Nullable
    private ObjectId nullableField;
}