package com.example.cephclient.s3;

import java.util.Objects;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;

/**
 * Facade for S3 client operations, providing methods to interact with S3 buckets and objects.
 * <p>
 * This class includes methods for creating, listing, and deleting buckets, as well as deleting objects, with input validation and error handling.
 * <p>
 * Refer to AWS SDK for Java v2 documentation for details on S3 client usage:
 * https://docs.aws.amazon.com/java/api/latest/software/amazon/awssdk/services/s3/S3Client.html
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class S3ClientFacade {

    private final S3Client s3Client;

    public boolean createBucket(String bucketName) {
        requireNotBlank(bucketName, "bucketName");

        try {
            var response = s3Client.createBucket(
                CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build()
            );

            boolean success = response != null
                && response.sdkHttpResponse() != null
                && response.sdkHttpResponse().isSuccessful();

            if (success) {
                log.info("Bucket '{}' created successfully", bucketName);
                return true;
            }

            log.error("Failed to create bucket '{}'", bucketName);
            return false;
        } catch (Exception e) {
            log.error("Exception occurred while creating bucket '{}': {}", bucketName, e.getMessage(), e);
            return false;
        }
    }

    public List<String> listBucketNames() {
        return s3Client.listBuckets(ListBucketsRequest.builder().build())
            .buckets()
            .stream()
            .map(b -> b.name())
            .toList();
    }

    public void printBucketNames() {
        var bucketNames = listBucketNames();
        if (bucketNames.isEmpty()) {
            log.info("no buckets found");
            return;
        }

        bucketNames.forEach(name -> log.info("bucket={}", name));
    }

    public boolean deleteBucket(String bucketName) {
        requireNotBlank(bucketName, "bucketName");

        try {
            var response = s3Client.deleteBucket(
                DeleteBucketRequest.builder()
                    .bucket(bucketName)
                    .build()
            );

            boolean success = response != null
                && response.sdkHttpResponse() != null
                && response.sdkHttpResponse().isSuccessful();

            if (success) {
                log.info("Bucket '{}' deleted successfully", bucketName);
                return true;
            }

            log.error("Failed to delete bucket '{}'", bucketName);
            return false;
        } catch (Exception e) {
            log.error("Exception occurred while deleting bucket '{}': {}", bucketName, e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteObject(String bucketName, String key) {
        requireNotBlank(bucketName, "bucketName");
        requireNotBlank(key, "key");

        try {
            var response = s3Client.deleteObject(
                DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build()
            );

            boolean success = response != null
                && response.sdkHttpResponse() != null
                && response.sdkHttpResponse().isSuccessful();

            if (success) {
                log.info("Object '{}' deleted successfully from bucket '{}'", key, bucketName);
                return true;
            }

            log.error("Failed to delete object '{}' from bucket '{}'", key, bucketName);
            return false;
        } catch (Exception e) {
            log.error("Exception occurred while deleting object '{}' from bucket '{}': {}", key, bucketName, e.getMessage(), e);
            return false;
        }
    }

    private static void requireNotBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
