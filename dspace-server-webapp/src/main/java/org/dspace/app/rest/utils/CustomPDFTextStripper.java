package org.dspace.app.rest.utils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomPDFTextStripper extends PDFTextStripper {

    static float lastX1 = Float.MIN_VALUE;
    static float lastY1 = Float.MIN_VALUE;
    private List<List<Float>> paragraphYPositions;

    public CustomPDFTextStripper() throws IOException {
        super();
        paragraphYPositions = new ArrayList<>();
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        super.writeString(text, textPositions);

        // Assuming each line break indicates a new paragraph
        if (text.contains("\n")) {
            List<Float> yPositions = new ArrayList<>();
            for (TextPosition position : textPositions) {
                yPositions.add(position.getY());
            }
            paragraphYPositions.add(yPositions);
        }
    }
    public List<List<Float>> getParagraphYPositions() {
        return paragraphYPositions;
    }
    public static void main(String[] args) throws IOException {
        PDDocument document = PDDocument.load(new File("D://n2.pdf"));
        processtogetxycordinatelastline(document,1);
        System.out.println("x>>>>>>>>>>>>>"+lastX1);
        System.out.println("y>>>>>>>>>>>>>"+lastY1);
    }
    public static void processtogetxycordinatelastline(PDDocument document,Integer pagnumber){
        try {
            PDFTextStripper textStripper = new PDFTextStripper() {
                float lastX = Float.MIN_VALUE;
                float lastY = Float.MIN_VALUE;
                @Override
                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    // Get the X and Y position of each text position
                    for (TextPosition textPosition : textPositions) {
                        float x = textPosition.getXDirAdj();
                        float y = textPosition.getYDirAdj();
                        lastX = Math.max(lastX, x); // Update last X position
                        lastY = Math.max(lastY, y); // Update last Y position
                        lastX1=lastX;
                        lastY1=lastY;
                    }
                }
                @Override
                protected void writeLineSeparator() throws IOException {
                    // Output the X and Y position of the last text in the line
                    System.out.println("Last line position: X=" + lastX + ", Y=" + lastY);
                    lastX = Float.MIN_VALUE; // Reset last X position for the next line
                    lastY = Float.MIN_VALUE; // Reset last Y position for the next line
                }
            };
            textStripper.setStartPage(pagnumber);
            textStripper.setEndPage(pagnumber);
            textStripper.getText(document);
            //document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}