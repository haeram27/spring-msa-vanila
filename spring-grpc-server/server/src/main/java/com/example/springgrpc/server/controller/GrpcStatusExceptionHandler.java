package com.example.springgrpc.server.controller;

import java.util.Collection;
import java.util.List;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * gRPC 호출 실패를 HTTP 응답으로 번역한다.
 * <p>
 * REST → gRPC 중계 컨트롤러에서 하위 호출이 실패하면 {@link StatusRuntimeException}이 그대로
 * 올라오는데, 핸들러가 없으면 Spring이 무조건 500으로 응답한다. 서버가 {@code INVALID_ARGUMENT}로
 * 정확히 거절해도 호출자에게는 "서버 오류"로 보이므로, gRPC 코드에 맞는 상태로 옮긴다.
 * <p>
 * 코드 대응은 gRPC 공식 HTTP 매핑을 따른다.
 */
@RestControllerAdvice
public class GrpcStatusExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GrpcStatusExceptionHandler.class);

    @ExceptionHandler(StatusRuntimeException.class)
    public ProblemDetail handleGrpcFailure(StatusRuntimeException e) {
        Status status = e.getStatus();
        HttpStatusCode httpStatus = toHttpStatus(status.getCode());

        if (httpStatus.is5xxServerError()) {
            log.error("gRPC call failed with {} -> HTTP {}", status.getCode(), httpStatus.value(), e);
        } else {
            log.warn("gRPC call rejected with {} -> HTTP {}: {}",
                status.getCode(), httpStatus.value(), status.getDescription());
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(httpStatus, detailFor(status, httpStatus));
        problem.setTitle("gRPC call failed");
        problem.setProperty("grpcCode", status.getCode().name());
        return problem;
    }

    /**
     * 요청 본문 역직렬화 실패. Jackson 예외는 {@code IllegalArgumentException}을 거치지 않아
     * 아래 핸들러들에 걸리지 않고, 처리하지 않으면 Spring 기본 400이 상세 없이 나간다.
     * <p>
     * 노출하는 것은 우리가 정의한 본문 필드명과 허용 상수뿐이다. Jackson의
     * {@code getPathReference()}는 DTO의 FQCN을 포함하므로 쓰지 않고 경로를 직접 조립한다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException e) {
        String detail = describeBodyFailure(e);
        log.warn("Request body rejected: {}", detail);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Invalid request");
        return problem;
    }

    private static String describeBodyFailure(HttpMessageNotReadableException e) {
        MismatchedInputException mismatch = findMismatch(e);
        if (mismatch == null) {
            return "Request body is not readable as JSON";
        }

        String field = pathOf(mismatch);
        Class<?> target = mismatch.getTargetType();

        if (mismatch instanceof InvalidFormatException invalid) {
            if (target != null && target.isEnum()) {
                return "%s must be one of %s but was '%s'".formatted(
                    field, List.of(target.getEnumConstants()), invalid.getValue());
            }
            return "%s has an invalid value '%s'".formatted(field, invalid.getValue());
        }
        return "%s has a value of the wrong shape".formatted(field);
    }

    private static MismatchedInputException findMismatch(Throwable e) {
        for (Throwable current = e; current != null; current = current.getCause()) {
            if (current instanceof MismatchedInputException mismatch) {
                return mismatch;
            }
            if (current.getCause() == current) {
                break;
            }
        }
        return null;
    }

    /** {@code status[0]}, {@code pageSize}처럼 본문 안의 위치만 표기한다. */
    private static String pathOf(JacksonException e) {
        StringBuilder path = new StringBuilder();
        for (JacksonException.Reference reference : e.getPath()) {
            String property = reference.getPropertyName();
            if (property != null) {
                if (!path.isEmpty()) {
                    path.append('.');
                }
                path.append(property);
            } else if (reference.getIndex() >= 0) {
                path.append('[').append(reference.getIndex()).append(']');
            }
        }
        return path.isEmpty() ? "request body" : path.toString();
    }

    /**
     * 쿼리 파라미터 바인딩 실패. {@code IllegalArgumentException} 핸들러보다 먼저 잡아야 한다.
     * <p>
     * Spring은 직접 매칭되는 핸들러가 없으면 예외의 cause로 내려가며 다시 찾는데, enum 변환 실패의
     * cause는 {@code Enum.valueOf}가 던진 {@code IllegalArgumentException}이라 아래 핸들러로 흘러가
     * "No enum constant com.example...ListViewEntry.Status.BOGUS" 같은 내부 클래스명이 그대로
     * 응답에 실린다. 여기서 파라미터 이름과 허용값만 담은 메시지로 대체한다.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String detail = describeMismatch(e);
        log.warn("Request rejected: {}", detail);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Invalid request");
        problem.setProperty("parameter", e.getName());
        return problem;
    }

    private static String describeMismatch(MethodArgumentTypeMismatchException e) {
        Class<?> type = elementTypeOf(e);
        if (type != null && type.isEnum()) {
            return "%s must be one of %s but was '%s'".formatted(
                e.getName(), List.of(type.getEnumConstants()), e.getValue());
        }
        return "%s has an invalid value '%s'".formatted(e.getName(), e.getValue());
    }

    /**
     * 파라미터가 실제로 담는 값의 타입. {@code List<Status>}처럼 컨테이너로 선언된 파라미터는
     * {@code getRequiredType()}이 {@code List}를 돌려주므로 원소 타입까지 풀어야 enum인지 알 수 있다.
     */
    private static Class<?> elementTypeOf(MethodArgumentTypeMismatchException e) {
        Class<?> required = e.getRequiredType();
        if (required == null) {
            return null;
        }
        if (required.isArray()) {
            return required.getComponentType();
        }
        if (Collection.class.isAssignableFrom(required) && e.getParameter() != null) {
            Class<?> element = ResolvableType.forMethodParameter(e.getParameter()).asCollection()
                .getGeneric(0).resolve();
            if (element != null) {
                return element;
            }
        }
        return required;
    }

    /**
     * 클라이언트가 넘긴 인자가 잘못된 경우. gRPC 호출 전에 클라이언트 스텁 래퍼가 먼저 걸러내는
     * 경로가 있어(예: 빈 message) 이쪽도 400으로 맞춘다.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidArgument(IllegalArgumentException e) {
        log.warn("Request rejected: {}", e.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("Invalid request");
        return problem;
    }

    /**
     * 서버 오류(5xx)의 description은 내부 예외 메시지를 담을 수 있어 그대로 노출하지 않는다.
     * 상세 내용은 위에서 로그로만 남기고, 응답에는 코드 이름만 실어 보낸다.
     */
    private static String detailFor(Status status, HttpStatusCode httpStatus) {
        if (httpStatus.is5xxServerError()) {
            return "Upstream gRPC call failed (" + status.getCode().name() + ")";
        }
        String description = status.getDescription();
        return description == null || description.isBlank()
            ? status.getCode().name()
            : description;
    }

    /** gRPC {@code Status.Code} → HTTP 상태. enum 전체를 덮으므로 default가 없다. */
    private static HttpStatusCode toHttpStatus(Status.Code code) {
        return switch (code) {
            case OK -> HttpStatus.OK;
            case INVALID_ARGUMENT, FAILED_PRECONDITION, OUT_OF_RANGE -> HttpStatus.BAD_REQUEST;
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ALREADY_EXISTS, ABORTED -> HttpStatus.CONFLICT;
            case RESOURCE_EXHAUSTED -> HttpStatus.TOO_MANY_REQUESTS;
            // 499 Client Closed Request — 표준 코드가 아니라 HttpStatus 상수가 없다.
            case CANCELLED -> HttpStatusCode.valueOf(499);
            case UNIMPLEMENTED -> HttpStatus.NOT_IMPLEMENTED;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case DEADLINE_EXCEEDED -> HttpStatus.GATEWAY_TIMEOUT;
            case UNKNOWN, INTERNAL, DATA_LOSS -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
