package com.example.springwebex.util;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"error_code", "error_msg", "revision", "response"})
public class ResponseJsonObject {

    private static final String SUCCESS_CODE = String.valueOf(ErrorCode.SUCCESS.getCode());

    @JsonProperty("error_code")
    private String errorCode = SUCCESS_CODE;

    @JsonProperty("error_msg")
    private String errorMsg = ErrorCode.SUCCESS.getDefaultMessage();

    @JsonProperty("revision")
    private int revision;

    @JsonProperty("response")
    private List<Map<String, Object>> response = null;

    @JsonIgnore
    public String getErrorCode() {
        return errorCode;
    }

    @JsonIgnore
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @JsonIgnore
    public String getErrorMsg() {
        return errorMsg;
    }

    @JsonIgnore
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @JsonIgnore
    public int getRevision() {
        return revision;
    }

    @JsonIgnore
    public void setRevision(int revision) {
        this.revision = revision;
    }

    @JsonIgnore
    public List<Map<String, Object>> getResponse() {
        return response;
    }

    @JsonIgnore
    public void setResponse(List<Map<String, Object>> response) {
        this.response = response;
    }

    @JsonIgnore
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

    public static ResponseJsonObject from(String codeString, String msg) {
        ResponseJsonObject obj = new ResponseJsonObject();
        obj.setErrorCode(codeString);
        obj.setErrorMsg(msg);

        return obj;
    }

    public static ResponseJsonObject from(Exception e) {
        return ResponseJsonObject.from("", e.getMessage());
    }
}
