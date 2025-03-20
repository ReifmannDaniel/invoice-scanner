package com.example.invoicescanner.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

class PdfScannerServiceTest {

    @Test
    void testScanForBlacklistedIban_whenBlacklistedIbanFound_returnsTrue() throws IOException {
        PdfScannerService spyService = Mockito.spy(new PdfScannerService());

        Mockito.doReturn("Some text containing DE15 3006 0601 0505 7807 80 which is blacklisted")
                .when(spyService)
                .downloadPdfAsText(anyString());

        boolean found = spyService.scanForBlacklistedIban("http://dummy.url/test.pdf");

        assertTrue(found, "Expected IBAN to be found in the text, so result = true");
    }

    @Test
    void testScanForBlacklistedIban_whenNoBlacklistedIban_returnsFalse() throws IOException {
        PdfScannerService spyService = Mockito.spy(new PdfScannerService());
        Mockito.doReturn("Completely IBAN-free text").when(spyService).downloadPdfAsText(anyString());

        boolean found = spyService.scanForBlacklistedIban("http://dummy.url/test.pdf");

        assertFalse(found, "Expected no blacklisted IBAN found => false");
    }

    @Test
    void testScanForBlacklistedIban_whenIOExceptionThrown_propagatesException() throws IOException {
        PdfScannerService spyService = Mockito.spy(new PdfScannerService());
        Mockito.doThrow(new IOException("Network error")).when(spyService).downloadPdfAsText(anyString());

        assertThrows(IOException.class, () -> {
            spyService.scanForBlacklistedIban("http://dummy.url/test.pdf");
        });
    }

    // Integrationstest
    @Test
    void testScanForBlacklistedIban_integrationRealPdf() throws IOException {
        File testPdf = new File("src/main/resources/pdfs/Testdata_Invoices.pdf");
        assertTrue(testPdf.exists(), "Test-PDF should exist for integration test!");

        String fileUrl = testPdf.toURI().toString();

        PdfScannerService service = new PdfScannerService();
        boolean result = service.scanForBlacklistedIban(fileUrl);

        assertTrue(result, "Expected the PDF to contain a blacklisted IBAN!");
    }
}
