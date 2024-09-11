package org.dspace.app.rest.utils;

import com.itextpdf.commons.utils.FileUtil;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.layout.font.FontProvider;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class PdfUtils {

    public static void main(String[] args) throws IOException {

        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        Random random = new Random();
        // Generate a random 4-digit number
        int randomNumber = random.nextInt(9000) + 1000;
        File acknowledgementfile = new File(TEMP_DIRECTORY, "Acknowledgement" + randomNumber + ".pdf");
        if (!acknowledgementfile.exists()) {
            try {
                acknowledgementfile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        String html="<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <style>\n" +
                "        /* Ensure text stays on one line */\n" +
                "        .no-wrap {\n" +
                "            white-space: nowrap;\n" +
                "            overflow: hidden;\n" +
                "            text-overflow: ellipsis; /* Optional: adds \"...\" if text overflows */\n" +
                "            font-family: 'MarathiFont', sans-serif; /* Ensure the Marathi font is used */\n" +
                "        }\n" +
                "        /* Set the container width large enough */\n" +
                "        .container {\n" +
                "            width: 100%;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <p class=\"no-wrap\">\n" +
                "            तुमचं स्वागत आहे! This is Marathi text that should stay on one line.\n" +
                "        </p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>\n";


        FileOutputStream files = new FileOutputStream(new File(acknowledgementfile.getAbsolutePath()));
        HtmlconvertToPdf(html,files);
        System.out.println("path"+acknowledgementfile.getAbsolutePath());
//        try {
//            PDDocument document = PDDocument.load(new File("example.pdf"));
//            int lastParagraphEnd = getLastParagraphEnd(document);
//            System.out.println("End of last paragraph: " + lastParagraphEnd);
//            document.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    public static void writeInputStreamToPDF(InputStream inputStream, String outputFilePath) throws IOException {
        // Load the PDF document from the InputStream

       PDDocument document = PDDocument.load(inputStream);

        // Write the loaded document to a file
        document.save(new File(outputFilePath));

        // Close the document and InputStream
        document.close();
        inputStream.close();
    }

    private static int getLastParagraphEnd(PDDocument document) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);

        // Extract text from the entire document
        String text = stripper.getText(document);

        // Split the text into paragraphs
        String[] paragraphs = text.split("\\r?\\n\\r?\\n");

        // Get the last paragraph
        String lastParagraph = paragraphs[paragraphs.length - 1];

        // Find the end position of the last paragraph
        int lastParagraphEnd = text.lastIndexOf(lastParagraph) + lastParagraph.length();

        return lastParagraphEnd;
    }

    public static String htmlToText(String htmltext) {
        try {
            org.jsoup.nodes.Document document = Jsoup.parse(htmltext);
            return document.text();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFilePathe(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
                return file.getAbsolutePath();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }
        }
        return  null;
    }

    public static String TextToHtml(String text) {
        try {
            org.jsoup.nodes.Document document = Jsoup.parseBodyFragment(text);
            // Convert the Document object to HTML.
            String html = document.html();
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int HtmlconvertToPdf(String htmtext, FileOutputStream out) {
        try {
            System.out.println("::::::::::in HtmlconvertToPdf:::::::::::::");
            ConverterProperties converterProperties = new ConverterProperties();
            //  final FontSet set = new FontSet();
            FontProvider provider = new FontProvider();
            provider.addStandardPdfFonts();
            int i=provider.addSystemFonts();
            System.out.println(" FileUtil.getFontsDir();::::::::"+ FileUtil.getFontsDir());
            System.out.println("count::::::::"+i);
            converterProperties.setFontProvider(provider);
            HtmlConverter.convertToPdf(htmtext, out, converterProperties);
            System.out.println("::::::::::in HtmlconvertToPdf::::::done!:::::::");
            return 1;
        } catch (Exception e) {
            System.out.println("::::::::::error in  HtmlconvertToPdf:::::::::::" + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}
