/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.dspace.app.rest.RestResourceController;
import org.dspace.content.WorkFlowProcessMasterValue;

/**
 * The Group REST Resource
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@LinksRest(links = {
        @LinkRest(
                name = GroupRest.SUBGROUPS,
                method = "getGroups"
        ),
        @LinkRest(
                name = GroupRest.EPERSONS,
                method = "getMembers"
        ),
        @LinkRest(
                name = GroupRest.OBJECT,
                method = "getParentObject"
        )
})
public class GroupRest extends DSpaceObjectRest {
    public static final String NAME = "group";
    public static final String CATEGORY = RestAddressableModel.EPERSON;

    public static final String GROUPS = "groups";
    public static final String SUBGROUPS = "subgroups";
    public static final String EPERSONS = "epersons";
    public static final String OBJECT = "object";
    private boolean permanent;
    private Boolean isdspace;
    private WorkFlowProcessMasterValueRest grouptypeRest = null;
    @Override
    public String getCategory() {
        return CATEGORY;
    }
    @Override
    public String getType() {
        return NAME;
    }
    public boolean isPermanent() {
        return permanent;
    }
    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }
    @Override
    @JsonIgnore
    public Class getController() {
        return RestResourceController.class;
    }

    public Boolean getIsdspace() {
        return isdspace;
    }

    public void setIsdspace(Boolean isdspace) {
        this.isdspace = isdspace;
    }

    public WorkFlowProcessMasterValueRest getGrouptypeRest() {
        return grouptypeRest;
    }
    public void setGrouptypeRest(WorkFlowProcessMasterValueRest grouptypeRest) {
        this.grouptypeRest = grouptypeRest;
    }
}
