package template.s3.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3BucketCreateRequestDto(
    @field:Schema(
        required = true,
        description = "생성할 버킷 이름. S3 버킷 명명 규칙을 따라야 한다 (소문자, 숫자, 하이픈, 3~63자).",
        example = "my-deployment-bucket"
    )
    val bucket: String,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3BucketDeleteRequestDto(
    @field:Schema(
        required = true,
        description = "삭제할 버킷 이름. 버킷 안에 오브젝트가 남아있으면 삭제에 실패할 수 있다.",
        example = "my-deployment-bucket"
    )
    val bucket: String,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3ObjectDeleteRequestDto(
    @field:Schema(required = true, description = "버킷 이름", example = "my-deployment-bucket")
    val bucket: String,

    @field:Schema(
        required = true,
        description = "삭제할 오브젝트 키",
        example = "deploy/agent/2026/05/agent-installer.pkg"
    )
    val key: String,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3MultipartPartsListRequestDto(
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
        description = "조회할 멀티파트 업로드 ID",
        example = "VXBsb2FkSWQxMjM0NTY3OA=="
    )
    val uploadId: String,
)
