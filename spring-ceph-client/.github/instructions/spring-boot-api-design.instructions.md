---
description: "Use when designing or implementing Spring Boot Kotlin backend APIs, REST controllers, request/response DTOs, Swagger/OpenAPI annotations, URL patterns, HTTP methods, and API response contracts for Admin, Console, Agent, OpenAPI, and Microservice services. Covers *Controller.kt, *Dto.kt, request/response DTOs, snake_case JSON, and ResponseEntity<ApiResponse<T>> patterns."
name: "Spring Boot API Design Rules"
applyTo: "**/*Controller.java, **/*Controller.kt, **/controller/**/*.java, **/controller/**/*.kt, **/*Api.java, **/*Api.kt, **/*Request.java, **/*Response.java, **/*Dto.java, **/*Dto.kt, **/*RequestDto.kt, **/*ResponseDto.kt, **/dto/**/*.java, **/dto/**/*.kt, **/request/**/*.java, **/request/**/*.kt, **/response/**/*.java, **/response/**/*.kt, **/openapi/**/*.yml, **/openapi/**/*.yaml, **/*openapi*.yml, **/*openapi*.yaml"
---

# Spring Boot API Design Rules

Use these rules when the task is to design or implement backend APIs in a Spring Boot web project.

These rules are team conventions, not generic REST advice. When they conflict with common REST style, follow these rules.

## Scope

- Apply these rules when creating or changing controller endpoints, request/response DTOs, API paths, HTTP methods, response contracts, and Swagger documentation.
- Treat Admin and Console APIs differently from general public OpenAPI design.
- Treat Kotlin Controller and Dto files as first-class targets, not Java-only examples.
- Follow existing project conventions before introducing a new response envelope, naming strategy, or annotation style.

## Baseline Style For This Team

- Prefer `@RestController` plus class-level `@RequestMapping` for the stable resource prefix.
- Prefer `@Tag` on the controller and `@Operation` on each endpoint when the project already documents APIs with Swagger annotations.
- Prefer `@Valid @RequestBody` for JSON request DTO inputs.

## URL Structure

- All API URLs must start with `/api`.
- API version must be included in the URL.
- Use lowercase only.
- Use nouns for top-level resources, not verbs.
- Use hyphens for multi-word path segments.
- Use plural nouns for collection resources unless the nearby project already standardizes on a singular domain resource such as `/policy` or `/scheduler`.
- Do not include file extensions such as `.json` or `.xml` in the URL.

Use these base patterns:

- Admin: `/api/admin/{resource}/{version}`
- Console: `/api/console/{resource}/{version}`
- Agent: `/api/agent/{version}/{resource}`
- OpenAPI: `/api/open-api/{resource}/{version}`
- Microservices: `/api/{version}/{service-area}/{service-name}/{resource}`

## Path Design Rules

- Do not use verb-style paths such as `/getNodes`.
- Do not use uppercase letters in any path segment.
- Do not use underscore-separated path segments when a hyphenated segment is needed.
- Do not use query strings for the main API contract when the request condition is large or complex.
- Avoid expressing filtering or sorting as path variables.
- It is acceptable to use an explicit action suffix for business operations under an established resource, such as `/create`, `/update`, `/delete`, `/assign`, `/cancel`, `/restart`, or `/execute`.
- It is acceptable to use a scoped sub-resource or qualifier segment when the domain model requires it, such as `/{product_domain}`, `/group`, `/node`, `/summary`, or `/list`.

Preferred examples:

- `/api/console/one/nodes/v1`
- `/api/console/one/node-view/v1`
- `/api/console/policy/{product_domain}/v1`
- `/api/console/policy/{product_domain}/list/v1`
- `/api/console/policy-assignment/group/assign/v1`

Avoid examples:

- `/v1/console/one/nodes`
- `/api/console/v1/getNodes`
- `/api/console/v1/one/Nodes`
- `/api/console/v1/one/node_view`

## HTTP Method Rules

- Only `GET` and `POST` are allowed by team rule.
- In practice, new business APIs should default to `POST` unless there is a clear reason to use `GET`.
- For OpenAPI, follow the external API standard as closely as possible.

## URL + Method Rules For Admin And Console APIs

### Create and Action Endpoints

- Use `POST`.
- Use `/create` at the end of the resource URL for create or add actions.
- Use `/execute` at the end of the resource URL for run, action, or command-style operations.
- Use a domain-specific action suffix when the business language is already established in the codebase, such as `/assign`, `/cancel`, `/restart`, or `/immediate`.

