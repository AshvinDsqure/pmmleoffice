package org.dspace.core;

import java.util.HashMap;
import java.util.Map;

public class OrderMappig {


    public static String getOrderby(HashMap<String, String> perameter) {
        StringBuffer orderBy = new StringBuffer(" ORDER BY ");
        boolean iscreateddate = false;
        boolean ispriority = false;
        boolean isreciveddate = false;
        String order="";

        for (Map.Entry<String, String> map : perameter.entrySet()) {
            //System.out.println("key : " + map.getKey() + " value : " + map.getValue());

            if ("ispriority".equalsIgnoreCase(map.getKey()) && map.getValue() != null) {
                ispriority = true;
            }
            if ("iscreateddate".equalsIgnoreCase(map.getKey()) && "true".equalsIgnoreCase(map.getValue())) {
                iscreateddate = true;
            }
            if ("isreciveddate".equalsIgnoreCase(map.getKey()) && "true".equalsIgnoreCase(map.getValue())) {
                isreciveddate = true;
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
        if(hasPrevious){
            orderBy.append(",MAX(h.actionDate) "+order);
        }
        return orderBy.toString();
    }


    public static String getgroupby(HashMap<String, String> perameter){
        StringBuffer orderBy = new StringBuffer("GROUP BY wp");
        Boolean ispriority=false;
        for (Map.Entry<String, String> map : perameter.entrySet()) {
            //System.out.println("key : " + map.getKey() + " value : " + map.getValue());
            if (map.getKey().equalsIgnoreCase("ispriority") && map.getValue() != null) {
                ispriority = true;
            }
        }
        if (Boolean.TRUE.equals(ispriority)) {
            orderBy.append(",priority.primaryvalue");
        }
        return orderBy.toString();
    }
}
