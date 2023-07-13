/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import javax.persistence.*;
import javax.persistence.Entity;
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
 * @author ashivnmajethiya

 */
@Entity
@Table(name = "logincounter")
public class LoginCounter extends DSpaceObject implements DSpaceObjectLegacySupport{
    @Column(name = "logincounter_id", insertable = false, updatable = false)
    private Integer legacyId;
    @Column(name = "login_date", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date logindate = new Date();
    @Column(name = "logout_date", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date logoutdate = new Date();
    private String month;
    private String year;
    private UUID userid;
    private String sesionid;

    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }
    @Override
    public int getType() {
        return 0;
    }
    @Override
    public String getName() {
        return "workflowprocessmastervalue";
    }
    @Override
    public Integer getLegacyId() {
        return this.legacyId;
    }

    public Date getLogindate() {
        return logindate;
    }

    public void setLogindate(Date logindate) {
        this.logindate = logindate;
    }

    public Date getLogoutdate() {
        return logoutdate;
    }

    public void setLogoutdate(Date logoutdate) {
        this.logoutdate = logoutdate;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public UUID getUserid() {
        return userid;
    }

    public void setUserid(UUID userid) {
        this.userid = userid;
    }

    public String getSesionid() {
        return sesionid;
    }

    public void setSesionid(String sesionid) {
        this.sesionid = sesionid;
    }
}
