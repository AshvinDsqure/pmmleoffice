/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.dspace.app.rest.model.helper.MyDateConverter;
import org.dspace.content.WorkFlowProcessHistory;
import org.dspace.content.WorkFlowProcessMasterValue;
import org.dspace.content.WorkflowProcess;
import org.dspace.content.WorkflowProcessReferenceDoc;
import org.dspace.eperson.EPerson;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WorkFlowProcessCommentRest extends DSpaceObjectRest {
    public static final String NAME = "workflowprocesscomment";
    public static final String PLURAL_NAME = "workflowprocesscomments";
    public static final String CATEGORY = RestAddressableModel.WORKFLOWPROCESSCOMMENT;
    public static final String GROUPS = "groups";
    private Integer legacyId;
    private String comment;
    private String subject;
    private List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRest = new ArrayList<>();
    private WorkFlowProcessHistoryRest workFlowProcessHistoryRest;

    private EPersonRest submitterRest = null;
    private ItemRest itemRest = null;
    private String margeddocuuid;

    private String sapdocumentno;
    private WorkFlowProcessMasterValueRest sapdocumenttypeRest;

    @JsonProperty
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date actionDate = null;

    @JsonProperty
    private WorkflowProcessReferenceDocRest noteRest;

    private WorkFlowProcessRest workflowProcessRest;
    private Boolean isdraftsave = false;


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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<WorkflowProcessReferenceDocRest> getWorkflowProcessReferenceDocRest() {
        return workflowProcessReferenceDocRest;
    }

    public void setWorkflowProcessReferenceDocRest(List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRest) {
        this.workflowProcessReferenceDocRest = workflowProcessReferenceDocRest;
    }

    public WorkFlowProcessHistoryRest getWorkFlowProcessHistoryRest() {
        return workFlowProcessHistoryRest;
    }

    public void setWorkFlowProcessHistoryRest(WorkFlowProcessHistoryRest workFlowProcessHistoryRest) {
        this.workFlowProcessHistoryRest = workFlowProcessHistoryRest;
    }

    public EPersonRest getSubmitterRest() {
        return submitterRest;
    }

    public void setSubmitterRest(EPersonRest submitterRest) {
        this.submitterRest = submitterRest;
    }

    public WorkFlowProcessRest getWorkflowProcessRest() {
        return workflowProcessRest;
    }

    public void setWorkflowProcessRest(WorkFlowProcessRest workflowProcessRest) {
        this.workflowProcessRest = workflowProcessRest;
    }

    public Date getActionDate() {
        return actionDate;
    }

    public void setActionDate(Date actionDate) {
        this.actionDate = actionDate;
    }

    public WorkflowProcessReferenceDocRest getNoteRest() {
        return noteRest;
    }

    public void setNoteRest(WorkflowProcessReferenceDocRest noteRest) {
        this.noteRest = noteRest;
    }

    public Boolean getIsdraftsave() {
        return isdraftsave;
    }

    public void setIsdraftsave(Boolean isdraftsave) {
        this.isdraftsave = isdraftsave;
    }

    public ItemRest getItemRest() {
        return itemRest;
    }

    public void setItemRest(ItemRest itemRest) {
        this.itemRest = itemRest;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }


    public String getSapdocumentno() {
        return sapdocumentno;
    }

    public void setSapdocumentno(String sapdocumentno) {
        this.sapdocumentno = sapdocumentno;
    }

    public WorkFlowProcessMasterValueRest getSapdocumenttypeRest() {
        return sapdocumenttypeRest;
    }

    public void setSapdocumenttypeRest(WorkFlowProcessMasterValueRest sapdocumenttypeRest) {
        this.sapdocumenttypeRest = sapdocumenttypeRest;
    }

    public String getMargeddocuuid() {
        return margeddocuuid;
    }

    public void setMargeddocuuid(String margeddocuuid) {
        this.margeddocuuid = margeddocuuid;
    }
}
