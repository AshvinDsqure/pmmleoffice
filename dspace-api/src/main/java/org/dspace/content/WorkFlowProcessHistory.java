/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;


import org.dspace.eperson.EPerson;

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
@Table(name = "workflowprocesshistory")
public class WorkFlowProcessHistory extends DSpaceObject implements DSpaceObjectLegacySupport {
    public WorkFlowProcessHistory() {
    }

    public WorkFlowProcessHistory(WorkflowProcessEperson workflowProcessEpeople, WorkflowProcess workflowProcess) {
        this.workflowProcessEpeople = workflowProcessEpeople;
        this.workflowProcess = workflowProcess;
    }

    /**
     * Wild card for Dublin Core metadata qualifiers/languages
     */

    public static final String ANY = "*";
    @Column(name = "workflowhistory_id", insertable = false, updatable = false)
    private Integer legacyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflowprocessepeople")
    private WorkflowProcessEperson workflowProcessEpeople;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sentto")
    private WorkflowProcessEperson sentto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflowprocess_id")
    private WorkflowProcess workflowProcess;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action")
    private WorkFlowProcessMasterValue action = null;

    @Column(name = "actiondate", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date actionDate = new Date();
    @Column(name = "receiveddate", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date receivedDate = new Date();
    @Column(name = "comment")
    private String comment;

    @Column(name = "sentbyname")
    private String sentbyname;

    @Column(name = "senttoname")
    private String senttoname;

    @Column(name = "isupdate")
    private  Boolean isupdate=false;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "workFlowProcessHistory", cascade = {CascadeType.ALL})
    private WorkFlowProcessComment workFlowProcessComment;

    public WorkFlowProcessMasterValue getAction() {
        return action;
    }

    public void setAction(WorkFlowProcessMasterValue action) {
        this.action = action;
    }

    public Date getActionDate() {
        return actionDate;
    }

    public void setActionDate(Date actionDate) {
        this.actionDate = actionDate;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public String getName() {
        return "workflowhistory";
    }

    @Override
    public Integer getLegacyId() {
        return this.legacyId;
    }
    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }

    public WorkflowProcessEperson getWorkflowProcessEpeople() {
        return workflowProcessEpeople;
    }

    public void setWorkflowProcessEpeople(WorkflowProcessEperson workflowProcessEpeople) {
        this.workflowProcessEpeople = workflowProcessEpeople;
    }

    public WorkflowProcess getWorkflowProcess() {
        return workflowProcess;
    }


    public void setWorkflowProcess(WorkflowProcess workflowProcess) {
        this.workflowProcess = workflowProcess;
    }
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public WorkFlowProcessComment getWorkFlowProcessComment() {
        return workFlowProcessComment;
    }

    public void setWorkFlowProcessComment(WorkFlowProcessComment workFlowProcessComment) {
        this.workFlowProcessComment = workFlowProcessComment;
    }

    public WorkflowProcessEperson getSentto() {
        return sentto;
    }

    public void setSentto(WorkflowProcessEperson sentto) {
        this.sentto = sentto;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getSentbyname() {
        return sentbyname;
    }

    public void setSentbyname(String sentbyname) {
        this.sentbyname = sentbyname;
    }

    public String getSenttoname() {
        return senttoname;
    }

    public void setSenttoname(String senttoname) {
        this.senttoname = senttoname;
    }

    public Boolean getIsupdate() {
        return isupdate;
    }

    public void setIsupdate(Boolean isupdate) {
        this.isupdate = isupdate;
    }
}
