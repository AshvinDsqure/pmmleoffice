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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
 * @author ashivnmajethiya

 */
@Entity
@Table(name = "countries")
public class Country extends DSpaceObject implements DSpaceObjectLegacySupport{

    @Column(name = "workflowprocessmaster_lid", insertable = false, updatable = false)
    private Integer legacyId;
    @Column(name = "countryname")
    private String countryname;

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
        return "workflowprocessmaster";
    }

    public String getCountryname() {
        return countryname;
    }

    public void setCountryname(String countryname) {
        this.countryname = countryname;
    }
}
