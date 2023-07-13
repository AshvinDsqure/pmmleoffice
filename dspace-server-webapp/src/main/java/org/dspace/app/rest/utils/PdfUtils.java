package org.dspace.app.rest.utils;

import com.itextpdf.commons.exceptions.ITextException;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.font.FontCacheKey;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.layout.font.FontSet;
import com.itextpdf.styledxmlparser.IXmlParser;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.xwpf.usermodel.*;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.content.WorkflowProcess;
import org.dspace.content.service.WorkflowProcessReferenceDocService;
import org.dspace.content.service.WorkflowProcessService;
import org.dspace.core.Context;
import org.jsoup.Jsoup;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.springframework.beans.factory.annotation.Autowired;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
public class PdfUtils {

    public static void main(String[] args) throws IOException {




      /*  String s = "<html> text sds wsdsds sds</html>";
        String string = "<b>સ્વાગત છે</b><p>Demo</p><p>AAAAAAAAAAAAAAAAAAAAAAAAA</p><p>じんけん の むし および けいぶ が、 じんるい の りょうしん を ふみにじった やばん こうい を もたらし、 げんろん および しんこう の じゆう が うけられ、 きょうふ および けつぼう の ない </p>";

        ITextException sds = new ITextException();
        FileOutputStream d = new FileOutputStream(new File("D://ss.pdf"));
        HtmlconvertToPdf(string, d);
        d.close();
        System.out.println(htmlToText(s));
        System.out.println(TextToHtml(htmlToText(s)));*/

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
