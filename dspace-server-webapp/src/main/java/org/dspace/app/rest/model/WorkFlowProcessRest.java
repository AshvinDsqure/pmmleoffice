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
import lombok.Data;
import org.dspace.app.rest.model.helper.MyDateConverter;
import org.dspace.app.rest.validation.WorkflowProcessMasterValueValid;
import org.dspace.app.rest.validation.WorkflowProcessValid;
import org.dspace.content.Item;
import org.dspace.content.WorkflowProcessEperson;
import org.dspace.content.WorkflowProcessSenderDiary;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Item REST Resource
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@LinksRest(links = {

})
public class WorkFlowProcessRest extends DSpaceObjectRest {
    public static final String NAME = "workflowprocesse";
    public static final String PLURAL_NAME = "workflowprocesses";
    public static final String CATEGORY = RestAddressableModel.WORKFLOWPROCESS;
    public static final String CATEGORY_INWARD = "inward";
    public static final String CATEGORY_OUTWARE = "outward";
    public static final String CATEGORY_DRAFT = "draft";
    @JsonProperty
    private WorkFlowProcessDraftDetailsRest workFlowProcessDraftDetailsRest;
    @JsonProperty
    private WorkflowProcessNoteRest workflowProcessNoteRest;
    @JsonProperty
    private WorkFlowProcessInwardDetailsRest workFlowProcessInwardDetailsRest;
    @JsonProperty
    private WorkFlowProcessOutwardDetailsRest workFlowProcessOutwardDetailsRest;
    @JsonProperty
    @Valid
    private WorkflowProcessSenderDiaryRest workflowProcessSenderDiaryRest;
    @JsonProperty
    @Valid
    @NotNull
    private WorkFlowProcessMasterValueRest dispatchModeRest = null;
    @JsonProperty
    private WorkFlowProcessMasterValueRest workflowStatus = null;
    @JsonProperty
    private WorkFlowProcessMasterValueRest workflowType = null;

    @JsonProperty
    private WorkFlowProcessMasterValueRest documenttypeRest = null;
    @JsonProperty
    private WorkFlowProcessMasterValueRest actionRest = null;

    private SAPResponse sapResponse;

    public WorkFlowProcessMasterValueRest getActionRest() {
        return actionRest;
    }

    public void setActionRest(WorkFlowProcessMasterValueRest actionRest) {
        this.actionRest = actionRest;
    }

    @JsonProperty
    private WorkFlowProcessMasterValueRest eligibleForFilingRest = null;
    @JsonProperty
    private WorkflowProcessEpersonRest owner = null;
    @JsonProperty
    private WorkflowProcessEpersonRest sender = null;
    @JsonProperty
    private ItemRest itemRest;
    @JsonProperty
    @NotBlank
    private String Subject;
    @JsonProperty
    private String remark;
    @JsonProperty
    private String workflowTypeStr;
    @JsonProperty
    private String sendername;
    @JsonProperty
    private String currentrecipient;
    @JsonProperty
    private String mode;

    @JsonProperty
    private String body;

    @JsonProperty
    private  Boolean ismode =false;

    @JsonProperty
    private  Boolean isread =false;
    @JsonProperty
    private Boolean isDraft = false;

    private Boolean isreplydraft = false;

    private Boolean isacknowledgement = false;
    @JsonProperty
    private  Boolean issignnote=false;

    @JsonProperty
    private String comment;
    private String margeddocuuid;
    @JsonProperty
    private String workflowtype;
    @JsonProperty
    private String workflowstatus;
    @JsonProperty
    private String priority;
    @JsonProperty
    private List<ItemRest> itemsRests=new ArrayList<>();
    @JsonProperty
    List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRests = new ArrayList<>();
    @JsonProperty
    WorkFlowProcessCommentRest workFlowProcessCommentRest=null;


    @JsonProperty
    List<WorkflowProcessSenderDiaryRest> workflowProcessSenderDiaryRests = new ArrayList<>();

    @JsonProperty
    @NotEmpty(message = "Input WorkflowProcessEpersonRest list cannot be empty.")
    private List<WorkflowProcessEpersonRest> workflowProcessEpersonRests = new ArrayList<>();
    @JsonProperty
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date InitDate = new Date();

    @JsonProperty
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date dateRecived = new Date();

    @JsonProperty
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date dueDate = new Date();


    @JsonProperty
    private WorkFlowProcessMasterValueRest priorityRest = null;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String entityType = null;

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getType() {
        return NAME;
    }

    public WorkFlowProcessInwardDetailsRest getWorkFlowProcessInwardDetailsRest() {
        return workFlowProcessInwardDetailsRest;
    }

