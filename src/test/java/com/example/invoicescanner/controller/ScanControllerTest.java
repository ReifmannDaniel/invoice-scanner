package com.example.invoicescanner.controller;

import com.example.invoicescanner.model.ScanResult;
import com.example.invoicescanner.service.PdfScannerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
// In der Testumgebung wird die Fehlermeldung immer mitgeliefert.
@TestPropertySource(properties = { "server.error.include-message=always" })
@Import({ScanControllerTest.TestConfig.class, ScanControllerTest.GlobalExceptionHandler.class})
class ScanControllerTest {

    @TestConfiguration
    static class TestConfig {
        /**
         * Definiert einen Mockito-Mock für den PdfScannerService und markiert ihn als primär,
         * damit dieser den realen Service in den Tests ersetzt.
         */
        @Bean
        @Primary
        public PdfScannerService pdfScannerService() {
            return org.mockito.Mockito.mock(PdfScannerService.class);
        }
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ControllerAdvice
    static class GlobalExceptionHandler {
        /**
         * Fängt ResponseStatusException ab und wandelt sie in ein JSON-Objekt um,
         * das die Felder "status", "error", "message" und "path" enthält.
         *
         * @param ex      Die aufgetretene ResponseStatusException
         * @param request Das aktuelle WebRequest
         * @return ResponseEntity mit den Fehlerattributen
         */
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<Map<String, Object>> handleResponseStatusException(
                ResponseStatusException ex, WebRequest request) {
            Map<String, Object> errorAttributes = new HashMap<>();
            int statusValue = ex.getStatusCode().value();
            errorAttributes.put("status", statusValue);
            HttpStatus status = HttpStatus.resolve(statusValue);
            errorAttributes.put("error", status != null ? status.getReasonPhrase() : "Unknown");
            errorAttributes.put("message", ex.getReason());
            String description = request.getDescription(false);
            String path = (description.startsWith("uri="))
                    ? description.substring(4)
                    : description;
            errorAttributes.put("path", path);
            return new ResponseEntity<>(errorAttributes, ex.getStatusCode());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PdfScannerService pdfScannerService;

    @BeforeEach
    void resetMocks() {
        // Setzt den Mock vor jedem Test zurück, um Interferenzen zwischen den Testfällen zu vermeiden.
        org.mockito.Mockito.reset(pdfScannerService);
    }

    /**
     * Testet, dass wenn der PDF-Text eine blacklisted IBAN enthält,
     * der Service eine ResponseStatusException mit HTTP 400 auslöst.
     */
    @Test
    void whenBlacklistedIbanFound_shouldReturn400() throws Exception {
        given(pdfScannerService.validatePdf(anyString()))
                .willThrow(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Validierung fehlgeschlagen: Blacklisted IBAN oder anderer Validierungsfehler."));

        mockMvc.perform(get("/api/v1/invoice-scan")
                        .param("url", "http://dummy.com/test.pdf"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("Validierung fehlgeschlagen: Blacklisted IBAN oder anderer Validierungsfehler.")))
                .andExpect(jsonPath("$.path").value("/api/v1/invoice-scan"));
    }

    /**
     * Testet, dass wenn beim Herunterladen oder Verarbeiten des PDFs ein Fehler auftritt,
     * der Service eine ResponseStatusException mit HTTP 500 auslöst.
     */
    @Test
    void whenIOExceptionOccurs_shouldReturn500() throws Exception {
        given(pdfScannerService.validatePdf(anyString()))
                .willThrow(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Fehler beim Herunterladen oder Verarbeiten des PDFs."));

        mockMvc.perform(get("/api/v1/invoice-scan")
                        .param("url", "http://dummy.com/test.pdf"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message")
                        .value(containsString("Fehler beim Herunterladen oder Verarbeiten des PDFs.")))
                .andExpect(jsonPath("$.path").value("/api/v1/invoice-scan"));
    }

    /**
     * Testet, dass wenn keine blacklisted IBAN gefunden wird,
     * der Service ein gültiges ScanResult zurückgibt und HTTP 200 liefert.
     */
    @Test
    void whenNoBlacklistedIbanFound_shouldReturn200() throws Exception {
        ScanResult validResult = new ScanResult(false, "PDF ist gültig – keine blacklisted IBAN gefunden.");
        given(pdfScannerService.validatePdf(anyString()))
                .willReturn(ResponseEntity.ok(validResult));

        mockMvc.perform(get("/api/v1/invoice-scan")
                        .param("url", "http://dummy.com/test.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foundBlacklistedIban").value(false))
                .andExpect(jsonPath("$.message").value("PDF ist gültig – keine blacklisted IBAN gefunden."));
    }
}
