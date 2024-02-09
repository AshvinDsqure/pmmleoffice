/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import javax.persistence.*;
import javax.persistence.Entity;

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
 * @author ashivnmajethiya

 */
@Entity
@Table(name = "workflowprocessmastervalue")
public class WorkFlowProcessMasterValue extends DSpaceObject implements DSpaceObjectLegacySupport{
    @Column(name = "workflowprocessmastervalue_id", insertable = false, updatable = false)
    private Integer legacyId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflowprocessmaster_id")
    private WorkFlowProcessMaster workflowprocessmaster;
    @Column(name = "primaryvalue")
    private String primaryvalue;
    @Column(name = "secondaryvalue")
    private String secondaryvalue;

    @Column(name = "isdelete")
    private Boolean isdelete =false;

    public WorkFlowProcessMaster getWorkflowprocessmaster() {
        return workflowprocessmaster;
    }
    public void setWorkflowprocessmaster(WorkFlowProcessMaster workflowprocessmaster) {
        this.workflowprocessmaster = workflowprocessmaster;
    }
    public String getPrimaryvalue() {
        return primaryvalue;
    }
    public void setPrimaryvalue(String primaryvalue) {
        this.primaryvalue = primaryvalue;
    }
    public String getSecondaryvalue() {
        return secondaryvalue;
    }

    public void setSecondaryvalue(String secondaryvalue) {
        this.secondaryvalue = secondaryvalue;
    }

    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }
    @Override
    public int getType() {
        return 0;
    }
    @Override
    public String getName() {
        return "workflowprocessmastervalue";
    }
    @Override
    public Integer getLegacyId() {
        return this.legacyId;
    }

    public Boolean getIsdelete() {
        return isdelete;
    }

    public void setIsdelete(Boolean isdelete) {
        this.isdelete = isdelete;
    }
}
