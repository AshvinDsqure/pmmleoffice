/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dspace.app.rest.model.DepartmentDTO;
import org.dspace.app.rest.model.ExcelDTO;
import org.dspace.app.rest.model.WithinDepartmentDTO;
import org.dspace.eperson.EPerson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    static String[] HEADERs_NO_OF_FILE_CREATED_AND_CLOSED = {
            "Sr No",
            "Department Name",
            "No of File Created",
            "No of File Closed",


    };
    static String[] HEADERs_NO_OF_TAPAL_CREATED_AND_CLOSED = {
            "Sr No",
            "Department Name",
            "No of Tapal Created",
            "No of Tapal Closed",


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
    static String SHEET_NO_OF_FIL_EAND_CLOSE = "No_of_file_created_and_closed";
    static String SHEET_NO_OF_TAPAL_CREATED_END_CLOSE = "No_of_Tapal_created_and_closed";

    static String SHEETName = "NO_OF_PROCESS";

    static String SHEETNamee = "USER-LIST";

    static String WITHIN_DEPARTMENT = "Within Department";

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
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage(), e);

        }
    }

    public static ByteArrayInputStream getNoOFFileCreatedAndClosedToExcel(List<WithinDepartmentDTO> tutorials) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(SHEET_NO_OF_FIL_EAND_CLOSE);
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERs_NO_OF_FILE_CREATED_AND_CLOSED.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERs_NO_OF_FILE_CREATED_AND_CLOSED[col]);
            }
            int rowIdx = 1;
            int i = 1;
            for (WithinDepartmentDTO item : tutorials) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(i);
                row.createCell(1).setCellValue(item.getName());//Department
                row.createCell(2).setCellValue(item.getDays());//No of creted file
                row.createCell(3).setCellValue(item.getFilecount());//No of close file
                i++;
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage(), e);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage(), e);

        }
    }

    public static ByteArrayInputStream getNoOFTapalCreatedAndClosedToExcel(List<WithinDepartmentDTO> tutorials) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(SHEET_NO_OF_TAPAL_CREATED_END_CLOSE);
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERs_NO_OF_TAPAL_CREATED_AND_CLOSED.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERs_NO_OF_TAPAL_CREATED_AND_CLOSED[col]);
            }
            int rowIdx = 1;
            int i = 1;
            for (WithinDepartmentDTO item : tutorials) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(i);
                row.createCell(1).setCellValue(item.getName());//Department
                row.createCell(2).setCellValue(item.getDays());//No of creted file
                row.createCell(3).setCellValue(item.getFilecount());//No of close file
                i++;
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage(), e);
        }catch (Exception e){
            e.printStackTrace();
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
        }catch (Exception e){
            e.printStackTrace();
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
        }catch (Exception e){
            e.printStackTrace();
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
    //
    public static ByteArrayInputStream getwithinDepartmentReport(
            List<WithinDepartmentDTO> dataList,
            List<String> departmentNameList
    ) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Within Department");

            // ✅ Create bold font for header
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            // ✅ Create a CellStyle with bold font
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            // 1. Header Row: Days + Department Names
            Row headerRow = sheet.createRow(0);
            Cell dayHeaderCell = headerRow.createCell(0);
            dayHeaderCell.setCellValue("Days");
            dayHeaderCell.setCellStyle(headerStyle); // apply bold

            for (int i = 0; i < departmentNameList.size(); i++) {
                Cell cell = headerRow.createCell(i + 1);
                cell.setCellValue(departmentNameList.get(i));
                cell.setCellStyle(headerStyle); // apply bold
            }

            // 2. Map<Day, Map<Dept, Count>>
            Map<Long, Map<String, Long>> dayToDeptCountMap = new TreeMap<>();
            for (WithinDepartmentDTO dto : dataList) {
                dayToDeptCountMap
                        .computeIfAbsent(dto.getDays(), k -> new HashMap<>())
                        .put(dto.getName(), dto.getFilecount());
            }

            // 3. Populate Rows: Days + FileCounts by department order
            int rowIdx = 1;
            for (Map.Entry<Long, Map<String, Long>> entry : dayToDeptCountMap.entrySet()) {
                Long day = entry.getKey();
                Map<String, Long> deptCountMap = entry.getValue();
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(day); // Set "Days" column

                for (int colIdx = 0; colIdx < departmentNameList.size(); colIdx++) {
                    String deptName = departmentNameList.get(colIdx);
                    Long count = deptCountMap.getOrDefault(deptName, 0L);
                    row.createCell(colIdx + 1).setCellValue(count);
                }
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage(), e);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage(), e);

        }
    }

    public static ByteArrayInputStream getouterDepartmentReport(
            List<WithinDepartmentDTO> dataList,
            List<String> departmentNameList
    ) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Inter-Department");

            //  Create bold font for header
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            // Create a CellStyle with bold font
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            // 1. Header Row: Days + Department Names
            Row headerRow = sheet.createRow(0);
            Cell dayHeaderCell = headerRow.createCell(0);
            dayHeaderCell.setCellValue("Days");
            dayHeaderCell.setCellStyle(headerStyle); // apply bold

            for (int i = 0; i < departmentNameList.size(); i++) {
                Cell cell = headerRow.createCell(i + 1);
                cell.setCellValue(departmentNameList.get(i));
                cell.setCellStyle(headerStyle); // apply bold
            }

            // 2. Map<Day, Map<Dept, Count>>
            Map<Long, Map<String, Long>> dayToDeptCountMap = new TreeMap<>();
            for (WithinDepartmentDTO dto : dataList) {
                dayToDeptCountMap
                        .computeIfAbsent(dto.getDays(), k -> new HashMap<>())
                        .put(dto.getName(), dto.getFilecount());
            }

            // 3. Populate Rows: Days + FileCounts by department order
            int rowIdx = 1;
            for (Map.Entry<Long, Map<String, Long>> entry : dayToDeptCountMap.entrySet()) {
                Long day = entry.getKey();
                Map<String, Long> deptCountMap = entry.getValue();

                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(day); // Set "Days" column

                for (int colIdx = 0; colIdx < departmentNameList.size(); colIdx++) {
                    String deptName = departmentNameList.get(colIdx);
                    Long count = deptCountMap.getOrDefault(deptName, 0L);
                    row.createCell(colIdx + 1).setCellValue(count);
                }
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage(), e);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage(), e);

        }
    }

    public static ByteArrayInputStream getstagewithinDepartmentReport(
            List<WithinDepartmentDTO> dataList,
            List<String> departmentNameList
    ) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Stage-Within Department");

            // ✅ Create bold font for header
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            // ✅ Create a CellStyle with bold font
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            // 1. Header Row: Days + Department Names
            Row headerRow = sheet.createRow(0);
            Cell dayHeaderCell = headerRow.createCell(0);
            dayHeaderCell.setCellValue("Stages");
            dayHeaderCell.setCellStyle(headerStyle); // apply bold

            for (int i = 0; i < departmentNameList.size(); i++) {
                Cell cell = headerRow.createCell(i + 1);
                cell.setCellValue(departmentNameList.get(i));
                cell.setCellStyle(headerStyle); // apply bold
            }

            // 2. Map<Day, Map<Dept, Count>>
            Map<Long, Map<String, Long>> dayToDeptCountMap = new TreeMap<>();
            for (WithinDepartmentDTO dto : dataList) {
                dayToDeptCountMap
                        .computeIfAbsent(dto.getDays(), k -> new HashMap<>())
                        .put(dto.getName(), dto.getFilecount());
            }

            // 3. Populate Rows: Days + FileCounts by department order
            int rowIdx = 1;
            for (Map.Entry<Long, Map<String, Long>> entry : dayToDeptCountMap.entrySet()) {
                Long day = entry.getKey();
                Map<String, Long> deptCountMap = entry.getValue();

                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(day); // Set "Days" column

                for (int colIdx = 0; colIdx < departmentNameList.size(); colIdx++) {
                    String deptName = departmentNameList.get(colIdx);
                    Long count = deptCountMap.getOrDefault(deptName, 0L);
                    row.createCell(colIdx + 1).setCellValue(count);
                }
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage(), e);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage(), e);

        }
    }

    public static ByteArrayInputStream getstageouterDepartmentReport(
            List<WithinDepartmentDTO> dataList,
            List<String> departmentNameList
    ) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Stage-Inter-Department");

            // Bold font for header and total
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            // 1. Header Row
            Row headerRow = sheet.createRow(0);
            Cell dayHeaderCell = headerRow.createCell(0);
            dayHeaderCell.setCellValue("Stages");
            dayHeaderCell.setCellStyle(headerStyle);

            for (int i = 0; i < departmentNameList.size(); i++) {
                Cell cell = headerRow.createCell(i + 1);
                cell.setCellValue(departmentNameList.get(i));
                cell.setCellStyle(headerStyle);
            }

            // 2. Data Mapping
            Map<Long, Map<String, Long>> dayToDeptCountMap = new TreeMap<>();
            for (WithinDepartmentDTO dto : dataList) {
                dayToDeptCountMap
                        .computeIfAbsent(dto.getDays(), k -> new HashMap<>())
                        .put(dto.getName(), dto.getFilecount());
            }

            // Track totals per department
            Map<String, Long> departmentTotals = new HashMap<>();
            departmentNameList.forEach(name -> departmentTotals.put(name, 0L));

            // 3. Populate Data Rows
            int rowIdx = 1;
            for (Map.Entry<Long, Map<String, Long>> entry : dayToDeptCountMap.entrySet()) {
                Long day = entry.getKey();
                Map<String, Long> deptCountMap = entry.getValue();
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(day);

                for (int colIdx = 0; colIdx < departmentNameList.size(); colIdx++) {
                    String deptName = departmentNameList.get(colIdx);
                    Long count = deptCountMap.getOrDefault(deptName, 0L);
                    row.createCell(colIdx + 1).setCellValue(count);

                    // Add to totals
                    departmentTotals.put(deptName, departmentTotals.get(deptName) + count);
                }
            }

            // 4. Total Row
            Row totalRow = sheet.createRow(rowIdx);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Total");
            totalLabelCell.setCellStyle(headerStyle); // bold for total label

            for (int colIdx = 0; colIdx < departmentNameList.size(); colIdx++) {
                String deptName = departmentNameList.get(colIdx);
                Long total = departmentTotals.get(deptName);
                Cell totalCell = totalRow.createCell(colIdx + 1);
                totalCell.setCellValue(total);
                totalCell.setCellStyle(headerStyle); // bold for total numbers
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage(), e);
        }
    }


}
