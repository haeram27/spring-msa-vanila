package com.example.springwebex.model.restresp;

import com.example.springwebex.exception.ecode.ErrorCode;
import com.example.springwebex.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({ "error_code", "error_msg", "response" })
public class ResponseJsonDto<T> {

    private static final String SUCCESS_CODE = String.valueOf(ErrorCode.SUCCESS.getCode());

    @JsonProperty("error_code")
    private String errorCode = SUCCESS_CODE;

    @JsonProperty("error_msg")
    private String errorMsg = ErrorCode.SUCCESS.getDefaultMessage();

    @JsonProperty("response")
    private T response;

    public boolean isSuccess() {
        return SUCCESS_CODE.equals(errorCode);
    }

    @Override
    public String toString() {
        String str = "";
        try {
            str = JsonUtil.serialize(this);
        } catch (Exception e) {

        }

        return str;
    }

    public static <T> ResponseJsonDto<T> from(String codeString, String msg) {
        ResponseJsonDto<T> obj = new ResponseJsonDto<T>();
        obj.setErrorCode(codeString);
        obj.setErrorMsg(msg);

        return obj;
    }

    public static <T> ResponseJsonDto<T> from(Exception e) {
        return ResponseJsonDto.from("", e.getMessage());
    }
}