/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.jbpm.models;


import lombok.Data;
import org.dspace.app.rest.model.WorkFlowProcessRest;
import org.dspace.app.rest.model.WorkflowProcessEpersonRest;
import org.dspace.content.WorkflowProcess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JBPMProcess implements Serializable {
    private String queueid;
    private String initiator;
    private List<Object> users;

    private String dispatch;

    private String referuserid;
    private Integer jbpmprocid;
    private Integer jbpmtaskid;
    private String procstatus;

    private String receiveditem;
    private String workflowType = "Inward";

    public JBPMProcess() {

    }

    public JBPMProcess(WorkFlowProcessRest workflowProcess) {
        this.queueid = workflowProcess.getId();
        Optional<WorkflowProcessEpersonRest> optionalWorkflowProcessEpersonRest = workflowProcess.getWorkflowProcessEpersonRests().stream().filter(d -> d.getIndex() == 0).findFirst();
        if (optionalWorkflowProcessEpersonRest.isPresent()) {
            this.initiator = optionalWorkflowProcessEpersonRest.get().getUuid();
        } else {
            this.initiator = "Dsquare";
        }
        if (workflowProcess.getWorkflowType() != null && workflowProcess.getWorkflowType().getPrimaryvalue() != null && workflowProcess.getWorkflowType().getPrimaryvalue().equalsIgnoreCase("Inward")) {
            if (workflowProcess.getDispatchModeRest() != null && workflowProcess.getDispatchModeRest().getPrimaryvalue() != null) {
                this.dispatch = workflowProcess.getDispatchModeRest().getPrimaryvalue().toLowerCase();
            }
        } else if (workflowProcess.getWorkflowType() != null && workflowProcess.getWorkflowType().getPrimaryvalue() != null && workflowProcess.getWorkflowType().getPrimaryvalue().equalsIgnoreCase("Outward")) {
            if (workflowProcess.getWorkFlowProcessOutwardDetailsRest() != null && workflowProcess.getWorkFlowProcessOutwardDetailsRest().getOutwardmediumRest() != null && workflowProcess.getWorkFlowProcessOutwardDetailsRest().getOutwardmediumRest().getPrimaryvalue() != null) {
                this.dispatch = workflowProcess.getWorkFlowProcessOutwardDetailsRest().getOutwardmediumRest().getPrimaryvalue().toLowerCase();
            }
        } else {
            this.dispatch = "electronic";
        }
        this.users = new ArrayList<>();

    }

    public String getQueueid() {
        return queueid;
    }

    public void setQueueid(String queueid) {
        this.queueid = queueid;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public List<Object> getUsers() {
        return users;
    }

    public void setUsers(List<Object> users) {
        this.users = users;
    }

    public String getDispatch() {
        return dispatch;
    }

    public void setDispatch(String dispatch) {
        this.dispatch = dispatch;
    }


    public Integer getJbpmprocid() {
        return jbpmprocid;
    }

    public void setJbpmprocid(Integer jbpmprocid) {
        this.jbpmprocid = jbpmprocid;
    }

    public Integer getJbpmtaskid() {
        return jbpmtaskid;
    }

    public void setJbpmtaskid(Integer jbpmtaskid) {
        this.jbpmtaskid = jbpmtaskid;
    }

    public String getProcstatus() {
        return procstatus;
    }

    public void setProcstatus(String procstatus) {
        this.procstatus = procstatus;
    }

    public String getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
    }

    public String getReferuserid() {
        return referuserid;
    }

    public void setReferuserid(String referuserid) {
        this.referuserid = referuserid;
    }

    public String getReceiveditem() {
        return receiveditem;
    }

    public void setReceiveditem(String receiveditem) {
        this.receiveditem = receiveditem;
    }
}
