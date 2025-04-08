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
import org.dspace.content.WorkFlowProcessInwardDetails;
import org.dspace.content.WorkFlowProcessMasterValue;
import org.dspace.content.WorkflowProcessSenderDiary;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WorkFlowProcessDraftDetailsRest extends  DSpaceObjectRest{
    public static final String NAME = "workflowprocessdraftdetail";
    public static final String PLURAL_NAME = "workflowprocessdraftdetails";
    public static final String CATEGORY = RestAddressableModel.WORKFLOWPROCESSDRAFTDETAIL;
    public static final String GROUPS = "groups";
    private Integer legacyId;
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date draftdate;
    //drafttype means notesheet or document
    private WorkFlowProcessMasterValueRest drafttypeRest;
    private EPersonRest documentsignatorRest;

    @JsonProperty
    private EpersonToEpersonMappingRest epersonToEpersonMappingRest = null;
    //for reply tapal
    private WorkFlowProcessMasterValueRest draftnatureRest;

    private WorkFlowProcessMasterValueRest confidentialRest;

    private WorkFlowProcessMasterValueRest replytypeRest;

    private String subject;

    private WorkFlowProcessInwardDetailsRest referencetapalnumberRest = null;

    private String referencefilenumberRest = null;

    private Boolean issinglatter = false;
    private Boolean isdispatchbyself = false;
    private Boolean isdispatchbycru = false;
    private Boolean isdelete = false;

    private Boolean issapdoc = false;

    private String sapdocumentno;
    private WorkFlowProcessMasterValueRest sapdocumenttypeRest;

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
    public Date getDraftdate() {
        return draftdate;
    }
    public void setDraftdate(Date draftdate) {
        this.draftdate = draftdate;
    }
    public WorkFlowProcessMasterValueRest getDrafttypeRest() {
        return drafttypeRest;
    }
    public void setDrafttypeRest(WorkFlowProcessMasterValueRest drafttypeRest) {
        this.drafttypeRest = drafttypeRest;
    }
    public EPersonRest getDocumentsignatorRest() {
        return documentsignatorRest;
    }
    public void setDocumentsignatorRest(EPersonRest documentsignatorRest) {
        this.documentsignatorRest = documentsignatorRest;
    }

    public WorkFlowProcessMasterValueRest getDraftnatureRest() {
        return draftnatureRest;
    }

    public void setDraftnatureRest(WorkFlowProcessMasterValueRest draftnatureRest) {
        this.draftnatureRest = draftnatureRest;
    }

    public WorkFlowProcessMasterValueRest getConfidentialRest() {
        return confidentialRest;
    }

    public void setConfidentialRest(WorkFlowProcessMasterValueRest confidentialRest) {
        this.confidentialRest = confidentialRest;
    }

    public WorkFlowProcessMasterValueRest getReplytypeRest() {
        return replytypeRest;
    }

    public void setReplytypeRest(WorkFlowProcessMasterValueRest replytypeRest) {
        this.replytypeRest = replytypeRest;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public WorkFlowProcessInwardDetailsRest getReferencetapalnumberRest() {
        return referencetapalnumberRest;
    }

    public void setReferencetapalnumberRest(WorkFlowProcessInwardDetailsRest referencetapalnumberRest) {
        this.referencetapalnumberRest = referencetapalnumberRest;
    }

    public String getReferencefilenumberRest() {
        return referencefilenumberRest;
    }

    public void setReferencefilenumberRest(String referencefilenumberRest) {
        this.referencefilenumberRest = referencefilenumberRest;
    }

    public Boolean getIssinglatter() {
        return issinglatter;
    }

    public void setIssinglatter(Boolean issinglatter) {
        this.issinglatter = issinglatter;
    }

    public Boolean getIsdispatchbyself() {
        return isdispatchbyself;
    }

    public void setIsdispatchbyself(Boolean isdispatchbyself) {
        this.isdispatchbyself = isdispatchbyself;
    }

    public Boolean getIsdispatchbycru() {
        return isdispatchbycru;
    }

    public void setIsdispatchbycru(Boolean isdispatchbycru) {
        this.isdispatchbycru = isdispatchbycru;
    }

    public Boolean getIsdelete() {
        return isdelete;
    }

    public void setIsdelete(Boolean isdelete) {
        this.isdelete = isdelete;
    }

    public Boolean getIssapdoc() {
        return issapdoc;
    }

    public void setIssapdoc(Boolean issapdoc) {
        this.issapdoc = issapdoc;
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

    public EpersonToEpersonMappingRest getEpersonToEpersonMappingRest() {
        return epersonToEpersonMappingRest;
    }

    public void setEpersonToEpersonMappingRest(EpersonToEpersonMappingRest epersonToEpersonMappingRest) {
        this.epersonToEpersonMappingRest = epersonToEpersonMappingRest;
    }
}
