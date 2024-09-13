package com.example.springwebex.util.mongo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.bson.types.ObjectId;

public class MongoObjectIdUtils {

    public static ObjectId getObjectIdFromDate(Instant instant) {
        return new ObjectId(Date.from(instant));
    }

    public static ObjectId getObjectIdFromDate(Date date) {
        return new ObjectId(date);
    }

    public static ObjectId getObjectIdFromDate(LocalDateTime dateTime, ZoneId zone) {
        Date date = Date.from(dateTime.atZone(zone).toInstant());
        return new ObjectId(date);
    }

    public static ObjectId getObjectIdFromDate(OffsetDateTime dateTime) {
        Date date = Date.from(dateTime.toInstant());
        return new ObjectId(date);
    }

    public static ObjectId getObjectIdFromDate(ZonedDateTime dateTime) {
        Date date = Date.from(dateTime.toInstant());
        return new ObjectId(date);
    }
}