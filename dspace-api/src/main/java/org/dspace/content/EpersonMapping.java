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
@Table(name = "epersonmapping")
public class EpersonMapping extends DSpaceObject{

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office")
    private WorkFlowProcessMasterValue office = null;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department")
    private WorkFlowProcessMasterValue department = null;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation")
    private WorkFlowProcessMasterValue designation = null;

    @Column(name = "tablenumber")
    private  Integer  tablenumber;


    @Override
    public int getType() {
        return 0;
    }

    @Override
    public String getName() {
        return "epersonmapping";
    }

    public WorkFlowProcessMasterValue getOffice() {
        return office;
    }

    public void setOffice(WorkFlowProcessMasterValue office) {
        this.office = office;
    }

    public WorkFlowProcessMasterValue getDepartment() {
        return department;
    }

    public void setDepartment(WorkFlowProcessMasterValue department) {
        this.department = department;
    }

    public WorkFlowProcessMasterValue getDesignation() {
        return designation;
    }

    public void setDesignation(WorkFlowProcessMasterValue designation) {
        this.designation = designation;
    }

    public Integer getTablenumber() {
        return tablenumber;
    }

    public void setTablenumber(Integer tablenumber) {
        this.tablenumber = tablenumber;
    }
}