Examples:

- `POST /api/console/{resource}/create/{version}`
- `POST /api/console/{resource}/execute/{version}`
- `POST /api/console/{resource}/assign/{version}`
- `POST /api/console/{resource}/cancel/{version}`

### Query Endpoints

- Even for reads, default to `POST` for Admin and Console APIs when request conditions, paging, projection, sorting, or security constraints are involved.
- Use the plain resource URL for single-resource or detail query endpoints.
- Pass query conditions in the request body.
- Use `/list` for list or paged queries when the API returns a collection rather than a single resource.
- Use `/summary` when the API returns a reduced projection intended for selectors or compact lookups.

Examples:

- `POST /api/console/{resource}/{version}`
- `POST /api/console/{resource}/list/{version}`
- `POST /api/console/{resource}/summary/{version}`

### Update Endpoints

- Use `POST`.
- Append `/update` to the resource URL.

Example:

- `POST /api/console/{resource}/update/{version}`

### Delete Endpoints

- Use `POST`.
- Append `/delete` to the resource URL.

Example:

- `POST /api/console/{resource}/delete/{version}`

### File Upload Endpoints

- Use `POST` unless there is a very specific reason not to.
- Append `/upload` to the resource URL.
- Use `multipart/form-data`.

Example:

- `POST /api/console/{resource}/upload/{version}`

## Request Rules

- Use JSON for normal requests and responses.
- Use `multipart/form-data` only for file upload scenarios.
- For Admin and Console service calls from frontend, use JWT in the request header.
- For service-to-service calls, use the service-issued token defined by the platform.
- For complex search conditions, paging, projection, and sorting, prefer a request body DTO over query parameters.
- In Kotlin DTOs, prefer `@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)` when the external JSON contract is snake_case.
- Keep example JSON in Swagger annotations aligned with the DTO naming strategy.
- Prefer shared nested DTOs for pagination, sorting, filter blocks, cursor paging, and target selectors when reused across related APIs.
- Use nullable fields only when the API contract actually permits omission.
- Prefer constructor defaults only when the API really has a server-side default or an optional input.

## Response Rules

- Except for file download, return `application/json`.
- Prefer the project's existing common response envelope. In the reference Console Kotlin code, that is `ApiResponse<T>` wrapped by `ResponseEntity.ok(...)`.
- Do not introduce a new `code/message/data` envelope if the target project already standardizes on `ApiResponse` or another common wrapper.
- Team rule says many business-level failures are expressed in the response body while HTTP status stays `200`.
- Use non-200 HTTP status mainly when the URL pattern or HTTP method itself is unsupported, or when the resource does not support that URL or method.

## Spring Boot Implementation Guidance

- Prefer controller methods that map exactly to the path patterns above.
- For Admin and Console APIs, do not introduce `PUT`, `PATCH`, or `DELETE` endpoints unless the task explicitly overrides this team rule.
- For list and search APIs, prefer a request DTO in `@RequestBody` rather than many `@RequestParam` fields.
- For create, update, delete, execute, and upload operations, make the action suffix explicit in the mapping path.
- Keep request and response DTO names aligned with the resource and action.
- Wrap business responses in the project's common response envelope instead of returning raw entities directly.
- For Console Kotlin controllers, prefer this structure unless the neighboring code uses something else:
  - class-level `@RequestMapping` for the shared resource path
  - `@PostMapping` for each versioned endpoint suffix
  - `@RequestHeader(HttpCustomHeaders.TENANT_ID)` when tenant context is required by nearby code
  - `@Valid @RequestBody` request DTO parameter
  - `ResponseEntity<ApiResponse<...>>` return type

## Kotlin Controller Rules

- Prefer controller class names ending with `Controller` and DTO class names ending with `RequestDto`, `ResponseDto`, or a specific shared `Dto` suffix.
- Prefer wildcard DTO imports only when the surrounding file already uses them. Otherwise prefer explicit imports.
- Keep controller methods small and named after the business action, such as `getPolicy`, `listPolicies`, `assignGroupPolicy`, or `restartExecution`.
- Use `@Operation(summary = "[CONSOLE] ...")` or the matching system prefix when nearby controllers follow that summary format.
- Add `security = [SecurityRequirement(name = "console-auth")]` for Console APIs when the surrounding controllers already require it.
- When documenting request bodies with Swagger, use `schema = Schema(implementation = SomeRequestDto::class)` and provide snake_case example payloads.
- When deprecating an endpoint, mark both Kotlin `@Deprecated(...)` and Swagger `deprecated = true` if the project already exposes deprecation in docs.

