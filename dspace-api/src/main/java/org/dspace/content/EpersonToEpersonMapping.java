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
@Table(name = "epersontoepersonmapping")
public class EpersonToEpersonMapping extends DSpaceObject{

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epersonmapping")
    public EpersonMapping epersonmapping=null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eperson")
    public EPerson eperson = null;


    @JoinColumn(name = "isactive")
    public Boolean isactive = false;

    @JoinColumn(name = "isdelete")
    public Boolean isdelete = false;


    @Override
    public int getType() {
        return 0;
    }

    @Override
    public String getName() {
        return "epersontoepersonmapping";
    }


    public EPerson getEperson() {
        return eperson;
    }

    public void setEperson(EPerson eperson) {
        this.eperson = eperson;
    }

    public EpersonMapping getEpersonmapping() {
        return epersonmapping;
    }

    public void setEpersonmapping(EpersonMapping epersonmapping) {
        this.epersonmapping = epersonmapping;
    }

    public Boolean getIsactive() {
        return isactive;
    }

    public void setIsactive(Boolean isactive) {
        this.isactive = isactive;
    }

    public Boolean getIsdelete() {
        return isdelete;
    }

    public void setIsdelete(Boolean isdelete) {
        this.isdelete = isdelete;
    }
}
