package com.example.cephclient.s3.facade;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.example.cephclient.s3.exception.BulkPresignException;
import com.example.cephclient.s3.exception.BulkPresignException.Reason;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.ChecksumAlgorithm;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.AbortMultipartUploadPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.CompleteMultipartUploadPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.CreateMultipartUploadPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.DeleteObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.HeadBucketPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.HeadObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedAbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedCompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedCreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedDeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedHeadBucketRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedHeadObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

/**
 * Facade for S3 presigner operations, providing methods to generate presigned URLs for various S3 actions.
 * <p>
 * This class includes methods for presigning single and bulk operations, with input validation and error handling.
 * <p>
 * Refer to AWS SDK for Java v2 documentation for details on S3 presigner usage:
 * https://docs.aws.amazon.com/java/api/latest/software/amazon/awssdk/services/s3/presigner/S3Presigner.html
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3PresignerFacade {

    private static final int DEFAULT_EXPIRES_IN_SECONDS = 300;
    private static final int BASE_TIMEOUT_SECONDS = 10;
    private static final int MAX_PARALLEL_TIMEOUT_SECONDS = 30;
    private static final int PARALLEL_PRESIGN_THRESHOLD = 256;
    private static final int PARALLELISM = Runtime.getRuntime().availableProcessors();

    private final ExecutorService presignExecutor = Executors.newFixedThreadPool(PARALLELISM);
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    public PresignResult presignPutObject(PutObjectUrlRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        PresignedPutObjectRequest presigned = presignPutObjectRequest(request);

        return toResult(presigned);
    }

    public List<PresignedPutObjectRequest> presignPutObjectBulk(List<PutObjectUrlRequest> requests) {
        Objects.requireNonNull(requests, "requests must not be null");
        if (requests.isEmpty()) {
            throw new IllegalArgumentException("requests must not be empty");
        }
        if (requests.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("requests must not contain null");
        }

        if (requests.size() < PARALLEL_PRESIGN_THRESHOLD) {
            try {
                return requests.stream().map(this::presignPutObjectRequest).toList();
            } catch (BulkPresignException e) {
                throw e;
            } catch (Exception e) {
                log.error("bulk presign failed: count={} cause={}", requests.size(), e.getMessage(), e);
                throw new BulkPresignException(BulkPresignException.Reason.PRESIGN_FAILURE,
                    "Bulk presign failed: " + e.getMessage(), e);
            }
        }

        return executeChunkedParallel(
            requests, this::presignPutObjectRequest, calculateDynamicTimeout(requests.size()));
    }

    private PresignedPutObjectRequest presignPutObjectRequest(PutObjectUrlRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        var putObjectRequestBuilder = PutObjectRequest.builder()
            .bucket(request.bucket())
            .key(request.key());

        if (request.contentType() != null && !request.contentType().isBlank()) {
            putObjectRequestBuilder.contentType(request.contentType());
        }

        if (request.contentLength() != null && request.contentLength() > 0) {
            putObjectRequestBuilder.contentLength(request.contentLength());
        }

        if (request.checksumSha256() != null) {
            putObjectRequestBuilder.checksumSHA256(request.checksumSha256());
        }

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(
            PutObjectPresignRequest.builder()
                .signatureDuration(resolveDuration(request.expiresInSeconds()))
                .putObjectRequest(putObjectRequestBuilder.build())
                .build()
        );

        return presigned;
    }

    public PresignResult presignGetObject(GetObjectUrlRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        var getObjectRequestBuilder = GetObjectRequest.builder()
            .bucket(request.bucket())
            .key(request.key());

        if (request.responseContentType() != null && !request.responseContentType().isBlank()) {
            getObjectRequestBuilder.responseContentType(request.responseContentType());
        }

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(
            GetObjectPresignRequest.builder()
                .signatureDuration(resolveDuration(request.expiresInSeconds()))
                .getObjectRequest(getObjectRequestBuilder.build())
                .build()
        );

        return toResult(presigned);
    }

    public PresignResult presignDeleteObject(DeleteObjectUrlRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        PresignedDeleteObjectRequest presigned = s3Presigner.presignDeleteObject(
            DeleteObjectPresignRequest.builder()
                .signatureDuration(resolveDuration(request.expiresInSeconds()))
                .deleteObjectRequest(
                    DeleteObjectRequest.builder()
                        .bucket(request.bucket())
                        .key(request.key())
                        .build()
                )
                .build()
        );

        return toResult(presigned);
    }

    public PresignResult presignHeadObject(HeadObjectUrlRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        PresignedHeadObjectRequest presigned = s3Presigner.presignHeadObject(
            HeadObjectPresignRequest.builder()
                .signatureDuration(resolveDuration(request.expiresInSeconds()))
                .headObjectRequest(
                    HeadObjectRequest.builder()
                        .bucket(request.bucket())
                        .key(request.key())
                        .build()
                )
                .build()
        );

        return toResult(presigned);
    }

    public PresignResult presignHeadBucket(HeadBucketUrlRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        PresignedHeadBucketRequest presigned = s3Presigner.presignHeadBucket(
            HeadBucketPresignRequest.builder()
                .signatureDuration(resolveDuration(request.expiresInSeconds()))
                .headBucketRequest(
                    HeadBucketRequest.builder()
                        .bucket(request.bucket())
                        .build()
                )
                .build()
        );

        return toResult(presigned);
    }

    public PresignResult presignCreateMultipartUpload(CreateMultipartUploadUrlRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        var createMultipartUploadRequestBuilder = CreateMultipartUploadRequest.builder()
            .bucket(request.bucket())
            .key(request.key())
            .contentType(request.contentType());

        if (Boolean.TRUE.equals(request.checksumSha256Enabled())) {
            createMultipartUploadRequestBuilder.checksumAlgorithm(ChecksumAlgorithm.SHA256);
        }

        PresignedCreateMultipartUploadRequest presigned = s3Presigner.presignCreateMultipartUpload(
            CreateMultipartUploadPresignRequest.builder()
                .signatureDuration(resolveDuration(request.expiresInSeconds()))
                .createMultipartUploadRequest(createMultipartUploadRequestBuilder.build())
                .build()
        );

        return toResult(presigned);
    }

    public MultipartUploadAutoPresignResult presignMultipartWithPartResources(MultipartUploadAutoPresignRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        var createMultipartUploadRequestBuilder = CreateMultipartUploadRequest.builder()
            .bucket(request.bucket())
            .key(request.key())
            .contentType(request.contentType());

        if (Boolean.TRUE.equals(request.checksumSha256Enabled())) {
            createMultipartUploadRequestBuilder.checksumAlgorithm(ChecksumAlgorithm.SHA256);
        }

        String uploadId;
        try {
            uploadId = s3Client.createMultipartUpload(createMultipartUploadRequestBuilder.build()).uploadId();
        } catch (Exception e) {
            log.error("multipart auto presign failed to create uploadId: bucket={} key={}",
                request.bucket(), request.key(), e);
            throw new BulkPresignException(Reason.PRESIGN_FAILURE,
                "Failed to create multipart uploadId: " + e.getMessage(), e);
        }

        PresignResult startUrl;
        try {
            startUrl = presignCreateMultipartUpload(
                new CreateMultipartUploadUrlRequest(
                    request.bucket(),
                    request.key(),
                    request.contentType(),
                    request.startExpiresInSeconds(),
                    request.checksumSha256Enabled()
                )
            );
        } catch (Exception e) {
            log.error("multipart auto presign failed to generate startUrl: bucket={} key={} uploadId={}",
                request.bucket(), request.key(), uploadId, e);
            throw new BulkPresignException(Reason.PRESIGN_FAILURE,
                "Failed to generate multipart startUrl: " + e.getMessage(), e);
        }

        // presignUploadPart is local SigV4 signing, no network call needed
        // use batch-based parallel processing for large part counts (>= threshold)
        List<MultipartPartPresignResult> partUrls;
        if (request.parts().size() >= PARALLEL_PRESIGN_THRESHOLD) {
            partUrls = presignMultipartPartsBatched(request, uploadId);
        } else {
            partUrls = presignMultipartPartsSequential(request, uploadId);
        }

        return new MultipartUploadAutoPresignResult(
            request.bucket(),
            request.key(),
            uploadId,
            startUrl,
            partUrls
        );
    }

    private List<MultipartPartPresignResult> presignMultipartPartsSequential(
            MultipartUploadAutoPresignRequest request, String uploadId) {
        return request.parts().stream()
            .map(part -> {
                try {
                    return new MultipartPartPresignResult(
                        part.partNumber(),
                        presignUploadPart(
                            new UploadPartUrlRequest(
                                request.bucket(),
                                request.key(),
                                uploadId,
                                part.partNumber(),
                                request.partExpiresInSeconds(),
                                part.checksumSha256()
                            )
                        )
                    );
                } catch (Exception e) {
                    log.error("multipart auto presign part-url failed: partNumber={} bucket={} key={} cause={}",
                        part.partNumber(), request.bucket(), request.key(), e.getMessage(), e);
                    throw new BulkPresignException(Reason.PRESIGN_FAILURE,
                        "Multipart auto presign part URL generation failed: " + e.getMessage(), e);
                }
            })
            .toList();
    }

    private List<MultipartPartPresignResult> presignMultipartPartsBatched(
            MultipartUploadAutoPresignRequest request, String uploadId) {
        return executeChunkedParallel(
            request.parts(),
            part -> {
                try {
                    return new MultipartPartPresignResult(
                        part.partNumber(),
                        presignUploadPart(new UploadPartUrlRequest(
                            request.bucket(),
                            request.key(),
                            uploadId,
                            part.partNumber(),
                            request.partExpiresInSeconds(),
                            part.checksumSha256()
                        ))
                    );
                } catch (Exception e) {
                    log.error("multipart auto presign part-url failed: partNumber={} bucket={} key={} cause={}",
                        part.partNumber(), request.bucket(), request.key(), e.getMessage(), e);
                    throw new BulkPresignException(Reason.PRESIGN_FAILURE,
                        "Multipart auto presign part URL generation failed: " + e.getMessage(), e);
                }
            },
            calculateDynamicTimeout(request.parts().size())
        );
    }

    private int calculateDynamicTimeout(int totalPartCount) {
        // Base: BASE_TIMEOUT_SECONDS + 1 second per PARALLEL_PRESIGN_THRESHOLD items
        // Cap at MAX_PARALLEL_TIMEOUT_SECONDS (API response time upper bound)
        return Math.min(MAX_PARALLEL_TIMEOUT_SECONDS,
            BASE_TIMEOUT_SECONDS + (totalPartCount / PARALLEL_PRESIGN_THRESHOLD));
    }

    private <T, R> List<R> executeChunkedParallel(List<T> items, Function<T, R> task, int timeoutSeconds) {
        List<List<T>> chunks = partitionEvenly(items, PARALLELISM);

        List<CompletableFuture<List<R>>> futures = chunks.stream()
            .map(chunk -> CompletableFuture.supplyAsync(
                () -> chunk.stream().map(task).toList(),
                presignExecutor
            ))
            .toList();

        try {
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            futures.forEach(f -> f.cancel(true));
            if (cause instanceof TimeoutException) {
                log.warn("chunked parallel presign timed out after {}s: itemCount={}", timeoutSeconds, items.size());
                throw new BulkPresignException(BulkPresignException.Reason.TIMEOUT,
                    "Chunked parallel presign timed out after " + timeoutSeconds + "s", cause);
            }
            log.error("chunked parallel presign failed: itemCount={} cause={}", items.size(), cause.getMessage(), cause);
            throw new BulkPresignException(BulkPresignException.Reason.PRESIGN_FAILURE,
                "Chunked parallel presign failed: " + cause.getMessage(), cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            futures.forEach(f -> f.cancel(true));
            throw new BulkPresignException(BulkPresignException.Reason.PRESIGN_FAILURE,
                "Chunked parallel presign interrupted", e);
        }

        List<R> result = new ArrayList<>(items.size());
        for (var future : futures) {
            result.addAll(future.join());
        }
        return result;
    }

    private <T> List<List<T>> partitionEvenly(List<T> list, int partitions) {
        int size = list.size();
        // Ceiling division: ensures all items are covered without remainder loss
        int chunkSize = (size + partitions - 1) / partitions;
        List<List<T>> chunks = new ArrayList<>(partitions);
        for (int i = 0; i < size; i += chunkSize) {
            chunks.add(list.subList(i, Math.min(i + chunkSize, size)));
        }
        return chunks;
    }

    @PreDestroy
    void shutdownExecutor() {
        presignExecutor.shutdown();
    }

    public List<PresignResult> presignUploadPartBulk(PresignUploadPartBulkRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        if (request.parts().size() >= PARALLEL_PRESIGN_THRESHOLD) {
            return presignUploadPartBulkBatched(request);
        }

        return presignUploadPartBulkSequential(request);
    }

    private List<PresignResult> presignUploadPartBulkSequential(PresignUploadPartBulkRequest request) {
        List<PresignResult> results = new ArrayList<>(request.parts().size());

        for (MultipartPartResource part : request.parts()) {
            try {
                results.add(presignUploadPart(new UploadPartUrlRequest(
                    request.bucket(),
                    request.key(),
                    request.uploadId(),
                    part.partNumber(),
                    request.partExpiresInSeconds(),
                    part.checksumSha256()
                )));
            } catch (Exception e) {
                log.error("multipart upload-part sequential presign failed: partNumber={} partCount={} cause={}",
                    part.partNumber(), request.parts().size(), e.getMessage(), e);
                throw new BulkPresignException(BulkPresignException.Reason.PRESIGN_FAILURE,
                    "Multipart upload-part sequential presign failed: " + e.getMessage(), e);
            }
        }

        return results;
    }

    private List<PresignResult> presignUploadPartBulkBatched(PresignUploadPartBulkRequest request) {
        return executeChunkedParallel(
            request.parts(),
            part -> {
                try {
                    return presignUploadPart(new UploadPartUrlRequest(
                        request.bucket(),
                        request.key(),
                        request.uploadId(),
                        part.partNumber(),
                        request.partExpiresInSeconds(),
                        part.checksumSha256()
                    ));
                } catch (Exception e) {
                    log.error("multipart upload-part presign failed: partNumber={} partCount={} cause={}",
                        part.partNumber(), request.parts().size(), e.getMessage(), e);
                    throw new BulkPresignException(BulkPresignException.Reason.PRESIGN_FAILURE,
                        "Multipart upload-part presign failed: " + e.getMessage(), e);
                }
            },
            calculateDynamicTimeout(request.parts().size())
        );
    }

    public PresignResult presignUploadPart(UploadPartUrlRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        PresignedUploadPartRequest presigned = s3Presigner.presignUploadPart(
            UploadPartPresignRequest.builder()
                .signatureDuration(resolveDuration(request.expiresInSeconds()))
                .uploadPartRequest(builder -> {
                    builder.bucket(request.bucket())
                        .key(request.key())
                        .uploadId(request.uploadId())
                        .partNumber(request.partNumber());
                    if (request.checksumSha256() != null) {
                        builder.checksumSHA256(request.checksumSha256());
                    }
                })
                .build()
        );

        return toResult(presigned);
    }

    public PresignResult presignCompleteMultipartUpload(CompleteMultipartUploadUrlRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        List<CompletedPart> completedParts = request.parts().stream()
            .map(part -> CompletedPart.builder().partNumber(part.partNumber()).eTag(part.eTag()).build())
            .toList();

        PresignedCompleteMultipartUploadRequest presigned = s3Presigner.presignCompleteMultipartUpload(
            CompleteMultipartUploadPresignRequest.builder()
                .signatureDuration(resolveDuration(request.expiresInSeconds()))
                .completeMultipartUploadRequest(builder -> builder
                    .bucket(request.bucket())
                    .key(request.key())
                    .uploadId(request.uploadId())
                    .multipartUpload(CompletedMultipartUpload.builder().parts(completedParts).build())
                )
                .build()
        );

        return toResult(presigned);
    }

    public PresignResult presignAbortMultipartUpload(AbortMultipartUploadUrlRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        PresignedAbortMultipartUploadRequest presigned = s3Presigner.presignAbortMultipartUpload(
            AbortMultipartUploadPresignRequest.builder()
                .signatureDuration(resolveDuration(request.expiresInSeconds()))
                .abortMultipartUploadRequest(builder -> builder
                    .bucket(request.bucket())
                    .key(request.key())
                    .uploadId(request.uploadId())
                )
                .build()
        );

        return toResult(presigned);
    }

    public PresignResult presignRangeGetObject(RangeGetObjectUrlRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String range = "bytes=" + request.rangeStart() + "-" + request.rangeEnd();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(
            GetObjectPresignRequest.builder()
                .signatureDuration(resolveDuration(request.expiresInSeconds()))
                .getObjectRequest(builder -> builder
                    .bucket(request.bucket())
                    .key(request.key())
                    .range(range)
                )
                .build()
        );

        return toResult(presigned);
    }

    private Duration resolveDuration(Integer expiresInSeconds) {
        int ttl = expiresInSeconds == null || expiresInSeconds <= 0
            ? DEFAULT_EXPIRES_IN_SECONDS
            : expiresInSeconds;

        return Duration.ofSeconds(ttl);
    }

    private static void requireNotBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private static void requirePositive(Integer value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
    }

    private static void requirePositive(Long value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
    }

    private static void requireNonNegative(Long value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
        }
    }

    private static void validateExpiresInSeconds(Integer expiresInSeconds) {
        if (expiresInSeconds == null) {
            return;
        }
        if (expiresInSeconds <= 0) {
            throw new IllegalArgumentException("expiresInSeconds must be greater than 0");
        }
        if (expiresInSeconds > 604800) {
            throw new IllegalArgumentException("expiresInSeconds must be less than or equal to 604800");
        }
    }

    private static void validateChecksumSha256(String checksumSha256, String fieldName) {
        if (checksumSha256 == null) {
            return;
        }
        if (checksumSha256.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(checksumSha256);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid Base64 string", e);
        }

        if (decoded.length != 32) {
            throw new IllegalArgumentException(fieldName + " must decode to 32 bytes (SHA-256)");
        }
    }

    private PresignResult toResult(PresignedPutObjectRequest request) {
        return new PresignResult(
            request.url().toString(),
            request.httpRequest().method().name(),
            request.httpRequest().headers(),
            request.expiration()
        );
    }

    private PresignResult toResult(PresignedGetObjectRequest request) {
        return new PresignResult(
            request.url().toString(),
            request.httpRequest().method().name(),
            request.httpRequest().headers(),
            request.expiration()
        );
    }

    private PresignResult toResult(PresignedDeleteObjectRequest request) {
        return new PresignResult(
            request.url().toString(),
            request.httpRequest().method().name(),
            request.httpRequest().headers(),
            request.expiration()
        );
    }

    private PresignResult toResult(PresignedCreateMultipartUploadRequest request) {
        return new PresignResult(
            request.url().toString(),
            request.httpRequest().method().name(),
            request.httpRequest().headers(),
            request.expiration()
        );
    }

    private PresignResult toResult(PresignedUploadPartRequest request) {
        return new PresignResult(
            request.url().toString(),
            request.httpRequest().method().name(),
            request.httpRequest().headers(),
            request.expiration()
        );
    }

    private PresignResult toResult(PresignedCompleteMultipartUploadRequest request) {
        return new PresignResult(
            request.url().toString(),
            request.httpRequest().method().name(),
            request.httpRequest().headers(),
            request.expiration()
        );
    }

    private PresignResult toResult(PresignedAbortMultipartUploadRequest request) {
        return new PresignResult(
            request.url().toString(),
            request.httpRequest().method().name(),
            request.httpRequest().headers(),
            request.expiration()
        );
    }

    private PresignResult toResult(PresignedHeadObjectRequest request) {
        return new PresignResult(
            request.url().toString(),
            request.httpRequest().method().name(),
            request.httpRequest().headers(),
            request.expiration()
        );
    }

    private PresignResult toResult(PresignedHeadBucketRequest request) {
        return new PresignResult(
            request.url().toString(),
            request.httpRequest().method().name(),
            request.httpRequest().headers(),
            request.expiration()
        );
    }

    public record PresignResult(
        String url,
        String method,
        Map<String, List<String>> headers,
        Instant expiresAt
    ) {}

    public record PutObjectUrlRequest(
        String bucket,
        String key,
        String contentType,
        Long contentLength,
        Integer expiresInSeconds,
        String checksumSha256
    ) {
        public PutObjectUrlRequest(String bucket, String key, String contentType, Long contentLength, Integer expiresInSeconds) {
            this(bucket, key, contentType, contentLength, expiresInSeconds, null);
        }

        public PutObjectUrlRequest {
            requireNotBlank(bucket, "bucket");
            requireNotBlank(key, "key");
            if (contentType != null && contentType.isBlank()) {
                throw new IllegalArgumentException("contentType must not be blank");
            }
            if (contentLength != null) {
                requirePositive(contentLength, "contentLength");
            }
            validateChecksumSha256(checksumSha256, "checksumSha256");
            validateExpiresInSeconds(expiresInSeconds);
        }
    }

    public record GetObjectUrlRequest(
        String bucket,
        String key,
        String responseContentType,
        Integer expiresInSeconds
    ) {
        public GetObjectUrlRequest {
            requireNotBlank(bucket, "bucket");
            requireNotBlank(key, "key");
            if (responseContentType != null && responseContentType.isBlank()) {
                throw new IllegalArgumentException("responseContentType must not be blank");
            }
            validateExpiresInSeconds(expiresInSeconds);
        }
    }

    public record DeleteObjectUrlRequest(
        String bucket,
        String key,
        Integer expiresInSeconds
    ) {
        public DeleteObjectUrlRequest {
            requireNotBlank(bucket, "bucket");
            requireNotBlank(key, "key");
            validateExpiresInSeconds(expiresInSeconds);
        }
    }

    public record HeadObjectUrlRequest(
        String bucket,
        String key,
        Integer expiresInSeconds
    ) {
        public HeadObjectUrlRequest {
            requireNotBlank(bucket, "bucket");
            requireNotBlank(key, "key");
            validateExpiresInSeconds(expiresInSeconds);
        }
    }

    public record HeadBucketUrlRequest(
        String bucket,
        Integer expiresInSeconds
    ) {
        public HeadBucketUrlRequest {
            requireNotBlank(bucket, "bucket");
            validateExpiresInSeconds(expiresInSeconds);
        }
    }

    public record CreateMultipartUploadUrlRequest(
        String bucket,
        String key,
        String contentType,
        Integer expiresInSeconds,
        Boolean checksumSha256Enabled
    ) {
        public CreateMultipartUploadUrlRequest(String bucket, String key, String contentType, Integer expiresInSeconds) {
            this(bucket, key, contentType, expiresInSeconds, null);
        }

        public CreateMultipartUploadUrlRequest {
            requireNotBlank(bucket, "bucket");
            requireNotBlank(key, "key");
            if (contentType != null && contentType.isBlank()) {
                throw new IllegalArgumentException("contentType must not be blank");
            }
            validateExpiresInSeconds(expiresInSeconds);
        }
    }

    public record UploadPartUrlRequest(
        String bucket,
        String key,
        String uploadId,
        Integer partNumber,
        Integer expiresInSeconds,
        String checksumSha256
    ) {
        public UploadPartUrlRequest(String bucket, String key, String uploadId, Integer partNumber, Integer expiresInSeconds) {
            this(bucket, key, uploadId, partNumber, expiresInSeconds, null);
        }

        public UploadPartUrlRequest {
            requireNotBlank(bucket, "bucket");
            requireNotBlank(key, "key");
            requireNotBlank(uploadId, "uploadId");
            requirePositive(partNumber, "partNumber");
            validateChecksumSha256(checksumSha256, "checksumSha256");
            validateExpiresInSeconds(expiresInSeconds);
        }
    }

    public record CompleteMultipartUploadUrlRequest(
        String bucket,
        String key,
        String uploadId,
        List<PartEtag> parts,
        Integer expiresInSeconds
    ) {
        public CompleteMultipartUploadUrlRequest {
            requireNotBlank(bucket, "bucket");
            requireNotBlank(key, "key");
            requireNotBlank(uploadId, "uploadId");
            Objects.requireNonNull(parts, "parts must not be null");
            if (parts.isEmpty()) {
                throw new IllegalArgumentException("parts must not be empty");
            }
            if (parts.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("parts must not contain null");
            }
            validateExpiresInSeconds(expiresInSeconds);
        }
    }

    public record PartEtag(
        Integer partNumber,
        String eTag
    ) {
        public PartEtag {
            requirePositive(partNumber, "partNumber");
            requireNotBlank(eTag, "eTag");
        }
    }

    public record AbortMultipartUploadUrlRequest(
        String bucket,
        String key,
        String uploadId,
        Integer expiresInSeconds
    ) {
        public AbortMultipartUploadUrlRequest {
            requireNotBlank(bucket, "bucket");
            requireNotBlank(key, "key");
            requireNotBlank(uploadId, "uploadId");
            validateExpiresInSeconds(expiresInSeconds);
        }
    }

    public record RangeGetObjectUrlRequest(
        String bucket,
        String key,
        Long rangeStart,
        Long rangeEnd,
        Integer expiresInSeconds
    ) {
        public RangeGetObjectUrlRequest {
            requireNotBlank(bucket, "bucket");
            requireNotBlank(key, "key");
            requireNonNegative(rangeStart, "rangeStart");
            requireNonNegative(rangeEnd, "rangeEnd");
            if (rangeEnd < rangeStart) {
                throw new IllegalArgumentException("rangeEnd must be greater than or equal to rangeStart");
            }
            validateExpiresInSeconds(expiresInSeconds);
        }
    }

    public record PresignUploadPartBulkRequest(
        String uploadId,
        String bucket,
        String key,
        Integer partExpiresInSeconds,
        List<MultipartPartResource> parts
    ) {
        public PresignUploadPartBulkRequest {
            requireNotBlank(uploadId, "uploadId");
            requireNotBlank(bucket, "bucket");
            requireNotBlank(key, "key");
            Objects.requireNonNull(parts, "parts must not be null");
            if (parts.isEmpty()) {
                throw new IllegalArgumentException("parts must not be empty");
            }
            if (parts.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("parts must not contain null");
            }
            validateExpiresInSeconds(partExpiresInSeconds);
        }
    }

    public record MultipartUploadAutoPresignRequest(
        String bucket,
        String key,
        List<MultipartPartResource> parts,
        String contentType,
        Integer startExpiresInSeconds,
        Integer partExpiresInSeconds,
        Boolean checksumSha256Enabled
    ) {
        public MultipartUploadAutoPresignRequest {
            requireNotBlank(bucket, "bucket");
            requireNotBlank(key, "key");
            Objects.requireNonNull(parts, "parts must not be null");
            if (parts.isEmpty()) {
                throw new IllegalArgumentException("parts must not be empty");
            }
            if (parts.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("parts must not contain null");
            }

            if (contentType != null && contentType.isBlank()) {
                throw new IllegalArgumentException("contentType must not be blank");
            }

            if (Boolean.TRUE.equals(checksumSha256Enabled)) {
                boolean hasMissingChecksum = parts.stream().anyMatch(part -> part.checksumSha256() == null);
                if (hasMissingChecksum) {
                    throw new IllegalArgumentException("checksumSha256 is required for all parts when checksumSha256Enabled is true");
                }
            }

            validateExpiresInSeconds(startExpiresInSeconds);
            validateExpiresInSeconds(partExpiresInSeconds);
        }
    }

    public record MultipartPartResource(
        Integer partNumber,
        String checksumSha256
    ) {
        public MultipartPartResource {
            requirePositive(partNumber, "partNumber");
            validateChecksumSha256(checksumSha256, "checksumSha256");
        }
    }

    public record MultipartUploadAutoPresignResult(
        String bucket,
        String key,
        String uploadId,
        PresignResult startUrl,
        List<MultipartPartPresignResult> partUrls
    ) {
        public MultipartUploadAutoPresignResult {
            requireNotBlank(bucket, "bucket");
            requireNotBlank(key, "key");
            requireNotBlank(uploadId, "uploadId");
            Objects.requireNonNull(startUrl, "startUrl must not be null");
            Objects.requireNonNull(partUrls, "partUrls must not be null");
        }
    }

    public record MultipartPartPresignResult(
        Integer partNumber,
        PresignResult partUrl
    ) {
        public MultipartPartPresignResult {
            requirePositive(partNumber, "partNumber");
            Objects.requireNonNull(partUrl, "partUrl must not be null");
        }
    }
}
