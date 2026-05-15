package com.example.cephclient.s3.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.cephclient.s3.facade.S3PresignerFacade;
import com.example.cephclient.s3.facade.S3PresignerFacade.AbortMultipartUploadUrlRequest;
import com.example.cephclient.s3.facade.S3PresignerFacade.CompleteMultipartUploadUrlRequest;
import com.example.cephclient.s3.facade.S3PresignerFacade.CreateMultipartUploadUrlRequest;
import com.example.cephclient.s3.facade.S3PresignerFacade.DeleteObjectUrlRequest;
import com.example.cephclient.s3.facade.S3PresignerFacade.GetObjectUrlRequest;
import com.example.cephclient.s3.facade.S3PresignerFacade.HeadBucketUrlRequest;
import com.example.cephclient.s3.facade.S3PresignerFacade.HeadObjectUrlRequest;
import com.example.cephclient.s3.facade.S3PresignerFacade.PresignResult;
import com.example.cephclient.s3.facade.S3PresignerFacade.PresignUploadPartBulkRequest;
import com.example.cephclient.s3.facade.S3PresignerFacade.PutObjectUrlRequest;
import com.example.cephclient.s3.facade.S3PresignerFacade.RangeGetObjectUrlRequest;
import com.example.cephclient.s3.facade.S3PresignerFacade.UploadPartUrlRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "S3 Presigner", description = "API for issuing Ceph/S3 presigned URLs")
public class S3PresignerController {

    private final S3PresignerFacade s3PresignerFacade;

    @PostMapping("/files/presign-put")
    @Operation(summary = "Issue PUT presigned URL", description = "Issues a presigned PUT URL for uploading a single object.")
    public S3PresignerFacade.PresignResult presignPutObject(
        @RequestBody S3PresignerFacade.PutObjectUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignPutObject(request);
    }

    @PostMapping("/files/presign-put/bulk")
    @Operation(summary = "Issue bulk PUT presigned URLs", description = "Issues presigned PUT URLs for multiple objects in one request.")
    public List<S3PresignerFacade.PresignResult> presignPutObjectBulk(
        @RequestBody List<S3PresignerFacade.PutObjectUrlRequest> requests
    ) {
        Objects.requireNonNull(requests, "requests must not be null");
        if (requests.isEmpty()) {
            throw new IllegalArgumentException("requests must not be empty");
        }
        if (requests.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("requests must not contain null");
        }

        List<PresignedPutObjectRequest> presignedRequests = s3PresignerFacade.presignPutObjectBulk(requests);

        return presignedRequests.stream()
            .map(request -> new S3PresignerFacade.PresignResult(
                request.url().toString(),
                request.httpRequest().method().name(),
                request.httpRequest().headers(),
                request.expiration()
            ))
            .toList();
    }

    @GetMapping("/files/presign-get")
    @Operation(summary = "Issue GET presigned URL", description = "Issues a presigned GET URL for downloading a single object.")
    public S3PresignerFacade.PresignResult presignGetObject(
        @Parameter(description = "Bucket name", example = "my-bucket")
        @RequestParam String bucket,
        @Parameter(description = "Object key", example = "images/2026/05/a.png")
        @RequestParam String key,
        @Parameter(description = "Response Content-Type (optional)", example = "image/png")
        @RequestParam(required = false) String responseContentType,
        @Parameter(description = "URL expiration time in seconds (max: 604800)", example = "300")
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
    @Operation(summary = "Issue DELETE presigned URL", description = "Issues a presigned DELETE URL for deleting an object.")
    public S3PresignerFacade.PresignResult presignDeleteObject(
        @RequestBody S3PresignerFacade.DeleteObjectUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignDeleteObject(request);
    }

    @GetMapping("/files/presign-head-object")
    @Operation(summary = "Issue HEAD Object presigned URL", description = "Issues a presigned HEAD URL for checking object metadata.")
    public S3PresignerFacade.PresignResult presignHeadObject(
        @Parameter(description = "Bucket name", example = "my-bucket")
        @RequestParam String bucket,
        @Parameter(description = "Object key", example = "images/2026/05/a.png")
        @RequestParam String key,
        @Parameter(description = "URL expiration time in seconds (max: 604800)", example = "300")
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
    @Operation(summary = "Issue HEAD Bucket presigned URL", description = "Issues a presigned HEAD URL to check bucket accessibility.")
    public S3PresignerFacade.PresignResult presignHeadBucket(
        @Parameter(description = "Bucket name", example = "my-bucket")
        @RequestParam String bucket,
        @Parameter(description = "URL expiration time in seconds (max: 604800)", example = "300")
        @RequestParam(required = false) Integer expiresInSeconds
    ) {
        requireNotBlank(bucket, "bucket");
        validateExpiresInSeconds(expiresInSeconds);

        return s3PresignerFacade.presignHeadBucket(
            new S3PresignerFacade.HeadBucketUrlRequest(bucket, expiresInSeconds)
        );
    }

    @PostMapping("/multipart/start")
    @Operation(summary = "Issue Multipart start URL", description = "Issues a presigned URL for starting multipart upload (CreateMultipartUpload).")
    public S3PresignerFacade.PresignResult presignCreateMultipartUpload(
        @RequestBody S3PresignerFacade.CreateMultipartUploadUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignCreateMultipartUpload(request);
    }

    @PostMapping("/multipart/part-urls")
    @Operation(
        summary = "Issue Multipart part URLs",
        description = "Issues presigned UploadPart URLs in bulk using an existing uploadId."
    )
    public List<S3PresignerFacade.PresignResult> presignUploadPartBulk(
        @RequestBody S3PresignerFacade.PresignUploadPartBulkRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignUploadPartBulk(request);
    }

    @PostMapping("/multipart/part-url")
    @Operation(summary = "Issue Multipart part URL", description = "Issues a presigned URL for uploading a multipart part (UploadPart).")
    public S3PresignerFacade.PresignResult presignUploadPart(
        @RequestBody S3PresignerFacade.UploadPartUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignUploadPart(request);
    }

    @PostMapping("/multipart/complete")
    @Operation(summary = "Issue Multipart complete URL", description = "Issues a presigned URL for completing multipart upload (CompleteMultipartUpload).")
    public S3PresignerFacade.PresignResult presignCompleteMultipartUpload(
        @RequestBody S3PresignerFacade.CompleteMultipartUploadUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignCompleteMultipartUpload(request);
    }

    @PostMapping("/multipart/abort-url")
    @Operation(summary = "Issue Multipart abort URL", description = "Issues a presigned URL for aborting multipart upload (AbortMultipartUpload).")
    public S3PresignerFacade.PresignResult presignAbortMultipartUpload(
        @RequestBody S3PresignerFacade.AbortMultipartUploadUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignAbortMultipartUpload(request);
    }

    @GetMapping("/files/presign-get-range")
    @Operation(summary = "Issue Range GET presigned URL", description = "Issues a presigned GET URL for partial download of large objects.")
    public S3PresignerFacade.PresignResult presignRangeGetObject(
        @Parameter(description = "Bucket name", example = "my-bucket")
        @RequestParam String bucket,
        @Parameter(description = "Object key", example = "videos/2026/demo.mp4")
        @RequestParam String key,
        @Parameter(description = "Start byte for download (>= 0)", example = "0")
        @RequestParam Long start,
        @Parameter(description = "End byte for download (>= start)", example = "1048575")
        @RequestParam Long end,
        @Parameter(description = "URL expiration time in seconds (max: 604800)", example = "300")
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
