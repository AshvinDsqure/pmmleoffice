//package org.dspace.app.rest.utils;
//
//import org.apache.poi.sl.usermodel.TextParagraph;
//import org.apache.poi.xslf.usermodel.*;
//import org.apache.poi.xwpf.usermodel.*;
//import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STOnOff1;
//import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
//import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
//import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
//
//import java.awt.*;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//public class HtmlToDocUtils {
//    public static XWPFHyperlinkRun createHyperlinkRun(XWPFParagraph paragraph, String uri) throws Exception {
//        String rId = paragraph.getPart().getPackagePart().addExternalRelationship(
//                uri,
//                XWPFRelation.HYPERLINK.getRelation()
//        ).getId();
//
//        CTHyperlink cthyperLink=paragraph.getCTP().addNewHyperlink();
//        cthyperLink.setId(rId);
//        cthyperLink.addNewR();
//
//        return new XWPFHyperlinkRun(
//                cthyperLink,
//                cthyperLink.getRArray(0),
//                paragraph
//        );
//    }
//
//
//    public static void main(String[] args) {
//
//        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
//        File oneFile = new File(TEMP_DIRECTORY, "test.docx");
//        if (!oneFile.exists()) {
//            try {
//                oneFile.createNewFile();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//        try (XWPFDocument doc = new XWPFDocument()) {
//            // create a paragraph
//            XWPFParagraph p1 = doc.createParagraph();
//            p1.setAlignment(ParagraphAlignment.CENTER);
//            // set font
//            XWPFRun r1 = p1.createRun();
//            r1.setBold(true);
//            r1.setText("Note#");
//
//            XWPFParagraph paragraph = doc.createParagraph();
//            XWPFRun run = paragraph.createRun();
//            XWPFHyperlinkRun hyperlinkrun = createHyperlinkRun(paragraph, "https://www.google.de");
//            hyperlinkrun.setText("Ashivn");
//            hyperlinkrun.setColor("0000FF");
//            hyperlinkrun.setUnderline(UnderlinePatterns.SINGLE);
//
//            run = paragraph.createRun();
//            run.setText(" in it.");
//
//
//         // save it to .docx file
//            try (FileOutputStream out = new FileOutputStream(oneFile)) {
//                doc.write(out);
//                out.close();
//                doc.close();
//            }
//            System.out.println("First doc save Done!" + oneFile.getAbsolutePath());
//        } catch (Exception e) {
//            System.out.println("Error First doc save !" + e.getMessage());
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
//
//    }
//}
