package com.interviewprep.platform.web.dto;

public record ApiResponse<T>(
        String status,
        boolean error,
        String errorMessage,
        T payload) {

    public static <T> ApiResponse<T> success(T payload) {
        return new ApiResponse<>("SUCCESS", false, null, payload);
    }

    public static ApiResponse<Void> failure(String status, String errorMessage) {
        return new ApiResponse<>(status, true, errorMessage, null);
    }
}
