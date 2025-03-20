package com.example.invoicescanner.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScanResultTest {

    @Test
    void testConstructorAndFields() {
        ScanResult result = new ScanResult(true, "IBAN found");

        assertTrue(result.isFoundBlacklistedIban());
        assertEquals("IBAN found", result.getMessage());
    }

    @Test
    void testNoArgsConstructor() {
        ScanResult result = new ScanResult();

        result.setFoundBlacklistedIban(false);
        result.setMessage("No IBAN found");

        assertFalse(result.isFoundBlacklistedIban());
        assertEquals("No IBAN found", result.getMessage());
    }
}