    public void setWorkFlowProcessInwardDetailsRest(WorkFlowProcessInwardDetailsRest workFlowProcessInwardDetailsRest) {
        this.workFlowProcessInwardDetailsRest = workFlowProcessInwardDetailsRest;
    }

    public WorkflowProcessSenderDiaryRest getWorkflowProcessSenderDiaryRest() {
        return workflowProcessSenderDiaryRest;
    }

    public void setWorkflowProcessSenderDiaryRest(WorkflowProcessSenderDiaryRest workflowProcessSenderDiaryRest) {
        this.workflowProcessSenderDiaryRest = workflowProcessSenderDiaryRest;
    }

    public WorkFlowProcessMasterValueRest getDispatchModeRest() {
        return dispatchModeRest;
    }

    public void setDispatchModeRest(WorkFlowProcessMasterValueRest dispatchModeRest) {
        this.dispatchModeRest = dispatchModeRest;
    }

    public WorkFlowProcessMasterValueRest getEligibleForFilingRest() {
        return eligibleForFilingRest;
    }

    public void setEligibleForFilingRest(WorkFlowProcessMasterValueRest eligibleForFilingRest) {
        this.eligibleForFilingRest = eligibleForFilingRest;
    }

    public Boolean getIssignnote() {
        return issignnote;
    }

    public void setIssignnote(Boolean issignnote) {
        this.issignnote = issignnote;
    }

    public ItemRest getItemRest() {
        return itemRest;
    }

