package com.example.invoicescanner.service;

import com.example.invoicescanner.model.ScanResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

class PdfScannerServiceTest {

    /**
     * Testet, dass bei leerem PDF-Inhalt eine ResponseStatusException mit HTTP 500 ausgelöst wird.
     */
    @Test
    void testValidatePdf_whenPdfEmpty_throws500() {
        PdfScannerService spyService = Mockito.spy(new PdfScannerService());
        // Simuliere leeren PDF-Text
        doReturn("").when(spyService).downloadPdfAsText(anyString());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                spyService.validatePdf("http://dummy.url/test.pdf")
        );

        assertEquals(500, exception.getStatusCode().value());
        assertEquals("PDF-Inhalt ist leer.", exception.getReason());
    }

    /**
     * Testet, dass wenn der PDF-Text eine blacklisted IBAN enthält,
     * eine ResponseStatusException mit HTTP 400 ausgelöst wird.
     */
    @Test
    void testValidatePdf_whenBlacklistedIbanFound_throws400() {
        PdfScannerService spyService = Mockito.spy(new PdfScannerService());
        // Simuliere, dass der PDF-Text die unerwünschte IBAN enthält
        doReturn("Text mit DE15 3006 0601 0505 7807 80").when(spyService).downloadPdfAsText(anyString());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                spyService.validatePdf("http://dummy.url/test.pdf")
        );
        assertEquals(400, exception.getStatusCode().value());
        assertEquals("Validierung fehlgeschlagen: Blacklisted IBAN oder anderer Validierungsfehler.", exception.getReason());
    }

    /**
     * Testet, dass wenn die zusätzliche Validierung fehlschlägt,
     * eine ResponseStatusException mit HTTP 400 ausgelöst wird.
     */
    @Test
    void testValidatePdf_whenAdditionalValidationFails_throws400() {
        PdfScannerService spyService = Mockito.spy(new PdfScannerService());
        // Simuliere einen PDF-Text ohne blacklisted IBAN
        doReturn("Text ohne unerwünschte IBAN").when(spyService).downloadPdfAsText(anyString());
        // Simuliere, dass die zusätzliche Validierung fehlschlägt
        Mockito.doReturn(false).when(spyService).checkForOtherValidations(anyString());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                spyService.validatePdf("http://dummy.url/test.pdf")
        );
        assertEquals(400, exception.getStatusCode().value());
        assertEquals("Validierung fehlgeschlagen: Blacklisted IBAN oder anderer Validierungsfehler.", exception.getReason());
    }

    /**
     * Testet den erfolgreichen Validierungsfall, bei dem der PDF-Text gültig ist.
     */
    @Test
    void testValidatePdf_whenPdfIsValid_returns200() {
        PdfScannerService spyService = Mockito.spy(new PdfScannerService());
        // Simuliere einen gültigen PDF-Text ohne blacklisted IBAN
        doReturn("Gültiger Text ohne unerwünschte IBAN").when(spyService).downloadPdfAsText(anyString());
        // Simuliere, dass die zusätzlichen Validierungen erfolgreich sind
        Mockito.doReturn(true).when(spyService).checkForOtherValidations(anyString());

        ResponseEntity<ScanResult> response = spyService.validatePdf("http://dummy.url/test.pdf");
        assertEquals(200, response.getStatusCode().value());
        ScanResult result = response.getBody();
        assertNotNull(result);
        assertFalse(result.isFoundBlacklistedIban());
        assertEquals("PDF ist gültig – keine blacklisted IBAN gefunden.", result.getMessage());
    }

    /**
     * Integrationstest: Lädt eine echte PDF-Datei aus dem Dateisystem.
     * Erwartung: Die Test-PDF enthält die blacklisted IBAN.
     */
    @Test
    void testValidatePdf_integrationRealPdf() {
        File testPdf = new File("src/main/resources/pdfs/Testdata_Invoices.pdf");
        assertTrue(testPdf.exists(), "Test-PDF sollte für den Integrationstest vorhanden sein.");
        String fileUrl = testPdf.toURI().toString();

        PdfScannerService service = new PdfScannerService();
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.validatePdf(fileUrl)
        );

        assertEquals(400, exception.getStatusCode().value());
    }
}
