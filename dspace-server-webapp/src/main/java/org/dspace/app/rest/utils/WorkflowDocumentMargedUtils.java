/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;



public class WorkflowDocumentMargedUtils {
    public static void main(String[] args) throws IOException {
        convertDocxToPdf("D://doc1.docx","D://doc_s.pdf");

    }
    public static void ConvertToHTML(String docPath, String htmlPath) {

    }

    public static void  convertDocxToPdf(String inputDocxPath, String outputPdfPath) throws IOException {

        String inputDocxFilePath = "D://a.docx";
        String outputMhtmlFilePath = "D://output_h.mhtml";

        try (FileInputStream fis = new FileInputStream(inputDocxFilePath);
             FileOutputStream fos = new FileOutputStream(outputMhtmlFilePath)) {

            XWPFDocument doc = new XWPFDocument(fis);

            // Create an MHTML header
            StringBuilder mhtmlContent = new StringBuilder();
            mhtmlContent.append("Content-Type: multipart/related; boundary=\"boundary\"\n\n");

            // Convert each paragraph in the DOCX to MHTML
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                mhtmlContent.append("--boundary\n");
                mhtmlContent.append("Content-Type: text/html\n\n");
                mhtmlContent.append(paragraph.getParagraphText()).append("\n\n");
            }

            // Add boundary closing
            mhtmlContent.append("--boundary--");

            // Write the MHTML content to the output file
            fos.write(mhtmlContent.toString().getBytes());

            System.out.println("DOCX to MHTML conversion complete.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copyInInputStreamToDocx(InputStream inputStream, String outputpathe) throws IOException {
        Files.copy(inputStream, Paths.get(outputpathe), StandardCopyOption.REPLACE_EXISTING);
        inputStream.close();
    }

    public static void copyInputStreamToPdf(InputStream inputStream, String outputFilePath) throws IOException {
        try (PDDocument doc = PDDocument.load(inputStream)) {
            doc.save(outputFilePath);
        }
    }


}
