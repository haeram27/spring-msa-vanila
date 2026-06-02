package com.example.cephclient.s3.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cephclient.s3.facade.S3PresignerFacade;
import com.example.cephclient.s3.facade.S3PresignerFacade.MultipartUploadAutoPresignRequest;
import com.example.cephclient.s3.facade.S3PresignerFacade.MultipartUploadAutoPresignResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@RestController
@RequestMapping("/api/deployment/s3/presign")
@RequiredArgsConstructor
@Tag(name = "S3 Presigner", description = "API for issuing Ceph/S3 presigned URLs")
public class S3PresignerController {

    private final S3PresignerFacade s3PresignerFacade;

    // ─── File Presign ─────────────────────────────────────────────────────────

    @PostMapping("/files/put/v1")
    @Operation(summary = "Issue PUT presigned URL", description = "Issues a presigned PUT URL for uploading a single object.")
    public S3PresignerFacade.PresignResult presignPutObject(
        @RequestBody S3PresignerFacade.PutObjectUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignPutObject(request);
    }

    @PostMapping("/files/put-bulk/v1")
    @Operation(summary = "Issue bulk PUT presigned URLs", description = "Issues presigned PUT URLs for multiple objects in one request.")
    public List<S3PresignerFacade.PresignResult> presignPutObjectBulk(
        @RequestBody PutObjectBulkRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        List<S3PresignerFacade.PutObjectUrlRequest> requests = request.items();
        Objects.requireNonNull(requests, "items must not be null");
        if (requests.isEmpty()) {
            throw new IllegalArgumentException("requests must not be empty");
        }
        if (requests.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("requests must not contain null");
        }

        List<PresignedPutObjectRequest> presignedRequests = s3PresignerFacade.presignPutObjectBulk(requests);

        return presignedRequests.stream()
            .map(presignedRequest -> new S3PresignerFacade.PresignResult(
                presignedRequest.url().toString(),
                presignedRequest.httpRequest().method().name(),
                presignedRequest.httpRequest().headers(),
                presignedRequest.expiration()
            ))
            .toList();
    }

    @PostMapping("/files/get/v1")
    @Operation(summary = "Issue GET presigned URL", description = "Issues a presigned GET URL for downloading a single object.")
    public S3PresignerFacade.PresignResult presignGetObject(
        @RequestBody S3PresignerFacade.GetObjectUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignGetObject(request);
    }

    @PostMapping("/files/get-range/v1")
    @Operation(summary = "Issue Range GET presigned URL", description = "Issues a presigned GET URL for partial download of large objects.")
    public S3PresignerFacade.PresignResult presignRangeGetObject(
        @RequestBody S3PresignerFacade.RangeGetObjectUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignRangeGetObject(request);
    }

    @PostMapping("/files/delete/v1")
    @Operation(summary = "Issue DELETE presigned URL", description = "Issues a presigned DELETE URL for deleting an object.")
    public S3PresignerFacade.PresignResult presignDeleteObject(
        @RequestBody S3PresignerFacade.DeleteObjectUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignDeleteObject(request);
    }

    @PostMapping("/files/head/v1")
    @Operation(summary = "Issue HEAD Object presigned URL", description = "Issues a presigned HEAD URL for checking object metadata.")
    public S3PresignerFacade.PresignResult presignHeadObject(
        @RequestBody S3PresignerFacade.HeadObjectUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignHeadObject(request);
    }

    @PostMapping("/bucket/head/v1")
    @Operation(summary = "Issue HEAD Bucket presigned URL", description = "Issues a presigned HEAD URL to check bucket accessibility.")
    public S3PresignerFacade.PresignResult presignHeadBucket(
        @RequestBody S3PresignerFacade.HeadBucketUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignHeadBucket(request);
    }

    // ─── Multipart Presign ────────────────────────────────────────────────────

    @PostMapping("/multipart/start/v1")
    @Operation(summary = "Issue Multipart start URL", description = "Issues a presigned URL for starting multipart upload (CreateMultipartUpload).")
    public S3PresignerFacade.PresignResult presignCreateMultipartUpload(
        @RequestBody S3PresignerFacade.CreateMultipartUploadUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignCreateMultipartUpload(request);
    }

    @PostMapping("/multipart/part-urls/v1")
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

    @PostMapping("/multipart/part-url/v1")
    @Operation(summary = "Issue Multipart part URL", description = "Issues a presigned URL for uploading a multipart part (UploadPart).")
    public S3PresignerFacade.PresignResult presignUploadPart(
        @RequestBody S3PresignerFacade.UploadPartUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignUploadPart(request);
    }

    @PostMapping("/multipart/auto-presign/v1")
    @Operation(
        summary = "Issue Multipart start and part URLs",
        description = "Creates uploadId on backend and returns presigned start URL plus presigned part URLs from part resources."
    )
    public MultipartUploadAutoPresignResult presignMultipartWithPartResources(
        @RequestBody MultipartUploadAutoPresignRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignMultipartWithPartResources(request);
    }

    @PostMapping("/multipart/complete/v1")
    @Operation(summary = "Issue Multipart complete URL", description = "Issues a presigned URL for completing multipart upload (CompleteMultipartUpload).")
    public S3PresignerFacade.PresignResult presignCompleteMultipartUpload(
        @RequestBody S3PresignerFacade.CompleteMultipartUploadUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignCompleteMultipartUpload(request);
    }

    @PostMapping("/multipart/abort-url/v1")
    @Operation(summary = "Issue Multipart abort URL", description = "Issues a presigned URL for aborting multipart upload (AbortMultipartUpload).")
    public S3PresignerFacade.PresignResult presignAbortMultipartUpload(
        @RequestBody S3PresignerFacade.AbortMultipartUploadUrlRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return s3PresignerFacade.presignAbortMultipartUpload(request);
    }

    public record PutObjectBulkRequest(List<S3PresignerFacade.PutObjectUrlRequest> items) {
    }
}
