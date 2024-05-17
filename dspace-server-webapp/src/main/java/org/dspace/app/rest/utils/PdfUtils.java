package org.dspace.app.rest.utils;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.layout.font.FontProvider;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
public class PdfUtils {

    public static void main(String[] args) throws IOException {


        try {
            PDDocument document = PDDocument.load(new File("example.pdf"));
            int lastParagraphEnd = getLastParagraphEnd(document);
            System.out.println("End of last paragraph: " + lastParagraphEnd);
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            provider.addSystemFonts();
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
