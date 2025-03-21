package com.example.invoicescanner.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datenmodell für das Ergebnis eines PDF-Scans.
 * Enthält ein Flag, ob eine unerwünschte (blacklisted) IBAN gefunden wurde,
 * und eine entsprechende Nachricht.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanResult {
    private boolean foundBlacklistedIban;
    private String message;
}
