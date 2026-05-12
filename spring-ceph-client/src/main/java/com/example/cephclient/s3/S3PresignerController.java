package com.example.cephclient.s3;

import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class S3PresignerController {

    private final S3PresignerFacade s3PresignerFacade;

    @PostMapping("/files/presign-put")
    public S3PresignerFacade.PresignResult presignPutObject(
        @RequestBody S3PresignerFacade.PutObjectUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignPutObject(request);
    }

    @GetMapping("/files/presign-get")
    public S3PresignerFacade.PresignResult presignGetObject(
        @RequestParam String bucket,
        @RequestParam String key,
        @RequestParam(required = false) String responseContentType,
        @RequestParam(required = false) Integer expiresInSeconds
    ) {
        requireNotBlank(bucket, "bucket");
        requireNotBlank(key, "key");
        if (responseContentType != null && responseContentType.isBlank()) {
            throw new IllegalArgumentException("responseContentType must not be blank");
        }
        validateExpiresInSeconds(expiresInSeconds);

        return s3PresignerFacade.presignGetObject(
            new S3PresignerFacade.GetObjectUrlRequest(
                bucket,
                key,
                responseContentType,
                expiresInSeconds
            )
        );
    }

    @PostMapping("/files/presign-delete")
    public S3PresignerFacade.PresignResult presignDeleteObject(
        @RequestBody S3PresignerFacade.DeleteObjectUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignDeleteObject(request);
    }

    @GetMapping("/files/presign-head-object")
    public S3PresignerFacade.PresignResult presignHeadObject(
        @RequestParam String bucket,
        @RequestParam String key,
        @RequestParam(required = false) Integer expiresInSeconds
    ) {
        requireNotBlank(bucket, "bucket");
        requireNotBlank(key, "key");
        validateExpiresInSeconds(expiresInSeconds);

        return s3PresignerFacade.presignHeadObject(
            new S3PresignerFacade.HeadObjectUrlRequest(
                bucket,
                key,
                expiresInSeconds
            )
        );
    }

    @GetMapping("/bucket/presign-head")
    public S3PresignerFacade.PresignResult presignHeadBucket(
        @RequestParam String bucket,
        @RequestParam(required = false) Integer expiresInSeconds
    ) {
        requireNotBlank(bucket, "bucket");
        validateExpiresInSeconds(expiresInSeconds);

        return s3PresignerFacade.presignHeadBucket(
            new S3PresignerFacade.HeadBucketUrlRequest(bucket, expiresInSeconds)
        );
    }

    @PostMapping("/multipart/start")
    public S3PresignerFacade.PresignResult presignCreateMultipartUpload(
        @RequestBody S3PresignerFacade.CreateMultipartUploadUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignCreateMultipartUpload(request);
    }

    @PostMapping("/multipart/part-url")
    public S3PresignerFacade.PresignResult presignUploadPart(
        @RequestBody S3PresignerFacade.UploadPartUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignUploadPart(request);
    }

    @PostMapping("/multipart/complete")
    public S3PresignerFacade.PresignResult presignCompleteMultipartUpload(
        @RequestBody S3PresignerFacade.CompleteMultipartUploadUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignCompleteMultipartUpload(request);
    }

    @PostMapping("/multipart/abort-url")
    public S3PresignerFacade.PresignResult presignAbortMultipartUpload(
        @RequestBody S3PresignerFacade.AbortMultipartUploadUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignAbortMultipartUpload(request);
    }

    @GetMapping("/files/presign-get-range")
    public S3PresignerFacade.PresignResult presignRangeGetObject(
        @RequestParam String bucket,
        @RequestParam String key,
        @RequestParam Long start,
        @RequestParam Long end,
        @RequestParam(required = false) Integer expiresInSeconds
    ) {
        requireNotBlank(bucket, "bucket");
        requireNotBlank(key, "key");
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (start < 0) {
            throw new IllegalArgumentException("start must be greater than or equal to 0");
        }
        if (end < 0) {
            throw new IllegalArgumentException("end must be greater than or equal to 0");
        }
        if (end < start) {
            throw new IllegalArgumentException("end must be greater than or equal to start");
        }
        validateExpiresInSeconds(expiresInSeconds);

        return s3PresignerFacade.presignRangeGetObject(
            new S3PresignerFacade.RangeGetObjectUrlRequest(
                bucket,
                key,
                start,
                end,
                expiresInSeconds
            )
        );
    }

    private static void requireNotBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
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
}
