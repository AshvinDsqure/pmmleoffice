/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.dspace.app.rest.RestResourceController;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class WorkflowProcessSenderDiaryRest extends DSpaceObjectRest {
    public static final String NAME = "workflowprocesssenderdiarie";
    public static final String PLURAL_NAME = "workflowprocesssenderdiaries";
    public static final String CATEGORY = RestAddressableModel.WORKFLOWPROCESSSENDERDIARY;

    public static final String GROUPS = "groups";

    private Integer legacyId;
    private String sendername;
    private String designation;
    private String contactNumber;
    @NotEmpty
    @Email
    private String email;
    private String organization;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private String fax;
    private String landline;

    private Integer status;
    @JsonProperty
    private String vipRest = null;
    @JsonProperty
    private String vipnameRest = null;


    @JsonProperty
    private WorkFlowProcessDraftDetailsRest workFlowProcessInwardDetailsRest;

    public WorkFlowProcessDraftDetailsRest getWorkFlowProcessInwardDetailsRest() {
        return workFlowProcessInwardDetailsRest;
    }

    public void setWorkFlowProcessInwardDetailsRest(WorkFlowProcessDraftDetailsRest workFlowProcessInwardDetailsRest) {
        this.workFlowProcessInwardDetailsRest = workFlowProcessInwardDetailsRest;
    }

    public Integer getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }


    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getType() {
        return NAME;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    public String getSendername() {
        return sendername;
    }

    public void setSendername(String sendername) {
        this.sendername = sendername;
    }

    @Override
    public Class getController() {
        return RestResourceController.class;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getLandline() {
        return landline;
    }

    public void setLandline(String landline) {
        this.landline = landline;
    }

    public String getVipRest() {
        return vipRest;
    }

    public void setVipRest(String vipRest) {
        this.vipRest = vipRest;
    }

    public String getVipnameRest() {
        return vipnameRest;
    }

    public void setVipnameRest(String vipnameRest) {
        this.vipnameRest = vipnameRest;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
