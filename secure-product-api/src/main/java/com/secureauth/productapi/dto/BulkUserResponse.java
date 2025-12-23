package com.secureauth.productapi.dto;

import lombok.Data;
import java.util.List;

@Data
public class BulkUserResponse {
    private int totalRecords;
    private int successfulImports;
    private int failedImports;
    private List<String> errors;

    public BulkUserResponse() {
        this.errors = new java.util.ArrayList<>();
    }

    public BulkUserResponse(int totalRecords, int successfulImports, int failedImports, List<String> errors) {
        this.totalRecords = totalRecords;
        this.successfulImports = successfulImports;
        this.failedImports = failedImports;
        this.errors = errors != null ? errors : new java.util.ArrayList<>();
    }
}