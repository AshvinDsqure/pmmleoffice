package org.dspace.app.rest.utils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.util.Date;

public class JAVAPDFDROW {
    public static void main(String[] args) {
        try {
            // Create a new PDF document
            PDDocument document = new PDDocument();

            // Add a blank page to the document
            PDPage page = new PDPage();
            document.addPage(page);

            // Create a content stream for writing to the page
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Define the coordinates and dimensions of the rectangle
            float x = 200;
            float y = 700;
            float width = 215;
            float height = 60;

            // Draw the rectangle
            contentStream.addRect(x, y, width, height);
            contentStream.stroke();

            // Add text inside the rectangle
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(x + 10, y + height - 20); // Adjust text position
            contentStream.showText("Digital Sign by :Ashvin");
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Date:"+new Date());
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Reson:Bihar.");
            contentStream.endText();


            //
            contentStream.addRect(x, y-50, width, height);
            contentStream.stroke();
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(x + 10, y-100 + height - 20); // Adjust text position
            contentStream.showText("Digital Sign by :Ashvin");
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Date:"+new Date());
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Reson:Bihar.");
            contentStream.endText();


            // Close the content stream
            contentStream.close();

            // Save the document to a file
            document.save("D://rectangle_with_text.pdf");

            // Close the document
            document.close();

            System.out.println("Rectangle with text drawn successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
