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
public class
WorkFlowProcessInwardDetails extends DSpaceObject implements DSpaceObjectLegacySupport {
    /**
     * Wild card for Dublin Core metadata qualifiers/languages
     */
    public static final String ANY = "*";
    @Column(name = "workflowprocessinwarddetails_id", insertable = false, updatable = false)
    private Integer legacyId;
    @Column(name = "inwardnumber")
    private String inwardNumber;
    @Column(name = "filereferencenumber")
    private String filereferencenumber;
    @Column(name = "letterrefno")
    private String letterrefno;
    @Column(name = "inwarddate", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date inwardDate;

    @Column(name = "latterdate", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date latterDate;

    @Column(name = "receiveddate", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date receivedDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category")
    private Category category = null;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory")
    private SubCategory subcategory = null;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lettercategory")
    private WorkFlowProcessMasterValue lettercategory = null;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inwardmode")
    private WorkFlowProcessMasterValue inwardmode = null;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vip")
    private Vip vip = null;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vipname")
    private VipName vipname = null;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language")
    private WorkFlowProcessMasterValue language = null;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documenttype")
    private WorkFlowProcessMasterValue documenttype = null;

    public WorkFlowProcessMasterValue getDocumenttype() {
        return documenttype;
    }

    public void setDocumenttype(WorkFlowProcessMasterValue documenttype) {
        this.documenttype = documenttype;
    }

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


    public WorkFlowProcessMasterValue getLanguage() {
        return language;
    }

    public void setLanguage(WorkFlowProcessMasterValue language) {
        this.language = language;
    }

    public String getFilereferencenumber() {
        return filereferencenumber;
    }

    public void setFilereferencenumber(String filereferencenumber) {
        this.filereferencenumber = filereferencenumber;
    }

    public Date getLatterDate() {
        return latterDate;
    }

    public void setLatterDate(Date latterDate) {
        this.latterDate = latterDate;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public SubCategory getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(SubCategory subcategory) {
        this.subcategory = subcategory;
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

    public String getLetterrefno() {
        return letterrefno;
    }

    public void setLetterrefno(String letterrefno) {
        this.letterrefno = letterrefno;
    }
}
