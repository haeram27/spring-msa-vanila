package com.example.springwebex.util.mongo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.bson.types.ObjectId;

public class ObjectIdUtils {

    // LocalDateTime of System Default Timezone
    public static ObjectId getObjectIdFromSystemDateTime(LocalDateTime dateTime) {
        Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        return new ObjectId(date);
    }

    // LocalDateTime of UTC
    public static ObjectId getObjectIdFromUTCDateTime(LocalDateTime dateTime) {
        Date date = Date.from(dateTime.atZone(ZoneId.of("UTC")).toInstant());
        return new ObjectId(date);
    }
}