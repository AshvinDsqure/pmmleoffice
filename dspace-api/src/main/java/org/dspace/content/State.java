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
@Table(name = "State")
public class State extends DSpaceObject implements DSpaceObjectLegacySupport{

    @Column(name = "State_lid", insertable = false, updatable = false)
    private Integer legacyId;
    @Column(name = "statename")
    private String statename;
    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;
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
        return "State";
    }

    public String getStatename() {
        return statename;
    }

    public void setStatename(String statename) {
        this.statename = statename;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
}
