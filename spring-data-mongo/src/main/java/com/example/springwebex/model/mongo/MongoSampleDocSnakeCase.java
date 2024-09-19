package com.example.springwebex.model.mongo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.mongodb.lang.Nullable;

import lombok.Data;

@Data
@Document(collection = "samples")
public class MongoSampleDocSnakeCase {

    @Id
    private ObjectId id;

    @Field("string_field")
    private String stringField;

    @Field("int_field")
    private int intField;

    @Field("double_field")
    private double doubleField;

    @Field("date_field")
    private Date dateField;

    @Field("boolean_field")
    private boolean booleanField;

    @Field("array_field")
    private List<String> arrayField;

    @Field("embedded_document_field")
    private Map<String, String> embeddedDocumentField;

    @Field("object_field")
    private ObjectId objectIdField;

    @Field("decimal_field")
    private BigDecimal decimalField;

    @Field("binary_field")
    private Binary binaryField;

    @Field("created_at")
    private Date createdAt;

    @Nullable
    @Field("nullable_field")
    private ObjectId nullableField;
}