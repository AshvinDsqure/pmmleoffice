/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import lombok.Data;
import org.dspace.content.enums.Dispatch;
import org.dspace.content.enums.Priority;
import org.dspace.content.service.WorkflowProcessEpersonService;
import org.dspace.eperson.EPerson;
import org.hibernate.annotations.Where;
import org.springframework.core.annotation.Order;

import javax.persistence.Entity;
import javax.persistence.*;
import java.util.*;

/**
 * Class representing an item in DSpace.
 * <p>
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
@Table(name = "workflowprocess")
@Data
public class WorkflowProcess extends DSpaceObject implements DSpaceObjectLegacySupport {
    /**
     * Wild card for Dublin Core metadata qualifiers/languages
     */
    public static final String ANY = "*";
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinColumn(name = "workflowprocessinwarddetails_idf")
    private WorkFlowProcessInwardDetails workFlowProcessInwardDetails = null;
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinColumn(name = "workflowprocessoutwarddetails_idf")
    private WorkFlowProcessOutwardDetails workFlowProcessOutwardDetails = null;
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinColumn(name = "workflowprocesssenderdiary")
    private WorkflowProcessSenderDiary workflowProcessSenderDiary = null;
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinColumn(name = "workflowprocessdraftdetails_idf")
    private WorkFlowProcessDraftDetails workFlowProcessDraftDetails;
    /* Filling Details*/

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eligible_for_filing_id")
    private WorkFlowProcessMasterValue eligibleForFiling = null;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_status_id")
    private WorkFlowProcessMasterValue workflowStatus = null;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_priority_id")
    private WorkFlowProcessMasterValue priority = null;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_type_id")
    private WorkFlowProcessMasterValue workflowType = null;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item")
    private Item item;
    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "workflowProcess")
    private List<Item> items = new ArrayList<>();

    /* Attechment  Details*/
    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "workflowProcess")
    private List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs = new ArrayList<>();
    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "workflowProcess",cascade = {CascadeType.ALL})
    private List<WorkflowProcessSenderDiary> workflowProcessSenderDiaries = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "workflowProcess", cascade = {CascadeType.ALL})
    @OrderBy("actionDate")
    private List<WorkFlowProcessHistory> workFlowProcessHistories = new ArrayList<>();
    /* Office   Details*/

    @Column(name = "subject")
    private String Subject;

    @Column(name = "remark")
    private String remark;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id")
    private WorkFlowProcessMasterValue action = null;
    @Column(name = "workflow_id", insertable = false, updatable = false)
    private Integer legacyId;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "workflowProcess", cascade = {CascadeType.ALL})
    @OrderBy("index desc")
    private List<WorkflowProcessEperson> workflowProcessEpeople;
    @Column(name = "init_date", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date InitDate = new Date();
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatchmode_id")
    private WorkFlowProcessMasterValue dispatchmode = null;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflowprocessnote_idf")
    private WorkflowProcessNote workflowProcessNote;
    @Column(name = "isdelete")
    private Boolean isdelete = false;

    @Column(name = "ismode")
    private Boolean ismode = false;

    @Column(name = "isreplydraft")
    private Boolean isreplydraft = false;

    @Column(name = "isread")
    private Boolean isread = false;

//    @Column(name = "assignduedate", columnDefinition = "timestamp with time zone")
//    @Temporal(TemporalType.TIMESTAMP)
//    private Date assignDueDate;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "workflowProcess", cascade = {CascadeType.ALL})
    private List<WorkFlowProcessHistory> WorkFlowProcessHistory = new ArrayList<>();

    public List<org.dspace.content.WorkFlowProcessHistory> getWorkFlowProcessHistory() {
        return WorkFlowProcessHistory;
    }

    public void setWorkFlowProcessHistory(List<org.dspace.content.WorkFlowProcessHistory> workFlowProcessHistory) {
        WorkFlowProcessHistory = workFlowProcessHistory;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public String getName() {
        return "workflow";
    }

    @Override
    public Integer getLegacyId() {
        return this.legacyId;
    }

    public WorkFlowProcessInwardDetails getWorkFlowProcessInwardDetails() {
        return workFlowProcessInwardDetails;
    }

    public void setWorkFlowProcessInwardDetails(WorkFlowProcessInwardDetails workFlowProcessInwardDetails) {
        this.workFlowProcessInwardDetails = workFlowProcessInwardDetails;
    }

    public WorkflowProcessSenderDiary getWorkflowProcessSenderDiary() {
        return workflowProcessSenderDiary;
    }

    public void setWorkflowProcessSenderDiary(WorkflowProcessSenderDiary workflowProcessSenderDiary) {
        this.workflowProcessSenderDiary = workflowProcessSenderDiary;
    }


    public WorkFlowProcessMasterValue getEligibleForFiling() {
        return eligibleForFiling;
    }

    public void setEligibleForFiling(WorkFlowProcessMasterValue eligibleForFiling) {
        this.eligibleForFiling = eligibleForFiling;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public List<WorkflowProcessReferenceDoc> getWorkflowProcessReferenceDocs() {
        return workflowProcessReferenceDocs;
    }

    public void setWorkflowProcessReferenceDocs(List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs) {
        this.workflowProcessReferenceDocs = workflowProcessReferenceDocs;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }

    public List<WorkflowProcessEperson> getWorkflowProcessEpeople() {
        return workflowProcessEpeople;
    }

    public void setnewUser(WorkflowProcessEperson workflowProcessEperson) {
       WorkflowProcessEperson workflowProcessEpersonmax = this.workflowProcessEpeople.stream().max(Comparator.comparing(WorkflowProcessEperson::getSequence)).orElseThrow(NoSuchElementException::new);
        var nextindex = workflowProcessEpersonmax.getIndex() + 1;
        workflowProcessEperson.setSequence(nextindex);
        this.workflowProcessEpeople.add(workflowProcessEperson);
        //this.workflowProcessEpeople.add(workflowProcessEperson);
      //  System.out.println("workflowProcessEpersonmax index::" + workflowProcessEpersonmax.getIndex());
    }

    public void setWorkflowProcessEpeople(List<WorkflowProcessEperson> workflowProcessEpeople) {
        this.workflowProcessEpeople = workflowProcessEpeople;
    }

    public Date getInitDate() {
        return InitDate;
    }

    public void setInitDate(Date initDate) {
        InitDate = initDate;
    }

    public WorkFlowProcessMasterValue getDispatchmode() {
        return dispatchmode;
    }

    public void setDispatchmode(WorkFlowProcessMasterValue dispatchmode) {
        this.dispatchmode = dispatchmode;
    }

    public WorkFlowProcessMasterValue getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(WorkFlowProcessMasterValue workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public List<org.dspace.content.WorkFlowProcessHistory> getWorkFlowProcessHistories() {
        return workFlowProcessHistories;
    }

    public void setWorkFlowProcessHistories(List<org.dspace.content.WorkFlowProcessHistory> workFlowProcessHistories) {
        this.workFlowProcessHistories = workFlowProcessHistories;
    }

    public WorkFlowProcessMasterValue getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(WorkFlowProcessMasterValue workflowType) {
        this.workflowType = workflowType;
    }

    public WorkFlowProcessOutwardDetails getWorkFlowProcessOutwardDetails() {
        return workFlowProcessOutwardDetails;
    }

    public void setWorkFlowProcessOutwardDetails(WorkFlowProcessOutwardDetails workFlowProcessOutwardDetails) {
        this.workFlowProcessOutwardDetails = workFlowProcessOutwardDetails;
    }

    public WorkFlowProcessMasterValue getPriority() {
        return priority;
    }

    public void setPriority(WorkFlowProcessMasterValue priority) {
        this.priority = priority;
    }

    public Boolean getIsdelete() {
        return isdelete;
    }

    public void setIsndelete(Boolean isdelete) {
        this.isdelete = isdelete;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public WorkFlowProcessMasterValue getAction() {
        return action;
    }

    public void setAction(WorkFlowProcessMasterValue action) {
        this.action = action;
    }

    public List<WorkflowProcessSenderDiary> getWorkflowProcessSenderDiaries() {
        return workflowProcessSenderDiaries;
    }

    public void setWorkflowProcessSenderDiaries(List<WorkflowProcessSenderDiary> workflowProcessSenderDiaries) {
        this.workflowProcessSenderDiaries = workflowProcessSenderDiaries;
    }

    public Boolean getIsreplydraft() {
        return isreplydraft;
    }

    public void setIsreplydraft(Boolean isreplydraft) {
        this.isreplydraft = isreplydraft;
    }
}
