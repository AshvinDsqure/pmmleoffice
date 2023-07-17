/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.dspace.app.rest.model.helper.MyDateConverter;
import org.dspace.app.rest.validation.WorkflowProcessMasterValueValid;
import org.dspace.app.rest.validation.WorkflowProcessValid;
import org.dspace.content.WorkFlowProcessMasterValue;
import org.dspace.content.WorkflowProcessEperson;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
public class WorkFlowProcessFilterRest extends DSpaceObjectRest {
    public static final String NAME = "workflowprocessefilter";
    public static final String PLURAL_NAME = "workflowprocessefilters";
    public static final String CATEGORY = RestAddressableModel.WORKFLOWPROCESS;

    @JsonProperty
    private WorkFlowProcessMasterValueRest priorityRest;
    @JsonProperty
    private WorkFlowProcessMasterValueRest workflowStatusRest;
    @JsonProperty
    private WorkFlowProcessMasterValueRest workflowTypeRest;

    @JsonProperty
    private WorkFlowProcessMasterValueRest departmentRest;

    @JsonProperty
    private EPersonRest ePersonRest ;


    @JsonProperty
    private String subject;

    @JsonProperty
    private String inward;
    @JsonProperty
    private String outward;

    @Override
    public String getCategory() {
        return "workflowprocessefilter";
    }
    @Override
    public String getType() {
        return "workflowprocessefilter";
    }

    public WorkFlowProcessMasterValueRest getPriorityRest() {
        return priorityRest;
    }

    public void setPriorityRest(WorkFlowProcessMasterValueRest priorityRest) {
        this.priorityRest = priorityRest;
    }

    public WorkFlowProcessMasterValueRest getWorkflowStatusRest() {
        return workflowStatusRest;
    }

    public void setWorkflowStatusRest(WorkFlowProcessMasterValueRest workflowStatusRest) {
        this.workflowStatusRest = workflowStatusRest;
    }

    public void setWorkflowTypeRest(WorkFlowProcessMasterValueRest workflowTypeRest) {
        this.workflowTypeRest = workflowTypeRest;
    }

    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }

    public WorkFlowProcessMasterValueRest getWorkflowTypeRest() {
        return workflowTypeRest;
    }

    public WorkFlowProcessMasterValueRest getDepartmentRest() {
        return departmentRest;
    }

    public void setDepartmentRest(WorkFlowProcessMasterValueRest departmentRest) {
        this.departmentRest = departmentRest;
    }

    public EPersonRest getePersonRest() {
        return ePersonRest;
    }

    public void setePersonRest(EPersonRest ePersonRest) {
        this.ePersonRest = ePersonRest;
    }

    public String getInward() {
        return inward;
    }

    public void setInward(String inward) {
        this.inward = inward;
    }

    public String getOutward() {
        return outward;
    }

    public void setOutward(String outward) {
        this.outward = outward;
    }
}
