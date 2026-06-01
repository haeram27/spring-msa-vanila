package template.s3.controller

import template.s3.*
import template.s3.controller.ApiResponse
import template.constants.HttpCustomHeaders
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "S3 스토리지 클라이언트",
    description = "Ceph/S3 버킷 및 오브젝트 관리 API. Presigned URL 방식 없이 서버가 직접 S3 작업을 수행한다."
)
@RestController
@RequestMapping("/api/deployment/s3/client")
class S3ClientController {

    @Operation(
        summary = "[CONSOLE] 버킷 생성",
        description = "S3 버킷을 생성한다.",
        security = [SecurityRequirement(name = "console-auth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = S3BucketCreateRequestDto::class),
                examples = [ExampleObject(value = """
                {
                    "bucket": "my-deployment-bucket"
                }
                """)]
            )]
        )
    )
    @PostMapping("/bucket/create/v1")
    fun createBucket(
        @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
        @Valid @RequestBody request: S3BucketCreateRequestDto,
    ): ResponseEntity<ApiResponse<S3BucketActionResponseDto>> {
        return ResponseEntity.ok(ApiResponse.success(S3BucketActionResponseDto(bucket = request.bucket, success = true)))
    }

    @Operation(
        summary = "[CONSOLE] 버킷 목록 조회",
        description = "접근 가능한 모든 버킷 이름 목록을 조회한다.",
        security = [SecurityRequirement(name = "console-auth")]
    )
    @PostMapping("/bucket/list/v1")
    fun listBuckets(
        @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
    ): ResponseEntity<ApiResponse<S3BucketNamesResponseDto>> {
        val response = S3BucketNamesResponseDto(
            bucketNames = listOf("my-deployment-bucket", "backup-bucket", "log-bucket")
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "[CONSOLE] 버킷 삭제",
        description = "S3 버킷을 삭제한다. 버킷 안에 오브젝트가 남아있으면 삭제에 실패할 수 있다.",
        security = [SecurityRequirement(name = "console-auth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = S3BucketDeleteRequestDto::class),
                examples = [ExampleObject(value = """
                {
                    "bucket": "my-deployment-bucket"
                }
                """)]
            )]
        )
    )
    @PostMapping("/bucket/delete/v1")
    fun deleteBucket(
        @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
        @Valid @RequestBody request: S3BucketDeleteRequestDto,
    ): ResponseEntity<ApiResponse<S3BucketActionResponseDto>> {
        return ResponseEntity.ok(ApiResponse.success(S3BucketActionResponseDto(bucket = request.bucket, success = true)))
    }

    @Operation(
        summary = "[CONSOLE] 오브젝트 삭제",
        description = "S3 버킷에서 특정 오브젝트를 삭제한다.",
        security = [SecurityRequirement(name = "console-auth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = S3ObjectDeleteRequestDto::class),
                examples = [ExampleObject(value = """
                {
                    "bucket": "my-deployment-bucket",
                    "key": "deploy/agent/2026/05/agent-installer.pkg"
                }
                """)]
            )]
        )
    )
    @PostMapping("/files/delete/v1")
    fun deleteObject(
        @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
        @Valid @RequestBody request: S3ObjectDeleteRequestDto,
    ): ResponseEntity<ApiResponse<S3ObjectDeleteResponseDto>> {
        return ResponseEntity.ok(
            ApiResponse.success(S3ObjectDeleteResponseDto(bucket = request.bucket, key = request.key, success = true))
        )
    }

    @Operation(
        summary = "[CONSOLE] Multipart 업로드 파트 목록 조회",
        description = "특정 uploadId로 업로드된 multipart 파트 메타데이터 목록을 조회한다.",
        security = [SecurityRequirement(name = "console-auth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = S3MultipartPartsListRequestDto::class),
                examples = [ExampleObject(value = """
                {
                    "bucket": "my-deployment-bucket",
                    "key": "deploy/agent/2026/05/agent-installer.pkg",
                    "upload_id": "VXBsb2FkSWQxMjM0NTY3OA=="
                }
                """)]
            )]
        )
    )
    @PostMapping("/multipart/parts/list/v1")
    fun listMultipartParts(
        @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
        @Valid @RequestBody request: S3MultipartPartsListRequestDto,
    ): ResponseEntity<ApiResponse<S3MultipartPartsListResponseDto>> {
        val response = S3MultipartPartsListResponseDto(
            bucket = request.bucket,
            key = request.key,
            uploadId = request.uploadId,
            parts = listOf(
                S3MultipartPartInfoResponseDto(
                    partNumber = 1,
                    eTag = "\"9b2cf535f27731c974343645a3985328\"",
                    size = 5242880,
                    lastModified = "2026-05-27T06:30:45Z"
                ),
                S3MultipartPartInfoResponseDto(
                    partNumber = 2,
                    eTag = "\"94f6d7e04a4d452035300f18b984988c\"",
                    size = 5242880,
                    lastModified = "2026-05-27T06:31:11Z"
                )
            )
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}
