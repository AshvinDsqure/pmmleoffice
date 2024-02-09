package org.dspace.app.rest.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    public static void main(String[] args) throws ParseException {
        Date d=new Date();
        System.out.println(getFinancialYear());
        System.out.println(getShortName("Computer Application"));
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
        System.out.println("todate date"+today);
        int year = today.getYear();
        int month = today.getMonthValue();
        System.out.println("date\t"+today.getDayOfMonth());
        System.out.println("dmonth\t"+today.getMonthValue());
        System.out.println("year\t"+today.getYear());
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


}