## Kotlin DTO Rules

- Prefer one Kotlin DTO file per resource area, grouping tightly related request and response models together only when that improves discoverability.
- Prefer `@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)` on DTOs that serialize to snake_case JSON.
- Prefer `@JsonInclude(JsonInclude.Include.NON_NULL)` on response DTOs when optional fields should be omitted from JSON.
- Use `@field:Schema(...)` on Kotlin primary-constructor properties, not bare `@Schema`, so schema metadata binds correctly.
- Keep DTO property names in Kotlin idiomatic camelCase while the serialized JSON can remain snake_case through Jackson naming.
- Prefer dedicated nested DTOs over `Map<String, Any>` when the shape is stable and well known.
- Allow `Map<String, Any>` only for truly flexible payload fragments such as tags, target metadata, or free-form context.
- Reuse shared pagination, sort, filter, and target selector DTOs instead of duplicating the same structure across request classes.

## Swagger Annotation Rules

- Swagger UI documentation must be sufficient for a frontend engineer to implement the API integration without reading backend source code.
- Write Swagger annotations so the agent can treat documentation completeness as part of the API contract, not as optional decoration.
- Document required headers and authentication schemes so they are visible directly in Swagger UI.
- Document each request and response field so the frontend does not have to infer meaning, nullable behavior, default values, allowed enum values, formats, units, or example values.
- Document not only success responses but also meaningful failure responses and business error codes that the frontend must handle.
- For list or search APIs, document pagination, sorting, cursor semantics, and empty-result behavior with concrete examples.
- When using `oneOf`, sealed hierarchies, or other polymorphic DTO shapes, document the discriminator and subtype-specific fields so the frontend can branch correctly.
- Do not leave Swagger summaries, descriptions, examples, or schema metadata blank when the omitted detail would force the frontend to guess.

## Swagger UI Completeness Rule

- The agent must generate Swagger annotations under the assumption that Swagger UI is a frontend-facing contract.
- The agent must prefer adding explicit Swagger metadata when the frontend would otherwise need to inspect controller code, DTO code, or ask the backend developer for clarification.
- The agent must treat missing request examples, missing response examples, missing error documentation, missing header documentation, and missing enum or format details as incomplete API documentation.
- If a controller or DTO is being added or changed, the agent should update Swagger annotations together with the code unless the user explicitly says not to.
- If exact business error codes or authentication header names are not known from nearby code, the agent should infer them from existing patterns in the project or leave a clearly marked TODO-style assumption in the documentation text instead of silently omitting them.

## Swagger Annotation Checklist For Controllers

- Add `@Tag` with a stable domain name that matches the resource area.
- Add `@Operation` on every public endpoint.
- Fill `summary` with a concise user-facing action name.
- Fill `description` when the endpoint has behavior, scope, side effects, constraints, or domain rules that are not obvious from the path alone.
- Add `security = [SecurityRequirement(...)]` when authentication is required.
- Document required headers with `@Parameter` or the project's preferred equivalent when they are not fully obvious from the generated signature alone.
- Document path variables, query parameters, and request body semantics so the role of each input is explicit.
- Use `io.swagger.v3.oas.annotations.parameters.RequestBody` with `Content`, `Schema`, and realistic `ExampleObject` values for JSON request bodies.
- Add `responses = [...]` when the default generated response docs would hide wrapper shape, failure cases, or business result semantics.
- For file upload endpoints, document `multipart/form-data`, part names, accepted content types, and example usage.
- For list endpoints, include examples for first page, next page or cursor request, sorted request, and empty result.
- For execute, assign, cancel, restart, delete, or other action endpoints, document the user-visible effect and idempotency or retry expectations when relevant.
- Mark deprecated endpoints with both Kotlin `@Deprecated` and Swagger `deprecated = true` when the API is still exposed.

## Swagger Annotation Checklist For DTOs

