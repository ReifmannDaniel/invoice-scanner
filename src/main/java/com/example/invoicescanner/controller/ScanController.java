package com.example.invoicescanner.controller;

import com.example.invoicescanner.model.ScanResult;
import com.example.invoicescanner.service.PdfScannerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoice-scan")
public class ScanController {

    private final PdfScannerService pdfScannerService;

    public ScanController(PdfScannerService pdfScannerService) {
        this.pdfScannerService = pdfScannerService;
    }

    /**
     * Nimmt eine URL entgegen, lädt das PDF herunter und prüft,
     * ob eine blacklisted IBAN enthalten ist.
     *
     * @param url Die URL des PDFs, das gescannt werden soll
     * @return ScanResult mit Erfolgsmeldung oder Fehler
     */
    @GetMapping
    public ResponseEntity<ScanResult> scanPdfUrl(@RequestParam String url) {
        try {
            boolean foundBlacklisted = pdfScannerService.scanForBlacklistedIban(url);
            if (foundBlacklisted) {
                // Status: 400 oder 409 – je nach Bedarf. Hier bspw. 400 (Bad Request)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ScanResult(true,
                                "Mindestens eine blacklisted IBAN gefunden!"));
            } else {
                return ResponseEntity.ok(new ScanResult(false,
                        "Keine blacklisted IBAN gefunden."));
            }
        } catch (Exception e) {
            // Bei Fehlern (Download fehlgeschlagen, PDF ungültig etc.)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ScanResult(false,
                            "Fehler beim Scannen des PDFs: " + e.getMessage()));
        }
    }
}
