package com.example.cephclient.s3.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.cephclient.s3.facade.S3ClientFacade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "S3 Client", description = "API for direct Ceph/S3 bucket and object operations")
public class S3ClientController {

    private final S3ClientFacade s3ClientFacade;

    @PostMapping("/bucket/create")
    @Operation(summary = "Create bucket", description = "Creates a bucket using S3Client.")
    public BucketActionResponse createBucket(
        @Parameter(description = "Bucket name", example = "my-bucket")
        @RequestParam String bucket
    ) {
        requireNotBlank(bucket, "bucket");
        boolean success = s3ClientFacade.createBucket(bucket);
        return new BucketActionResponse(bucket, success);
    }

    @GetMapping("/bucket/names")
    @Operation(summary = "List bucket names", description = "Returns all accessible bucket names.")
    public BucketNamesResponse listBucketNames() {
        return new BucketNamesResponse(s3ClientFacade.listBucketNames());
    }

    @DeleteMapping("/bucket")
    @Operation(summary = "Delete bucket", description = "Deletes an existing bucket.")
    public BucketActionResponse deleteBucket(
        @Parameter(description = "Bucket name", example = "my-bucket")
        @RequestParam String bucket
    ) {
        requireNotBlank(bucket, "bucket");
        boolean success = s3ClientFacade.deleteBucket(bucket);
        return new BucketActionResponse(bucket, success);
    }

    @DeleteMapping("/files/object")
    @Operation(summary = "Delete object", description = "Deletes an object from the specified bucket.")
    public DeleteObjectActionResponse deleteObject(
        @Parameter(description = "Bucket name", example = "my-bucket")
        @RequestParam String bucket,
        @Parameter(description = "Object key", example = "images/2026/05/a.png")
        @RequestParam String key
    ) {
        requireNotBlank(bucket, "bucket");
        requireNotBlank(key, "key");
        boolean success = s3ClientFacade.deleteObject(bucket, key);
        return new DeleteObjectActionResponse(bucket, key, success);
    }

    public record BucketActionResponse(String bucket, boolean success) {
    }

    public record BucketNamesResponse(List<String> bucketNames) {
    }

    public record DeleteObjectActionResponse(String bucket, String key, boolean success) {
    }

    private static void requireNotBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}