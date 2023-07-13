/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.dspace.app.rest.RestResourceController;
import org.dspace.app.rest.model.helper.MyDateConverter;
import org.dspace.content.WorkflowProcess;
import org.dspace.content.WorkflowProcessReferenceDoc;
import org.dspace.eperson.EPerson;

import javax.persistence.*;
import java.util.*;

/**
 * The rest resource used for workflow definitions
 *
 * @author Maria Verdonck (Atmire) on 11/12/2019
 */
@LinksRest(links = {

})
public class LoginCounterRest extends DSpaceObjectRest{

    public static final String CATEGORY = "logincounter";
    public static final String NAME = "logincounter";
    public static final String NAME_PLURAL = "logincounters";

    public static final String COLLECTIONS_MAPPED_TO = "collections";
    public static final String STEPS = "steps";

    @JsonProperty
    private String month;
    @JsonProperty
    private String year;
    @JsonProperty
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date logindate =null;
    @JsonProperty
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date logoutdate =null;

    @Override
    public String getCategory() {
        return CATEGORY;
    }
    @Override
    public String getType() {
        return NAME;
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
}
