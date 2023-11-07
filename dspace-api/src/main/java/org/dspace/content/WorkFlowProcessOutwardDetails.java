/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

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
@Table(name = "workflowprocessoutwarddetails")
public class WorkFlowProcessOutwardDetails extends DSpaceObject implements DSpaceObjectLegacySupport {
    /**
     * Wild card for Dublin Core metadata qualifiers/languages
     */
    public static final String ANY = "*";
    @Column(name = "workflowprocessoutwarddetails_id", insertable = false, updatable = false)
    private Integer legacyId;
    @Column(name = "outwardnumber")
    private String outwardNumber;
    @Column(name = "outwarddate", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date outwardDate;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outwarddepartment_id")
    private Group outwardDepartment;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outwardmedium_id")
    private WorkFlowProcessMasterValue outwardmedium = null;
    @Column(name = "serviceprovider")
    private String  serviceprovider;
    @Column(name = "awbno")
    private String  awbno;
    @Column(name = "dispatchdate", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dispatchdate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category")
    private WorkFlowProcessMasterValue category = null;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory")
    private WorkFlowProcessMasterValue subcategory = null;
    public String getOutwardNumber() {
        return outwardNumber;
    }
    public void setOutwardNumber(String outwardNumber) {
        this.outwardNumber = outwardNumber;
    }
    public Date getOutwardDate() {
        return outwardDate;
    }
    public void setOutwardDate(Date outwardDate) {
        this.outwardDate = outwardDate;
    }
    @Override
    public int getType() {
        return 0;
    }
    @Override
    public String getName() {
        return "workflowprocessoutwarddetails";
    }
    @Override
    public Integer getLegacyId() {
        return this.legacyId;
    }
    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }
    public Group getOutwardDepartment() {
        return outwardDepartment;
    }
    public void setOutwardDepartment(Group outwardDepartment) {
        this.outwardDepartment = outwardDepartment;
    }
    public String getServiceprovider() {
        return serviceprovider;
    }
    public void setServiceprovider(String serviceprovider) {
        this.serviceprovider = serviceprovider;
    }
    public String getAwbno() {
        return awbno;
    }
    public void setAwbno(String awbno) {
        this.awbno = awbno;
    }
    public Date getDispatchdate() {
        return dispatchdate;
    }
    public void setDispatchdate(Date dispatchdate) {
        this.dispatchdate = dispatchdate;
    }
    public WorkFlowProcessMasterValue getOutwardmedium() {
        return outwardmedium;
    }
    public void setOutwardmedium(WorkFlowProcessMasterValue outwardmedium) {
        this.outwardmedium = outwardmedium;
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
}
