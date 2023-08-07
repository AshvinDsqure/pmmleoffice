/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.dspace.app.rest.enums.WorkFlowUserType;
import org.dspace.app.rest.model.helper.MyDateConverter;
import org.dspace.eperson.EPerson;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotBlank;
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
public class WorkflowProcessEpersonRest extends DSpaceObjectRest {
    public static final String NAME = "workflowprocesseperson";
    public static final String PLURAL_NAME = "WorkflowProcessDefinitionEpersonRest";
    public static final String CATEGORY = RestAddressableModel.CORE;
    @JsonProperty
    private Integer index;
    @JsonProperty()
    @NotBlank
    private EPersonRest ePersonRest = null;
    @JsonProperty()
    private WorkFlowProcessDefinitionRest workflowProcessReferenceDocRest;
    @JsonProperty
    @NotBlank
    private WorkFlowProcessMasterValueRest departmentRest = null;
    @JsonProperty
    @NotBlank
    private WorkFlowProcessMasterValueRest officeRest = null;
    @JsonProperty
    private WorkFlowProcessMasterValueRest userType = null;

    @JsonProperty
    private List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRests = null;

    @JsonProperty
    private List<EPersonRest> ePersonRests = new ArrayList<>();

    @JsonProperty
    private WorkFlowProcessMasterValueRest dispatchModeRest = null;
    @JsonProperty
    private WorkFlowProcessMasterValueRest eligibleForFilingRest = null;

    @JsonProperty
    private ItemRest itemRest = null;
    @JsonProperty
    private String comment;
    @JsonProperty
    private  Boolean initiator=false;
    @Transient
    private WorkFlowUserType workFlowUserType;
    @JsonProperty
    private Integer sequence;
    @JsonProperty
    private  Boolean isOwner;

    @JsonProperty
    private  Boolean issequence=false;

    @JsonProperty
    private  Boolean isrefer=false;
    @JsonProperty
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date assignDate =null;

    @Override
    public String getCategory() {
        return CATEGORY;
    }
    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getType() {
        return NAME;
    }

    public Integer getIndex() {
        return index;
    }


    public Date getAssignDate() {
        return assignDate;
    }

    public void setAssignDate(Date assignDate) {
        this.assignDate = assignDate;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public EPersonRest getePersonRest() {
        return ePersonRest;
    }

    public void setePersonRest(EPersonRest ePersonRest) {
        this.ePersonRest = ePersonRest;
    }

    public WorkFlowProcessDefinitionRest getWorkflowProcessReferenceDocRest() {
        return workflowProcessReferenceDocRest;
    }

    public void setWorkflowProcessReferenceDocRest(WorkFlowProcessDefinitionRest workflowProcessReferenceDocRest) {
        this.workflowProcessReferenceDocRest = workflowProcessReferenceDocRest;
    }

    public WorkFlowProcessMasterValueRest getDepartmentRest() {
        return departmentRest;
    }

    public void setDepartmentRest(WorkFlowProcessMasterValueRest departmentRest) {
        this.departmentRest = departmentRest;
    }

    public WorkFlowProcessMasterValueRest getOfficeRest() {
        return officeRest;
    }

    public void setOfficeRest(WorkFlowProcessMasterValueRest officeRest) {
        this.officeRest = officeRest;
    }

    public WorkFlowProcessMasterValueRest getUserType() {
        return userType;
    }

    public void setUserType(WorkFlowProcessMasterValueRest userType) {
        this.userType = userType;
    }

    public WorkFlowUserType getWorkFlowUserType() {
        return workFlowUserType;
    }

    public void setWorkFlowUserType(WorkFlowUserType workFlowUserType) {
        this.workFlowUserType = workFlowUserType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getInitiator() {
        return initiator;
    }

    public void setInitiator(Boolean initiator) {
        this.initiator = initiator;
    }

    public ItemRest getItemRest() {
        return itemRest;
    }

    public void setItemRest(ItemRest itemRest) {
        this.itemRest = itemRest;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public Boolean getIssequence() {
        return issequence;
    }

    public void setIssequence(Boolean issequence) {
        this.issequence = issequence;
    }

    public Boolean getOwner() {
        return isOwner;
    }

    public void setOwner(Boolean owner) {
        isOwner = owner;
    }

    public List<WorkflowProcessReferenceDocRest> getWorkflowProcessReferenceDocRests() {
        return workflowProcessReferenceDocRests;
    }
    public void setWorkflowProcessReferenceDocRests(List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRests) {
        this.workflowProcessReferenceDocRests = workflowProcessReferenceDocRests;
    }

    public Boolean getIsrefer() {
        return isrefer;
    }

    public void setIsrefer(Boolean isrefer) {
        this.isrefer = isrefer;
    }

    public List<EPersonRest> getePersonRests() {
        return ePersonRests;
    }

    public void setePersonRests(List<EPersonRest> ePersonRests) {
        this.ePersonRests = ePersonRests;
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
}
