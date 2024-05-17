package org.dspace.app.rest.utils;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

public class FileUtils {

    public static void main(String[] args) throws Exception {
        // Path to the PKCS#12 file and its password
        String password1 = "badssl.com";
        char[] password = password1.toCharArray();
        X509Certificate cert = null;
        PrivateKey privateKey = null;
        Boolean showSignature = true;
        String reason = "sd";
        String location = "sd";
        String name = "sd";
        Integer pageNumber = 1;
        String pkcs12FilePath = "D://badssl.com-client.p12";
        File pdf1 = new File("D://dummy.pdf");
        // Load the PKCS#12 file
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream(pkcs12FilePath), password);
        // Get the private key and certificate from the keystore
        String alias = keystore.aliases().nextElement();
        if (!keystore.isKeyEntry(alias)) {
            throw new IllegalArgumentException("The provided PKCS12 file does not contain a private key.");
        }
        System.out.println("THE PROVIDE PKCS12 done");
        privateKey = (PrivateKey) keystore.getKey(alias, password);
        cert = (X509Certificate) keystore.getCertificate(alias);
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE); // default filter
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_SHA1);
        signature.setName("aSHIVN");
        signature.setLocation("SsdsdD");
        signature.setReason("reason");
        signature.setSignDate(Calendar.getInstance());
        try (PDDocument document = PDDocument.load(pdf1)) {
            //  logger.info("Successfully loaded the provided PDF");
            SignatureOptions signatureOptions = new SignatureOptions();

            // If you want to show the signature

            // ATTEMPT 2
            if (showSignature != null && showSignature) {
                PDPage page = document.getPage(pageNumber - 1);

                PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
                if (acroForm == null) {
                    acroForm = new PDAcroForm(document);
                    document.getDocumentCatalog().setAcroForm(acroForm);
                }

                // Create a new signature field and widget

                PDSignatureField signatureField = new PDSignatureField(acroForm);
                PDAnnotationWidget widget = signatureField.getWidgets().get(0);
                PDRectangle rect = new PDRectangle(400, 400, 300, 50); // Define the rectangle size here
                widget.setRectangle(rect);
                page.getAnnotations().add(widget);

// Set the appearance for the signature field
                PDAppearanceDictionary appearanceDict = new PDAppearanceDictionary();
                PDAppearanceStream appearanceStream = new PDAppearanceStream(document);
                appearanceStream.setResources(new PDResources());
                appearanceStream.setBBox(rect);
                appearanceDict.setNormalAppearance(appearanceStream);
                widget.setAppearance(appearanceDict);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, appearanceStream)) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(410, 430);
                    //contentStream.newLineAtOffset(110, 130);
                    contentStream.showText("Digitally signed by: " + (name != null ? name : "Unknown"));
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Date: " + new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z").format(new Date()));
                    contentStream.newLineAtOffset(0, -15);
                    if (reason != null && !reason.isEmpty()) {
                        contentStream.showText("Reason: " + reason);
                        contentStream.newLineAtOffset(0, -15);
                    }
                    if (location != null && !location.isEmpty()) {
                        contentStream.showText("Location: " + location);
                        contentStream.newLineAtOffset(0, -15);
                    }
                    contentStream.endText();
                }

                // Add the widget annotation to the page
                page.getAnnotations().add(widget);

                // Add the signature field to the acroform
                acroForm.getFields().add(signatureField);

                // Handle multiple signatures by ensuring a unique field name
                String baseFieldName = "Signature";
                String signatureFieldName = baseFieldName;
                int suffix = 1;
                while (acroForm.getField(signatureFieldName) != null) {
                    suffix++;
                    signatureFieldName = baseFieldName + suffix;
                }
                signatureField.setPartialName(signatureFieldName);
            }

            document.addSignature(signature, signatureOptions);
            //  logger.info("Signature added to the PDF document");
            // External signing
            ExternalSigningSupport externalSigning = document
                    .saveIncrementalForExternalSigning(new ByteArrayOutputStream());

            byte[] content = IOUtils.toByteArray(externalSigning.getContent());

            // Using BouncyCastle to sign
            CMSTypedData cmsData = new CMSProcessableByteArray(content);

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            BouncyCastleProvider d = new BouncyCastleProvider();
            d.getProperty(BouncyCastleProvider.PROVIDER_NAME);
            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(privateKey);

            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build())
                    .build(signer, cert));

            gen.addCertificates(new JcaCertStore(Collections.singletonList(cert)));
            CMSSignedData signedData = gen.generate(cmsData, false);

            byte[] cmsSignature = signedData.getEncoded();
            // logger.info("About to sign content using BouncyCastle");
            externalSigning.setSignature(cmsSignature);
            //logger.info("Signature set successfully");

            // After setting the signature, return the resultant PDF
            try (ByteArrayOutputStream signedPdfOutput = new ByteArrayOutputStream()) {
                document.save(signedPdfOutput);
                //document.save("D://"+pdf.getOriginalFilename()+".pdf");
                boasToWebResponse(signedPdfOutput, "D://output_1.pdf");

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getNameWithoutExtension(String file) {
        int dotIndex = file.lastIndexOf('.');
        return (dotIndex == -1) ? file : file.substring(0, dotIndex);
    }

    public static int getPageCountInPDF(InputStream inputStream) {
        int pageCount = 0;
        try {
            PDDocument document = PDDocument.load(inputStream);
            // Get the number of pages in the PDF document
            pageCount = document.getNumberOfPages();
            return pageCount;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pageCount;
    }

    public static Optional<String> getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    public static ResponseEntity<byte[]> boasToWebResponse(ByteArrayOutputStream baos, String docName) throws IOException {
        return bytesToWebResponse(baos.toByteArray(), docName);
    }

    public static ResponseEntity<byte[]> boasToWebResponse(ByteArrayOutputStream baos, String docName, MediaType mediaType) throws IOException {
        return bytesToWebResponse(baos.toByteArray(), docName, mediaType);
    }


    public static ResponseEntity<byte[]> multiPartFileToWebResponse(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        MediaType mediaType = MediaType.parseMediaType(file.getContentType());

        byte[] bytes = file.getBytes();

        return bytesToWebResponse(bytes, fileName, mediaType);
    }

    public static ResponseEntity<byte[]> bytesToWebResponse(byte[] bytes, String docName, MediaType mediaType) throws IOException {

        // Return the PDF as a response
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentLength(bytes.length);
        String encodedDocName = URLEncoder.encode(docName, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
        headers.setContentDispositionFormData("attachment", encodedDocName);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    public static ResponseEntity<byte[]> bytesToWebResponse(byte[] bytes, String docName) throws IOException {
        return bytesToWebResponse(bytes, docName, MediaType.APPLICATION_PDF);
    }

    public static ResponseEntity<byte[]> pdfDocToWebResponse(PDDocument document, String docName) throws IOException {

        // Open Byte Array and save document to it
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        // Close the document
        document.close();

        return boasToWebResponse(baos, docName);
    }
}
