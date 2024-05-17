package org.dspace.app.rest.utils;
import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.Environment;
import org.dspace.app.rest.model.SAPResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Service
public class SapService {

    @Value("${jco.client.lang}")
    private String lang;

    @Value("${jco.destination.peak_limit}")
    private String peak_limit;

    @Value("${jco.client.client}")
    private String client;

    @Value("${jco.client.user}")
    private String user;

    @Value("${jco.client.passwd}")
    private String passwd;

    @Value("${jco.client.sysnr}")
    private String sysnr;

    @Value("${jco.destination.pool_capacity}")
    private String capacity;

    @Value("${jco.client.ashost}")
    private String ashost;



    @PostConstruct
    public void initializeSapDestination() {
        CustomDestinationDataProvider destinationDataProvider = new CustomDestinationDataProvider();
        Environment.registerDestinationDataProvider(destinationDataProvider);

        Properties sapProperties = new Properties();
        sapProperties.setProperty("jco.client.lang", lang);
        sapProperties.setProperty("jco.destination.peak_limit", peak_limit);
        sapProperties.setProperty("jco.client.client", client);
        sapProperties.setProperty("jco.client.user", user);
        sapProperties.setProperty("jco.client.sysnr", sysnr);
        sapProperties.setProperty("jco.destination.pool_capacity", capacity);
        sapProperties.setProperty("jco.client.ashost", ashost);
        sapProperties.setProperty("jco.client.passwd", passwd);

        destinationDataProvider.addDestination("ZDMS_DOCUMENT_REJECT", sapProperties);
    }

    public JCoDestination getDestination() throws JCoException {
        return JCoDestinationManager.getDestination("ZDMS_DOCUMENT_REJECT");
    }
    public   JCoFunction getFunctionZDMS_DOCUMENT_REJECT(JCoDestination destination) {
        try {
            return destination.getRepository().getFunction("ZDMS_DOCUMENT_REJECT");
        } catch (JCoException e) {
            e.printStackTrace();
            return null;
        }
    }
    public   JCoFunction getFunctionZDMS_DOCUMENT_POST(JCoDestination destination) {
        try {
            return destination.getRepository().getFunction("ZDMS_DOCUMENT_POST");
        } catch (JCoException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Set input data and table parameter
    public SAPResponse executeSAP(JCoFunction function, JCoDestination destination,String DocumentNumber) throws JCoException {
        SAPResponse sapResponse=new SAPResponse();
        System.out.println(":::::::::::::::::::::::executeSAP::::::::::::::::::::::::::");
        if(function!=null) {
            JCoParameterList importParams = function.getImportParameterList();
            importParams.setValue("DOCUMENT",DocumentNumber);
            function.execute(destination);
            JCoTable outputTable = function.getTableParameterList().getTable("IT_MESSAGES");
            if(outputTable!=null) {
                System.out.println("MESSAGE::::::::::::::"+outputTable.getString("MESSAGE"));
                System.out.println("MSGTYP::::::::::::"+outputTable.getString("MSGTYP"));
                sapResponse.setMESSAGE(outputTable.getString("MESSAGE"));
                sapResponse.setMSGTYP(outputTable.getString("MSGTYP"));
                return sapResponse;
            }else {
                System.out.println("JCoTable  not found");
            }
        }
        System.out.println(":::::::::::::::::::::::executeSAP::::::::::::::::::::::::::done!");
        return sapResponse;
    }
}