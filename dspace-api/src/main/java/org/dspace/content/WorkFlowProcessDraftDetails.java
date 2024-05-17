/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import javax.persistence.Entity;
import javax.persistence.*;
import java.util.*;

/**
 * Class representing an item in DSpace.
 * <P>
 * This class holds in memory the item Dublin Core metadata, the bundles in the
 * item, and the bitstreams in those bundles. When modifying the item, if you
 * modify the Dublin Core or the "in archive" flag, you must call
 * <code>update</code> for the changes to be written to the database.
 * Creating, adding or removing bundles or bitstreams has immediate effect in
 * the database.
 *
 * @author Robert Tansley
 * @author Martin Hald
 */
@Entity
@Table(name = "workflowprocessdraftdetails")
public class WorkFlowProcessDraftDetails extends DSpaceObject implements DSpaceObjectLegacySupport {
    /**
     * Wild card for Dublin Core metadata qualifiers/languages
     */
    public static final String ANY = "*";
    @Column(name = "workflowprocessdraftdetails_id", insertable = false, updatable = false)
    private Integer legacyId;
    @Column(name = "draftdate", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date draftdate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documentsignator_id")
    private EPerson documentsignator;

    //for reply Inward screen
    //drafttype means doc type  notesheet or document,inward,outward,comment,referel
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_type_id")
    private WorkFlowProcessMasterValue drafttype;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_nature")
    private WorkFlowProcessMasterValue draftnature;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confidential")
    private WorkFlowProcessMasterValue confidential;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replytype")
    private WorkFlowProcessMasterValue replytype;
    @Column(name = "subject")
    private String subject;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referencetapalnumber")
    private WorkFlowProcessInwardDetails referencetapalnumber = null;

    @Column(name = "referencefilenumber")
    private String referencefilenumber = null;
    @Column(name = "issinglatter")
    private Boolean issinglatter = false;
    @Column(name = "isdispatchbyself")
    private Boolean isdispatchbyself = false;
    @Column(name = "isdispatchbycru")
    private Boolean isdispatchbycru = false;
    @Column(name = "isdelete")
    private Boolean isdelete = false;

    @Column(name = "issapdoc")
    private Boolean issapdoc = false;

    @Column(name = "sapdocumentno")
    private String sapdocumentno;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sapdocumenttype")
    private WorkFlowProcessMasterValue sapdocumenttype;


    @Override
    public int getType() {
        return 0;
    }
    @Override
    public String getName() {
        return "workflowprocessdraftdetails";
    }
    @Override
    public Integer getLegacyId() {
        return this.legacyId;
    }
    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }

    public Date getDraftdate() {
        return draftdate;
    }
    public void setDraftdate(Date draftdate) {
        this.draftdate = draftdate;
    }
    public WorkFlowProcessMasterValue getDrafttype() {
        return drafttype;
    }

    public void setDrafttype(WorkFlowProcessMasterValue drafttype) {
        this.drafttype = drafttype;
    }

    public EPerson getDocumentsignator() {
        return documentsignator;
    }

    public void setDocumentsignator(EPerson documentsignator) {
        this.documentsignator = documentsignator;
    }

    public WorkFlowProcessMasterValue getDraftnature() {
        return draftnature;
    }

    public void setDraftnature(WorkFlowProcessMasterValue draftnature) {
        this.draftnature = draftnature;
    }

    public WorkFlowProcessMasterValue getConfidential() {
        return confidential;
    }

    public void setConfidential(WorkFlowProcessMasterValue confidential) {
        this.confidential = confidential;
    }

    public WorkFlowProcessMasterValue getReplytype() {
        return replytype;
    }

    public void setReplytype(WorkFlowProcessMasterValue replytype) {
        this.replytype = replytype;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public WorkFlowProcessInwardDetails getReferencetapalnumber() {
        return referencetapalnumber;
    }

    public void setReferencetapalnumber(WorkFlowProcessInwardDetails referencetapalnumber) {
        this.referencetapalnumber = referencetapalnumber;
    }

    public String getReferencefilenumber() {
        return referencefilenumber;
    }

    public void setReferencefilenumber(String referencefilenumber) {
        this.referencefilenumber = referencefilenumber;
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

    public WorkFlowProcessMasterValue getSapdocumenttype() {
        return sapdocumenttype;
    }

    public void setSapdocumenttype(WorkFlowProcessMasterValue sapdocumenttype) {
        this.sapdocumenttype = sapdocumenttype;
    }
}
