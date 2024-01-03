    /**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.dspace.app.rest.model.helper.MyDateConverter;
import org.dspace.content.WorkFlowProcessMasterValue;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.util.Date;

public class WorkFlowProcessInwardDetailsRest extends  DSpaceObjectRest{
    public static final String NAME = "workflowprocessinwarddetail";
    public static final String PLURAL_NAME = "workflowprocessinwarddetails";
    public static final String CATEGORY = RestAddressableModel.WORKFLOWPROCESSINWARDDETAIL;

    public static final String GROUPS = "groups";

    private Integer legacyId;

    private String inwardNumber;
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date inwardDate;
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date receivedDate;
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date latterDate;


    private String filereferencenumber;

    @JsonProperty
    private WorkFlowProcessMasterValueRest categoryRest = null;
    @JsonProperty
    private WorkFlowProcessMasterValueRest subcategoryRest = null;
    @JsonProperty
    private WorkFlowProcessMasterValueRest lettercategoryRest = null;

    @JsonProperty
    private WorkFlowProcessMasterValueRest inwardmodeRest = null;
    @JsonProperty
    private WorkFlowProcessMasterValueRest vipRest = null;
    @JsonProperty
    private WorkFlowProcessMasterValueRest vipnameRest = null;

    @JsonProperty
    private WorkFlowProcessMasterValueRest languageRest = null;


    public String getInwardNumber() {
        return inwardNumber;
    }

    public void setInwardNumber(String inwardNumber) {
        this.inwardNumber = inwardNumber;
    }

    public Date getInwardDate() {
        return inwardDate;
    }

    public void setInwardDate(Date inwardDate) {
        this.inwardDate = inwardDate;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
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



    public WorkFlowProcessMasterValueRest getInwardmodeRest() {
        return inwardmodeRest;
    }

    public void setInwardmodeRest(WorkFlowProcessMasterValueRest inwardmodeRest) {
        this.inwardmodeRest = inwardmodeRest;
    }

    public WorkFlowProcessMasterValueRest getLettercategoryRest() {
        return lettercategoryRest;
    }

    public void setLettercategoryRest(WorkFlowProcessMasterValueRest lettercategoryRest) {
        this.lettercategoryRest = lettercategoryRest;
    }

    public WorkFlowProcessMasterValueRest getVipRest() {
        return vipRest;
    }

    public void setVipRest(WorkFlowProcessMasterValueRest vipRest) {
        this.vipRest = vipRest;
    }

    public WorkFlowProcessMasterValueRest getVipnameRest() {
        return vipnameRest;
    }

    public void setVipnameRest(WorkFlowProcessMasterValueRest vipnameRest) {
        this.vipnameRest = vipnameRest;
    }

    public WorkFlowProcessMasterValueRest getLanguageRest() {
        return languageRest;
    }

    public void setLanguageRest(WorkFlowProcessMasterValueRest languageRest) {
        this.languageRest = languageRest;
    }

    public String getFilereferencenumber() {
        return filereferencenumber;
    }

    public void setFilereferencenumber(String filereferencenumber) {
        this.filereferencenumber = filereferencenumber;
    }

    public Date getLatterDate() {
        return latterDate;
    }

    public void setLatterDate(Date latterDate) {
        this.latterDate = latterDate;
    }

}
