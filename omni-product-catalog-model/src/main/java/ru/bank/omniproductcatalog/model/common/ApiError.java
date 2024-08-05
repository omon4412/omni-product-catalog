package ru.bank.omniproductcatalog.model.common;

import java.time.Instant;
import java.util.Objects;

public class ApiError {
    private Instant timestamp;
    private String path;
    private int status;
    private String error;
    private String requestId;

    public ApiError(Instant timestamp, String path, int status, String error, String requestId) {
        this.timestamp = timestamp;
        this.path = path;
        this.status = status;
        this.error = error;
        this.requestId = requestId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiError apiError = (ApiError) o;
        return Objects.equals(timestamp, apiError.timestamp) && Objects.equals(path, apiError.path) && Objects.equals(status, apiError.status) && Objects.equals(error, apiError.error) && Objects.equals(requestId, apiError.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, path, status, error, requestId);
    }
}
