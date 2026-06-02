package template.s3.controller

import template.s3.dto.AppException;
import com.fasterxml.jackson.annotation.JsonIgnore;

public record ApiResponse<T>(String code, String message, T data) {

    public static final String SUCCESS_CODE = "ONE-00000";
    public static final String INTERNAL_ERROR_CODE = "ONE-99999";

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> error(String code) {
        return new ApiResponse<>(code, "Error is occurred.", null);
    }

    public static <T> ApiResponse<T> error(Throwable t) {
        return ApiResponse.error(t instanceof AppException ? ((AppException) t).getErrorCode() : INTERNAL_ERROR_CODE,
                t.getMessage());
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(SUCCESS_CODE, "success", null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(SUCCESS_CODE, "success", data);
    }

    @JsonIgnore
    public boolean isOk() {
        return SUCCESS_CODE.equals(this.code);
    }

    @JsonIgnore
    public boolean isFailed() {
        return !isOk();
    }

}
