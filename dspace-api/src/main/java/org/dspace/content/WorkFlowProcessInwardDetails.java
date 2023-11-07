/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;
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
@Table(name = "workflowprocessinwarddetails")
public class WorkFlowProcessInwardDetails extends DSpaceObject implements DSpaceObjectLegacySupport {
    /**
     * Wild card for Dublin Core metadata qualifiers/languages
     */
    public static final String ANY = "*";
    @Column(name = "workflowprocessinwarddetails_id", insertable = false, updatable = false)
    private Integer legacyId;

    @Column(name = "inwardnumber")
    private String inwardNumber;
    @Column(name = "inwarddate", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date inwardDate;

    @Column(name = "receiveddate", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date receivedDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category")
    private WorkFlowProcessMasterValue category = null;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory")
    private WorkFlowProcessMasterValue subcategory = null;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lettercategory")
    private WorkFlowProcessMasterValue lettercategory = null;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inwardmode")
    private WorkFlowProcessMasterValue inwardmode = null;
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
    @Override
    public int getType() {
        return 0;
    }

    @Override
    public String getName() {
        return "workflowprocessinwarddetails";
    }

    @Override
    public Integer getLegacyId() {
        return this.legacyId;
    }
    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }

    public WorkFlowProcessMasterValue getCategory() {
        return category;
    }

    public void setCategory(WorkFlowProcessMasterValue category) {
        this.category = category;
    }

    public WorkFlowProcessMasterValue getSubcategory() {
        return subcategory;
    }
    public void setSubcategory(WorkFlowProcessMasterValue subcategory) {
        this.subcategory = subcategory;
    }

    public WorkFlowProcessMasterValue getInwardmode() {
        return inwardmode;
    }

    public void setInwardmode(WorkFlowProcessMasterValue inwardmode) {
        this.inwardmode = inwardmode;
    }
    public WorkFlowProcessMasterValue getLettercategory() {
        return lettercategory;
    }

    public void setLettercategory(WorkFlowProcessMasterValue lettercategory) {
        this.lettercategory = lettercategory;
    }
}
