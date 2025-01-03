/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.dspace.app.rest.enums.WorkFlowUserType;
import org.dspace.app.rest.model.helper.MyDateConverter;
import org.dspace.content.WorkFlowProcessMasterValue;
import org.dspace.content.WorkflowProcess;
import org.dspace.eperson.EPerson;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Item REST Resource
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@LinksRest(links = {

})
public class WorkflowProcessSenderDiaryEpersonRest extends DSpaceObjectRest {
    public static final String NAME = "workflowprocesssenderdiaryeperson";
    public static final String PLURAL_NAME = "WorkflowProcessDefinitionEpersonRest";
    public static final String CATEGORY = RestAddressableModel.CORE;
    @JsonProperty
    private Integer index;
    @JsonProperty()
    @NotBlank
    private EPersonRest ePersonRest = null;

    @JsonProperty
    @NotBlank
    private WorkFlowProcessMasterValueRest departmentRest = null;
    @JsonProperty
    @NotBlank
    private WorkFlowProcessMasterValueRest officeRest = null;
    @JsonProperty
    private WorkFlowProcessMasterValueRest userType = null;


    private WorkFlowProcessRest workFlowProcessRest = null;

    @Override
    public String getCategory() {
        return CATEGORY;
    }
    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getType() {
        return NAME;
    }
    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public EPersonRest getePersonRest() {
        return ePersonRest;
    }

    public void setePersonRest(EPersonRest ePersonRest) {
        this.ePersonRest = ePersonRest;
    }

    public WorkFlowProcessMasterValueRest getDepartmentRest() {
        return departmentRest;
    }

    public void setDepartmentRest(WorkFlowProcessMasterValueRest departmentRest) {
        this.departmentRest = departmentRest;
    }
    public WorkFlowProcessMasterValueRest getOfficeRest() {
        return officeRest;
    }

    public void setOfficeRest(WorkFlowProcessMasterValueRest officeRest) {
        this.officeRest = officeRest;
    }

    public WorkFlowProcessMasterValueRest getUserType() {
        return userType;
    }

    public void setUserType(WorkFlowProcessMasterValueRest userType) {
        this.userType = userType;
    }

    public WorkFlowProcessRest getWorkFlowProcessRest() {
        return workFlowProcessRest;
    }

    public void setWorkFlowProcessRest(WorkFlowProcessRest workFlowProcessRest) {
        this.workFlowProcessRest = workFlowProcessRest;
    }
}
