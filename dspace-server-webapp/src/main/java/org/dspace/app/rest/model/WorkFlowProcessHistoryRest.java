/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.dspace.app.rest.model.helper.MyDateConverter;

import java.util.Date;
@LinksRest(links = {
        @LinkRest(
                name = WorkflowItemRest.STEP,
                method = "getStep"
        )
})
public class WorkFlowProcessHistoryRest extends  DSpaceObjectRest{
    public static final String NAME = "workflowprocesshistorie";
    public static final String PLURAL_NAME = "workflowprocesshistories";
    public static final String CATEGORY = RestAddressableModel.WORKFLOWPROCESSHISTORY;

    public static final String GROUPS = "groups";

    private Integer legacyId;

    private WorkflowProcessEpersonRest workflowProcessEpersonRest;
    private WorkflowProcessEpersonRest senttoRest;

    private WorkFlowProcessMasterValueRest action;

    private String comment;
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date actionDate;
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date receivedDate;
    private String sentbyname;
    private String senttoname;


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public WorkFlowProcessMasterValueRest getAction() {
        return action;
    }

    public void setAction(WorkFlowProcessMasterValueRest action) {
        this.action = action;
    }

    public WorkflowProcessEpersonRest getWorkflowProcessEpersonRest() {
        return workflowProcessEpersonRest;
    }

    public void setWorkflowProcessEpersonRest(WorkflowProcessEpersonRest workflowProcessEpersonRest) {
        this.workflowProcessEpersonRest = workflowProcessEpersonRest;
    }

    public Integer getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getType() {
        return NAME;
    }


    public Date getActionDate() {
        return actionDate;
    }

    public void setActionDate(Date actionDate) {
        this.actionDate = actionDate;
    }

    public WorkflowProcessEpersonRest getSenttoRest() {
        return senttoRest;
    }

    public void setSenttoRest(WorkflowProcessEpersonRest senttoRest) {
        this.senttoRest = senttoRest;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getSentbyname() {
        return sentbyname;
    }

    public void setSentbyname(String sentbyname) {
        this.sentbyname = sentbyname;
    }

    public String getSenttoname() {
        return senttoname;
    }

    public void setSenttoname(String senttoname) {
        this.senttoname = senttoname;
    }
}
