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
import org.dspace.content.WorkFlowProcessMasterValue;
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
public class EpersonMappingRest extends DSpaceObjectRest{
    public static final String CATEGORY = "epersonmapping";

    public static final String NAME = "epersonmapping";

    public static final String NAME_PLURAL = "epersonmappings";
    @JsonProperty
    private WorkFlowProcessMasterValueRest officeRest = null;
    @JsonProperty
        private WorkFlowProcessMasterValueRest departmentRest = null;
    @JsonProperty
    private WorkFlowProcessMasterValueRest designationRest = null;
    @JsonProperty
    private  Integer  tablenumber;
    @Override
    public String getCategory() {
        return CATEGORY;
    }
    @Override
    public String getType() {
        return NAME;
    }


    public WorkFlowProcessMasterValueRest getOfficeRest() {
        return officeRest;
    }

    public void setOfficeRest(WorkFlowProcessMasterValueRest officeRest) {
        this.officeRest = officeRest;
    }

    public WorkFlowProcessMasterValueRest getDepartmentRest() {
        return departmentRest;
    }

    public void setDepartmentRest(WorkFlowProcessMasterValueRest departmentRest) {
        this.departmentRest = departmentRest;
    }

    public WorkFlowProcessMasterValueRest getDesignationRest() {
        return designationRest;
    }

    public void setDesignationRest(WorkFlowProcessMasterValueRest designationRest) {
        this.designationRest = designationRest;
    }

    public Integer getTablenumber() {
        return tablenumber;
    }

    public void setTablenumber(Integer tablenumber) {
        this.tablenumber = tablenumber;
    }
}
