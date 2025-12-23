/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.emas;

import org.dspace.content.DSpaceObject;
import org.dspace.content.DSpaceObjectLegacySupport;
import org.dspace.eperson.EPerson;

import javax.persistence.Entity;
import javax.persistence.*;
import java.util.Date;

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
@Table(name = "emas")
public class Emas extends DSpaceObject implements DSpaceObjectLegacySupport {

    @Column(name = "legacyid", insertable = false, updatable = false)
    private Integer legacyId;
    @Column(name = "key")
    private String key;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eperson")
    private EPerson eperson = null;
    @Column(name = "actiondate", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date actionDate = new Date();

    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }
    @Override
    public Integer getLegacyId() {
        return this.legacyId;
    }


    @Override
    public int getType() {
        return 0;
    }

    @Override
    public String getName() {
        return "emas";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public EPerson getEperson() {
        return eperson;
    }

    public void setEperson(EPerson eperson) {
        this.eperson = eperson;
    }

    public Date getActionDate() {
        return actionDate;
    }

    public void setActionDate(Date actionDate) {
        this.actionDate = actionDate;
    }
}
