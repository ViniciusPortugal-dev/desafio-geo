package dev.challenge.common.error;

public record ApiError(
        int status,
        String error,
        String message,
        String path,
        Object details
) {
    public static ApiError of(int status, String error, String message, String path, Object details) {
        return new ApiError(status, error, message, path, details);
    }
}
