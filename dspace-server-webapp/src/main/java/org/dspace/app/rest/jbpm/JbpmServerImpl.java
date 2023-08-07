/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.jbpm;

import com.google.gson.Gson;
import org.dspace.app.rest.enums.WorkFlowAction;
import org.dspace.app.rest.jbpm.models.JBPMResponse;
import org.dspace.app.rest.model.WorkFlowProcessRest;
import org.dspace.app.rest.model.WorkflowProcessEpersonRest;
import org.dspace.content.WorkflowProcess;
import org.dspace.app.rest.jbpm.constant.JBPM;
import org.dspace.app.rest.jbpm.models.JBPMProcess;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


import java.util.*;
import java.util.stream.Collectors;

@Component
public class JbpmServerImpl {
    @Autowired
    public RestTemplate restTemplate;
    @Autowired
    private ConfigurationService configurationService;
    public String startProcess(WorkFlowProcessRest workflowProcessw,List<String> users) throws  RuntimeException{
        System.out.println("::::::::::::::CREATE ACTION::::::::::::::::::::::");
        String baseurl=configurationService.getProperty("jbpm.server");
        JBPMProcess jbpmProcess=new JBPMProcess(workflowProcessw);
        jbpmProcess.setUsers(new ArrayList<Object>(users));
        jbpmProcess.setWorkflowType(workflowProcessw.getWorkflowType().getPrimaryvalue());
        System.out.println("jbpm json::Request"+new Gson().toJson(jbpmProcess));
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess,headers);
        System.out.println("::::::::::::::URL::::::::::::::::::::::"+baseurl+ JBPM.CREATEPROCESS);
        return restTemplate.exchange(baseurl+ JBPM.CREATEPROCESS, HttpMethod.POST, entity, String.class).getBody();

    }
    public String   forwardTask(WorkFlowProcessRest workflowProcess,List<String> users) throws  RuntimeException{
        System.out.println("::::::::::::::FORWARD ACTION::::::::::::::::::::::");
        String baseurl=configurationService.getProperty("jbpm.server");
        JBPMProcess jbpmProcess=new JBPMProcess();
        jbpmProcess.setQueueid(workflowProcess.getId());
        jbpmProcess.setWorkflowType(workflowProcess.getWorkflowType().getPrimaryvalue());
        jbpmProcess.setUsers(new ArrayList<Object>(users));
        jbpmProcess.setProcstatus("inprogress");
        System.out.println("jbpm json::"+new Gson().toJson(jbpmProcess));
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess,headers);
        return restTemplate.exchange(baseurl+ JBPM.FORWARDPROCESS, HttpMethod.POST, entity, String.class).getBody();

    }
    public String completeTask(WorkFlowProcessRest workflowProcess,List<String> users) throws  RuntimeException{
        System.out.println("::::::::::::::COMPLETE ACTION::::::::::::::::::::::");
        String baseurl=configurationService.getProperty("jbpm.server");
        JBPMProcess jbpmProcess=new JBPMProcess();
        jbpmProcess.setQueueid(workflowProcess.getId());
        jbpmProcess.setWorkflowType(workflowProcess.getWorkflowType().getPrimaryvalue());
        jbpmProcess.setUsers(new ArrayList<Object>(users));
        jbpmProcess.setProcstatus("completed");
        System.out.println("jbpm json::"+new Gson().toJson(jbpmProcess));
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess,headers);
        return restTemplate.exchange(baseurl+ JBPM.FORWARDPROCESS, HttpMethod.POST, entity, String.class).getBody();

    }
    public String dispatchReady(WorkFlowProcessRest workflowProcess,List<String> users,List<String> dispatchUsers) throws  RuntimeException{
        System.out.println(":::::::::::::: DISPATCH READY ACTION::::::::::::::::::::::");
        String baseurl=configurationService.getProperty("jbpm.server");
        JBPMProcess jbpmProcess=new JBPMProcess();
        jbpmProcess.setQueueid(workflowProcess.getId());
        jbpmProcess.setWorkflowType(workflowProcess.getWorkflowType().getPrimaryvalue());
        List<Object> usersobj= new ArrayList<Object>(users);
        System.out.println("usersobj current user:"+new Gson().toJson(usersobj));
        usersobj.add(dispatchUsers);
        System.out.println("user"+dispatchUsers);

        System.out.println("final make objeck like "+usersobj);
        jbpmProcess.setUsers(usersobj);
        jbpmProcess.setProcstatus("inprogress");
        System.out.println("jbpm json::"+new Gson().toJson(jbpmProcess));
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess,headers);
        System.out.println("::::::::::::::URL::::::::::::::::::::::"+baseurl+ JBPM.FORWARDPROCESS);
        return restTemplate.exchange(baseurl+ JBPM.FORWARDPROCESS, HttpMethod.POST, entity, String.class).getBody();

    }
    public String backwardTask(WorkFlowProcessRest workflowProcess) throws  RuntimeException{
        System.out.println(":::::::::::::: BACKWARD ACTION::::::::::::::::::::::");
        String baseurl=configurationService.getProperty("jbpm.server");
        JBPMProcess jbpmProcess=new JBPMProcess();
        jbpmProcess.setQueueid(workflowProcess.getId());
        jbpmProcess.setUsers(new ArrayList<>());
        jbpmProcess.setWorkflowType(workflowProcess.getWorkflowType().getPrimaryvalue());
        jbpmProcess.setProcstatus("inprogress");
        System.out.println("jbpm json::"+new Gson().toJson(jbpmProcess));
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess,headers);
        return restTemplate.exchange(baseurl+ JBPM.BACKWARDPROCESS, HttpMethod.POST, entity, String.class).getBody();
    }
    public String holdTask(WorkFlowProcessRest workflowProcess) throws  RuntimeException{
        System.out.println(":::::::::::::: holdTask ACTION::::::::::::::::::::::");
        String baseurl=configurationService.getProperty("jbpm.server");
        JBPMProcess jbpmProcess=new JBPMProcess();
        jbpmProcess.setQueueid(workflowProcess.getId());
        jbpmProcess.setWorkflowType(null);
       /* if(workflowProcess.getWorkflowProcessEpersonRests()!= null) {
            jbpmProcess.setUsers(workflowProcess.getWorkflowProcessEpersonRests().stream().map(w -> w.getUuid()).collect(Collectors.toList()));
        }*/
        //jbpmProcess.setProcstatus("inprogress");

        System.out.println("jbpm URL::"+baseurl+ JBPM.HOLDPROCESS);
        System.out.println("jbpm json::"+new Gson().toJson(jbpmProcess));
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess,headers);
        System.out.println("test body:"+entity.getBody());
        return restTemplate.exchange(baseurl+ JBPM.HOLDPROCESS, HttpMethod.PUT, entity, String.class).getBody();
    }
    public String resumeTask(WorkFlowProcessRest workflowProcess) throws  RuntimeException{
        String baseurl=configurationService.getProperty("jbpm.server");
        JBPMProcess jbpmProcess=new JBPMProcess();
        jbpmProcess.setQueueid(workflowProcess.getId());
        if(workflowProcess.getWorkflowProcessEpersonRests()!= null) {
            jbpmProcess.setUsers(workflowProcess.getWorkflowProcessEpersonRests().stream().map(w -> w.getUuid()).collect(Collectors.toList()));
        }
        jbpmProcess.setProcstatus("inprogress");
        System.out.println("jbpm json::"+new Gson().toJson(jbpmProcess));
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess,headers);
        return restTemplate.exchange(baseurl+ JBPM.RESUMEPROCESS, HttpMethod.PUT, entity, String.class).getBody();
    }
    public String refer(WorkFlowProcessRest workflowProcess,String referuserid) throws  RuntimeException{
        String baseurl=configurationService.getProperty("jbpm.server");
        System.out.println("URL :"+baseurl+ JBPM.REFERTASK);
        JBPMProcess jbpmProcess=new JBPMProcess();
        jbpmProcess.setWorkflowType(null);
        jbpmProcess.setQueueid(workflowProcess.getId());
        if(workflowProcess.getWorkflowProcessEpersonRests()!= null) {
            jbpmProcess.setReferuserid(referuserid);
        }
        System.out.println("jbpm json::"+new Gson().toJson(jbpmProcess));
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess,headers);
        return restTemplate.exchange(baseurl+ JBPM.REFERTASK, HttpMethod.POST, entity, String.class).getBody();
    }
    public String received(WorkFlowProcessRest workflowProcess) throws  RuntimeException{
        String baseurl=configurationService.getProperty("jbpm.server");
        System.out.println("URL :"+baseurl+ JBPM.RECEIVED);
        JBPMProcess jbpmProcess=new JBPMProcess();
        jbpmProcess.setWorkflowType(null);
        jbpmProcess.setQueueid(workflowProcess.getId());
       jbpmProcess.setReceiveditem("yes");
        System.out.println("jbpm json::"+new Gson().toJson(jbpmProcess));
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<JBPMProcess> entity = new HttpEntity<JBPMProcess>(jbpmProcess,headers);
        return restTemplate.exchange(baseurl+ JBPM.RECEIVED, HttpMethod.POST, entity, String.class).getBody();
    }
    public String gettasklist(String uuid) throws  RuntimeException{
        String baseurl=configurationService.getProperty("jbpm.server");
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(uuid,headers);
        return restTemplate.exchange(baseurl+ JBPM.GETTASKLIST+"/"+uuid, HttpMethod.GET, entity, String.class).getBody();
    }
}

