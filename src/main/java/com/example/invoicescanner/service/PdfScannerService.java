package com.example.invoicescanner.service;

import com.example.invoicescanner.model.ScanResult;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfScannerService {

    // Beispielhafte Blacklist: Enthält hier eine IBAN, die nicht erlaubt ist.
    private static final List<String> BLACKLISTED_IBANS = List.of("DE15 3006 0601 0505 7807 80");

    /**
     * Validiert das PDF von der angegebenen URL. Dabei wird folgendes durchgeführt:
     * - Das PDF wird heruntergeladen und der Text extrahiert.
     * - Es wird geprüft, ob im Text eine blacklisted IBAN enthalten ist.
     * - Es werden zusätzliche Validierungen (Platzhalter) durchgeführt.
     *
     * Folgende Fehlerfälle werden behandelt:
     * - Ist der PDF-Inhalt leer, wird eine ResponseStatusException mit HTTP 500 ausgelöst.
     * - Wird eine unerwünschte IBAN oder ein anderer Validierungsfehler festgestellt,
     *   wird eine ResponseStatusException mit HTTP 400 geworfen.
     * - Andernfalls wird ein ScanResult mit Erfolgsmeldung zurückgegeben (HTTP 200).
     *
     * @param pdfUrl URL des zu validierenden PDFs
     * @return ResponseEntity mit dem ScanResult
     */
    public ResponseEntity<ScanResult> validatePdf(String pdfUrl) {
        String pdfText = downloadPdfAsText(pdfUrl);
        if (pdfText == null || pdfText.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "PDF-Inhalt ist leer.");
        }
        // IBAN-Validierung: Das PDF ist nur gültig, wenn keine blacklisted IBAN enthalten ist.
        boolean ibanValid = !containsBlacklistedIban(pdfText);
        // Weitere Validierungen (Platzhalter) – z.B.:
        // - Prüfung auf Pflichtfelder (Rechnungsnummer, Datum, Betrag)
        // - Überprüfung des Layouts (Header, Footer, Logo-Präsenz)
        // - Validierung digitaler Signaturen
        boolean otherValid = checkForOtherValidations(pdfText);
        if (!ibanValid || !otherValid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Validierung fehlgeschlagen: Blacklisted IBAN oder anderer Validierungsfehler.");
        }
        return ResponseEntity.ok(new ScanResult(false, "PDF ist gültig – keine blacklisted IBAN gefunden."));
    }

    /**
     * Prüft, ob der aus dem PDF extrahierte Text eine blacklisted IBAN enthält.
     *
     * @param pdfText Der extrahierte Text des PDFs
     * @return true, wenn eine blacklisted IBAN gefunden wird, sonst false.
     */
    protected boolean containsBlacklistedIban(String pdfText) {
        return BLACKLISTED_IBANS.stream().anyMatch(pdfText::contains);
    }

    /**
     * Platzhaltermethode für weitere Validierungen.
     * Mögliche zukünftige Erweiterungen:
     * - Prüfung auf Pflichtfelder (Rechnungsnummer, Datum, Betrag)
     * - Überprüfung des Layouts (Header, Footer, Logo-Präsenz)
     * - Validierung digitaler Signaturen
     *
     * @param pdfText Der extrahierte PDF-Text
     * @return true, wenn alle zusätzlichen Validierungen erfolgreich sind (derzeit immer true).
     */
    protected boolean checkForOtherValidations(String pdfText) {
        // Zusätzliche Validierungen können hier hinzugefügt werden.
        return true;
    }

    /**
     * Lädt das PDF von der angegebenen URL herunter und extrahiert den Textinhalt.
     *
     * @param url URL des PDFs
     * @return Der extrahierte Text des PDFs
     * @throws ResponseStatusException, falls beim Download oder der Verarbeitung ein Fehler auftritt.
     */
    protected String downloadPdfAsText(String url) {
        URI uri = URI.create(url);
        try (InputStream is = uri.toURL().openStream();
             BufferedInputStream bis = new BufferedInputStream(is);
             PDDocument document = PDDocument.load(bis)) {

            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Fehler beim Herunterladen oder Verarbeiten des PDFs.", e);
        }
    }
}
