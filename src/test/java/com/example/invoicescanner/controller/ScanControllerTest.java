package com.example.invoicescanner.controller;


import com.example.invoicescanner.service.PdfScannerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ScanControllerFullTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PdfScannerService pdfScannerService;

    @Test
    void whenIbanFound_shouldReturn400() throws Exception {
        // Mock-Verhalten: Der Service gibt true zurück, wenn eine IBAN gefunden wird.
        given(pdfScannerService.scanForBlacklistedIban(anyString())).willReturn(true);

        mockMvc.perform(get("/api/v1/invoice-scan")
                        .param("url", "http://dummy.com/test.pdf"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.foundBlacklistedIban").value(true))
                .andExpect(jsonPath("$.message").value("Mindestens eine blacklisted IBAN gefunden!"));
    }

    @Test
    void whenNoIbanFound_shouldReturn200() throws Exception {
        // Mock-Verhalten: Der Service gibt false zurück, wenn keine IBAN gefunden wird.
        given(pdfScannerService.scanForBlacklistedIban(anyString())).willReturn(false);

        mockMvc.perform(get("/api/v1/invoice-scan")
                        .param("url", "http://dummy.com/test.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foundBlacklistedIban").value(false))
                .andExpect(jsonPath("$.message").value("Keine blacklisted IBAN gefunden."));
    }

    @Test
    void whenIOExceptionThrown_shouldReturn500() throws Exception {
        // Mock-Verhalten: Bei einem Fehler (z.B. Netzwerkfehler) soll eine IOException ausgelöst werden.
        given(pdfScannerService.scanForBlacklistedIban(anyString())).willThrow(new IOException("Network error"));

        mockMvc.perform(get("/api/v1/invoice-scan")
                        .param("url", "http://dummy.com/test.pdf"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.foundBlacklistedIban").value(false))
                .andExpect(jsonPath("$.message").value("Fehler beim Scannen des PDFs: Network error"));
    }
}