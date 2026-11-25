package com.mafioso.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standard JSON envelope used across authentication and authorization endpoints.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String error;
    private Map<String, String> fieldErrors;

    public ApiResponse() {
    }

    private ApiResponse(boolean success,
                        String message,
                        T data,
                        String error,
                        Map<String, String> fieldErrors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
        this.fieldErrors = fieldErrors == null ? null : Collections.unmodifiableMap(new LinkedHashMap<String, String>(fieldErrors));
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<T>(true, null, data, null, null);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<T>(true, message, data, null, null);
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<T>(false, message, null, errorCode, null);
    }

    public static <T> ApiResponse<T> validationError(String message, Map<String, String> fieldErrors) {
        return new ApiResponse<T>(false, message, null, "VALIDATION_ERROR", fieldErrors);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
