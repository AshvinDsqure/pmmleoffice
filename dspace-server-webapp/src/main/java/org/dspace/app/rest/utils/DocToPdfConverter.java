package org.dspace.app.rest.utils;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.*;
import org.dspace.content.WorkFlowProcessComment;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBackground;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DocToPdfConverter {

    static final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
    private static File inputstreamdocpathe = new File(TEMP_DIRECTORY, "inputstream.docx");

    private static File newdocfile = new File(TEMP_DIRECTORY, "newdoc.docx");



    public static XWPFDocument getXWPFDocument() throws IOException {
        Path msWordPath = Paths.get(inputstreamdocpathe.getAbsolutePath());
        return new XWPFDocument(Files.newInputStream(msWordPath));
    }

    public static void main(String[] args) throws Exception {

        System.out.println("in>>>>>>>>>>>>>");
        InputStream inputStream = new FileInputStream(new File("D://doc//note3.docx"));
        XWPFDocument document2 = DocToPdfConverter.getXWPFDocument();
        String firstPergraph = getfirstParagraphText(document2);
        insertBeforeParagraph(document2,firstPergraph,1l);
        // insertAfterParagraph(document2);
        System.out.println("in 2>>>>>>>>>>>>>>>>>");

        try (FileOutputStream fos = new FileOutputStream("D://news.docx")) {
            document2.write(fos);
        }

        document2.close();
        System.out.println("in >>>>>>>>>>>>>>>>>");
        ConvertToPDF("D://news.docx", "D://test.pdf");

    }

    public static void genarateDocumentFlowNote(Map<String, Object> hashMap, String pathe,long notenumber) throws Exception {
        System.out.println(":::::::in:::::::::::::genarateDocumentFlowNote:::::::::::::::::::");
        XWPFDocument xwpfDocument= getXWPFDocument();
        String firstPergraph = getfirstParagraphText(xwpfDocument);
        insertBeforeParagraph(xwpfDocument,firstPergraph,notenumber);
        insertAfterParagraph(xwpfDocument,hashMap);
        try (FileOutputStream fos = new FileOutputStream(newdocfile.getAbsolutePath())) {
            xwpfDocument.write(fos);
        }
        xwpfDocument.close();
        System.out.println("::::::::::::::::::::final Doc:::::::::::::::::::"+newdocfile.getAbsolutePath());
        ConvertToPDF(newdocfile.getAbsolutePath(),pathe);
        System.out.println(":::::::out:::::::::::::genarateDocumentFlowNote:::::::::::::::::::");
    }
    public static void copyInInputStreamToDocx(InputStream inputStream) throws IOException {
        System.out.println(":::::::    In   ::::::copyInInputStreamToDocx::::::::::::::::::::::::::");
        Files.copy(inputStream, Paths.get(inputstreamdocpathe.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
        inputStream.close();
        System.out.println(":::::::   out  ::::::copyInInputStreamToDocx::::::::::::::::::::::::::");
    }

    public void removetext(String pathe) throws IOException {
        Path msWordPath = Paths.get(pathe);
        XWPFDocument document2 = new XWPFDocument(Files.newInputStream(msWordPath));
        List<XWPFParagraph> paragraphs = document2.getParagraphs();
        System.out.println(paragraphs.size());
        for (int i = document2.getParagraphs().size() - 1; i >= 0; i--) {
            XWPFParagraph paragraphq = document2.getParagraphs().get(i);
            // Check if the paragraph contains the specific string
            if (paragraphq.getText().contains("from HTTP fundamentals to API Mastery")) {
                // Remove the paragraph from the document
                System.out.println(":::::" + paragraphq.getText());
                document2.removeBodyElement(i);
            }
        }
        String outputPath = "D://modified_file1.docx";
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            document2.write(fos);
        }
        document2.close();
    }

    public static void insertBeforeParagraph(XWPFDocument document2, String firstParagraphText,long notenumber) throws IOException {
        List<XWPFParagraph> paragraphs = document2.getParagraphs();
        System.out.println(paragraphs.size());
        for (int i = document2.getParagraphs().size() - 1; i >= 0; i--) {
            XWPFParagraph paragraphq = document2.getParagraphs().get(i);
            // Check if the paragraph contains the specific string
            if (paragraphq.getText().contains(firstParagraphText)) {
                XWPFParagraph newParagraph = document2.insertNewParagraph(paragraphq.getCTP().newCursor());
                newParagraph.setAlignment(ParagraphAlignment.CENTER);
                newParagraph.setVerticalAlignment(paragraphq.getVerticalAlignment());
                // Add the content you want to insert before the paragraph
                String contentToAdd = "Note#"+notenumber;
                XWPFRun run = newParagraph.createRun();
                run.setText(contentToAdd);
                run.setBold(true);
            }
        }
    }

    public static void insertAfterParagraph(XWPFDocument doc, Map<String, Object> hashMap) throws Exception {
        try {
            // create a paragraph
            int total = hashMap.entrySet().size();
            for (int i = 0; i < total; i++) {

                for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
                    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                    //creater
                    if (entry.getKey().contains("creator") && i == 0) {
                        List<String> list = (List<String>) entry.getValue();
                        for (String creators : list) {
                            XWPFParagraph p1 = doc.createParagraph();
                            p1.setAlignment(ParagraphAlignment.RIGHT);
                            XWPFRun r1 = p1.createRun();
                            r1.setText(creators);
                        }
                    }
                    if (entry.getKey().contains("Reference Noting") && i == 2) {
                        XWPFParagraph Noting = doc.createParagraph();
                        Noting.setAlignment(ParagraphAlignment.RIGHT);
                        XWPFRun r1 = Noting.createRun();
                        r1.setBold(true);
                        r1.setText("Reference Noting");

                        List<Map<String, String>> listreferencenotting = (List<Map<String, String>>) entry.getValue();
                        if (listreferencenotting != null) {
                            System.out.println(listreferencenotting.size());
                            StringBuffer sb1 = new StringBuffer();
                            for (Map<String, String> maps : listreferencenotting) {
                                int t = maps.entrySet().size();
                                for (int a = 1; a <= t; a++) {
                                    System.out.println("::::::::::a:::::::::::::::::::" + a);
                                    System.out.println("map>>>>>>>size>>>>>>>>" + maps.entrySet().size());
                                    for (Map.Entry<String, String> noteingmap : maps.entrySet()) {
                                        if (noteingmap.getKey().contains("link") && a == 1) {
                                            sb1.append(noteingmap.getValue().toString());
                                        }
                                        if (noteingmap.getKey().contains("name1") && a == 2) {
                                            XWPFParagraph p3 = doc.createParagraph();
                                            p3.setAlignment(ParagraphAlignment.RIGHT);
                                            XWPFHyperlinkRun hyperlinkrun = createHyperlinkRun(p3, sb1.toString());
                                            System.out.println(":::::::name::::::::::::" + noteingmap.getValue().toString());
                                            hyperlinkrun.setText(noteingmap.getValue().toString());
                                            hyperlinkrun.setColor("0000FF");
                                            hyperlinkrun.setUnderline(UnderlinePatterns.SINGLE);
                                        }
                                        if (noteingmap.getKey().contains("subject") && a == 3) {
                                            XWPFParagraph p3 = doc.createParagraph();
                                            p3.setAlignment(ParagraphAlignment.RIGHT);
                                            XWPFRun r3 = p3.createRun();
                                            r3.setText(noteingmap.getValue().toString());
                                        }
                                        if (noteingmap.getKey().contains("notecreateor") && a == 4) {
                                            XWPFParagraph p3 = doc.createParagraph();
                                            p3.setAlignment(ParagraphAlignment.RIGHT);
                                            XWPFRun r3 = p3.createRun();
                                            r3.setText(noteingmap.getValue().toString());
                                        }
                                        if (noteingmap.getKey().contains("filename") && a == 5) {
                                            XWPFParagraph p3 = doc.createParagraph();
                                            p3.setAlignment(ParagraphAlignment.RIGHT);
                                            XWPFRun r3 = p3.createRun();
                                            r3.setText(noteingmap.getValue().toString());
                                        }
                                    }

                                }
                                lineBreak(1, doc);
                            }
                        }
                    }

                    //Reference Documents start
                    if (entry.getKey().contains("Reference Documents") && i == 1) {
                        XWPFParagraph Noting = doc.createParagraph();
                        Noting.setAlignment(ParagraphAlignment.LEFT);
                        XWPFRun r1 = Noting.createRun();
                        r1.setBold(true);
                        r1.setText("Reference Documents");
                        List<Map<String, String>> listofmapReferenceDocuments = (List<Map<String, String>>) entry.getValue();
                        System.out.println(listofmapReferenceDocuments.size());
                        System.out.println("List Reference Documents::::" + listofmapReferenceDocuments);

                        if (listofmapReferenceDocuments != null) {
                            System.out.println(listofmapReferenceDocuments.size());
                            StringBuffer sb11 = new StringBuffer();
                            for (Map<String, String> maps : listofmapReferenceDocuments) {
                                int t = maps.entrySet().size();
                                for (int j = 0; j < t; j++) {
                                    for (Map.Entry<String, String> entry1 : maps.entrySet()) {
                                        if (entry1.getKey().contains("link") && j == 0) {
                                            sb11.append(entry1.getValue().toString());
                                        }
                                        if (entry1.getKey().contains("doctyperefnumber") && j == 1) {
                                            XWPFParagraph p3 = doc.createParagraph();
                                            p3.setAlignment(ParagraphAlignment.LEFT);
                                            XWPFHyperlinkRun hyperlinkrun = createHyperlinkRun(p3, sb11.toString());
                                            hyperlinkrun.setText(entry1.getValue().toString());
                                            hyperlinkrun.setColor("0000FF");
                                            hyperlinkrun.setUnderline(UnderlinePatterns.SINGLE);

                                        }
                                        if (entry1.getKey().contains("datelettercategory") && j == 2) {
                                            XWPFParagraph p3 = doc.createParagraph();
                                            p3.setAlignment(ParagraphAlignment.LEFT);
                                            XWPFRun r3 = p3.createRun();
                                            r3.setText(entry1.getValue().toString());
                                        }
                                        if (entry1.getKey().contains("description") && j == 3) {
                                            XWPFParagraph p3 = doc.createParagraph();
                                            p3.setAlignment(ParagraphAlignment.LEFT);
                                            XWPFRun r3 = p3.createRun();
                                            r3.setText(entry1.getValue().toString());
                                        }
                                    }
                                }
                                lineBreak(1, doc);
                            }
                        }
                    }
                    ////Reference Documents end

                    if (entry.getKey().contains("comment") && i == 3) {
                        List<WorkFlowProcessComment> list = (List<WorkFlowProcessComment>) entry.getValue();
                        XWPFParagraph p1 = doc.createParagraph();
                        p1.setAlignment(ParagraphAlignment.LEFT);
                        XWPFRun rc = p1.createRun();
                        rc.setText("Comments [" + list.size() + "]");
                        rc.setBold(true);
                        lineBreak(1, doc);
                        int index = 1;
                        for (WorkFlowProcessComment b : list) {
                            //comment count
                            XWPFParagraph p2 = doc.createParagraph();
                            p2.setAlignment(ParagraphAlignment.LEFT);
                            XWPFRun r2 = p2.createRun();
                            r2.setBold(true);
                            r2.setText("Comment #" + index + "");
                            //Comment
                            if (b.getComment() != null) {
                                XWPFParagraph p3 = doc.createParagraph();
                                p3.setAlignment(ParagraphAlignment.LEFT);
                                XWPFRun r3 = p3.createRun();
                                r3.setText(b.getComment());

                            }
                            if (b.getSubmitter().getFullName() != null) {
                                XWPFParagraph p3 = doc.createParagraph();
                                p3.setAlignment(ParagraphAlignment.RIGHT);
                                XWPFRun r3 = p3.createRun();
                                r3.setText(b.getSubmitter().getFullName());
                            }
                            if (b.getSubmitter().getDesignation() != null) {
                                XWPFParagraph p3 = doc.createParagraph();
                                p3.setAlignment(ParagraphAlignment.RIGHT);
                                XWPFRun r3 = p3.createRun();
                                r3.setText(b.getSubmitter().getDesignation().getPrimaryvalue());
                            }
                            if (b.getWorkFlowProcessHistory().getActionDate() != null) {
                                XWPFParagraph p3 = doc.createParagraph();
                                p3.setAlignment(ParagraphAlignment.RIGHT);
                                XWPFRun r3 = p3.createRun();
                                r3.setText(DateFormate(b.getWorkFlowProcessHistory().getActionDate()));
                            }
                            index++;
                        }
                    }
                }
            }

            // save it to .docx file
            System.out.println("3 doc save Done!");

    } catch(
    Exception e)

    {
        e.printStackTrace();
    }

}

    public static String getLastParagraphText(String pathe) {

        try (FileInputStream fis = new FileInputStream(pathe); XWPFDocument document = new XWPFDocument(fis)) {
            int lastParagraphIndex = document.getParagraphs().size() - 1;
            if (lastParagraphIndex >= 0) {
                XWPFParagraph lastParagraph = document.getParagraphs().get(lastParagraphIndex);
                // Get the text content of the last paragraph
                String lastParagraphText = lastParagraph.getText();
                System.out.println("Last paragraph text: ");
                return lastParagraphText;
            } else {
                System.out.println("The document is empty, and there are no paragraphs.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static String DateFormate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        return formatter.format(date);
    }
    public static String getfirstParagraphText(XWPFDocument document) {
        try {
            if (document.getParagraphs().size() != 0) {
                XWPFParagraph lastParagraph = document.getParagraphs().get(0);
                // Get the text content of the 1 paragraph
                String lastParagraphText = lastParagraph.getText();
                System.out.println("Fistr paragraph text: " + lastParagraphText);
                return lastParagraphText;
            } else {
                System.out.println("The document is empty, and there are no paragraphs.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void convertDocxToPdf(String docxFilePath, String pdfFilePath) throws Exception {
        System.out.println("in doc to pdf");
        try (FileInputStream inputStream = new FileInputStream(docxFilePath);
             FileOutputStream outputStream = new FileOutputStream(pdfFilePath)) {
            XWPFDocument docxDocument = new XWPFDocument(inputStream);
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document pdfDoc = new Document(pdfDocument);

            for (XWPFParagraph paragraph : docxDocument.getParagraphs()) {
                pdfDoc.add(new Paragraph(paragraph.getText()));
            }

            pdfDoc.close();
            pdfDocument.close();
            writer.close();
            docxDocument.close();
        }
        System.out.println("out doc to pdf");
    }

    public static void lineBreak(int n, XWPFDocument doc) {
        for (int i = 1; i <= n; i++) {
            XWPFParagraph p2 = doc.createParagraph();
            p2.setAlignment(ParagraphAlignment.CENTER);
            // set font
            XWPFRun r1 = p2.createRun();
            r1.setText(" ");
        }
    }

    public static void ConvertToPDF(String docPath, String pdfPath) {
        System.out.println("::::::::::::::::::::IN DOC TO PDF CONVERT :::::::::::::::::::::::::::::::::");
        try {
            InputStream in = new FileInputStream(new File(docPath));
            XWPFDocument doc = new XWPFDocument(in);
            PdfOptions options = PdfOptions.create();
            OutputStream out = new FileOutputStream(new File(pdfPath));
            PdfConverter.getInstance().convert(doc, out, options);
            doc.close();
            out.close();
            System.out.println(
                    "::::::::::::::::::::DONE  DOC TO PDF CONVERT :::::::::::::::::::::::::::::::::" + pdfPath);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("::::::::::::::::::::Error:::::::::::::::::::::::::::::::::" + e.getMessage());
        }
    }

    public static XWPFHyperlinkRun createHyperlinkRun(XWPFParagraph paragraph, String uri) throws Exception {
        String rId = paragraph.getPart().getPackagePart().addExternalRelationship(
                uri,
                XWPFRelation.HYPERLINK.getRelation()
        ).getId();

        CTHyperlink cthyperLink = paragraph.getCTP().addNewHyperlink();
        cthyperLink.setId(rId);
        cthyperLink.addNewR();

        return new XWPFHyperlinkRun(
                cthyperLink,
                cthyperLink.getRArray(0),
                paragraph
        );
    }


}
