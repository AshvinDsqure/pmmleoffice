package org.dspace.app.rest.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.dspace.app.rest.model.DigitalSignRequet;

public class PDFTextSearch {

    final static String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");

    public static void main(String[] args) throws IOException {

        List<DigitalSignRequet> digitalSignRequets = new ArrayList<>();
        DigitalSignRequet o1 = new DigitalSignRequet();
        o1.setName("Ashivn");
        o1.setPassword("dspace123");
        o1.setP12FileName("D://PATNAHIGHCOURT.p12");
        o1.setFileInputName("D://d1.pdf");
        o1.setReason("Bhihar.");
        o1.setLocation("India");
        o1.setCertType("Note# 1");

        DigitalSignRequet o2 = new DigitalSignRequet();
        o2.setName("ddddddddd");
        o2.setPassword("badssl.com");
        o2.setP12FileName("D://DemoDoc//badssl.com-client.p12");
        o2.setFileInputName("D://d1.pdf");
        o2.setReason("Bhihar.");
        o2.setLocation("India");
        o2.setCertType("Note# 2");
        digitalSignRequets.add(o1);
        digitalSignRequets.add(o2);
        getSingDoc("D://d1.pdf",digitalSignRequets);
    }
public static File getSingDoc(String docfathe,List<DigitalSignRequet> digitalSignRequets) throws IOException {
    File tempFileDoc=null;
    int i = 1;
    String filePath = "";
    for (DigitalSignRequet obj : digitalSignRequets) {
        if (i == 1) {
            obj.setFileInputName(docfathe);
            tempFileDoc = new File(TEMP_DIRECTORY, "Note#" + i + ".pdf");
            filePath = tempFileDoc.getAbsolutePath();
        } else {
            obj.setFileInputName(filePath);
            tempFileDoc = new File(TEMP_DIRECTORY, "Note#" + i + ".pdf");
            filePath = tempFileDoc.getAbsolutePath();
        }
        System.out.println("file path::::output:::::::::::"+filePath);
        System.out.println("::obj.getCertType():::::::::::"+obj.getCertType());
        PDFLastLineStripper stripper1 = new PDFLastLineStripper();
        stripper1.searchTerm = obj.getCertType();
        loadPDF(obj.getFileInputName(), stripper1);
        System.out.println("x:::" + stripper1.getX());
        System.out.println("y:::" + stripper1.getY());
        obj.setxCordinate(stripper1.getX());
        obj.setyCordinate(stripper1.getY());
        DigitalSingPDF.AddDigitalSignClient(filePath, obj);
        i++;
    }
    System.out.println("::::::::::filePath::::::::::"+filePath);
  return tempFileDoc;
}


    public static void loadPDF(String filePath, PDFLastLineStripper stripper1) {
        System.out.println("filePath:::::::::" + filePath);
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            // Process each page
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                System.out.println(" page " + page);
                stripper1.setStartPage(page + 1);
                stripper1.setEndPage(page + 1);
                stripper1.getText(document);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class PDFLastLineStripper extends PDFTextStripper {
        float x = 0;
        float y = 0;

        public PDFLastLineStripper() throws IOException {
            super();
        }

        String searchTerm = "";

        public float getY() {
            return y;
        }

        public float getX() {
            return x;
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            super.writeString(text, textPositions);
            // Extracting the last line coordinates
            if (!text.isEmpty()) {
               // System.out.println(":::searchTerm::::" + this.searchTerm);
                //System.out.println(":::text.trim()::::" + text.trim());
                if (text.trim().contains(searchTerm)) {
                    TextPosition lastPosition = textPositions.get(textPositions.size() - 1);
                    TextPosition firstPosition = textPositions.get(0);
                    float startX = firstPosition.getXDirAdj();
                    float startY = firstPosition.getYDirAdj();
                    float endX = lastPosition.getEndX();
                    float endY = lastPosition.getEndY();
                    // Adjust as needed for your specific use case
                   // System.out.println(text.trim() + " Coordinates:");
                   // System.out.println("Start X: " + startX + ", Start Y: " + startY);
                  //  System.out.println("End X: " + endX + ", End Y: " + endY);
                  //  System.out.println("Text: " + text.trim()); // Trim to remove leading/trailing whitespace
                    System.out.println();
                    this.x = startX;
                    this.y = endY;
                }
            }
        }
    }
}