- Add `@field:Schema` to each externally visible request and response property unless the field is intentionally hidden.
- Write field descriptions from the consumer point of view, not just the backend storage term.
- Mark requiredness accurately instead of marking everything as required.
- Provide `example` or `examples` values for fields whose meaning is easier to understand with a sample.
- Provide `allowableValues` for enum-like string fields when the project uses string literals instead of enum classes.
- Document format expectations such as ISO 8601 timestamps, base64 payloads, hash strings, cursor tokens, IDs, cron expressions, and timezone values.
- Document units for numeric values such as seconds, milliseconds, bytes, page size, or count.
- Explain nullable fields so the frontend can distinguish omitted, null, empty, and default cases.
- When a response omits null values, keep Swagger examples consistent with that omission.
- When a DTO contains nested selector, filter, pagination, sort, or policy blocks, annotate the nested DTOs too instead of relying only on the outer field description.
- When a field is polymorphic, document the discriminator field and subtype rules at both parent and child schema levels.

## OpenAPI And Example Rules

- Swagger examples must match the real request and response contract, including snake_case field names, nullable behavior, and wrapper shape.
- Do not document a raw body shape if the controller actually returns `ApiResponse<T>`.
- Prefer concise but realistic examples that demonstrate required headers, key filters, and domain-specific fields.
- When path variables scope the query, keep the same scope visible in both `@RequestMapping` and `@PostMapping` paths.
- Swagger UI should expose enough detail for frontend implementation without requiring backend code reading.
- Document major failure responses and business error handling expectations in addition to the success case.
- For list responses, include examples that show paging or cursor fields and an empty-result example when that case is meaningful.
- For polymorphic schemas, prefer explicit discriminator documentation over vague free-text descriptions.

## Annotated Templates

Use the following templates as the default starting point when creating new Console or Admin Kotlin APIs that must be understandable from Swagger UI alone.

### Controller.kt Annotated Template

```kotlin
package com.example.console.api

import com.example.api.rest.v1.resource.ResourceGetRequestDto
import com.example.api.rest.v1.resource.ResourceResponseDto
import com.example.one.common.api.rest.ApiResponse
import com.example.one.common.constants.HttpCustomHeaders
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
  name = "리소스",
  description = "프론트엔드가 Swagger UI만 보고도 요청/응답 구조와 예외 처리를 구현할 수 있도록 문서화된 리소스 API"
)
@RestController
@RequestMapping("/api/console/resources")
class ResourceController {

  @Operation(
    summary = "[CONSOLE] 리소스 단건 조회",
    description = "리소스 단건 상세를 조회한다. Swagger UI에 노출된 헤더, 요청 바디, 성공/실패 응답 예시만으로 FE 구현이 가능해야 한다.",
    security = [SecurityRequirement(name = "console-auth")],
    parameters = [
      Parameter(
        name = HttpCustomHeaders.TENANT_ID,
        `in` = ParameterIn.HEADER,
        required = true,
        description = "테넌트 식별 헤더",
        example = "T0001"
      ),
      Parameter(
        name = "product_domain",
        `in` = ParameterIn.PATH,
        required = true,
        description = "도메인 구분값",
        example = "AV_NET"
      )
    ],
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
      required = true,
      description = "조회 조건. 어떤 필드가 필수인지와 null 허용 여부가 Swagger UI에서 명확해야 한다.",
      content = [
        Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = Schema(implementation = ResourceGetRequestDto::class),
          examples = [
            ExampleObject(
              name = "기본 조회",
              value = """
              {
                "resource_id": "res_001",
                "include_history": false
              }
              """
            )
          ]
        )
      ]
    )
  )
  @ApiResponses(
    value = [
      SwaggerApiResponse(
        responseCode = "200",
        description = "성공. 응답 래퍼와 data 구조가 Swagger 예시에서 모두 보여야 한다.",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = ResourceResponseDto::class),
            examples = [
              ExampleObject(
                name = "성공 응답",
                value = """
                {
                  "header": {
                    "is_successful": true,
                    "result_code": 0,
                    "result_message": "SUCCESS"
                  },
                  "body": {
                    "resource_id": "res_001",
                    "name": "기본 리소스",
                    "status": "ACTIVE",
                    "modified_time": "2026-05-26T13:00:00"
                  }
                }
                """
              )
            ]
          )
        ]
      ),
      SwaggerApiResponse(
        responseCode = "200",
        description = "비즈니스 실패. FE가 분기 처리해야 하는 business error code를 식별 가능해야 한다.",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = [
              ExampleObject(
                name = "리소스 없음",
                value = """
                {
                  "header": {
                    "is_successful": false,
                    "result_code": 40401,
                    "result_message": "RESOURCE_NOT_FOUND"
                  },
                  "body": null
                }
                """
              )
            ]
          )
        ]
      )
    ]
  )
  @PostMapping("/{product_domain}/v1")
  fun getResource(
    @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
    @PathVariable("product_domain") productDomain: String,
    @Valid @RequestBody request: ResourceGetRequestDto,
  ): ResponseEntity<ApiResponse<ResourceResponseDto>> {
    throw UnsupportedOperationException()
  }
}
```

