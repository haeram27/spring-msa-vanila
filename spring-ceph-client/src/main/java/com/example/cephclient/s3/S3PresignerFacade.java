package com.example.cephclient.s3;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
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

@Component
@RequiredArgsConstructor
public class S3PresignerFacade {

    private static final int DEFAULT_EXPIRES_IN_SECONDS = 300;

    private final S3Presigner s3Presigner;

    public PresignResult presignPutObject(PutObjectUrlRequest request) {
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

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(
            PutObjectPresignRequest.builder()
                .signatureDuration(resolveDuration(request.expiresInSeconds()))
                .putObjectRequest(putObjectRequestBuilder.build())
                .build()
        );

        return toResult(presigned);
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

        PresignedCreateMultipartUploadRequest presigned = s3Presigner.presignCreateMultipartUpload(
            CreateMultipartUploadPresignRequest.builder()
                .signatureDuration(resolveDuration(request.expiresInSeconds()))
                .createMultipartUploadRequest(
                    CreateMultipartUploadRequest.builder()
                        .bucket(request.bucket())
                        .key(request.key())
                        .contentType(request.contentType())
                        .build()
                )
                .build()
        );

        return toResult(presigned);
    }

    public PresignResult presignUploadPart(UploadPartUrlRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        PresignedUploadPartRequest presigned = s3Presigner.presignUploadPart(
            UploadPartPresignRequest.builder()
                .signatureDuration(resolveDuration(request.expiresInSeconds()))
                .uploadPartRequest(builder -> builder
                    .bucket(request.bucket())
                    .key(request.key())
                    .uploadId(request.uploadId())
                    .partNumber(request.partNumber())
                )
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
        Integer expiresInSeconds
    ) {
        public PutObjectUrlRequest {
            requireNotBlank(bucket, "bucket");
            requireNotBlank(key, "key");
            if (contentType != null && contentType.isBlank()) {
                throw new IllegalArgumentException("contentType must not be blank");
            }
            if (contentLength != null) {
                requirePositive(contentLength, "contentLength");
            }
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
        Integer expiresInSeconds
    ) {
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
        Integer expiresInSeconds
    ) {
        public UploadPartUrlRequest {
            requireNotBlank(bucket, "bucket");
            requireNotBlank(key, "key");
            requireNotBlank(uploadId, "uploadId");
            requirePositive(partNumber, "partNumber");
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
}
