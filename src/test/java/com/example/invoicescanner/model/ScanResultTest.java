package com.example.invoicescanner.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScanResultTest {

    /**
     * Testet den All-Args-Konstruktor und die Getter-Methoden.
     *
     * Es wird ein ScanResult-Objekt erstellt, das anzeigt, dass eine IBAN gefunden wurde,
     * und anschließend wird überprüft, ob die Felder korrekt gesetzt sind.
     */
    @Test
    void testConstructorAndFields() {
        ScanResult result = new ScanResult(true, "IBAN found");

        assertTrue(result.isFoundBlacklistedIban());
        assertEquals("IBAN found", result.getMessage());
    }

    /**
     * Testet den No-Args-Konstruktor und die Setter-Methoden.
     *
     * Zuerst wird ein leeres ScanResult-Objekt erstellt. Anschließend werden die Felder
     * über die Setter-Methoden gesetzt, und zum Schluss wird überprüft, ob die Werte korrekt
     * übernommen wurden.
     */
    @Test
    void testNoArgsConstructor() {
        ScanResult result = new ScanResult();

        result.setFoundBlacklistedIban(false);
        result.setMessage("No IBAN found");

        assertFalse(result.isFoundBlacklistedIban());
        assertEquals("No IBAN found", result.getMessage());
    }
}