### Dto.kt Annotated Template

```kotlin
package com.example.api.rest.v1.resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ResourceGetRequestDto(
  @field:Schema(
    required = true,
    description = "조회 대상 리소스 ID",
    example = "res_001"
  )
  val resourceId: String,

  @field:Schema(
    required = false,
    description = "이력 포함 여부. null이면 false와 동일하게 처리되는지 반드시 문서로 드러나야 한다.",
    example = "false",
    defaultValue = "false"
  )
  val includeHistory: Boolean? = false,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ResourceResponseDto(
  @field:Schema(
    required = true,
    description = "리소스 ID",
    example = "res_001"
  )
  val resourceId: String,

  @field:Schema(
    required = true,
    description = "리소스 표시 이름",
    example = "기본 리소스"
  )
  val name: String,

  @field:Schema(
    required = true,
    description = "리소스 상태",
    allowableValues = ["ACTIVE", "INACTIVE", "DELETED"],
    example = "ACTIVE"
  )
  val status: String,

  @field:Schema(
    required = false,
    description = "마지막 수정 시각. ISO 8601 형식",
    example = "2026-05-26T13:00:00"
  )
  val modifiedTime: String? = null,

  @field:Schema(
    required = false,
    description = "결과 타임아웃. 단위는 초",
    example = "3600"
  )
  val timeoutSeconds: Int? = null,

  @field:Schema(
    required = false,
    description = "동적 속성. 구조가 고정되지 않은 경우에만 사용",
  )
  val metadata: Map<String, Any>? = null,
)

@Schema(
  description = "프론트가 discriminator 값으로 분기 처리해야 하는 polymorphic 필터",
  discriminatorProperty = "type",
  oneOf = [SpecificTargetFilterDto::class, GroupTargetFilterDto::class],
  discriminatorMapping = [
    DiscriminatorMapping(value = "SPECIFIC", schema = SpecificTargetFilterDto::class),
    DiscriminatorMapping(value = "GROUP", schema = GroupTargetFilterDto::class),
  ]
)
sealed interface ResourceTargetFilterDto

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SpecificTargetFilterDto(
  @field:Schema(
    required = true,
    description = "discriminator 값",
    allowableValues = ["SPECIFIC"],
    example = "SPECIFIC"
  )
  val type: String,

  @field:Schema(
    required = true,
    description = "개별 대상 ID",
    example = "node_001"
  )
  val targetId: String,
) : ResourceTargetFilterDto

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GroupTargetFilterDto(
  @field:Schema(
    required = true,
    description = "discriminator 값",
    allowableValues = ["GROUP"],
    example = "GROUP"
  )
  val type: String,

  @field:Schema(
    required = true,
    description = "그룹 ID 목록",
    example = "[100, 200]"
  )
  val groupIds: List<Long>,
) : ResourceTargetFilterDto
```

### Template Usage Notes

- Adjust the response example shape to the real common wrapper used by the target project. If the project exposes `ApiResponse<T>` with `header/body`, keep the example identical to that wrapper.
- Keep request and response examples synchronized with `@JsonNaming`, null omission rules, and DTO defaults.
- Expand the response examples for list APIs to include paging, sort, cursor, and empty-result cases.
- Expand `@ApiResponses` when the frontend must branch on more than one business error code.
- If headers are required for FE integration, ensure they are visible in Swagger UI through `@Parameter`, security schemes, or both.

## Design Examples

Use the examples below as the default shape when the user asks for API design or Spring Boot implementation.

### Admin API Example

Use Admin endpoints under the Admin base path.

- Query: `POST /api/admin/users/v1`
- Create: `POST /api/admin/users/create/v1`
- Update: `POST /api/admin/users/update/v1`
- Delete: `POST /api/admin/users/delete/v1`
- Execute: `POST /api/admin/users/execute/v1`

Spring Boot Kotlin style example:

