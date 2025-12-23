package org.dspace.core;

import java.util.HashMap;
import java.util.Map;

public class OrderMappig {


    public static String getOrderby(HashMap<String, String> perameter) {
        StringBuffer orderBy = new StringBuffer(" ORDER BY ");
        boolean iscreateddate = false;
        boolean ispriority = false;
        boolean isdepartment=false;
        boolean isreciveddate = false;
        boolean issender = false;


        String order="";

        for (Map.Entry<String, String> map : perameter.entrySet()) {
            //System.out.println("key : " + map.getKey() + " value : " + map.getValue());

            if ("ispriority".equalsIgnoreCase(map.getKey()) && map.getValue() != null) {
                ispriority = true;
            }
            if ("iscreateddate".equalsIgnoreCase(map.getKey()) && "true".equalsIgnoreCase(map.getValue())) {
                iscreateddate = true;
            }
            if ("isdepartment".equalsIgnoreCase(map.getKey()) && "true".equalsIgnoreCase(map.getValue())) {
                isdepartment = true;
            }

            if ("isreciveddate".equalsIgnoreCase(map.getKey()) && "true".equalsIgnoreCase(map.getValue())) {
                isreciveddate = true;
            }
            if ("issender".equalsIgnoreCase(map.getKey()) && "true".equalsIgnoreCase(map.getValue())) {
                issender = true;
            }

            if ("order".equalsIgnoreCase(map.getKey())) {
                //System.out.println("order::value::"+map.getValue());
                order = map.getValue();
            }
        }

        boolean hasPrevious = false;

        if (ispriority) {
            orderBy.append("CASE priority.primaryvalue ")
                    .append("WHEN 'Most Immediate' THEN 1 ")
                    .append("WHEN 'High' THEN 2 ")
                    .append("WHEN 'Medium' THEN 3 ")
                    .append("WHEN 'Low' THEN 4 ")
                    .append("ELSE 5 END");
            hasPrevious = true;
        }
        if (iscreateddate) {
            orderBy.append("wp.InitDate "+order);
        }
        if (isreciveddate) {
            orderBy.append("MAX(h.actionDate) "+order);
        }
        if (isdepartment) {
            orderBy.append("d.primaryvalue "+order);
            hasPrevious = true;
        } if (issender) {
            orderBy.append("metadatavalue.value "+order+" NULLS LAST");
            hasPrevious = true;
        }
        if(hasPrevious){
            orderBy.append(",MAX(h.actionDate) DESC");
        }
        return orderBy.toString();
    }


    public static String getgroupby(HashMap<String, String> perameter){
        StringBuffer orderBy = new StringBuffer("GROUP BY wp");
        Boolean ispriority=false;
        Boolean isdepartment=false;
        Boolean issender=false;

        for (Map.Entry<String, String> map : perameter.entrySet()) {
            //System.out.println("key : " + map.getKey() + " value : " + map.getValue());
            if (map.getKey().equalsIgnoreCase("ispriority") && map.getValue() != null) {
                ispriority = true;
            }
            if (map.getKey().equalsIgnoreCase("isdepartment") && map.getValue() != null) {
                isdepartment = true;
            }
            if (map.getKey().equalsIgnoreCase("issender") && map.getValue() != null) {
                issender = true;
            }
        }
        if (Boolean.TRUE.equals(ispriority)) {
            orderBy.append(",priority.primaryvalue");
        }  if (Boolean.TRUE.equals(isdepartment)) {
            orderBy.append(",d.primaryvalue");
        }if (Boolean.TRUE.equals(issender)) {
            orderBy.append(",metadatavalue.value ");
        }
        return orderBy.toString();
    }


    public static Boolean findkey(HashMap<String, String> perameter,String key){
        Boolean iskey=false;
        for (Map.Entry<String, String> map : perameter.entrySet()) {
            //System.out.println("key : " + map.getKey() + " value : " + map.getValue());
            if (map.getKey().equalsIgnoreCase(key) && map.getValue() != null) {
                iskey = true;
            }
        }
        return iskey;
    }
    public static String getDEpartmentjoinString(){
        String str=" "+
                "LEFT JOIN wp.workflowProcessEpeople AS senderEp WITH senderEp.sequence='0' " +
                "LEFT JOIN senderEp.epersontoepersonmapping AS em "+
                "LEFT JOIN em.epersonmapping AS eee "+
                "LEFT JOIN eee.department AS d ";
        return str;
    }
    public static String getSenderJoin(){
        String str=" "+
                "LEFT JOIN wp.workflowProcessEpeople AS senderEp WITH senderEp.isSender='true' " +
                "LEFT JOIN senderEp.ePerson AS eperson1 "+
                "LEFT JOIN eperson1.metadata AS metadatavalue ";

        return str;
    }
    public static String getsentoJoin(){
        String str=" "+
                "LEFT JOIN wp.workflowProcessEpeople AS senderEp WITH senderEp.isOwner='true' " +
                "LEFT JOIN senderEp.ePerson AS eperson1 "+
                "LEFT JOIN eperson1.metadata AS metadatavalue WITH metadatavalue.metadataField = :metadataField ";

        return str;
    }
}
