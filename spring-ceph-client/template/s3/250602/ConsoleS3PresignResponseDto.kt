package package template.s3.250602

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ConsoleS3PresignResultDto(
    @field:Schema(
        required = true,
        description = "Presigned URL. 이 URL로 직접 S3/Ceph에 HTTP 요청을 보낸다.",
        example = "https://ceph.example.com/my-bucket/deploy/agent/2026/05/agent-installer.pkg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=300&X-Amz-Signature=..."
    )
    val url: String,

    @field:Schema(
        required = true,
        description = "presigned URL 호출 시 사용해야 하는 HTTP 메서드",
        example = "PUT",
        allowableValues = ["PUT", "GET", "DELETE", "HEAD", "POST"]
    )
    val method: String,

    @field:Schema(
        required = true,
        description = "presigned URL 호출 시 포함해야 하는 헤더 맵. 키는 헤더명, 값은 헤더 값 목록. 비어있을 수 있다."
    )
    val headers: Map<String, List<String>>,

    @field:Schema(
        required = true,
        description = "URL 만료 시각 (ISO 8601 UTC)",
        example = "2026-05-26T12:05:00Z"
    )
    val expiresAt: String,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ConsoleS3MultipartPartPresignResultDto(
    @field:Schema(required = true, description = "파트 번호 (1-based)", example = "1")
    val partNumber: Int,

    @field:Schema(required = true, description = "해당 파트 업로드를 위한 Presigned URL 정보")
    val partUrl: ConsoleS3PresignResultDto,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ConsoleS3MultipartAutoPresignResponseDto(
    @field:Schema(
        required = true,
        description = "오브젝트 키",
        example = "deploy/agent/2026/05/large-file.bin"
    )
    val key: String,

    @field:Schema(
        required = true,
        description = "서버에서 생성된 multipart upload ID. CompleteMultipartUpload 및 AbortMultipartUpload 요청 시 사용한다.",
        example = "VXBsb2FkSWQxMjM0NTY3OA=="
    )
    val uploadId: String,

    @field:Schema(
        required = true,
        description = "CreateMultipartUpload를 실행하기 위한 Presigned URL. 이 URL로 실제 multipart upload를 시작한다."
    )
    val startUrl: ConsoleS3PresignResultDto,

    @field:Schema(
        required = true,
        description = "각 파트 업로드를 위한 Presigned URL 목록. 파트 번호 오름차순으로 정렬된다."
    )
    val partUrls: List<ConsoleS3MultipartPartPresignResultDto>,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ConsoleS3MultipartPartsListResponseDto(
    @field:Schema(
        required = true,
        description = "멀티파트 업로드 대상 오브젝트 키",
        example = "deploy/agent/2026/05/agent-installer.pkg"
    )
    val key: String,

    @field:Schema(
        required = true,
        description = "조회한 멀티파트 업로드 ID",
        example = "VXBsb2FkSWQxMjM0NTY3OA=="
    )
    val uploadId: String,

    @field:Schema(required = true, description = "업로드된 파트 목록")
    val parts: List<ConsoleS3MultipartPartInfoResponseDto>,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ConsoleS3MultipartPartInfoResponseDto(
    @field:Schema(required = true, description = "파트 번호", example = "1")
    val partNumber: Int,

    @field:Schema(required = true, description = "업로드된 파트의 ETag", example = "\"9b2cf535f27731c974343645a3985328\"")
    val eTag: String,

    @field:Schema(required = true, description = "파트 크기(bytes)", example = "5242880")
    val size: Long,

    @field:Schema(
        required = true,
        description = "파트 마지막 수정 시각(ISO-8601 UTC)",
        example = "2026-05-27T06:30:45Z"
    )
    val lastModified: String,
)