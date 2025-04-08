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
import org.dspace.content.WorkFlowProcessMasterValue;
import org.dspace.content.WorkflowProcessEperson;

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
public class WorkFlowProcessFilterRest extends DSpaceObjectRest {
    public static final String NAME = "workflowprocessefilter";
    public static final String PLURAL_NAME = "workflowprocessefilters";
    public static final String CATEGORY = RestAddressableModel.WORKFLOWPROCESS;

    @JsonProperty
    private WorkFlowProcessMasterValueRest priorityRest;
    @JsonProperty
    private WorkFlowProcessMasterValueRest workflowStatusRest;
    @JsonProperty
    private WorkFlowProcessMasterValueRest workflowTypeRest;
    @JsonProperty
    private WorkFlowProcessMasterValueRest departmentRest;
    @JsonProperty
    private EPersonRest ePersonRest;
    @JsonProperty
    private String subject;
    @JsonProperty
    private String inward;

    @JsonProperty
    private String year;
    @JsonProperty
    private String outward;
    @JsonProperty
    private String inwarddate;
    @JsonProperty
    private String outwarddate;
    @JsonProperty
    private String receiveddate;
    @JsonProperty
    private WorkFlowProcessMasterValueRest categoryRest;
    @JsonProperty
    private WorkFlowProcessMasterValueRest subcategoryRest;
    @JsonProperty
    private WorkFlowProcessMasterValueRest officeRest;
    @JsonProperty
    private WorkFlowProcessMasterValueRest inwardmodeRest;
    @JsonProperty
    private WorkFlowProcessMasterValueRest outwardmodeRest;
    @JsonProperty
    private WorkFlowProcessMasterValueRest outwardmediumRest;
    @JsonProperty
    private WorkFlowProcessMasterValueRest designationRest;
    @JsonProperty
    private String username;
    @JsonProperty

    private String sendername;
    @JsonProperty
    private String senderemail;
    @JsonProperty
    private String senderphonenumber;
    @JsonProperty
    private String senderorganization;
    @JsonProperty
    private String senderaddress;
    @JsonProperty
    private String sendercity;
    @JsonProperty
    private String sendercountry;
    @JsonProperty
    private String senderpincode;


    @JsonProperty
    private String startdate;
    @JsonProperty
    private String enddate;

    @Override
    public String getCategory() {
        return "workflowprocessefilter";
    }

    @Override
    public String getType() {
        return "workflowprocessefilter";
    }

    public WorkFlowProcessMasterValueRest getPriorityRest() {
        return priorityRest;
    }

    public void setPriorityRest(WorkFlowProcessMasterValueRest priorityRest) {
        this.priorityRest = priorityRest;
    }

    public WorkFlowProcessMasterValueRest getWorkflowStatusRest() {
        return workflowStatusRest;
    }

    public void setWorkflowStatusRest(WorkFlowProcessMasterValueRest workflowStatusRest) {
        this.workflowStatusRest = workflowStatusRest;
    }

    public void setWorkflowTypeRest(WorkFlowProcessMasterValueRest workflowTypeRest) {
        this.workflowTypeRest = workflowTypeRest;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public WorkFlowProcessMasterValueRest getWorkflowTypeRest() {
        return workflowTypeRest;
    }

    public WorkFlowProcessMasterValueRest getDepartmentRest() {
        return departmentRest;
    }

    public void setDepartmentRest(WorkFlowProcessMasterValueRest departmentRest) {
        this.departmentRest = departmentRest;
    }

    public EPersonRest getePersonRest() {
        return ePersonRest;
    }

    public void setePersonRest(EPersonRest ePersonRest) {
        this.ePersonRest = ePersonRest;
    }

    public String getInward() {
        return inward;
    }

    public void setInward(String inward) {
        this.inward = inward;
    }

    public String getOutward() {
        return outward;
    }

    public void setOutward(String outward) {
        this.outward = outward;
    }

    public String getInwarddate() {
        return inwarddate;
    }

    public void setInwarddate(String inwarddate) {
        this.inwarddate = inwarddate;
    }

    public String getOutwarddate() {
        return outwarddate;
    }

    public void setOutwarddate(String outwarddate) {
        this.outwarddate = outwarddate;
    }

    public String getReceiveddate() {
        return receiveddate;
    }

    public void setReceiveddate(String receiveddate) {
        this.receiveddate = receiveddate;
    }

    public WorkFlowProcessMasterValueRest getCategoryRest() {
        return categoryRest;
    }

    public void setCategoryRest(WorkFlowProcessMasterValueRest categoryRest) {
        this.categoryRest = categoryRest;
    }

    public WorkFlowProcessMasterValueRest getSubcategoryRest() {
        return subcategoryRest;
    }

    public void setSubcategoryRest(WorkFlowProcessMasterValueRest subcategoryRest) {
        this.subcategoryRest = subcategoryRest;
    }

    public WorkFlowProcessMasterValueRest getOfficeRest() {
        return officeRest;
    }

    public void setOfficeRest(WorkFlowProcessMasterValueRest officeRest) {
        this.officeRest = officeRest;
    }

    public WorkFlowProcessMasterValueRest getInwardmodeRest() {
        return inwardmodeRest;
    }

    public void setInwardmodeRest(WorkFlowProcessMasterValueRest inwardmodeRest) {
        this.inwardmodeRest = inwardmodeRest;
    }

    public WorkFlowProcessMasterValueRest getOutwardmodeRest() {
        return outwardmodeRest;
    }

    public void setOutwardmodeRest(WorkFlowProcessMasterValueRest outwardmodeRest) {
        this.outwardmodeRest = outwardmodeRest;
    }

    public WorkFlowProcessMasterValueRest getOutwardmediumRest() {
        return outwardmediumRest;
    }

    public void setOutwardmediumRest(WorkFlowProcessMasterValueRest outwardmediumRest) {
        this.outwardmediumRest = outwardmediumRest;
    }

    public WorkFlowProcessMasterValueRest getDesignationRest() {
        return designationRest;
    }

    public void setDesignationRest(WorkFlowProcessMasterValueRest designationRest) {
        this.designationRest = designationRest;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSendername() {
        return sendername;
    }

    public void setSendername(String sendername) {
        this.sendername = sendername;
    }

    public String getSenderemail() {
        return senderemail;
    }

    public void setSenderemail(String senderemail) {
        this.senderemail = senderemail;
    }

    public String getSenderphonenumber() {
        return senderphonenumber;
    }

    public void setSenderphonenumber(String senderphonenumber) {
        this.senderphonenumber = senderphonenumber;
    }

    public String getSenderorganization() {
        return senderorganization;
    }

    public void setSenderorganization(String senderorganization) {
        this.senderorganization = senderorganization;
    }

    public String getSenderaddress() {
        return senderaddress;
    }

    public void setSenderaddress(String senderaddress) {
        this.senderaddress = senderaddress;
    }

    public String getSendercity() {
        return sendercity;
    }

    public void setSendercity(String sendercity) {
        this.sendercity = sendercity;
    }

    public String getSendercountry() {
        return sendercountry;
    }

    public void setSendercountry(String sendercountry) {
        this.sendercountry = sendercountry;
    }

    public String getSenderpincode() {
        return senderpincode;
    }

    public void setSenderpincode(String senderpincode) {
        this.senderpincode = senderpincode;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }
}
