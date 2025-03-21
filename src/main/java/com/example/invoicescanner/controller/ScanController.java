package com.example.invoicescanner.controller;

import com.example.invoicescanner.model.ScanResult;
import com.example.invoicescanner.service.PdfScannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoice-scan")
@RequiredArgsConstructor
public class ScanController {

    private final PdfScannerService pdfScannerService;

    /**
     * Endpunkt, der eine URL als Request-Parameter entgegennimmt und das PDF validiert.
     *
     * @param url URL des PDFs, das gescannt werden soll.
     * @return ResponseEntity mit dem ScanResult und passendem HTTP-Status.
     */
    @GetMapping
    public ResponseEntity<ScanResult> scanPdfUrl(@RequestParam String url) {
        return pdfScannerService.validatePdf(url);
    }
}
