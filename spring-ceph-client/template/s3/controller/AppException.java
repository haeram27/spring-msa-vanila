package template.s3.controller

public class AppException extends RuntimeException {

    private final String errorCode;

    public AppException(String errorCode, String message, Throwable cause) {
        super(String.format("[%s] %s", errorCode, message), cause);
        this.errorCode = errorCode;
    }

    public AppException(String errorCode, String message) {
        super(String.format("[%s] %s", errorCode, message));
        this.errorCode = errorCode;
    }

    public AppException(String errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public AppException(String errorCode, String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(String.format("[%s] %s", errorCode, message), cause, enableSuppression, writableStackTrace);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

}