```kotlin
@RestController
@RequestMapping("/api/admin/users")
class AdminUserController {

  @PostMapping("/v1")
  fun getUsers(
    @Valid @RequestBody request: UserSearchRequestDto,
  ): ResponseEntity<ApiResponse<UserListResponseDto>> {
    throw UnsupportedOperationException()
  }

  @PostMapping("/create/v1")
  fun createUser(
    @Valid @RequestBody request: UserCreateRequestDto,
  ): ResponseEntity<ApiResponse<UserResponseDto>> {
    throw UnsupportedOperationException()
  }
}
```

### Console API Example

Use Console endpoints under the Console base path.

- Query: `POST /api/console/nodes/v1`
- List: `POST /api/console/nodes/list/v1`
- Create: `POST /api/console/nodes/create/v1`
- Update: `POST /api/console/nodes/update/v1`
- Delete: `POST /api/console/nodes/delete/v1`
- Upload: `POST /api/console/nodes/upload/v1`

Spring Boot Kotlin style example:

```kotlin
@Tag(name = "노드")
@RestController
@RequestMapping("/api/console/nodes")
class NodeController {

  @Operation(
    summary = "[CONSOLE] 노드 목록 조회",
    security = [SecurityRequirement(name = "console-auth")],
  )
  @PostMapping("/list/v1")
  fun listNodes(
    @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
    @Valid @RequestBody request: NodeListRequestDto,
  ): ResponseEntity<ApiResponse<NodeListResponseDto>> {
    throw UnsupportedOperationException()
  }

  @PostMapping("/upload/v1")
  fun uploadNodes(
    @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
    @RequestPart("file") file: MultipartFile,
  ): ResponseEntity<ApiResponse<NodeUploadResponseDto>> {
    throw UnsupportedOperationException()
  }
}
```

### Console Scoped Resource Example

- Detail query: `POST /api/console/policy/{product_domain}/v1`
- List query: `POST /api/console/policy/{product_domain}/list/v1`
- Group assignment: `POST /api/console/policy-assignment/group/assign/v1`

Kotlin style example:

```kotlin
@Tag(name = "정책")
@RestController
@RequestMapping("/api/console/policy")
class PolicyController {

  @Operation(
    summary = "[CONSOLE] 정책 단건 조회",
    security = [SecurityRequirement(name = "console-auth")],
  )
  @PostMapping("/{product_domain}/v1")
  fun getPolicy(
    @RequestHeader(HttpCustomHeaders.TENANT_ID) tenantId: String,
    @PathVariable("product_domain") productDomain: String,
    @Valid @RequestBody request: PolicyGetRequestDto,
  ): ResponseEntity<ApiResponse<PolicyDetailResponseDto>> {
    throw UnsupportedOperationException()
  }
}
```

### OpenAPI Example

OpenAPI should follow the external standard first. Do not force the Admin and Console POST-only rule unless the user explicitly asks for it.

- Base pattern: `/api/open-api/{resource}/{version}`
- Example query: `GET /api/open-api/users/v1`
- Example create: `POST /api/open-api/users/v1`
- Example update: `PUT /api/open-api/users/v1/{userId}` when the external standard requires it

When generating OpenAPI YAML, keep the external contract primary and apply the common response envelope only if the target standard requires it.

### Microservice API Example

Use the microservice path shape when the task is internal service-to-service API design.

- Query: `POST /api/v1/platform/account/users`
- Create: `POST /api/v1/platform/account/users/create`

If the user asks for a Microservice endpoint, confirm whether the version belongs at the front as defined by this rule and keep service area and service name explicit in the path.

## What The Agent Should Do

- When asked to design an API, propose paths and methods that follow these conventions first.
- When asked to implement a Spring controller, generate mappings, DTOs, and response structures that match these rules.
- When the surrounding code is Kotlin, prefer Kotlin controller and DTO output without being asked.
- When the surrounding code already uses `ApiResponse`, Swagger annotations, `snake_case`, or header-based tenant resolution, preserve those conventions.
- When a path needs a scoped qualifier such as `/{product_domain}` or a business segment like `/group`, `/node`, `/list`, or `/summary`, keep the URL explicit instead of forcing everything into a single flat resource path.
- When adding or changing APIs, treat Swagger annotation completeness as a deliverable. The target is that frontend developers can implement from Swagger UI alone.
- When Swagger documentation is too weak to support frontend implementation, prefer strengthening annotations, examples, and schema metadata rather than leaving implicit behavior in code only.
- When a generic REST pattern conflicts with these rules, explain the tradeoff briefly but follow these rules by default.
- If the task is for OpenAPI and the team says to follow an external standard, do not force the Admin or Console POST-only conventions unless the user asks for that explicitly.