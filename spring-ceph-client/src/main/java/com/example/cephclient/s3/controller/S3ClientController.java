package com.example.cephclient.s3.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.example.cephclient.s3.facade.S3ClientFacade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/deployment/s3/client")
@RequiredArgsConstructor
@Tag(name = "S3 Client", description = "API for direct Ceph/S3 bucket and object operations")
public class S3ClientController {

    private final S3ClientFacade s3ClientFacade;

    @PostMapping("/bucket/create/v1")
    @Operation(summary = "Create bucket", description = "Creates a bucket using S3Client.")
    public BucketActionResponse createBucket(
        @RequestBody BucketActionRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        boolean success = s3ClientFacade.createBucket(request.bucket());
        return new BucketActionResponse(request.bucket(), success);
    }

    @PostMapping("/bucket/list/v1")
    @Operation(summary = "List bucket names", description = "Returns all accessible bucket names.")
    public BucketNamesResponse listBucketNames() {
        return new BucketNamesResponse(s3ClientFacade.listBucketNames());
    }

    @PostMapping("/bucket/delete/v1")
    @Operation(summary = "Delete bucket", description = "Deletes an existing bucket.")
    public BucketActionResponse deleteBucket(
        @RequestBody BucketActionRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        boolean success = s3ClientFacade.deleteBucket(request.bucket());
        return new BucketActionResponse(request.bucket(), success);
    }

    @PostMapping("/files/delete/v1")
    @Operation(summary = "Delete object", description = "Deletes an object from the specified bucket.")
    public DeleteObjectActionResponse deleteObject(
        @RequestBody DeleteObjectRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        boolean success = s3ClientFacade.deleteObject(request.bucket(), request.key());
        return new DeleteObjectActionResponse(request.bucket(), request.key(), success);
    }

    @PostMapping("/multipart/parts/list/v1")
    @Operation(summary = "List multipart uploaded parts", description = "Returns uploaded part metadata for the given multipart uploadId.")
    public ListPartsResponse listParts(
        @RequestBody ListPartsRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        List<S3ClientFacade.MultipartPartInfo> parts = s3ClientFacade.listParts(
            request.bucket(),
            request.key(),
            request.uploadId()
        );
        return new ListPartsResponse(request.bucket(), request.key(), request.uploadId(), parts);
    }

    public record BucketActionResponse(String bucket, boolean success) {
    }

    public record BucketNamesResponse(List<String> bucketNames) {
    }

    public record DeleteObjectActionResponse(String bucket, String key, boolean success) {
    }

    public record BucketActionRequest(String bucket) {
        public BucketActionRequest {
            requireNotBlank(bucket, "bucket");
        }
    }

    public record DeleteObjectRequest(String bucket, String key) {
        public DeleteObjectRequest {
            requireNotBlank(bucket, "bucket");
            requireNotBlank(key, "key");
        }
    }

    public record ListPartsRequest(String bucket, String key, @JsonAlias("upload_id") String uploadId) {
        public ListPartsRequest {
            requireNotBlank(bucket, "bucket");
            requireNotBlank(key, "key");
            requireNotBlank(uploadId, "uploadId");
        }
    }

    public record ListPartsResponse(
        String bucket,
        String key,
        String uploadId,
        List<S3ClientFacade.MultipartPartInfo> parts
    ) {
    }

    private static void requireNotBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}