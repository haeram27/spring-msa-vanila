package template.s3.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

// ─── Shared ───────────────────────────────────────────────────────────────────

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3MultipartPartResourceDto(
    @field:Schema(required = true, description = "파트 번호 (1-based, 1 ~ 10000)", example = "1")
    val partNumber: Int,

    @field:Schema(
        required = false,
        description = "파트 데이터의 SHA-256 체크섬 (Base64 인코딩, 32바이트). checksumSha256Enabled=true인 경우 필수.",
        example = "n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg="
    )
    val checksumSha256: String? = null,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3PartEtagDto(
    @field:Schema(required = true, description = "파트 번호 (1-based, 1 ~ 10000)", example = "1")
    val partNumber: Int,

    @field:Schema(
        required = true,
        description = "UploadPart 요청 응답 헤더에서 반환된 ETag 값 (따옴표 포함)",
        example = "\"d8e8fca2dc0f896fd7cb4cb0031ba249\""
    )
    val eTag: String,
)

// ─── File Presign Request DTOs ────────────────────────────────────────────────

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3PresignPutRequestDto(
    @field:Schema(required = true, description = "버킷 이름", example = "my-bucket")
    val bucket: String,

    @field:Schema(
        required = true,
        description = "오브젝트 키 (디렉터리 경로 포함)",
        example = "deploy/agent/2026/05/agent-installer.pkg"
    )
    val key: String,

    @field:Schema(
        required = false,
        description = "업로드할 파일의 Content-Type. 지정하면 presigned URL 사용 시 동일한 Content-Type 헤더를 포함해야 한다.",
        example = "application/octet-stream"
    )
    val contentType: String? = null,

    @field:Schema(
        required = false,
        description = "업로드할 파일 크기 (bytes). 지정하면 presigned URL 사용 시 Content-Length 헤더가 필요하다.",
        example = "10485760"
    )
    val contentLength: Long? = null,

    @field:Schema(
        required = false,
        description = "URL 유효 시간 (초). null이면 기본값 300초 적용. 최대 604800초(7일).",
        example = "300",
        defaultValue = "300"
    )
    val expiresInSeconds: Int? = null,

    @field:Schema(
        required = false,
        description = "업로드할 파일의 SHA-256 체크섬 (Base64 인코딩). 지정하면 S3가 업로드 후 체크섬을 검증한다.",
        example = "n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg="
    )
    val checksumSha256: String? = null,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3PresignPutBulkRequestDto(
    @field:Schema(required = true, description = "Presigned PUT URL을 발급할 오브젝트 목록. 비어있으면 안 된다.")
    val items: List<S3PresignPutRequestDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3PresignGetRequestDto(
    @field:Schema(required = true, description = "버킷 이름", example = "my-bucket")
    val bucket: String,

    @field:Schema(
        required = true,
        description = "다운로드할 오브젝트 키",
        example = "deploy/agent/2026/05/agent-installer.pkg"
    )
    val key: String,

    @field:Schema(
        required = false,
        description = "응답에 사용할 Content-Type 오버라이드. null이면 S3에 저장된 원래 Content-Type을 사용한다.",
        example = "application/octet-stream"
    )
    val responseContentType: String? = null,

    @field:Schema(
        required = false,
        description = "URL 유효 시간 (초). null이면 기본값 300초 적용. 최대 604800초(7일).",
        example = "300",
        defaultValue = "300"
    )
    val expiresInSeconds: Int? = null,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3PresignDeleteRequestDto(
    @field:Schema(required = true, description = "버킷 이름", example = "my-bucket")
    val bucket: String,

    @field:Schema(
        required = true,
        description = "삭제할 오브젝트 키",
        example = "deploy/agent/2026/05/agent-installer.pkg"
    )
    val key: String,

    @field:Schema(
        required = false,
        description = "URL 유효 시간 (초). null이면 기본값 300초 적용. 최대 604800초(7일).",
        example = "300",
        defaultValue = "300"
    )
    val expiresInSeconds: Int? = null,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3PresignHeadObjectRequestDto(
    @field:Schema(required = true, description = "버킷 이름", example = "my-bucket")
    val bucket: String,

    @field:Schema(
        required = true,
        description = "메타데이터를 확인할 오브젝트 키",
        example = "deploy/agent/2026/05/agent-installer.pkg"
    )
    val key: String,

    @field:Schema(
        required = false,
        description = "URL 유효 시간 (초). null이면 기본값 300초 적용. 최대 604800초(7일).",
        example = "300",
        defaultValue = "300"
    )
    val expiresInSeconds: Int? = null,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3PresignHeadBucketRequestDto(
    @field:Schema(required = true, description = "접근 가능 여부를 확인할 버킷 이름", example = "my-bucket")
    val bucket: String,

    @field:Schema(
        required = false,
        description = "URL 유효 시간 (초). null이면 기본값 300초 적용. 최대 604800초(7일).",
        example = "300",
        defaultValue = "300"
    )
    val expiresInSeconds: Int? = null,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3PresignGetRangeRequestDto(
    @field:Schema(required = true, description = "버킷 이름", example = "my-bucket")
    val bucket: String,

    @field:Schema(
        required = true,
        description = "다운로드할 오브젝트 키",
        example = "deploy/agent/2026/05/large-file.bin"
    )
    val key: String,

    @field:Schema(
        required = true,
        description = "다운로드 시작 바이트 (0-based, >= 0)",
        example = "0"
    )
    val rangeStart: Long,

    @field:Schema(
        required = true,
        description = "다운로드 종료 바이트 (inclusive, >= rangeStart). 예: 0~1048575는 첫 1MiB.",
        example = "1048575"
    )
    val rangeEnd: Long,

    @field:Schema(
        required = false,
        description = "URL 유효 시간 (초). null이면 기본값 300초 적용. 최대 604800초(7일).",
        example = "300",
        defaultValue = "300"
    )
    val expiresInSeconds: Int? = null,
)

// ─── Multipart Request DTOs ───────────────────────────────────────────────────

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3MultipartStartRequestDto(
    @field:Schema(required = true, description = "버킷 이름", example = "my-bucket")
    val bucket: String,

    @field:Schema(
        required = true,
        description = "오브젝트 키",
        example = "deploy/agent/2026/05/large-file.bin"
    )
    val key: String,

    @field:Schema(
        required = false,
        description = "업로드할 파일의 Content-Type",
        example = "application/octet-stream"
    )
    val contentType: String? = null,

    @field:Schema(
        required = false,
        description = "URL 유효 시간 (초). null이면 기본값 300초 적용. 최대 604800초(7일).",
        example = "300",
        defaultValue = "300"
    )
    val expiresInSeconds: Int? = null,

    @field:Schema(
        required = false,
        description = "SHA-256 체크섬 활성화 여부. true이면 각 UploadPart 요청 시 checksumSha256이 필수.",
        example = "false",
        defaultValue = "false"
    )
    val checksumSha256Enabled: Boolean? = null,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3MultipartPartUrlRequestDto(
    @field:Schema(required = true, description = "버킷 이름", example = "my-bucket")
    val bucket: String,

    @field:Schema(
        required = true,
        description = "오브젝트 키",
        example = "deploy/agent/2026/05/large-file.bin"
    )
    val key: String,

    @field:Schema(
        required = true,
        description = "CreateMultipartUpload 응답에서 반환된 uploadId",
        example = "VXBsb2FkSWQxMjM0NTY3OA=="
    )
    val uploadId: String,

    @field:Schema(
        required = true,
        description = "파트 번호 (1-based, 1 ~ 10000)",
        example = "1"
    )
    val partNumber: Int,

    @field:Schema(
        required = false,
        description = "URL 유효 시간 (초). null이면 기본값 300초 적용. 최대 604800초(7일).",
        example = "300",
        defaultValue = "300"
    )
    val expiresInSeconds: Int? = null,

    @field:Schema(
        required = false,
        description = "파트 데이터의 SHA-256 체크섬 (Base64 인코딩). CreateMultipartUpload 요청 시 checksumSha256Enabled=true인 경우 필수.",
        example = "n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg="
    )
    val checksumSha256: String? = null,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3MultipartPartUrlsBulkRequestDto(
    @field:Schema(
        required = true,
        description = "CreateMultipartUpload 응답에서 반환된 uploadId",
        example = "VXBsb2FkSWQxMjM0NTY3OA=="
    )
    val uploadId: String,

    @field:Schema(required = true, description = "버킷 이름", example = "my-bucket")
    val bucket: String,

    @field:Schema(
        required = true,
        description = "오브젝트 키",
        example = "deploy/agent/2026/05/large-file.bin"
    )
    val key: String,

    @field:Schema(
        required = false,
        description = "각 파트 URL의 유효 시간 (초). null이면 기본값 300초 적용. 최대 604800초(7일).",
        example = "3600",
        defaultValue = "300"
    )
    val partExpiresInSeconds: Int? = null,

    @field:Schema(required = true, description = "Presigned URL을 발급할 파트 목록. 비어있으면 안 된다.")
    val parts: List<S3MultipartPartResourceDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3MultipartAutoPresignRequestDto(
    @field:Schema(required = true, description = "버킷 이름", example = "my-bucket")
    val bucket: String,

    @field:Schema(
        required = true,
        description = "오브젝트 키",
        example = "deploy/agent/2026/05/large-file.bin"
    )
    val key: String,

    @field:Schema(
        required = true,
        description = "업로드할 파트 목록. 파트 번호(1-based)와 선택적 체크섬을 포함한다. 비어있으면 안 된다."
    )
    val parts: List<S3MultipartPartResourceDto>,

    @field:Schema(
        required = false,
        description = "업로드할 파일의 Content-Type",
        example = "application/octet-stream"
    )
    val contentType: String? = null,

    @field:Schema(
        required = false,
        description = "시작(CreateMultipartUpload) presigned URL의 유효 시간 (초). null이면 기본값 300초.",
        example = "300",
        defaultValue = "300"
    )
    val startExpiresInSeconds: Int? = null,

    @field:Schema(
        required = false,
        description = "각 파트 업로드 presigned URL의 유효 시간 (초). null이면 기본값 300초.",
        example = "3600",
        defaultValue = "300"
    )
    val partExpiresInSeconds: Int? = null,

    @field:Schema(
        required = false,
        description = "SHA-256 체크섬 활성화 여부. true이면 parts의 각 항목에 checksumSha256이 필수.",
        example = "false",
        defaultValue = "false"
    )
    val checksumSha256Enabled: Boolean? = null,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3MultipartCompleteRequestDto(
    @field:Schema(required = true, description = "버킷 이름", example = "my-bucket")
    val bucket: String,

    @field:Schema(
        required = true,
        description = "오브젝트 키",
        example = "deploy/agent/2026/05/large-file.bin"
    )
    val key: String,

    @field:Schema(
        required = true,
        description = "CreateMultipartUpload 응답에서 반환된 uploadId",
        example = "VXBsb2FkSWQxMjM0NTY3OA=="
    )
    val uploadId: String,

    @field:Schema(
        required = true,
        description = "완료할 파트 목록. UploadPart 응답 헤더에서 반환된 ETag를 파트 번호와 함께 전달해야 한다. 비어있으면 안 된다."
    )
    val parts: List<S3PartEtagDto>,

    @field:Schema(
        required = false,
        description = "URL 유효 시간 (초). null이면 기본값 300초 적용. 최대 604800초(7일).",
        example = "300",
        defaultValue = "300"
    )
    val expiresInSeconds: Int? = null,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class S3MultipartAbortRequestDto(
    @field:Schema(required = true, description = "버킷 이름", example = "my-bucket")
    val bucket: String,

    @field:Schema(
        required = true,
        description = "오브젝트 키",
        example = "deploy/agent/2026/05/large-file.bin"
    )
    val key: String,

    @field:Schema(
        required = true,
        description = "중단할 multipart upload의 uploadId",
        example = "VXBsb2FkSWQxMjM0NTY3OA=="
    )
    val uploadId: String,

    @field:Schema(
        required = false,
        description = "URL 유효 시간 (초). null이면 기본값 300초 적용. 최대 604800초(7일).",
        example = "300",
        defaultValue = "300"
    )
    val expiresInSeconds: Int? = null,
)