    public void setItemRest(ItemRest itemRest) {
        this.itemRest = itemRest;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public List<WorkflowProcessReferenceDocRest> getWorkflowProcessReferenceDocRests() {
        return workflowProcessReferenceDocRests;
    }

    public void setWorkflowProcessReferenceDocRests(List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRests) {
        this.workflowProcessReferenceDocRests = workflowProcessReferenceDocRests;
    }

    public List<WorkflowProcessEpersonRest> getWorkflowProcessEpersonRests() {
        return workflowProcessEpersonRests;
    }

    public void setWorkflowProcessEpersonRests(List<WorkflowProcessEpersonRest> workflowProcessEpersonRests) {
        this.workflowProcessEpersonRests = workflowProcessEpersonRests;
    }

    public Date getInitDate() {
        return InitDate;
    }

    public void setInitDate(Date initDate) {
        InitDate = initDate;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public WorkFlowProcessMasterValueRest getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(WorkFlowProcessMasterValueRest workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public WorkFlowProcessMasterValueRest getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(WorkFlowProcessMasterValueRest workflowType) {
        this.workflowType = workflowType;
    }

    public Boolean getDraft() {
        return isDraft;
    }

    public void setDraft(Boolean draft) {
        isDraft = draft;
    }

    public String getWorkflowTypeStr() {
        return workflowTypeStr;
    }

    public void setWorkflowTypeStr(String workflowTypeStr) {
        this.workflowTypeStr = workflowTypeStr;
    }

    public WorkflowProcessEpersonRest getOwner() {
        return owner;
    }

    public void setOwner(WorkflowProcessEpersonRest owner) {
        this.owner = owner;
    }

    public WorkflowProcessEpersonRest getSender() {
        return sender;
    }

    public void setSender(WorkflowProcessEpersonRest sender) {
        this.sender = sender;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public WorkFlowProcessOutwardDetailsRest getWorkFlowProcessOutwardDetailsRest() {
        return workFlowProcessOutwardDetailsRest;
    }

    public void setWorkFlowProcessOutwardDetailsRest(WorkFlowProcessOutwardDetailsRest workFlowProcessOutwardDetailsRest) {
        this.workFlowProcessOutwardDetailsRest = workFlowProcessOutwardDetailsRest;
    }

    public WorkFlowProcessMasterValueRest getPriorityRest() {
        return priorityRest;
    }

    public void setPriorityRest(WorkFlowProcessMasterValueRest priorityRest) {
        this.priorityRest = priorityRest;
    }

    public WorkFlowProcessDraftDetailsRest getWorkFlowProcessDraftDetailsRest() {
        return workFlowProcessDraftDetailsRest;
    }

    public void setWorkFlowProcessDraftDetailsRest(WorkFlowProcessDraftDetailsRest workFlowProcessDraftDetailsRest) {
        this.workFlowProcessDraftDetailsRest = workFlowProcessDraftDetailsRest;
    }

    public WorkflowProcessNoteRest getWorkflowProcessNoteRest() {
        return workflowProcessNoteRest;
    }

    public void setWorkflowProcessNoteRest(WorkflowProcessNoteRest workflowProcessNoteRest) {
        this.workflowProcessNoteRest = workflowProcessNoteRest;
    }

    public Date getDateRecived() {
        return dateRecived;
    }

    public void setDateRecived(Date dateRecived) {
        this.dateRecived = dateRecived;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getSendername() {
        return sendername;
    }

    public void setSendername(String sendername) {
        this.sendername = sendername;
    }

    public String getWorkflowtype() {
        return workflowtype;
    }

    public void setWorkflowtype(String workflowtype) {
        this.workflowtype = workflowtype;
    }

    public String getWorkflowstatus() {
        return workflowstatus;
    }

    public void setWorkflowstatus(String workflowstatus) {
        this.workflowstatus = workflowstatus;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Boolean getIsmode() {
        return ismode;
    }

    public void setIsmode(Boolean ismode) {
        this.ismode = ismode;
    }

    public Boolean getIsread() {
        return isread;
    }

    public void setIsread(Boolean isread) {
        this.isread = isread;
    }

    public List<ItemRest> getItemsRests() {
        return itemsRests;
    }

    public void setItemsRests(List<ItemRest> itemsRests) {
        this.itemsRests = itemsRests;
    }

    public String getCurrentrecipient() {
        return currentrecipient;
    }

    public void setCurrentrecipient(String currentrecipient) {
        this.currentrecipient = currentrecipient;
    }

    @Override
    public String toString() {
        return "WorkFlowProcessRest{" +
                "workFlowProcessDraftDetailsRest=" + workFlowProcessDraftDetailsRest +
                ", workflowProcessNoteRest=" + workflowProcessNoteRest +
                ", workFlowProcessInwardDetailsRest=" + workFlowProcessInwardDetailsRest +
                ", workFlowProcessOutwardDetailsRest=" + workFlowProcessOutwardDetailsRest +
                ", workflowProcessSenderDiaryRest=" + workflowProcessSenderDiaryRest +
                ", dispatchModeRest=" + dispatchModeRest +
                ", workflowStatus=" + workflowStatus +
                ", workflowType=" + workflowType +
                ", eligibleForFilingRest=" + eligibleForFilingRest +
                ", owner=" + owner +
                ", sender=" + sender +
                ", itemRest=" + itemRest +
                ", Subject='" + Subject + '\'' +
                ", workflowTypeStr='" + workflowTypeStr + '\'' +
                ", sendername='" + sendername + '\'' +
                ", currentrecipient='" + currentrecipient + '\'' +
                ", mode='" + mode + '\'' +
                ", ismode=" + ismode +
                ", isDraft=" + isDraft +
                ", comment='" + comment + '\'' +
                ", workflowtype='" + workflowtype + '\'' +
                ", workflowstatus='" + workflowstatus + '\'' +
                ", priority='" + priority + '\'' +
                ", itemsRests=" + itemsRests +
                ", workflowProcessReferenceDocRests=" + workflowProcessReferenceDocRests +
                ", workflowProcessEpersonRests=" + workflowProcessEpersonRests +
                ", InitDate=" + InitDate +
                ", dateRecived=" + dateRecived +
                ", dueDate=" + dueDate +
                ", priorityRest=" + priorityRest +
                ", entityType='" + entityType + '\'' +
                '}';
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public WorkFlowProcessMasterValueRest getDocumenttypeRest() {
        return documenttypeRest;
    }

    public void setDocumenttypeRest(WorkFlowProcessMasterValueRest documenttypeRest) {
        this.documenttypeRest = documenttypeRest;
    }

    public List<WorkflowProcessSenderDiaryRest> getWorkflowProcessSenderDiaryRests() {
        return workflowProcessSenderDiaryRests;
    }

    public void setWorkflowProcessSenderDiaryRests(List<WorkflowProcessSenderDiaryRest> workflowProcessSenderDiaryRests) {
        this.workflowProcessSenderDiaryRests = workflowProcessSenderDiaryRests;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Boolean getIsreplydraft() {
        return isreplydraft;
    }

    public void setIsreplydraft(Boolean isreplydraft) {
        this.isreplydraft = isreplydraft;
    }

    public String getMargeddocuuid() {
        return margeddocuuid;
    }

    public void setMargeddocuuid(String margeddocuuid) {
        this.margeddocuuid = margeddocuuid;
    }

    public Boolean getIsacknowledgement() {
        return isacknowledgement;
    }

    public void setIsacknowledgement(Boolean isacknowledgement) {
        this.isacknowledgement = isacknowledgement;
    }

    public WorkFlowProcessCommentRest getWorkFlowProcessCommentRest() {
        return workFlowProcessCommentRest;
    }

    public void setWorkFlowProcessCommentRest(WorkFlowProcessCommentRest workFlowProcessCommentRest) {
        this.workFlowProcessCommentRest = workFlowProcessCommentRest;
    }

    public SAPResponse getSapResponse() {
        return sapResponse;
    }

    public void setSapResponse(SAPResponse sapResponse) {
        this.sapResponse = sapResponse;
    }
}
