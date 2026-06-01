package template.s3.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3BucketActionResponseDto(
    @field:Schema(required = true, description = "대상 버킷 이름", example = "my-deployment-bucket")
    val bucket: String,

    @field:Schema(required = true, description = "작업 성공 여부", example = "true")
    val success: Boolean,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3BucketNamesResponseDto(
    @field:Schema(required = true, description = "접근 가능한 버킷 이름 목록")
    val bucketNames: List<String>,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3ObjectDeleteResponseDto(
    @field:Schema(required = true, description = "버킷 이름", example = "my-deployment-bucket")
    val bucket: String,

    @field:Schema(
        required = true,
        description = "삭제된 오브젝트 키",
        example = "deploy/agent/2026/05/agent-installer.pkg"
    )
    val key: String,

    @field:Schema(required = true, description = "삭제 성공 여부", example = "true")
    val success: Boolean,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3MultipartPartsListResponseDto(
    @field:Schema(required = true, description = "버킷 이름", example = "my-deployment-bucket")
    val bucket: String,

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
    val parts: List<S3MultipartPartInfoResponseDto>,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3MultipartPartInfoResponseDto(
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
