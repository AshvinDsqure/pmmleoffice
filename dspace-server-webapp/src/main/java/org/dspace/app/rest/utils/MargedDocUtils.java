package org.dspace.app.rest.utils;

import com.spire.doc.Document;
import com.spire.doc.DocumentObject;
import com.spire.doc.FileFormat;
import com.spire.doc.Section;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.dspace.content.WorkFlowProcessComment;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBackground;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSettings;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MargedDocUtils {
    static String[] filePaths = new String[3];
    static List<String> documentPaths = new ArrayList<>();

    static void setDisplayBackgroundShape(XWPFSettings settings, boolean booleanOnOff) throws Exception {
        java.lang.reflect.Field _ctSettings = XWPFSettings.class.getDeclaredField("ctSettings");
        _ctSettings.setAccessible(true);
        CTSettings ctSettings = (CTSettings) _ctSettings.get(settings);
//        CTOnOff onOff = CTOnOff.Factory.newInstance();
//        onOff.setVal(onOff.getVal());
//        ctSettings.setDisplayBackgroundShape(onOff);
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

    public static void main(String[] args) throws Exception {
      /*  InputStream inputStream = new FileInputStream(new File("D://doc/note3.docx"));
        OutputStream out = new FileOutputStream(new File("D://demo123.pdf"));
        Converter converter =new DocToPDFConverter(inputStream,out,true,true);
        converter.convert();
        Sys                                                                                                         tem.out.println("done");*/
        // ConvertToPDF("D://doc/note2.docx", "D://a1.pdf");
        // copyInputStreamToPdf(inputStream,"D://out12as.pdf");


        DocOneWrite(1l);
        InputStream input3 = new FileInputStream(new File("D://new.docx"));
        DocTwoWrite(input3);
        InputStream input3a = new FileInputStream(new File("D://doc//note4.docx"));
        DocTwoWrite1(input3a);
        DocumentMerger("D://test111112.pdf");

        // DocthreWrite1();
        //writeMultipleFiles();
        // DocumentMerger("D://finalaaaaaaa.docx");
        // Create a new documen
        //DocthreWrite1();


        //removetext2("D://Note#9.docx","D://ddddd.pdf");
    }


    public static void ConvertToPDF(String docPath, String pdfPath) {
        System.out.println("::::::::::::::::::::IN DOC TO PDF CONVERT :::::::::::::::::::::::::::::::::");
        try {
            InputStream in = new FileInputStream(new File(docPath));
            XWPFDocument doc = new XWPFDocument(in);
            PdfOptions options = PdfOptions.create();

            final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
            File test1 = new File(TEMP_DIRECTORY, "test1.pdf");
            if (!test1.exists()) {
                try {
                    test1.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            OutputStream out = new FileOutputStream(new File(pdfPath));
            PdfConverter.getInstance().convert(doc, out, options);
            doc.close();
            out.close();
            System.out.println("::::::::::::::::::::DONE  DOC TO PDF CONVERT :::::::::::::::::::::::::::::::::" + pdfPath);
           //setcolor(pdfPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("::::::::::::::::::::Error:::::::::::::::::::::::::::::::::" + e.getMessage());
        }
    }
    public static String ConvertDocInputstremToPDF(InputStream inputStream, String pdfname) {
        System.out.println("::::::::::::::::::::IN DOC TO PDF CONVERT :::::::::::::::::::::::::::::::::"+pdfname);
        try {
            XWPFDocument doc = new XWPFDocument(inputStream);
            PdfOptions options = PdfOptions.create();
            final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
            File pdfstorepathe = new File(TEMP_DIRECTORY, pdfname+".pdf");

            if (!pdfstorepathe.exists()) {
                try {
                    pdfstorepathe.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            System.out.println("path::::::::::::::::::::"+pdfstorepathe.getAbsolutePath());
            OutputStream out = new FileOutputStream(new File(pdfstorepathe.getAbsolutePath()));
            PdfConverter.getInstance().convert(doc, out, options);
            doc.close();
            out.close();
            System.out.println("::::::::::::::::::::DONE  DOC TO PDF CONVERT :::::::::::::::::::::::::::::::::" + pdfstorepathe.getAbsolutePath());
            return pdfstorepathe.getAbsolutePath();
            //setcolor(pdfPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("::::::::::::::::::::Error:::::::::::::::::::::::::::::::::" + e.getMessage());
        }
        return null;
    }
    public static  void setcolor(String pathe) throws IOException {
        System.out.println("set color in pdf ");
        PDDocument document = PDDocument.load(new File(pathe));
        PDPage originalPage = document.getPage(0); // Assuming you want to work with the first page (index 0)
        PDPage newPage = new PDPage(originalPage.getMediaBox());
        document.addPage(newPage);
        PDPageContentStream contentStream = new PDPageContentStream(document, newPage);
        Color backgroundColor = new Color(255, 0, 0); // Red color in RGB format
        contentStream.setNonStrokingColor(backgroundColor);
        contentStream.fillRect(0, 0, PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());
        contentStream.close();
        document.save("D://test12.pdf");
        document.close();
        System.out.println("done set pdf color ");

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

    public static void finalwriteDocument(String path) throws Exception {
        DocumentMerger(path);
    }

    public static void DocOneWrite(Long notecount) {
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        File oneFile = new File(TEMP_DIRECTORY, "file1.docx");
        if (!oneFile.exists()) {
            try {
                oneFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        filePaths[0] = oneFile.getAbsolutePath();
        documentPaths.add(oneFile.getAbsolutePath());
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFSettings settings = getSettings(doc);
            setDisplayBackgroundShape(settings, true);
            CTBackground background = doc.getDocument().addNewBackground();
            background.setColor("d9fdd3");
            // create a paragraph
            XWPFParagraph p1 = doc.createParagraph();
            p1.setAlignment(ParagraphAlignment.CENTER);
            // set font
            XWPFRun r1 = p1.createRun();
            r1.setBold(true);
            r1.setText("Note#" + notecount);

            XWPFParagraph p2 = doc.createParagraph();
            p2.setAlignment(ParagraphAlignment.CENTER);
            // set font
            XWPFRun r2 = p2.createRun();
            r1.setText(" ");

            // save it to .docx file
            try (FileOutputStream out = new FileOutputStream(oneFile)) {
                doc.write(out);
            }
            System.out.println("First doc save Done!" + oneFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error First doc save !" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void DocTwoWrite(InputStream in) {
        System.out.println("in Seconned doc save Done!");
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        File twoFile = new File(TEMP_DIRECTORY, "file2.docx");
        if (!twoFile.exists()) {
            try {
                twoFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        filePaths[1] = twoFile.getAbsolutePath();
        documentPaths.add(twoFile.getAbsolutePath());
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFSettings settings = getSettings(doc);
            setDisplayBackgroundShape(settings, true);
            CTBackground background = doc.getDocument().addNewBackground();
            background.setColor("d9fdd3");
            Files.copy(in, Paths.get(twoFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            in.close();
            System.out.println("Second doc save Done!" + twoFile.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error Seconned doc save !" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void DocTwoWrite1(InputStream in) {
        System.out.println("in Seconned doc save Done!");
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        File twoFile = new File(TEMP_DIRECTORY, "files2.docx");
        if (!twoFile.exists()) {
            try {
                twoFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        filePaths[2] = twoFile.getAbsolutePath();
        documentPaths.add(twoFile.getAbsolutePath());
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFSettings settings = getSettings(doc);
            setDisplayBackgroundShape(settings, true);
            CTBackground background = doc.getDocument().addNewBackground();
            background.setColor("d9fdd3");
            Files.copy(in, Paths.get(twoFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            in.close();
            System.out.println("Second doc save Done!" + twoFile.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error Seconned doc save !" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void DocthreWrite(Map<String, Object> hashMap) {
        System.out.println("in 3 doc!");
        boolean position = false;
        List<String> creator = null;
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        File threeFile = new File(TEMP_DIRECTORY, "file3.docx");
        if (!threeFile.exists()) {
            try {
                threeFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        filePaths[2] = threeFile.getAbsolutePath();
        documentPaths.add(threeFile.getAbsolutePath());
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFSettings settings = getSettings(doc);
            setDisplayBackgroundShape(settings, true);
            CTBackground background = doc.getDocument().addNewBackground();
            background.setColor("d9fdd3");

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

                            XWPFParagraph p2w = doc.createParagraph();
                            p2w.setAlignment(ParagraphAlignment.CENTER);
                            // set font
                            XWPFRun r2w = p2w.createRun();
                            r2w.setText(" ");

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
            try (FileOutputStream out = new FileOutputStream(threeFile)) {
                doc.write(out);
            }
            System.out.println("3 doc save Done!" + threeFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error First doc save !" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void DocumentMerger(String finalpathe) throws Exception {
        System.out.println("in marged doc 1 to 2 ");
        Document document1 = new Document(filePaths[0]);
        Document document2 = new Document(filePaths[1]);
        Section lastSection = document1.getLastSection();
        for (Section section : (Iterable<Section>) document2.getSections()) {
            for (DocumentObject obj : (Iterable<DocumentObject>) section.getBody().getChildObjects()
            ) {
                lastSection.getBody().getChildObjects().add(obj.deepClone());
            }
        }
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        File file1andfile2 = new File(TEMP_DIRECTORY, "file1andfile2.docx");
        document1.saveToFile(file1andfile2.getAbsolutePath(), FileFormat.Docx_2013);
        System.out.println("in marged doc 1 to 2 done");
        DocumentMerger2(file1andfile2.getAbsolutePath(), finalpathe);

    }

    public static void DocumentMerger2(String mrgedoneandtwo, String finalpathe) throws Exception {
        Document document = new Document();
        System.out.println("in marged doc 2 to 3 ");
        Document document1 = new Document(mrgedoneandtwo);
        Document document2 = new Document(filePaths[2]);
        Section lastSection = document1.getLastSection();
        for (Section section : (Iterable<Section>) document2.getSections()) {
            for (DocumentObject obj : (Iterable<DocumentObject>) section.getBody().getChildObjects()
            ) {
                lastSection.getBody().getChildObjects().add(obj.deepClone());
            }
        }
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        File finaldoc = new File(TEMP_DIRECTORY, "finaldoc.docx");
        File finaldocbeforremove = new File(TEMP_DIRECTORY, "finaldocbeforremove.pdf");
        document1.saveToFile(finaldoc.getAbsolutePath(), FileFormat.Docx_2013);
        System.out.println("in marged doc 2 to 3 done");
        System.out.println("Make final doc pathe is " + finaldoc.getAbsolutePath());
        //  ConvertToPDF(finaldoc.getAbsolutePath(),finaldocbeforremove.getAbsolutePath());
        System.out.println("Make final pdf before remove text pathe is " + finaldoc.getAbsolutePath());
        removetext2(finaldoc.getAbsolutePath(), finalpathe);

    }

    private static String DateFormate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        return formatter.format(date);
    }

    static XWPFSettings getSettings(XWPFDocument document) throws Exception {
        java.lang.reflect.Field settings = XWPFDocument.class.getDeclaredField("settings");
        settings.setAccessible(true);
        return (XWPFSettings) settings.get(document);
    }

    public static void deleteParagraph(XWPFParagraph p) {
        XWPFDocument doc = p.getDocument();
        int pPos = doc.getPosOfParagraph(p);
        //doc.getDocument().getBody().removeP(pPos);
        doc.removeBodyElement(pPos);
    }

    public static void setBGColor(String pathe, String path2) {
        try {

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removetext2(String fileopen, String finalpath) throws Exception {
        System.out.println(":::::::::::remove text:::::::::::");
        Path msWordPath = Paths.get(fileopen);
        XWPFDocument document2 = new XWPFDocument(Files.newInputStream(msWordPath));

        XWPFSettings settings = getSettings(document2);
        setDisplayBackgroundShape(settings, true);
        CTBackground background = document2.getDocument().addNewBackground();
        background.setColor("d9fdd3");
        List<XWPFParagraph> paragraphs = document2.getParagraphs();
        System.out.println(paragraphs.size());
        for (int i = document2.getParagraphs().size() - 1; i >= 0; i--) {

            XWPFParagraph paragraphq = document2.getParagraphs().get(i);
            // Check if the paragraph contains the specific string
            if (paragraphq.getText().contains("Evaluation Warning: The document was created with Spire.Doc for JAVA.")) {
                // Remove the paragraph from the document
                System.out.println(":::::" + paragraphq.getText());
                document2.removeBodyElement(i);
            }
        }
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        File removed = new File(TEMP_DIRECTORY, "removsed.docx");
        if (!removed.exists()) {
            try {
                removed.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try (FileOutputStream fos = new FileOutputStream(removed.getAbsolutePath())) {
            document2.write(fos);
        }
        document2.close();
        System.out.println(":::::::::::remove text:::::::::::" + removed.getAbsolutePath());
        ConvertToPDF(removed.getAbsolutePath(), finalpath);
    }

    public static void removetext(String fileopen, String finalpath) {
        System.out.println(":::::::::::remove text:::::::::::");
        try {
            XWPFDocument doc = new XWPFDocument(OPCPackage.open(new File(fileopen)));
            XWPFSettings settings = getSettings(doc);
            setDisplayBackgroundShape(settings, true);
            CTBackground background = doc.getDocument().addNewBackground();
            background.setColor("d9fdd3");

            for (XWPFParagraph p : doc.getParagraphs()) {
                List<XWPFRun> runs = p.getRuns();
                if (runs != null) {
                    int i = 0;
                    for (XWPFRun r : runs) {
                        if (i == 0) {
                            String text = r.getText(0);
                            if (text != null && text.contains("Evaluation Warning: The document was created with Spire.Doc for JAVA.")) {
                                System.out.println("remove text :::::::::::::::" + text);
                                text = text.replace("Evaluation Warning: The document was created with Spire.Doc for JAVA.", " ");
                                r.setText(text, 0);
                            }
                        }
                        if (i == 1) {
                            String text = r.getText(0);
                            System.out.println("remove text ::::::s:::::::::" + text);
                            if (text != null && text.contains("cument was created with Spire.Doc for JAVA.")) {
                                text = text.replace("cument was created with Spire.Doc for JAVA.", " ");
                                r.setText(text, 0);
                            }
                        }
                        i++;
                    }
                }
            }

            final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
            File removed = new File(TEMP_DIRECTORY, "removsed.docx");
            if (!removed.exists()) {
                try {
                    removed.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            doc.write(new FileOutputStream(removed.getAbsolutePath()));
            System.out.println(":::::::::::remove text::::::::::done!:");
            ConvertToPDF(removed.getAbsolutePath(), finalpath);
        } catch (Exception e) {
            e.printStackTrace();
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
