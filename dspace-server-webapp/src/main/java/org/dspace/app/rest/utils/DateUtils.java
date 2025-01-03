package org.dspace.app.rest.utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    public static void main(String[] args) throws ParseException {
        Date d=new Date();
       // System.out.println(DateFormateMMDDYYYY(new Date()));
        //System.out.println(getCurrentDDMMYY());
        //System.out.println(getShortName("Computer Application"));
      //  System.out.println(getFolderTmp("UnSign"));

        //String s=getFolderTmp("UnSign");
       // System.out.println("s"+s);
        int currentYear = Year.now().getValue();
        System.out.println("cyear:::::::::"+currentYear);

       // String s="2024-12-23 00:00:00.0";

        //System.out.println("DA>>>"+DateSTRToDateFormatedd_mm_yyyy(s));



        //deleteFolderTmp("E:\\tomcate\\tomcat\\apache-tomcat-9.0.72\\apache-tomcat-9.0.72\\temp\\UnSign_17Dec2024162705");


       // deleteFolder("E:\\tomcate\\tomcat\\apache-tomcat-9.0.72\\apache-tomcat-9.0.72\\temp\\UnSign_17Dec2024162705");


       // System.out.println("test::"+strDateToString("2023-06-17 16:09:41.481"))
    }
    public static boolean isNullOrEmptyOrBlank(String str) {
            return str == null || str.trim().isEmpty();
    }
    // Method for handling null values of Integer
    public static boolean isNullOrZero(Integer value) {
        return value == null || value == 0;
    }

    // Method for handling null values of int
    public static boolean isNullOrZero(int value) {
        return value == 0;
    }

    // Similarly, methods can be implemented for other primitive types and their wrappers
    // Double
    public static boolean isNullOrZero(Double value) {
        return value == null || value == 0.0;
    }

    public static boolean isNullOrZero(double value) {
        return value == 0.0;
    }

    // Float
    public static boolean isNullOrZero(Float value) {
        return value == null || value == 0.0f;
    }

    public static boolean isNullOrZero(float value) {
        return value == 0.0f;
    }

    // Boolean
    public static boolean isNullOrFalse(Boolean value) {
        return value == null || !value;
    }

    public static boolean isNullOrFalse(boolean value) {
        return !value;
    }

    // Character
    public static boolean isNullOrBlank(Character value) {
        return value == null || value == ' ';
    }

    public static boolean isNullOrBlank(char value) {
        return value == ' ';
    }

    // Byte
    public static boolean isNullOrZero(Byte value) {
        return value == null || value == 0;
    }

    public static boolean isNullOrZero(byte value) {
        return value == 0;
    }

    // Short
    public static boolean isNullOrZero(Short value) {
        return value == null || value == 0;
    }

    public static boolean isNullOrZero(short value) {
        return value == 0;
    }

    // Long
    public static boolean isNullOrZero(Long value) {
        return value == null || value == 0L;
    }

    public static boolean isNullOrZero(long value) {
        return value == 0L;
    }

    
    public  static  String getShortName(String str){
        StringBuilder initials = new StringBuilder();
        for (String word : str.split(" ")) {
            initials.append(word.charAt(0));
        }
        return initials.toString();
    }

    public static String getFinancialYear (){
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        String financialYear;
        String financialYears;
        if (month <= 1) {
            financialYear = String.format("%d-%d", year - 1, year);
        } else {
            financialYear = String.format("%d-%d", year, year + 1);
        }
        String s[]=financialYear.split("-");
        financialYears=s[0].toString().substring(2)+"-"+s[1].toString().substring(2);
        return  financialYears;
    }
    public static String DateToSTRDDMMYYYHHMMSS(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        return formatter.format(date);
    }
    public static String DateFormateDDMMYYYY(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }
    public static String DateFormateMMDDYYYY(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        return formatter.format(date);
    }
    public static String strDateToString(String date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return DateFormateDDMMYYYY(dateFormat.parse(date));
    }
    public static Date DateToDateFormate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date1=null;
        String datestr=formatter.format(date);
        System.out.println(datestr);
        try {
            date1=new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(datestr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return date1;
    }

    public static String DateSTRToDateFormatedd_mm_yyyy(String inputDate) {

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
            Date date = inputFormat.parse(inputDate);
            // Format the Date object into the desired format
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
            String formattedDate = outputFormat.format(date);
            System.out.println("Formatted Date: " + formattedDate);
            return formattedDate;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    public static String getCurrentDDMMYY(){
        LocalDate currentDate = LocalDate.now();
        // Define the date format (ddMMyyyy)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        // Format the current date
        return currentDate.format(formatter);
    }

    public static String getFolderTmp(String folderName) {
        final String tempDirectory = System.getProperty("java.io.tmpdir");
        String timestamp = java.time.format.DateTimeFormatter.ofPattern("ddMMMyyyyHHmmss")
                .format(java.time.LocalDateTime.now().plusMinutes(3));
        File directory = new File(tempDirectory + File.separator + folderName + "_" + timestamp);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Failed to create temporary directory: " + directory.getAbsolutePath());
        }
        return directory.getAbsolutePath();
    }

    public static void deleteFolderTmp(String s){
        String folderPath = s;
        System.out.println(":::::::::"+folderPath);
        File folder = new File(folderPath);
        if (folder.exists()) {
            boolean isDeleted = deleteFolder(folder);
            if (isDeleted) {
                System.out.println("Folder deleted successfully: " + folderPath);
            } else {
                System.out.println("Failed to delete folder: " + folderPath);
            }
        } else {
            System.out.println("Folder does not exist: " + folderPath);
        }
    }

    public static boolean deleteFolder(File folder) {
        if (folder.isDirectory()) {
            //System.out.println("in ");
            for (File file : folder.listFiles()) {
                //System.out.println("in sub");
                deleteFolder(file); // Recursive call for files and subdirectories
            }
        }
      //  System.out.println("in out"+folder.delete());
        return folder.delete(); // Deletes the folder or file
    }

    public static Integer getVersion() throws Exception {
        int currentYear = Year.now().getValue();
       // System.out.println("::::::::currentYear:::::::::::"+currentYear);
        if(currentYear==2025){
            return 1;
        }
        return  0;
    }



}
