/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dspace.app.rest.model.DepartmentDTO;
import org.dspace.app.rest.model.ExcelDTO;
import org.dspace.eperson.EPerson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelHelper {

    public static void main(String[] args) {
        System.out.println("Formatted Date and Time: " + DateFormateddmmyyyy("2023-02-24T05:20:35Z"));
    }

    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static String[] HEADERs = {
            "Sr No",
            "Case Detail",
            "Hierarchy(Community & Collection)",
            "Upload date",
            "Uploaded by",
    };

    static String[] HEADERsDepertment = {
            "Sr No",
            "Department Name",
            "No of Process WorkFlow"
    };

    static String[] HEADERsDepertmente = {
            "Sr No",
            "User Name",
            "Email",
            "Employee ID",
            "Department Name"

    };
    static String SHEET = "items";
    static String SHEETName = "NO_OF_PROCESS";

    static String SHEETNamee = "USER-LIST";

    public static ByteArrayInputStream tutorialsToExcel(List<ExcelDTO> tutorials) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(SHEET);
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERs.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERs[col]);
            }
            int rowIdx = 1;
            int i = 1;
            for (ExcelDTO item : tutorials) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(i);
                row.createCell(1).setCellValue(item.getCaseDetail());
                row.createCell(2).setCellValue(item.getHierarchy());
                row.createCell(3).setCellValue(DateFormateddmmyyyy(item.getUploaddate()));
                row.createCell(4).setCellValue(item.getUploadedby());
                i++;
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage(), e);
        }
    }

    public static ByteArrayInputStream tutorialsToExceldEPARTMENT(List<DepartmentDTO> tutorials) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(SHEETName);
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERsDepertment.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERsDepertment[col]);
            }
            int rowIdx = 1;
            int i = 1;
            for (DepartmentDTO item : tutorials) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(i);
                row.createCell(1).setCellValue(item.getName());
                row.createCell(2).setCellValue(String.valueOf(item.getCount()));
                i++;
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage(), e);
        }
    }
    public static ByteArrayInputStream tutorialsToExceldEpersion(List<EPerson> tutorials) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(SHEETNamee);
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERsDepertmente.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERsDepertmente[col]);
            }
            int rowIdx = 1;
            int i = 1;
            for (EPerson item : tutorials) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(i);
                row.createCell(1).setCellValue(item.getFullName()!=null?item.getFullName():"-");
                row.createCell(2).setCellValue(item.getEmail()!=null?item.getEmail():"-");
                row.createCell(2).setCellValue(item.getEmployeeid()!=null?item.getEmployeeid():"-");
                row.createCell(2).setCellValue(item.getDepartment()!=null&&item.getDepartment().getPrimaryvalue()!=null ?item.getDepartment().getPrimaryvalue():"-");
                i++;
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage(), e);
        }
    }

    public static String DateFormateddmmyyyy(String dates) {
        try {
            Instant instant = Instant.parse(dates);
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, java.time.ZoneId.of("UTC"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            return zonedDateTime.format(formatter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
