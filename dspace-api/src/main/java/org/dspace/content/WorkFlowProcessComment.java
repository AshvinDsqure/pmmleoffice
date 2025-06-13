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
@Table(name = "workflowprocesscomment")
public class WorkFlowProcessComment extends DSpaceObject implements DSpaceObjectLegacySupport {
    /**
     * Wild card for Dublin Core metadata qualifiers/languages
     */
    public static final String ANY = "*";
    @Column(name = "workflowprocesscomment_id", insertable = false, updatable = false)
    private Integer legacyId;
    @Column(name = "comment")
    private String comment;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "workflowprocesscomment")
    private List<WorkflowProcessReferenceDoc> workflowProcessReferenceDoc = new ArrayList<>();
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflowprocesshistory_idf")
    private WorkFlowProcessHistory workFlowProcessHistory;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note")
    private WorkflowProcessReferenceDoc note;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflowprocess_fid")
    private WorkflowProcess workFlowProcess;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitter_id")
    private EPerson submitter = null;

    @Column(name = "actiondate", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date actionDate = new Date();

    @Column(name = "isdraftsave")
    private Boolean isdraftsave = true;

    @Column(name = "margeddocuuid")
    private String margeddocuuid;
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
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<WorkflowProcessReferenceDoc> getWorkflowProcessReferenceDoc() {
        return workflowProcessReferenceDoc;
    }

    public void setWorkflowProcessReferenceDoc(List<WorkflowProcessReferenceDoc> workflowProcessReferenceDoc) {
        this.workflowProcessReferenceDoc = workflowProcessReferenceDoc;
    }

    public WorkFlowProcessHistory getWorkFlowProcessHistory() {
        return workFlowProcessHistory;
    }
    public void setWorkFlowProcessHistory(WorkFlowProcessHistory workFlowProcessHistory) {
        this.workFlowProcessHistory = workFlowProcessHistory;
    }
    public EPerson getSubmitter() {
        return submitter;
    }
    public void setSubmitter(EPerson submitter) {
        this.submitter = submitter;
    }
    public WorkflowProcess getWorkFlowProcess() {
        return workFlowProcess;
    }
    public void setWorkFlowProcess(WorkflowProcess workFlowProcess) {
        this.workFlowProcess = workFlowProcess;
    }

    public Date getActionDate() {
        return actionDate;
    }

    public void setActionDate(Date actionDate) {
        this.actionDate = actionDate;
    }

    public WorkflowProcessReferenceDoc getNote() {
        return note;
    }

    public void setNote(WorkflowProcessReferenceDoc note) {
        this.note = note;
    }

    public Boolean getIsdraftsave() {
        return isdraftsave;
    }

    public void setIsdraftsave(Boolean isdraftsave) {
        this.isdraftsave = isdraftsave;
    }

    public String getMargeddocuuid() {
        return margeddocuuid;
    }

    public void setMargeddocuuid(String margeddocuuid) {
        this.margeddocuuid = margeddocuuid;
    }
}
