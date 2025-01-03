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
import org.dspace.app.rest.model.helper.MyDateConverter;

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
    private String letterrefno;

    @JsonProperty
    private String categoryRest = null;
    @JsonProperty
    private String subcategoryRest = null;
    @JsonProperty
    private WorkFlowProcessMasterValueRest lettercategoryRest = null;

    @JsonProperty
    private WorkFlowProcessMasterValueRest inwardmodeRest = null;
    @JsonProperty
    private String vipRest = null;
    @JsonProperty
    private String vipnameRest = null;

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

    public String getCategoryRest() {
        return categoryRest;
    }

    public void setCategoryRest(String categoryRest) {
        this.categoryRest = categoryRest;
    }

    public String getSubcategoryRest() {
        return subcategoryRest;
    }

    public void setSubcategoryRest(String subcategoryRest) {
        this.subcategoryRest = subcategoryRest;
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

    public String getLetterrefno() {
        return letterrefno;
    }

    public void setLetterrefno(String letterrefno) {
        this.letterrefno = letterrefno;
    }
}
