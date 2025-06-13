/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.utils;

import org.dspace.app.rest.model.PDfObject;
import org.dspace.content.*;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Component
public class PdfOprationServerImpl {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ConfigurationService configurationService;
    public InputStream convertByteArrayToInputStream(byte[] byteArray) {
        return new ByteArrayInputStream(byteArray);
    }
    public InputStream fetchpdfFromhtml(PDfObject pDfObject) throws RuntimeException {
        InputStream inputStream = null;
        try {
            String baseurl = configurationService.getProperty("html.to.pdf");
            System.out.println(":::baseurl:::::::::::"+baseurl);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Access-Control-Allow-Origin", "*.*");
            headers.set("Content-Type", "application/json");
            headers.set("allow_headers", "*");
            HttpEntity<PDfObject> entity = new HttpEntity<PDfObject>(pDfObject, headers);
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(
                    baseurl + DspaceEventAction.HTMLTOPDF,
                    HttpMethod.POST,
                    entity,
                    byte[].class
            );
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                byte[] response = responseEntity.getBody();
                inputStream=  convertByteArrayToInputStream(response);
            } else {
                throw new RuntimeException("Failed to fetch PDF. HTTP Code: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    public  String getHtmlTamplateByType(String type){
        String htmlformatepath=null;
        if(type.equalsIgnoreCase("Transfer Letter")){
            htmlformatepath = configurationService.getProperty("transfer.latter");
        }else if(type.equalsIgnoreCase("Fresh Draft")){
            htmlformatepath = configurationService.getProperty("fresh.draft");
        }else if(type.equalsIgnoreCase("3")){
            System.out.println("with.connected.short.title");
            htmlformatepath = configurationService.getProperty("with.connected.short.title");
        }else if(type.equalsIgnoreCase("4")){
            System.out.println("with.connected.full.title");
            htmlformatepath = configurationService.getProperty("with.connected.full.title");
        }else if(type.equalsIgnoreCase("5")){
            System.out.println("html.pdf.template.officesheet");
            htmlformatepath = configurationService.getProperty("html.pdf.template.officesheet");
        }
        return htmlformatepath;
    }
    public  Map<String,String> getMAPByType(String type,Context context, String editortext, WorkflowProcess workflowProcess){

        if(type.equalsIgnoreCase("Transfer Letter")){
         return getMapTransferLatter(context,editortext,workflowProcess);
        }else if(type.equalsIgnoreCase("Fresh Draft")){
        return getMapFreshDraft(context,editortext,workflowProcess);
        }else if(type.equalsIgnoreCase("3")){

        }else if(type.equalsIgnoreCase("4")){

        }else if(type.equalsIgnoreCase("5")){

        }
        return null;
    }



    public Map<String,String> getMapTransferLatter(Context context, String editortext, WorkflowProcess workflowProcess) throws RuntimeException {
        try {
            String create_department1=null;
            Optional<EpersonToEpersonMapping> maps= context.getCurrentUser().getEpersonToEpersonMappings().stream().filter(d->d.getIsactive()==true).findFirst();
            if (maps.isPresent()) {
                create_department1=  maps.get().getEpersonmapping().getDepartment().getSecondaryvalue();
            }
            String to_department = getToUserList(workflowProcess);
            String cc_department = getccUserList(workflowProcess);
            to_department=to_department!=null?to_department:"NA";
            cc_department=cc_department!=null?cc_department:"NA";
            //create_department1=create_department1!=null?create_department1:"NA";
            Map<String, String> map = new HashMap<>();
            //String d="पिंपरी चिंचवड महानगरपालिका<br>पिंपरी १८,"+create_department1;
           // map.put("create.department", d);
            map.put("to.department","प्रती -"+to_department);
            map.put("cc.department","प्रत -"+cc_department);
            map.put("editor.text",editortext);
            return map;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public Map<String,String> getMapFreshDraft(Context context, String editortext, WorkflowProcess workflowProcess) throws RuntimeException {
        try {
            Map<String, String> map = new HashMap<>();
            String title="पिंपरी चिंचवड महानगरपालिका<br>पिंपरी,";
            String logopath = configurationService.getProperty("pcmc.acknowledgement.logo");
            String base64Image = java.util.Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(logopath)));
            if(base64Image!=null) {
                map.put("logo", base64Image);
            }
            map.put("title", title);
            map.put("editor.text",editortext);
            return map;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public String getToUserList(WorkflowProcess workflowProcess) {
        StringBuffer to_user = new StringBuffer();
        if (workflowProcess.getIsinternal() && workflowProcess.getWorkflowProcessSenderDiaryEpeople() != null) {
            Optional<WorkflowProcessSenderDiary> list = workflowProcess.getWorkflowProcessSenderDiaries().stream().filter(d -> d.getStatus() == 2).findFirst();
            if (workflowProcess.getIsinternal() && workflowProcess.getWorkflowProcessSenderDiaryEpeople() != null) {
                int i = 1;
                for (WorkflowProcessSenderDiaryEperson to : workflowProcess.getWorkflowProcessSenderDiaryEpeople()) {
                    if (to.getUsertype() != null && to.getUsertype().getPrimaryvalue().equalsIgnoreCase("To")) {
                        if (to.getePerson() != null) {
                            if (to.getePerson().getEpersonToEpersonMappings() != null && to.getEpersontoepersonmapping().getEpersonmapping() != null && to.getEpersontoepersonmapping().getEpersonmapping().getDepartment() != null && to.getEpersontoepersonmapping().getEpersonmapping().getDepartment().getPrimaryvalue() != null) {
                                to_user.append(" " + i + ") " + to.getEpersontoepersonmapping().getEpersonmapping().getDepartment().getSecondaryvalue());
                                i++;
                            }
                        }
                    }
                }
            }
            return to_user.toString();
        } else {
            int i = 1;
            if(workflowProcess.getWorkflowProcessSenderDiaries()!=null) {
                for (WorkflowProcessSenderDiary to : workflowProcess.getWorkflowProcessSenderDiaries()) {
                    if (to.getStatus()!=null && to.getStatus() == 2) {
                        if (to.getSendername() != null) {
                            to_user.append(" " + i + ") " + to.getSendername());
                            i++;
                        }
                    }
                }

            }
            return to_user.toString();
        }
    }
    public String getccUserList(WorkflowProcess workflowProcess) {
        StringBuffer cc_user = new StringBuffer();
        if(workflowProcess.getIsinternal() && workflowProcess.getWorkflowProcessSenderDiaryEpeople() != null) {
            Optional<WorkflowProcessSenderDiary> list = workflowProcess.getWorkflowProcessSenderDiaries().stream().filter(d -> d.getStatus() == 2).findFirst();
            if (workflowProcess.getIsinternal() && workflowProcess.getWorkflowProcessSenderDiaryEpeople() != null) {
                int i = 1;
                for (WorkflowProcessSenderDiaryEperson to : workflowProcess.getWorkflowProcessSenderDiaryEpeople()) {
                    if (to.getUsertype() != null && to.getUsertype().getPrimaryvalue().equalsIgnoreCase("cc")) {
                        if (to.getePerson() != null) {
                            if (to.getePerson().getEpersonToEpersonMappings() != null && to.getEpersontoepersonmapping().getEpersonmapping() != null && to.getEpersontoepersonmapping().getEpersonmapping().getDepartment() != null && to.getEpersontoepersonmapping().getEpersonmapping().getDepartment().getPrimaryvalue() != null) {
                                cc_user.append(" " + i + ") " + to.getEpersontoepersonmapping().getEpersonmapping().getDepartment().getSecondaryvalue());
                                i++;
                            }
                        }
                    }
                }
            }
            return cc_user.toString();
        }else{
            int i = 1;
            if(workflowProcess.getWorkflowProcessSenderDiaries()!=null) {
                for (WorkflowProcessSenderDiary to : workflowProcess.getWorkflowProcessSenderDiaries()) {
                    if (to.getStatus()!=null && to.getStatus() == 3) {
                        if (to.getSendername() != null) {
                            cc_user.append(" " + i + ") " + to.getSendername());
                            i++;
                        }
                    }
                }

            }
            return cc_user.toString();
        }
    }
    class GuestTokenRequest {
        private List<Resources> resources= new ArrayList<>();
        private List<Rls> rls = new ArrayList<>();
        private User user = new User("admin","admin","admin");

        public List<Resources> getResources() {
            return resources;
        }
        public void addresources( Resources resourcesobj){
            resources.add(resourcesobj);
        }
        public void setResources(List<Resources> resourceslist) {
            this.resources = resourceslist;
        }

        public List<Rls> getRls() {
            return rls;
        }

        public void setRls(List<Rls> rls) {
            this.rls = rls;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    class Resources {
        private String type;
        private String id;

        public Resources(String type, String id) {
            this.type = type;
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    class Rls {

    }

    class User {
        private String username;
        private String first_name;
        private String last_name;

        public User(String username, String first_name, String last_name) {
            this.username = username;
            this.first_name = first_name;
            this.last_name = last_name;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFirst_name() {
            return first_name;
        }

        public void setFirst_name(String first_name) {
            this.first_name = first_name;
        }

        public String getLast_name() {
            return last_name;
        }

        public void setLast_name(String last_name) {
            this.last_name = last_name;
        }
    }
}

