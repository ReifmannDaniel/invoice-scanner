package com.example.invoicescanner.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

@Service
public class PdfScannerService {

    // Beispielhafte Blacklist mit IBANs
    private static final List<String> BLACKLISTED_IBANS = List.of(
            "DE15 3006 0601 0505 7807 80"
    );

    /**
     * Lädt das PDF von einer gegebenen URL und durchsucht es nach IBANs,
     * die in unserer BLACKLISTED_IBANS enthalten sind.
     *
     * @param pdfUrl Die URL des PDFs
     * @return true, wenn ein Blacklist-Eintrag gefunden wird, sonst false
     */
    public boolean scanForBlacklistedIban(String pdfUrl) throws IOException {
        String pdfText = downloadPdfAsText(pdfUrl);
        // Wir durchsuchen den gesamten PDF-Text auf alle bekannten Blacklist-IBANs:
        return BLACKLISTED_IBANS.stream().anyMatch(pdfText::contains);
    }

    /**
     * Lädt ein PDF von der übergebenen URL herunter und gibt den extrahierten
     * Text zurück.
     *
     * @param url Das PDF-URL
     * @return Kompletter Textinhalt des PDFs
     * @throws IOException bei Netzwerk- oder Lesefehlern
     */
    private String downloadPdfAsText(String url) throws IOException {
        URI uri = URI.create(url);
        try (InputStream is = uri.toURL().openStream();
             BufferedInputStream bis = new BufferedInputStream(is);
             PDDocument document = PDDocument.load(bis)) {

            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }
}