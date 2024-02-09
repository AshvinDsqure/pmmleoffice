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
import java.util.Date;
import java.util.HashSet;
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
@Table(name = "workflowprocessnote")
public class WorkflowProcessNote extends DSpaceObject implements DSpaceObjectLegacySupport {
    /**
     * Wild card for Dublin Core metadata qualifiers/languages
     */
    public static final String ANY = "*";

    @Column(name = "workflowprocessnote_id", insertable = false, updatable = false)
    private Integer legacyId;
    @Column(name = "subject")
    private String subject;
    @Column(name = "description")
    private String description;
    @Column(name = "init_date", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date InitDate = new Date();
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "workflowprocessnote",cascade = { CascadeType.ALL})
    private Set<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs=new HashSet<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitter_id")
    private EPerson submitter = null;

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

    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getInitDate() {
        return InitDate;
    }

    public void setInitDate(Date initDate) {
        InitDate = initDate;
    }

    public Set<WorkflowProcessReferenceDoc> getWorkflowProcessReferenceDocs() {
        return workflowProcessReferenceDocs;
    }

    public void setWorkflowProcessReferenceDocs(Set<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs) {
        this.workflowProcessReferenceDocs = workflowProcessReferenceDocs;
    }

    public EPerson getSubmitter() {
        return submitter;
    }

    public void setSubmitter(EPerson submitter) {
        this.submitter = submitter;
    }

}
