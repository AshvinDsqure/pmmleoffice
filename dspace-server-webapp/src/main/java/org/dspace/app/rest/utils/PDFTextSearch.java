package org.dspace.app.rest.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.dspace.app.rest.model.DigitalSignRequet;

public class PDFTextSearch {

    final static String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");

    public static void main(String[] args) throws IOException {

        int no=1;
        PDFLastLineStripper stripper1 = new PDFLastLineStripper();
        int pageno= getPageNumberByText(no,"D://approvaldraft.pdf");
        System.out.println("page>>>>>>>"+pageno);
        stripper1.searchTerm = "Signature_"+no+"_Name:";
        loadPDF("D://approvaldraft.pdf", stripper1,pageno);
        System.out.println("cordinate::::::" + stripper1.getCoordinates());

//        List<DigitalSignRequet> digitalSignRequets = new ArrayList<>();
//        DigitalSignRequet o1 = new DigitalSignRequet();
//        o1.setName("Ashivn");
//        o1.setPassword("dspace123");
//        o1.setP12FileName("D://PATNAHIGHCOURT.p12");
//        o1.setFileInputName("D://d1.pdf");
//        o1.setReason("Bhihar.");
//        o1.setLocation("India");
//        o1.setCertType("Note# 1");
//
//        DigitalSignRequet o2 = new DigitalSignRequet();
//        o2.setName("ddddddddd");
//        o2.setPassword("badssl.com");
//        o2.setP12FileName("D://DemoDoc//badssl.com-client.p12");
//        o2.setFileInputName("D://d1.pdf");
//        o2.setReason("Bhihar.");
//        o2.setLocation("India");
//        o2.setCertType("Note# 2");
//        digitalSignRequets.add(o1);
//        digitalSignRequets.add(o2);
//        getSingDoc("D://d1.pdf",digitalSignRequets);
    }

    public static int  getPageNumberByText(int index,String pdfpath){
        int no = index;  // Change this to the desired occurrence number (e.g., 2nd occurrence)
        Map<Integer, Integer> map = countOccurrencesPageWise(pdfpath, "Signature");
        System.out.println("Occurrences page-wise: " + map);
        int page = -1;  // Initialize to -1 to indicate not found
        int cumulativeOccurrences = 0;  // Track cumulative occurrences across pages
        // Iterate through the map
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            int currentPage = entry.getKey();  // Current page
            int occurrencesOnPage = entry.getValue();  // Occurrences on this page
            cumulativeOccurrences += occurrencesOnPage;  // Add occurrences to cumulative count
            // Check if the cumulative occurrences have reached or exceeded the desired 'no'
            if (cumulativeOccurrences >= no) {
                page = currentPage;  // The page where the 'no'th occurrence is found
                break;  // Break the loop since we found the desired page
            }
        }
        if (page != -1) {
            System.out.println("The " + no + "th occurrence of the search term is on page: " + page);
        } else {
            System.out.println("The search term doesn't occur " + no + " times in the document.");
        }
        return page-1;
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
        loadPDF(obj.getFileInputName(), stripper1,0);
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
    public  static String getCordinate(String path,String serchtext,int page) throws IOException {
        System.out.println(":::::::::::in getCordinate:::::::::::");
        PDFTextSearch.PDFLastLineStripper stripper1 = new PDFTextSearch.PDFLastLineStripper();
        stripper1.searchTerm = serchtext;
        loadPDF(path, stripper1,page);
        return stripper1.getCoordinates();
    }
    public  static String getCordinate(String path,String serchtext) throws IOException {
        System.out.println(":::::::::::in getCordinate:::::::::::");
        PDFTextSearch.PDFLastLineStripper stripper1 = new PDFTextSearch.PDFLastLineStripper();
        stripper1.searchTerm = serchtext;
        loadPDF(path, stripper1,0);
        return stripper1.getCoordinates();
    }

    public static Map<Integer,Integer> countOccurrencesPageWise(String pdfFilePath, String searchText) {
        Map<Integer,Integer>map=new HashMap<>();
        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            // Loop through each page in the PDF
            for (int page = 1; page <= totalPages; page++) {
                // Set the page range to process (one page at a time)
                pdfStripper.setStartPage(page);
                pdfStripper.setEndPage(page);

                // Extract text from the current page
                String pageText = pdfStripper.getText(document);

                // Count occurrences of the search term in the current page
                int pageOccurrences = countOccurrences(pageText, searchText);

                // Print the result for the current page
                System.out.println("Page " + page + ": \"" + searchText + "\" appears " + pageOccurrences + " times.");
                map.put(page,pageOccurrences);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    private static int countOccurrences(String text, String searchText) {
        int count = 0;
        int index = 0;

        // Loop through the text to find occurrences of the search term
        while ((index = text.toLowerCase().indexOf(searchText.toLowerCase(), index)) != -1) {
            count++;
            index += searchText.length();  // Move the index to the end of the current match
        }

        return count;
    }
    public  static String getCordinateByInputStream(InputStream inputStream,String serchtext) throws IOException {
        System.out.println(":::::::::::in getCordinate:::::::::::");
        PDFTextSearch.PDFLastLineStripper stripper1 = new PDFTextSearch.PDFLastLineStripper();
        stripper1.searchTerm = serchtext;
        loadPDFInputSteam(inputStream, stripper1);
        return stripper1.getCoordinates();
    }

    public static void loadPDF(String filePath, PDFLastLineStripper stripper1,int page) {
        System.out.println("filePath:::::::::" + filePath);
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            // Process each page
            stripper1.setStartPage(page + 1);
            stripper1.setEndPage(page + 1);
            stripper1.getText(document);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadPDFInputSteam(InputStream inputStream, PDFLastLineStripper stripper1) {

        try (PDDocument document = PDDocument.load(inputStream)) {
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

        public String coordinates;

        public PDFLastLineStripper() throws IOException {
            super();
        }

        static String searchTerm = "";

        public float getY() {
            return y;
        }

        public float getX() {
            return x;
        }

        public String getCoordinates() {
            return coordinates;
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
                    int startX = (int)firstPosition.getXDirAdj();
                    int startY = (int)firstPosition.getYDirAdj();
                    int endX = (int)lastPosition.getEndX();
                    int endY = (int)lastPosition.getEndY();
                    int width =(int)firstPosition.getWidthDirAdj();
                    int height =(int)firstPosition.getHeightDir();

                    // Adjust as needed for your specific use case
                    System.out.println(text.trim() + " Coordinates:");
                    System.out.println("Start X: " + startX + ", Start Y: " + startY);
                    System.out.println("End X: " + endX + ", End Y: " + endY);
                    System.out.println("with: " + width+", height  "+height); // Trim to remove leading/trailing whitespace
                    System.out.println("Text: " + text.trim()); // Trim to remove leading/trailing whitespace
                    this.x = startX;
                    this.y = endY;
                    int e=endX+60;
                    int e1=endX+65;
                    int ey=endY+6;
                    int ey1=endY-40;
                    this.coordinates=startX+","+ey1+","+e1+","+ey;
                }
            }
        }
    }
}
