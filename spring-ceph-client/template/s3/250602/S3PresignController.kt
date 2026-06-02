package com.ahnlab.one.bff.console.server.api.deployment.s3

import com.ahnlab.one.bff.console.api.rest.v1.deployment.s3.*
import com.ahnlab.one.common.api.rest.ApiResponse
import com.ahnlab.one.common.constants.HttpCustomHeaders
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
    name = "S3 Presigned URL 발급",
    description = """
        Ceph/S3 오브젝트 스토리지에 대한 Presigned URL 발급 API.
        FE는 발급된 URL을 이용해 서버를 거치지 않고 S3에 직접 업로드/다운로드/삭제 요청을 보낸다.
        Swagger UI만으로 업로드 플로우(단일 파일, 멀티파트)를 구현할 수 있도록 문서화되어 있다.
    """
)
@RestController
@RequestMapping("/api/deployment/s3/presign")
class S3PresignController {

    // ─── File Presign ─────────────────────────────────────────────────────────

    @Operation(
        summary = "[CONSOLE] PUT Presigned URL 발급",
        description = "단일 오브젝트 업로드를 위한 Presigned PUT URL을 발급한다. FE는 발급된 URL로 직접 S3에 PUT 요청을 보낸다.",
        security = [SecurityRequirement(name = "console-auth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = S3PresignPutRequestDto::class),
                examples = [
                    ExampleObject(
                        value = """
                        {
                            "fileName": "agent-installer.pkg",
                            "fileSize": 10485760,
                            "fileType": "AGENT_INSTALLER"
                        }
                        """
                    )
                ]
            )]
        )
    )
    @PostMapping("/files/put/v1")
    fun presignPut(
        @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
        @Valid @RequestBody request: S3PresignPutRequestDto,
    ): ResponseEntity<ApiResponse<S3PresignResultDto>> {
        val response = S3PresignResultDto(
            url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/agent-installer.pkg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ACCESS_KEY%2F20260526%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260526T120000Z&X-Amz-Expires=300&X-Amz-SignedHeaders=host&X-Amz-Signature=abc123signature",
            method = "PUT",
            headers = mapOf("host" to listOf("ceph.example.com")),
            expiresAt = "2026-05-26T12:05:00Z"
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "[CONSOLE] PUT Presigned URL 일괄 발급",
        description = "여러 오브젝트 업로드를 위한 Presigned PUT URL을 한 번에 발급한다. 응답 순서는 요청 items 순서와 동일하다.",
        security = [SecurityRequirement(name = "console-auth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = S3PresignPutBulkRequestDto::class),
                examples = [
                    ExampleObject(
                        value = """
                        {
                            "items": [
                                {
                                    "fileName": "agent-installer1.pkg",
                                    "fileSize": 10485760,
                                    "fileType": "AGENT_INSTALLER"
                                },
                                {
                                    "fileName": "agent-installer2.pkg",
                                    "fileSize": 10485760,
                                    "fileType": "AGENT_INSTALLER"
                                }
                            ]
                        }
                        """
                    )
                ]
            )]
        )
    )
    @PostMapping("/files/put-bulk/v1")
    fun presignPutBulk(
        @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
        @Valid @RequestBody request: S3PresignPutBulkRequestDto,
    ): ResponseEntity<ApiResponse<List<S3PresignResultDto>>> {
        val mockItem1 = S3PresignResultDto(
            url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/file1.pkg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=300&X-Amz-Signature=abc111",
            method = "PUT",
            headers = mapOf("host" to listOf("ceph.example.com")),
            expiresAt = "2026-05-26T12:05:00Z"
        )
        val mockItem2 = S3PresignResultDto(
            url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/file2.pkg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=300&X-Amz-Signature=abc222",
            method = "PUT",
            headers = mapOf("host" to listOf("ceph.example.com")),
            expiresAt = "2026-05-26T12:05:00Z"
        )
        return ResponseEntity.ok(ApiResponse.success(listOf(mockItem1, mockItem2)))
    }

    @Operation(
        summary = "[CONSOLE] GET(Download) Presigned URL 발급",
        description = "단일 오브젝트 다운로드를 위한 Presigned GET URL을 발급한다. FE는 발급된 URL로 직접 S3에 GET 요청을 보낸다.",
        security = [SecurityRequirement(name = "console-auth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = S3PresignGetRequestDto::class),
                examples = [ExampleObject(value = """
                {
                    "fileType": "AGENT_INSTALLER",
                }
                """)]
            )]
        )
    )
    @PostMapping("/files/get/v1")
    fun presignGet(
        @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
        @Valid @RequestBody request: S3PresignGetRequestDto,
    ): ResponseEntity<ApiResponse<S3PresignResultDto>> {
        val response = S3PresignResultDto(
            url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/agent-installer.pkg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ACCESS_KEY%2F20260526%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260526T120000Z&X-Amz-Expires=300&X-Amz-SignedHeaders=host&X-Amz-Signature=abc123signature",
            method = "GET",
            headers = mapOf("host" to listOf("ceph.example.com")),
            expiresAt = "2026-05-26T12:05:00Z"
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    // @Operation(
    //     summary = "[CONSOLE] Range GET(Download) Presigned URL 발급",
    //     description = """
    //         대용량 파일의 특정 바이트 범위만 다운로드하기 위한 Presigned URL을 발급한다.
    //         FE는 발급된 URL로 직접 S3에 GET 요청을 보낼 때 Range 헤더(bytes=rangeStart-rangeEnd)를 포함해야 한다.
    //     """,
    //     security = [SecurityRequirement(name = "console-auth")],
    //     requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
    //         required = true,
    //         content = [Content(
    //             mediaType = MediaType.APPLICATION_JSON_VALUE,
    //             schema = Schema(implementation = S3PresignGetRangeRequestDto::class),
    //             examples = [
    //                 ExampleObject(
    //                     name = "첫 1MiB",
    //                     value = """
    //                     {
    //                         "bucket": "my-bucket",
    //                         "key": "deploy/agent/2026/05/large-file.bin",
    //                         "range_start": 0,
    //                         "range_end": 1048575,
    //                         "expires_in_seconds": 300
    //                     }
    //                     """
    //                 ),
    //                 ExampleObject(
    //                     name = "다음 1MiB (청크 분할 다운로드)",
    //                     value = """
    //                     {
    //                         "bucket": "my-bucket",
    //                         "key": "deploy/agent/2026/05/large-file.bin",
    //                         "range_start": 1048576,
    //                         "range_end": 2097151,
    //                         "expires_in_seconds": 300
    //                     }
    //                     """
    //                 )
    //             ]
    //         )]
    //     )
    // )
    // @PostMapping("/files/get-range/v1")
    // fun presignGetRange(
    //     @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
    //     @Valid @RequestBody request: S3PresignGetRangeRequestDto,
    // ): ResponseEntity<ApiResponse<S3PresignResultDto>> {
    //     val response = S3PresignResultDto(
    //         url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/large-file.bin?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ACCESS_KEY%2F20260526%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260526T120000Z&X-Amz-Expires=300&X-Amz-SignedHeaders=host&X-Amz-Signature=abc123signature",
    //         method = "GET",
    //         headers = mapOf("host" to listOf("ceph.example.com"), "range" to listOf("bytes=0-1048575")),
    //         expiresAt = "2026-05-26T12:05:00Z"
    //     )
    //     return ResponseEntity.ok(ApiResponse.success(response))
    // }

    // @Operation(
    //     summary = "[CONSOLE] DELETE Presigned URL 발급",
    //     description = "오브젝트 삭제를 위한 Presigned DELETE URL을 발급한다. FE는 발급된 URL로 직접 S3에 DELETE 요청을 보낸다.",
    //     security = [SecurityRequirement(name = "console-auth")],
    //     requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
    //         required = true,
    //         content = [Content(
    //             mediaType = MediaType.APPLICATION_JSON_VALUE,
    //             schema = Schema(implementation = S3PresignDeleteRequestDto::class),
    //             examples = [ExampleObject(value = """
    //             {
    //                 "bucket": "my-bucket",
    //                 "key": "deploy/agent/2026/05/agent-installer.pkg",
    //                 "expires_in_seconds": 300
    //             }
    //             """)]
    //         )]
    //     )
    // )
    // @PostMapping("/files/delete/v1")
    // fun presignDelete(
    //     @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
    //     @Valid @RequestBody request: S3PresignDeleteRequestDto,
    // ): ResponseEntity<ApiResponse<S3PresignResultDto>> {
    //     val response = S3PresignResultDto(
    //         url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/agent-installer.pkg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ACCESS_KEY%2F20260526%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260526T120000Z&X-Amz-Expires=300&X-Amz-SignedHeaders=host&X-Amz-Signature=abc123signature",
    //         method = "DELETE",
    //         headers = mapOf("host" to listOf("ceph.example.com")),
    //         expiresAt = "2026-05-26T12:05:00Z"
    //     )
    //     return ResponseEntity.ok(ApiResponse.success(response))
    // }

    // @Operation(
    //     summary = "[CONSOLE] HEAD Object Presigned URL 발급",
    //     description = "오브젝트 메타데이터(크기, Content-Type, ETag 등) 확인을 위한 Presigned HEAD URL을 발급한다.",
    //     security = [SecurityRequirement(name = "console-auth")],
    //     requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
    //         required = true,
    //         content = [Content(
    //             mediaType = MediaType.APPLICATION_JSON_VALUE,
    //             schema = Schema(implementation = S3PresignHeadObjectRequestDto::class),
    //             examples = [ExampleObject(value = """
    //             {
    //                 "bucket": "my-bucket",
    //                 "key": "deploy/agent/2026/05/agent-installer.pkg",
    //                 "expires_in_seconds": 300
    //             }
    //             """)]
    //         )]
    //     )
    // )
    // @PostMapping("/files/head/v1")
    // fun presignHeadObject(
    //     @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
    //     @Valid @RequestBody request: S3PresignHeadObjectRequestDto,
    // ): ResponseEntity<ApiResponse<S3PresignResultDto>> {
    //     val response = S3PresignResultDto(
    //         url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/agent-installer.pkg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ACCESS_KEY%2F20260526%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260526T120000Z&X-Amz-Expires=300&X-Amz-SignedHeaders=host&X-Amz-Signature=abc123signature",
    //         method = "HEAD",
    //         headers = mapOf("host" to listOf("ceph.example.com")),
    //         expiresAt = "2026-05-26T12:05:00Z"
    //     )
    //     return ResponseEntity.ok(ApiResponse.success(response))
    // }

    // ─── Bucket Presign ───────────────────────────────────────────────────────

    // @Operation(
    //     summary = "[CONSOLE] HEAD Bucket Presigned URL 발급",
    //     description = "버킷의 접근 가능 여부를 확인하기 위한 Presigned HEAD URL을 발급한다.",
    //     security = [SecurityRequirement(name = "console-auth")],
    //     requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
    //         required = true,
    //         content = [Content(
    //             mediaType = MediaType.APPLICATION_JSON_VALUE,
    //             schema = Schema(implementation = S3PresignHeadBucketRequestDto::class),
    //             examples = [ExampleObject(value = """
    //             {
    //                 "bucket": "my-bucket",
    //                 "expires_in_seconds": 300
    //             }
    //             """)]
    //         )]
    //     )
    // )
    // @PostMapping("/bucket/head/v1")
    // fun presignHeadBucket(
    //     @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
    //     @Valid @RequestBody request: S3PresignHeadBucketRequestDto,
    // ): ResponseEntity<ApiResponse<S3PresignResultDto>> {
    //     val response = S3PresignResultDto(
    //         url = "https://ceph.example.com/my-bucket?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ACCESS_KEY%2F20260526%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260526T120000Z&X-Amz-Expires=300&X-Amz-SignedHeaders=host&X-Amz-Signature=abc123signature",
    //         method = "HEAD",
    //         headers = mapOf("host" to listOf("ceph.example.com")),
    //         expiresAt = "2026-05-26T12:05:00Z"
    //     )
    //     return ResponseEntity.ok(ApiResponse.success(response))
    // }

    // ─── Multipart Presign ────────────────────────────────────────────────────

    // @Operation(
    //     summary = "[CONSOLE] Multipart 시작 Presigned URL 발급",
    //     description = """
    //         멀티파트 업로드를 시작(CreateMultipartUpload)하기 위한 Presigned URL을 발급한다.
    //         FE는 발급된 URL로 POST 요청을 보내고 응답 XML에서 uploadId를 추출한 뒤 각 파트 업로드에 사용한다.
    //     """,
    //     security = [SecurityRequirement(name = "console-auth")],
    //     requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
    //         required = true,
    //         content = [Content(
    //             mediaType = MediaType.APPLICATION_JSON_VALUE,
    //             schema = Schema(implementation = S3MultipartStartRequestDto::class),
    //             examples = [
    //                 ExampleObject(
    //                     name = "기본 멀티파트 시작",
    //                     value = """
    //                     {
    //                         "bucket": "my-bucket",
    //                         "key": "deploy/agent/2026/05/large-file.bin",
    //                         "content_type": "application/octet-stream",
    //                         "expires_in_seconds": 300
    //                     }
    //                     """
    //                 ),
    //                 ExampleObject(
    //                     name = "체크섬 활성화 멀티파트 시작",
    //                     value = """
    //                     {
    //                         "bucket": "my-bucket",
    //                         "key": "deploy/agent/2026/05/large-file.bin",
    //                         "content_type": "application/octet-stream",
    //                         "expires_in_seconds": 300,
    //                         "checksum_sha256_enabled": true
    //                     }
    //                     """
    //                 )
    //             ]
    //         )]
    //     )
    // )
    // @PostMapping("/multipart/start/v1")
    // fun presignMultipartStart(
    //     @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
    //     @Valid @RequestBody request: S3MultipartStartRequestDto,
    // ): ResponseEntity<ApiResponse<S3PresignResultDto>> {
    //     val response = S3PresignResultDto(
    //         url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/large-file.bin?uploads&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ACCESS_KEY%2F20260526%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260526T120000Z&X-Amz-Expires=300&X-Amz-SignedHeaders=host&X-Amz-Signature=abc123signature",
    //         method = "POST",
    //         headers = mapOf("host" to listOf("ceph.example.com")),
    //         expiresAt = "2026-05-26T12:05:00Z"
    //     )
    //     return ResponseEntity.ok(ApiResponse.success(response))
    // }

    // @Operation(
    //     summary = "[CONSOLE] Multipart 단일 파트 Presigned URL 발급",
    //     description = """
    //         멀티파트 업로드의 단일 파트(UploadPart)를 위한 Presigned URL을 발급한다.
    //         uploadId는 CreateMultipartUpload 응답 XML에서 얻는다. 각 파트 업로드 후 응답 헤더의 ETag를 저장해 CompleteMultipartUpload 시 사용해야 한다.
    //     """,
    //     security = [SecurityRequirement(name = "console-auth")],
    //     requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
    //         required = true,
    //         content = [Content(
    //             mediaType = MediaType.APPLICATION_JSON_VALUE,
    //             schema = Schema(implementation = S3MultipartPartUrlRequestDto::class),
    //             examples = [
    //                 ExampleObject(
    //                     name = "기본 파트 URL 발급",
    //                     value = """
    //                     {
    //                         "bucket": "my-bucket",
    //                         "key": "deploy/agent/2026/05/large-file.bin",
    //                         "upload_id": "VXBsb2FkSWQxMjM0NTY3OA==",
    //                         "part_number": 1,
    //                         "expires_in_seconds": 3600
    //                     }
    //                     """
    //                 ),
    //                 ExampleObject(
    //                     name = "체크섬 포함 파트 URL 발급",
    //                     value = """
    //                     {
    //                         "bucket": "my-bucket",
    //                         "key": "deploy/agent/2026/05/large-file.bin",
    //                         "upload_id": "VXBsb2FkSWQxMjM0NTY3OA==",
    //                         "part_number": 1,
    //                         "expires_in_seconds": 3600,
    //                         "checksum_sha256": "n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg="
    //                     }
    //                     """
    //                 )
    //             ]
    //         )]
    //     )
    // )
    // @PostMapping("/multipart/part-url/v1")
    // fun presignMultipartPartUrl(
    //     @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
    //     @Valid @RequestBody request: S3MultipartPartUrlRequestDto,
    // ): ResponseEntity<ApiResponse<S3PresignResultDto>> {
    //     val response = S3PresignResultDto(
    //         url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/large-file.bin?partNumber=1&uploadId=VXBsb2FkSWQxMjM0NTY3OA%3D%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ACCESS_KEY%2F20260526%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260526T120000Z&X-Amz-Expires=3600&X-Amz-SignedHeaders=host&X-Amz-Signature=abc123signature",
    //         method = "PUT",
    //         headers = mapOf("host" to listOf("ceph.example.com")),
    //         expiresAt = "2026-05-26T13:00:00Z"
    //     )
    //     return ResponseEntity.ok(ApiResponse.success(response))
    // }

    // @Operation(
    //     summary = "[CONSOLE] Multipart 파트 Presigned URL 일괄 발급",
    //     description = """
    //         멀티파트 업로드의 여러 파트(UploadPart) Presigned URL을 한 번에 발급한다.
    //         uploadId는 CreateMultipartUpload 응답 XML에서 얻는다. 응답 순서는 요청 parts 순서와 동일하다.
    //     """,
    //     security = [SecurityRequirement(name = "console-auth")],
    //     requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
    //         required = true,
    //         content = [Content(
    //             mediaType = MediaType.APPLICATION_JSON_VALUE,
    //             schema = Schema(implementation = S3MultipartPartUrlsBulkRequestDto::class),
    //             examples = [
    //                 ExampleObject(
    //                     name = "기본 파트 URL 일괄 발급",
    //                     value = """
    //                     {
    //                         "upload_id": "VXBsb2FkSWQxMjM0NTY3OA==",
    //                         "bucket": "my-bucket",
    //                         "key": "deploy/agent/2026/05/large-file.bin",
    //                         "part_expires_in_seconds": 3600,
    //                         "parts": [
    //                             { "part_number": 1 },
    //                             { "part_number": 2 },
    //                             { "part_number": 3 }
    //                         ]
    //                     }
    //                     """
    //                 ),
    //                 ExampleObject(
    //                     name = "체크섬 포함 파트 URL 일괄 발급",
    //                     value = """
    //                     {
    //                         "upload_id": "VXBsb2FkSWQxMjM0NTY3OA==",
    //                         "bucket": "my-bucket",
    //                         "key": "deploy/agent/2026/05/large-file.bin",
    //                         "part_expires_in_seconds": 3600,
    //                         "parts": [
    //                             { "part_number": 1, "checksum_sha256": "n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg=" },
    //                             { "part_number": 2, "checksum_sha256": "n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg=" },
    //                             { "part_number": 3, "checksum_sha256": "n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg=" }
    //                         ]
    //                     }
    //                     """
    //                 )
    //             ]
    //         )]
    //     )
    // )
    // @PostMapping("/multipart/part-urls/v1")
    // fun presignMultipartPartUrls(
    //     @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
    //     @Valid @RequestBody request: S3MultipartPartUrlsBulkRequestDto,
    // ): ResponseEntity<ApiResponse<List<S3PresignResultDto>>> {
    //     val items = listOf(
    //         S3PresignResultDto(
    //             url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/large-file.bin?partNumber=1&uploadId=VXBsb2FkSWQxMjM0NTY3OA%3D%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600&X-Amz-Signature=abc111",
    //             method = "PUT",
    //             headers = mapOf("host" to listOf("ceph.example.com")),
    //             expiresAt = "2026-05-26T13:00:00Z"
    //         ),
    //         S3PresignResultDto(
    //             url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/large-file.bin?partNumber=2&uploadId=VXBsb2FkSWQxMjM0NTY3OA%3D%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600&X-Amz-Signature=abc222",
    //             method = "PUT",
    //             headers = mapOf("host" to listOf("ceph.example.com")),
    //             expiresAt = "2026-05-26T13:00:00Z"
    //         ),
    //         S3PresignResultDto(
    //             url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/large-file.bin?partNumber=3&uploadId=VXBsb2FkSWQxMjM0NTY3OA%3D%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600&X-Amz-Signature=abc333",
    //             method = "PUT",
    //             headers = mapOf("host" to listOf("ceph.example.com")),
    //             expiresAt = "2026-05-26T13:00:00Z"
    //         )
    //     )
    //     return ResponseEntity.ok(ApiResponse.success(items))
    // }

    @Operation(
        summary = "[CONSOLE] Multipart 자동 Presigned URL 일괄 발급",
        description = """
            서버에서 uploadId를 직접 생성하고, CreateMultipartUpload Presigned URL과 각 파트 업로드 Presigned URL을 한 번에 발급한다.
            FE는 별도로 CreateMultipartUpload를 호출하지 않아도 되며, 응답의 uploadId를 CompleteMultipartUpload/AbortMultipartUpload 시 사용한다.
        """,
        security = [SecurityRequirement(name = "console-auth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = S3MultipartAutoPresignRequestDto::class),
                examples = [
                    ExampleObject(
                        value = """
                        {
                            "fileName": "agent-installer.pkg",
                            "fileSize": 10485760,
                            "fileType": "AGENT_INSTALLER",
                            "partNumber": 3
                        }
                        """
                    )
                ]
            )]
        )
    )
    @PostMapping("/multipart/auto/v1")
    fun presignMultipartAuto(
        @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
        @Valid @RequestBody request: S3MultipartAutoPresignRequestDto,
    ): ResponseEntity<ApiResponse<S3MultipartAutoPresignResponseDto>> {
        val response = S3MultipartAutoPresignResponseDto(
            bucket = "my-bucket",
            key = "deploy/agent/2026/05/large-file.bin",
            uploadId = "VXBsb2FkSWQxMjM0NTY3OA==",
            startUrl = S3PresignResultDto(
                url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/large-file.bin?uploads&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=300&X-Amz-Signature=startabc123",
                method = "POST",
                headers = mapOf("host" to listOf("ceph.example.com")),
                expiresAt = "2026-05-26T12:05:00Z"
            ),
            partUrls = listOf(
                S3MultipartPartPresignResultDto(
                    partNumber = 1,
                    partUrl = S3PresignResultDto(
                        url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/large-file.bin?partNumber=1&uploadId=VXBsb2FkSWQxMjM0NTY3OA%3D%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600&X-Amz-Signature=partabc111",
                        method = "PUT",
                        headers = mapOf("host" to listOf("ceph.example.com")),
                        expiresAt = "2026-05-26T13:00:00Z"
                    )
                ),
                S3MultipartPartPresignResultDto(
                    partNumber = 2,
                    partUrl = S3PresignResultDto(
                        url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/large-file.bin?partNumber=2&uploadId=VXBsb2FkSWQxMjM0NTY3OA%3D%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600&X-Amz-Signature=partabc222",
                        method = "PUT",
                        headers = mapOf("host" to listOf("ceph.example.com")),
                        expiresAt = "2026-05-26T13:00:00Z"
                    )
                ),
                S3MultipartPartPresignResultDto(
                    partNumber = 3,
                    partUrl = S3PresignResultDto(
                        url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/large-file.bin?partNumber=3&uploadId=VXBsb2FkSWQxMjM0NTY3OA%3D%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600&X-Amz-Signature=partabc333",
                        method = "PUT",
                        headers = mapOf("host" to listOf("ceph.example.com")),
                        expiresAt = "2026-05-26T13:00:00Z"
                    )
                )
            )
        )
        return ResponseEntity.ok(ApiResponse.success(response))
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

    @Operation(
        summary = "[CONSOLE] Multipart 완료 Presigned URL 발급",
        description = """
            멀티파트 업로드를 완료(CompleteMultipartUpload)하기 위한 Presigned URL을 발급한다.
            FE는 발급된 URL로 POST 요청을 보낼 때 각 파트의 ETag 목록을 XML 형식으로 포함해야 한다.
        """,
        security = [SecurityRequirement(name = "console-auth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = S3MultipartCompleteRequestDto::class),
                examples = [ExampleObject(value = """
                {
                    "key": "deploy/agent/2026/05/large-file.bin",
                    "upload_id": "VXBsb2FkSWQxMjM0NTY3OA==",
                    "parts": [
                        { "part_number": 1, "e_tag": "\"d8e8fca2dc0f896fd7cb4cb0031ba249\"" },
                        { "part_number": 2, "e_tag": "\"9e107d9d372bb6826bd81d3542a419d6\"" },
                        { "part_number": 3, "e_tag": "\"b94f6f125c79e3a5ffaa826f584c10d6\"" }
                    ],
                    "expires_in_seconds": 300
                }
                """)]
            )]
        )
    )
    @PostMapping("/multipart/complete/v1")
    fun presignMultipartComplete(
        @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
        @Valid @RequestBody request: S3MultipartCompleteRequestDto,
    ): ResponseEntity<ApiResponse<S3PresignResultDto>> {
        val response = S3PresignResultDto(
            url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/large-file.bin?uploadId=VXBsb2FkSWQxMjM0NTY3OA%3D%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ACCESS_KEY%2F20260526%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260526T120000Z&X-Amz-Expires=300&X-Amz-SignedHeaders=host&X-Amz-Signature=abc123signature",
            method = "POST",
            headers = mapOf("host" to listOf("ceph.example.com")),
            expiresAt = "2026-05-26T12:05:00Z"
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    // @Operation(
    //     summary = "[CONSOLE] Multipart 중단 Presigned URL 발급",
    //     description = """
    //         멀티파트 업로드를 중단(AbortMultipartUpload)하기 위한 Presigned URL을 발급한다.
    //         업로드 실패 시 반드시 중단 요청을 보내 S3에 업로드 중인 파트 리소스를 정리해야 한다.
    //     """,
    //     security = [SecurityRequirement(name = "console-auth")],
    //     requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
    //         required = true,
    //         content = [Content(
    //             mediaType = MediaType.APPLICATION_JSON_VALUE,
    //             schema = Schema(implementation = S3MultipartAbortRequestDto::class),
    //             examples = [ExampleObject(value = """
    //             {
    //                 "bucket": "my-bucket",
    //                 "key": "deploy/agent/2026/05/large-file.bin",
    //                 "upload_id": "VXBsb2FkSWQxMjM0NTY3OA==",
    //                 "expires_in_seconds": 300
    //             }
    //             """)]
    //         )]
    //     )
    // )
    // @PostMapping("/multipart/abort/v1")
    // fun presignMultipartAbort(
    //     @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
    //     @Valid @RequestBody request: S3MultipartAbortRequestDto,
    // ): ResponseEntity<ApiResponse<S3PresignResultDto>> {
    //     val response = S3PresignResultDto(
    //         url = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/large-file.bin?uploadId=VXBsb2FkSWQxMjM0NTY3OA%3D%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ACCESS_KEY%2F20260526%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260526T120000Z&X-Amz-Expires=300&X-Amz-SignedHeaders=host&X-Amz-Signature=abc123signature",
    //         method = "DELETE",
    //         headers = mapOf("host" to listOf("ceph.example.com")),
    //         expiresAt = "2026-05-26T12:05:00Z"
    //     )
    //     return ResponseEntity.ok(ApiResponse.success(response))
    // }
}
