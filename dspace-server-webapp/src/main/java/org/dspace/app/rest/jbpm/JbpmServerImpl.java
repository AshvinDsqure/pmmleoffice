/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.jbpm;

import com.google.gson.Gson;
import org.dspace.app.rest.exception.JBPMServerExpetion;
import org.dspace.app.rest.jbpm.constant.JBPM;
import org.dspace.app.rest.jbpm.models.HtmltppdfModel;
import org.dspace.app.rest.jbpm.models.JBPMCallbackRequest;
import org.dspace.app.rest.jbpm.models.JBPMProcess;
import org.dspace.app.rest.jbpm.models.JBPMResponse_;
import org.dspace.app.rest.model.WorkFlowProcessRest;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JbpmServerImpl {
    @Autowired
    public RestTemplate restTemplate;
    @Autowired
    private ConfigurationService configurationService;

    public String startProcess(WorkFlowProcessRest workflowProcessw, List<Object> users) throws RuntimeException,JBPMServerExpetion {
        System.out.println("::::::::::::::CREATE ACTION::::::::::::::::::::::");
       try {
           String baseurl = configurationService.getProperty("jbpm.server");
           JBPMProcess jbpmProcess = new JBPMProcess(workflowProcessw);
           jbpmProcess.setUsers(users);
           jbpmProcess.setWorkflowType(workflowProcessw.getWorkflowType().getPrimaryvalue());
           System.out.println("jbpm json::Request" + new Gson().toJson(jbpmProcess));
           HttpHeaders headers = new HttpHeaders();
           headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
           HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess, headers);
           System.out.println("::::::::::::::URL::::::::::::::::::::::" + baseurl + JBPM.CREATEPROCESS);
           return restTemplate.exchange(baseurl + JBPM.CREATEPROCESS, HttpMethod.POST, entity, String.class).getBody();
       }catch (Exception e){
        throw new JBPMServerExpetion(e.getMessage());
       }
    }

    public String forwardTask(WorkFlowProcessRest workflowProcess, List<Object> users) throws RuntimeException ,JBPMServerExpetion{
        System.out.println("::::::::::::::FORWARD ACTION::::::::::::::::::::::");
        try {
            String baseurl = configurationService.getProperty("jbpm.server");
            JBPMProcess jbpmProcess = new JBPMProcess();
            jbpmProcess.setQueueid(workflowProcess.getId());
            jbpmProcess.setWorkflowType(workflowProcess.getWorkflowType().getPrimaryvalue());
            jbpmProcess.setUsers(new ArrayList<Object>(users));
            jbpmProcess.setProcstatus("inprogress");
            System.out.println("jbpm json::" + new Gson().toJson(jbpmProcess));
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess, headers);
            return restTemplate.exchange(baseurl + JBPM.FORWARDPROCESS, HttpMethod.POST, entity, String.class).getBody();
        }catch (Exception e){
            throw new JBPMServerExpetion(e.getMessage());
        }
    }

    public String completeTask(WorkFlowProcessRest workflowProcess, List<String> users) throws RuntimeException {
        System.out.println("::::::::::::::COMPLETE ACTION::::::::::::::::::::::");
        try {
            String baseurl = configurationService.getProperty("jbpm.server");
            JBPMProcess jbpmProcess = new JBPMProcess();
            jbpmProcess.setQueueid(workflowProcess.getId());
            jbpmProcess.setWorkflowType(workflowProcess.getWorkflowType().getPrimaryvalue());
            jbpmProcess.setUsers(new ArrayList<Object>(users));
            jbpmProcess.setProcstatus("completed");
            System.out.println("jbpm json::" + new Gson().toJson(jbpmProcess));
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess, headers);
            return restTemplate.exchange(baseurl + JBPM.FORWARDPROCESS, HttpMethod.POST, entity, String.class).getBody();
        }catch (Exception e){
            throw new JBPMServerExpetion(e.getMessage());
        }
    }

    public String dispatchReady(WorkFlowProcessRest workflowProcess, List<String> users, List<String> dispatchUsers) throws RuntimeException ,JBPMServerExpetion{
        System.out.println(":::::::::::::: DISPATCH READY ACTION::::::::::::::::::::::");
       try {
           String baseurl = configurationService.getProperty("jbpm.server");
           JBPMProcess jbpmProcess = new JBPMProcess();
           jbpmProcess.setQueueid(workflowProcess.getId());
           jbpmProcess.setWorkflowType(workflowProcess.getWorkflowType().getPrimaryvalue());
           List<Object> usersobj = new ArrayList<Object>(users);
           System.out.println("usersobj current user:" + new Gson().toJson(usersobj));
           usersobj.add(dispatchUsers);
           System.out.println("user" + dispatchUsers);
           System.out.println("final make objeck like " + usersobj);
           jbpmProcess.setUsers(usersobj);
           jbpmProcess.setProcstatus("inprogress");
           System.out.println("jbpm json::" + new Gson().toJson(jbpmProcess));
           HttpHeaders headers = new HttpHeaders();
           headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
           HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess, headers);
           System.out.println("::::::::::::::URL::::::::::::::::::::::" + baseurl + JBPM.FORWARDPROCESS);
           return restTemplate.exchange(baseurl + JBPM.FORWARDPROCESS, HttpMethod.POST, entity, String.class).getBody();
       }catch (Exception e){
           throw new JBPMServerExpetion(e.getMessage());
       }
    }

    public String backwardTask(WorkFlowProcessRest workflowProcess) throws JBPMServerExpetion {
        System.out.println(":::::::::::::: BACKWARD ACTION::::::::::::::::::::::");
        try {
            String baseurl = configurationService.getProperty("jbpm.server");
            JBPMProcess jbpmProcess = new JBPMProcess();
            jbpmProcess.setQueueid(workflowProcess.getId());
            jbpmProcess.setUsers(new ArrayList<>());
            jbpmProcess.setWorkflowType(workflowProcess.getWorkflowType().getPrimaryvalue());
            jbpmProcess.setProcstatus("inprogress");
            System.out.println("jbpm json::" + new Gson().toJson(jbpmProcess));
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess, headers);
            return restTemplate.exchange(baseurl + JBPM.BACKWARDPROCESS, HttpMethod.POST, entity, String.class).getBody();
        }catch (Exception e){
            throw new JBPMServerExpetion(e.getMessage());
        }
    }

    public String holdTask(WorkFlowProcessRest workflowProcess) throws RuntimeException {
        System.out.println(":::::::::::::: holdTask ACTION::::::::::::::::::::::");
       try {
           String baseurl = configurationService.getProperty("jbpm.server");
           JBPMProcess jbpmProcess = new JBPMProcess();
           jbpmProcess.setQueueid(workflowProcess.getId());
           jbpmProcess.setWorkflowType(null);
           System.out.println("jbpm URL::" + baseurl + JBPM.HOLDPROCESS);
           System.out.println("jbpm json::" + new Gson().toJson(jbpmProcess));
           HttpHeaders headers = new HttpHeaders();
           headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
           HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess, headers);
           System.out.println("test body:" + entity.getBody());
           return restTemplate.exchange(baseurl + JBPM.HOLDPROCESS, HttpMethod.PUT, entity, String.class).getBody();
       }catch (Exception e){
           throw new JBPMServerExpetion(e.getMessage());
       }
    }

    public String resumeTask(WorkFlowProcessRest workflowProcess) throws RuntimeException {

        try {
            String baseurl = configurationService.getProperty("jbpm.server");
            JBPMProcess jbpmProcess = new JBPMProcess();
            jbpmProcess.setQueueid(workflowProcess.getId());
            if (workflowProcess.getWorkflowProcessEpersonRests() != null) {
                jbpmProcess.setUsers(workflowProcess.getWorkflowProcessEpersonRests().stream().map(w -> w.getUuid()).collect(Collectors.toList()));
            }
            jbpmProcess.setProcstatus("inprogress");
            System.out.println("jbpm json::" + new Gson().toJson(jbpmProcess));
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess, headers);
            return restTemplate.exchange(baseurl + JBPM.RESUMEPROCESS, HttpMethod.PUT, entity, String.class).getBody();
        }catch (Exception e){
            throw new JBPMServerExpetion(e.getMessage());
        }
        }

    public String refer(WorkFlowProcessRest workflowProcess, String referuserid) throws RuntimeException ,JBPMServerExpetion{

       try {
           String baseurl = configurationService.getProperty("jbpm.server");
           System.out.println("URL :" + baseurl + JBPM.REFERTASK);
           JBPMProcess jbpmProcess = new JBPMProcess();
           jbpmProcess.setWorkflowType(null);
           jbpmProcess.setQueueid(workflowProcess.getId());
           if (workflowProcess.getWorkflowProcessEpersonRests() != null) {
               jbpmProcess.setReferuserid(referuserid);
           }
           System.out.println("jbpm json::" + new Gson().toJson(jbpmProcess));
           HttpHeaders headers = new HttpHeaders();
           headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
           HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess, headers);
           return restTemplate.exchange(baseurl + JBPM.REFERTASK, HttpMethod.POST, entity, String.class).getBody();
       }catch (Exception e){
           throw new JBPMServerExpetion(e.getMessage());
       }
       }

    public String received(WorkFlowProcessRest workflowProcess) throws RuntimeException {
        try {
            String baseurl = configurationService.getProperty("jbpm.server");
            System.out.println("URL :" + baseurl + JBPM.RECEIVED);
            JBPMProcess jbpmProcess = new JBPMProcess();
            jbpmProcess.setWorkflowType(null);
            jbpmProcess.setQueueid(workflowProcess.getId());
            jbpmProcess.setReceiveditem("yes");
            System.out.println("jbpm json::" + new Gson().toJson(jbpmProcess));
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess, headers);
            return restTemplate.exchange(baseurl + JBPM.RECEIVED, HttpMethod.POST, entity, String.class).getBody();
        } catch (Exception e){
                throw new JBPMServerExpetion(e.getMessage());
            }
        }
    public String callback(WorkFlowProcessRest workflowProcess) throws RuntimeException {
        try {
            String baseurl = configurationService.getProperty("jbpm.server");
            System.out.println("URL :" + baseurl + JBPM.CALLBACK);
            JBPMCallbackRequest jbpmCallbackRequest=new JBPMCallbackRequest();
            jbpmCallbackRequest.setQueueid(workflowProcess.getId());
            jbpmCallbackRequest.setProcstatus("inprogress");
            System.out.println("jbpm json::" + new Gson().toJson(jbpmCallbackRequest));
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<JBPMCallbackRequest> entity = new HttpEntity<JBPMCallbackRequest>(jbpmCallbackRequest, headers);
            return restTemplate.exchange(baseurl + JBPM.CALLBACK, HttpMethod.POST, entity, String.class).getBody();
        } catch (Exception e) {
           throw new JBPMServerExpetion(e.getMessage());
        }
    }
    public int htmltopdf(String htmlcontent, FileOutputStream out) throws RuntimeException ,JBPMServerExpetion{
        try {
            // String url = "http://lab.d2t.co:36/api/admin/dspace/htmltopdf";
            String url = configurationService.getProperty("html.to.pdf");
            System.out.println("URL: " + url);
            // Prepare the request model
            HtmltppdfModel jbpmCallbackRequest = new HtmltppdfModel();
            jbpmCallbackRequest.setHtmlContent(htmlcontent);
            //System.out.println("jbpm json: " + new Gson().toJson(jbpmCallbackRequest));
            // Set headers and create the request entity
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<HtmltppdfModel> entity = new HttpEntity<>(jbpmCallbackRequest, headers);
            // Make the POST request
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);

            // Check the response status
            if (response.getStatusCode() == HttpStatus.OK) {
                byte[] pdfBytes = response.getBody();

                // Write the PDF bytes to the provided FileOutputStream
                if (pdfBytes != null) {
                    out.write(pdfBytes);
                    out.flush(); // Ensure all data is written
                    System.out.println("PDF file written successfully!");
                    return 1;
                } else {
                    System.out.println("Error: Response body is null.");
                }
            } else {
                System.out.println("Error: Received response status " + response.getStatusCode());
            }

            System.out.println(":::::::::HTML TO PDF DONE!");
            return 0;

        } catch (Exception e) {
            System.err.println("Error during HTML to PDF conversion: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }


    /*public String callback(WorkFlowProcessRest workflowProcess) throws  RuntimeException{
        String baseurl=configurationService.getProperty("jbpm.server");
        System.out.println("URL :"+baseurl+JBPM.CALLBACK);
        JBPMProcess jbpmProcess=new JBPMProcess();
        jbpmProcess.setQueueid(workflowProcess.getId());
        jbpmProcess.setProcstatus("inprogress");
        jbpmProcess.setWorkflowType(null);
        System.out.println("jbpm json::"+new Gson().toJson(jbpmProcess));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentLength(2);
        HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess,headers);
        return restTemplate.exchange(baseurl+JBPM.CALLBACK, HttpMethod.POST, entity, String.class).getBody();
    }*/
    public String gettasklist(String uuid) throws RuntimeException {
        String baseurl = configurationService.getProperty("jbpm.server");
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(uuid, headers);
        return restTemplate.exchange(baseurl + JBPM.GETTASKLIST + "/" + uuid, HttpMethod.GET, entity, String.class).getBody();
    }
}

