/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.dspace.content.enums.Priority;
import org.dspace.content.enums.WorkFlowProcessReferenceDocType;
import org.dspace.eperson.EPerson;

import javax.persistence.Entity;
import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

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
@Table(name = "workflowprocessreferencedocversion")
public class WorkflowProcessReferenceDocVersion extends DSpaceObject implements DSpaceObjectLegacySupport {
    /**
     * Wild card for Dublin Core metadata qualifiers/languages
     */
    public static final String ANY = "*";
    @Column(name = "workflowprocessreferencedocversion_id", insertable = false, updatable = false)
    private Integer legacyId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator")
    private EPerson creator = null;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflowprocessreferencedoc_fid")
    private WorkflowProcessReferenceDoc workflowProcessReferenceDoc = null;
    @Column(name = "creationdatetime", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationdatetime =null;
    @Column(name = "remark")
    private String remark;
    @Column(name = "versionnumber")
    private Double versionnumber;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bitstream")
    private Bitstream bitstream;
    @Column(name = "isactive")
    private Boolean isactive;

    @Column(name = "issign")
    private Boolean issign=false;

    @Column(name = "editortext")
    private String editortext;
    @Override
    public int getType() {
        return 0;
    }
    @Override
    public String getName() {
        return "workflowprocessreferencedocversion";
    }
    @Override
    public Integer getLegacyId() {
        return this.legacyId;
    }
    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }
    public EPerson getCreator() {
        return creator;
    }

    public void setCreator(EPerson creator) {
        this.creator = creator;
    }

    public WorkflowProcessReferenceDoc getWorkflowProcessReferenceDoc() {
        return workflowProcessReferenceDoc;
    }

    public void setWorkflowProcessReferenceDoc(WorkflowProcessReferenceDoc workflowProcessReferenceDoc) {
        this.workflowProcessReferenceDoc = workflowProcessReferenceDoc;
    }

    public Date getCreationdatetime() {
        return creationdatetime;
    }
    public void setCreationdatetime(Date creationdatetime) {
        this.creationdatetime = creationdatetime;
    }
    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Double getVersionnumber() {
        return versionnumber;
    }

    public Bitstream getBitstream() {
        return bitstream;
    }

    public void setBitstream(Bitstream bitstream) {
        this.bitstream = bitstream;
    }

    public void setVersionnumber(Double versionnumber) {
        this.versionnumber = versionnumber;
    }

    public Boolean getIsactive() {
        return isactive;
    }

    public void setIsactive(Boolean isactive) {
        this.isactive = isactive;
    }

    public String getEditortext() {
        return editortext;
    }

    public void setEditortext(String editortext) {
        this.editortext = editortext;
    }

    public Boolean getIssign() {
        return issign;
    }

    public void setIssign(Boolean issign) {
        this.issign = issign;
    }
}
