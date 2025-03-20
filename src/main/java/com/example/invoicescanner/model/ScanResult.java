package com.example.invoicescanner.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ScanResult {
    private boolean foundBlacklistedIban;
    private String message;

    public ScanResult() {
    }

    public ScanResult(boolean foundBlacklistedIban, String message) {
        this.foundBlacklistedIban = foundBlacklistedIban;
        this.message = message;
    }

}
