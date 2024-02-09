/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.dspace.content.enums.Dispatch;
import org.dspace.content.enums.Priority;
import org.dspace.eperson.EPerson;

import javax.persistence.Entity;
import javax.persistence.*;
import java.util.Date;
import java.util.Set;

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
@Table(name = "workflowprocesssenderdiary")
public class WorkflowProcessSenderDiary extends DSpaceObject implements DSpaceObjectLegacySupport {
    /**
     * Wild card for Dublin Core metadata qualifiers/languages
     */
    public static final String ANY = "*";
    @Column(name = "workflowprocesssenderdiary_id", insertable = false, updatable = false)
    private Integer legacyId;
    @Column(name = "sendername")
    private String sendername;
    @Column(name = "designation")
    private String designation;
    @Column(name = "contactnumber")
    private String contactNumber;
    @Column(name = "email")
    private String email;
    @Column(name = "organization")
    private String organization;
    @Column(name = "address")
    private String address;
    @Column(name = "city")
    private String city;
    @Column(name = "state")
    private String state;
    @Column(name = "pincode")
    private String pincode;
    @Column(name = "country")
    private String country;
    @Column(name = "fax")
    private String fax;
    @Column(name = "landline")
    private String landline;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflowprocess")
    private WorkflowProcess workflowProcess;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vip")
    private Vip vip = null;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vipname")
    private VipName vipname = null;
    @Column(name = "status")
    private Integer status;
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

    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }

    public String getSendername() {
        return sendername;
    }

    public void setSendername(String sendername) {
        this.sendername = sendername;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getLandline() {
        return landline;
    }

    public void setLandline(String landline) {
        this.landline = landline;
    }
    public Vip getVip() {
        return vip;
    }

    public void setVip(Vip vip) {
        this.vip = vip;
    }

    public VipName getVipname() {
        return vipname;
    }

    public void setVipname(VipName vipname) {
        this.vipname = vipname;
    }

    public WorkflowProcess getWorkflowProcess() {
        return workflowProcess;
    }

    public void setWorkflowProcess(WorkflowProcess workflowProcess) {
        this.workflowProcess = workflowProcess;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
