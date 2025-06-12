package com.finance.admin.certificate.service;

import com.finance.admin.certificate.model.Certificate;
import com.finance.admin.certificate.model.CertificateTemplate;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Real PDF Generation Service using iText library
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationService {

    @Autowired
    private CloudStorageService cloudStorageService;

    @Autowired
    private DigitalSignatureService digitalSignatureService;

    /**
     * Generate PDF certificate from template and certificate data
     */
    public CertificateGenerationResult generateCertificatePdf(Certificate certificate, CertificateTemplate template) {
        log.info("Generating PDF certificate for certificate number: {}", certificate.getCertificateNumber());

        try {
            // Generate PDF content using iText
            byte[] pdfContent = generateRealPdfContent(certificate, template);

            // Calculate file hash for integrity
            String fileHash = calculateFileHash(pdfContent);

            // Generate digital signature
            String digitalSignature = digitalSignatureService.signDocument(pdfContent, certificate.getCertificateNumber());

            // Generate file name
            String fileName = generateFileName(certificate);

            // Upload to cloud storage
            Map<String, String> metadata = new HashMap<>();
            metadata.put("certificate-number", certificate.getCertificateNumber());
            metadata.put("client-id", certificate.getClient().getId().toString());
            metadata.put("certificate-type", certificate.getCertificateType().toString());
            metadata.put("file-hash", fileHash);
            metadata.put("digital-signature", digitalSignature);

            String fileKey = cloudStorageService.uploadFile(fileName, pdfContent, "application/pdf", metadata);

            log.info("PDF certificate generated and uploaded successfully: {}", fileName);

            return CertificateGenerationResult.builder()
                .success(true)
                .filePath(fileKey)
                .fileName(fileName)
                .fileSize((long) pdfContent.length)
                .fileHash(fileHash)
                .digitalSignature(digitalSignature)
                .generatedAt(LocalDateTime.now())
                .build();

        } catch (Exception e) {
            log.error("Failed to generate PDF certificate for certificate number: {}", 
                certificate.getCertificateNumber(), e);
            
            return CertificateGenerationResult.builder()
                .success(false)
                .errorMessage("Failed to generate PDF: " + e.getMessage())
                .generatedAt(LocalDateTime.now())
                .build();
        }
    }

    /**
     * Generate batch certificates
     */
    public Map<String, CertificateGenerationResult> generateBatchCertificates(
            Map<Certificate, CertificateTemplate> certificateTemplateMap) {
        
        log.info("Generating batch certificates for {} certificates", certificateTemplateMap.size());
        
        Map<String, CertificateGenerationResult> results = new HashMap<>();
        
        for (Map.Entry<Certificate, CertificateTemplate> entry : certificateTemplateMap.entrySet()) {
            Certificate certificate = entry.getKey();
            CertificateTemplate template = entry.getValue();
            
            CertificateGenerationResult result = generateCertificatePdf(certificate, template);
            results.put(certificate.getCertificateNumber(), result);
        }
        
        log.info("Batch certificate generation completed. Success: {}, Failed: {}", 
            results.values().stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum(),
            results.values().stream().mapToLong(r -> r.isSuccess() ? 0 : 1).sum());
        
        return results;
    }

    /**
     * Generate real PDF content using iText library
     */
    private byte[] generateRealPdfContent(Certificate certificate, CertificateTemplate template) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {

            // Set up fonts
            PdfFont titleFont = PdfFontFactory.createFont();
            PdfFont headerFont = PdfFontFactory.createFont();
            PdfFont bodyFont = PdfFontFactory.createFont();

            // Add title
            Paragraph title = new Paragraph("E-SHARE CERTIFICATE")
                .setFont(titleFont)
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.BLUE)
                .setMarginBottom(20);
            document.add(title);

            // Add certificate number
            Paragraph certNumber = new Paragraph("Certificate Number: " + certificate.getCertificateNumber())
                .setFont(headerFont)
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
            document.add(certNumber);

            // Add certificate details table
            Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

            // Add certificate information
            addTableRow(detailsTable, "Certificate Type:", certificate.getCertificateType().getDisplayName(), headerFont, bodyFont);
            addTableRow(detailsTable, "Issue Date:", certificate.getIssueDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), headerFont, bodyFont);
            addTableRow(detailsTable, "Expiry Date:", certificate.getExpiryDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), headerFont, bodyFont);

            // Add client information
            if (certificate.getClient() != null) {
                addTableRow(detailsTable, "Client Name:", certificate.getClient().getFirstName() + " " + certificate.getClient().getLastName(), headerFont, bodyFont);
                addTableRow(detailsTable, "Membership Number:", certificate.getClient().getMembershipNumber(), headerFont, bodyFont);
                addTableRow(detailsTable, "Email:", certificate.getClient().getEmailPrimary(), headerFont, bodyFont);
            }

            // Add investment information
            if (certificate.getInvestment() != null) {
                addTableRow(detailsTable, "Investment Name:", certificate.getInvestment().getInvestmentName(), headerFont, bodyFont);
                addTableRow(detailsTable, "Investment Type:", certificate.getInvestment().getInvestmentType().toString(), headerFont, bodyFont);
            }
            
            addTableRow(detailsTable, "Investment Amount:", String.format("$%.2f", certificate.getInvestmentAmount()), headerFont, bodyFont);
            addTableRow(detailsTable, "Number of Shares:", certificate.getNumberOfShares().toString(), headerFont, bodyFont);
            addTableRow(detailsTable, "Share Price:", String.format("$%.2f", certificate.getSharePrice()), headerFont, bodyFont);

            document.add(detailsTable);

            // Add certificate statement
            Paragraph statement = new Paragraph()
                .setFont(bodyFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginTop(30)
                .setMarginBottom(30);

            statement.add("This is to certify that ");
            statement.add(certificate.getClient().getFirstName() + " " + certificate.getClient().getLastName());
            statement.add(" is the registered holder of ");
            statement.add(certificate.getNumberOfShares().toString());
            statement.add(" shares in ");
            statement.add(certificate.getInvestment().getInvestmentName());
            statement.add(" with a total investment value of ");
            statement.add(String.format("$%.2f", certificate.getInvestmentAmount()));
            statement.add(".");

            document.add(statement);

            // Add company information
            Paragraph companyInfo = new Paragraph()
                .setFont(bodyFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(50);

            companyInfo.add("LifeTech Financial Services\n")
                .add("123 Financial District, Sydney NSW 2000\n")
                .add("Phone: +61 2 9999 8888 | Email: info@lifetech.com.au\n")
                .add("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' HH:mm")));

            document.add(companyInfo);

            // Add digital signature placeholder
            Paragraph signature = new Paragraph("Digitally Signed")
                .setFont(bodyFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(30)
                .setFontColor(ColorConstants.GRAY);

            document.add(signature);
        }

        return baos.toByteArray();
    }

    private void addTableRow(Table table, String label, String value, PdfFont labelFont, PdfFont valueFont) {
        table.addCell(new Paragraph(label).setFont(labelFont).setBold().setFontSize(10));
        table.addCell(new Paragraph(value).setFont(valueFont).setFontSize(10));
    }

    private String generateFileName(Certificate certificate) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("certificate_%s_%s.pdf", certificate.getCertificateNumber(), timestamp);
    }

    private String calculateFileHash(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to calculate file hash", e);
            return "hash_calculation_failed";
        }
    }

    public boolean verifyCertificateIntegrity(String fileKey, String expectedHash) {
        try {
            byte[] fileContent = cloudStorageService.downloadFile(fileKey);
            String actualHash = calculateFileHash(fileContent);
            return expectedHash.equals(actualHash);
        } catch (Exception e) {
            log.error("Failed to verify certificate integrity for file: {}", fileKey, e);
            return false;
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CertificateGenerationResult {
        private boolean success;
        private String filePath;
        private String fileName;
        private Long fileSize;
        private String fileHash;
        private String digitalSignature;
        private String errorMessage;
        private LocalDateTime generatedAt;
    }
} 