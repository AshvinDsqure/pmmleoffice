/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.*;
import org.dspace.app.rest.projection.Projection;
import org.dspace.app.rest.utils.DateUtils;
import org.dspace.content.*;
import org.dspace.content.service.BitstreamService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is the converter from/to the EPerson in the DSpace API data model and the
 * REST data model
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@Component
public class WorkFlowProcessEpersonConverter extends DSpaceObjectConverter<WorkflowProcessEperson, WorkflowProcessEpersonRest> {
    @Autowired
    EPersonConverter ePersonConverter;
    @Autowired
    EPersonService ePersonService;
    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public WorkflowProcessEpersonRest convert(WorkflowProcessEperson obj, Projection projection) {
        WorkflowProcessEpersonRest workflowProcessDefinitionEpersonRest = super.convert(obj, projection);
        if (obj.getePerson() != null) {
            workflowProcessDefinitionEpersonRest.setePersonRest(ePersonConverter.convert(obj.getePerson(), projection));
        }
        if (obj.getDepartment() != null) {
            workflowProcessDefinitionEpersonRest.setDepartmentRest(workFlowProcessMasterValueConverter.convert(obj.getDepartment(), projection));
        }
        if (obj.getOffice() != null) {
            workflowProcessDefinitionEpersonRest.setOfficeRest(workFlowProcessMasterValueConverter.convert(obj.getOffice(), projection));
        }
        if (obj.getUsertype() != null) {
            workflowProcessDefinitionEpersonRest.setUserType(workFlowProcessMasterValueConverter.convert(obj.getUsertype(), projection));
        }
        if (obj.getResponsebyallusers() != null) {
            workflowProcessDefinitionEpersonRest.setResponsebyallusersRest(workFlowProcessMasterValueConverter.convert(obj.getResponsebyallusers(), projection));
        }
        if(obj.getInitiator()!=null) {
            workflowProcessDefinitionEpersonRest.setInitiator(obj.getInitiator());
        }
        if(obj.getRemark()!=null){
            workflowProcessDefinitionEpersonRest.setRemark(obj.getRemark());
        }

        workflowProcessDefinitionEpersonRest.setAssignDate(obj.getAssignDate());
        workflowProcessDefinitionEpersonRest.setIndex(obj.getIndex());
        workflowProcessDefinitionEpersonRest.setIssequence(obj.getIssequence());
        workflowProcessDefinitionEpersonRest.setSequence(obj.getSequence());
        workflowProcessDefinitionEpersonRest.setOwner(obj.getOwner());
        workflowProcessDefinitionEpersonRest.setIsrefer(obj.getIsrefer());
        workflowProcessDefinitionEpersonRest.setIsapproved(obj.getIsapproved());
        workflowProcessDefinitionEpersonRest.setIsapproved(obj.getIsapproved());
        workflowProcessDefinitionEpersonRest.setIssignnote(obj.getIssignnote());
        workflowProcessDefinitionEpersonRest.setIsdraftreview(obj.getIsdraftreview());

        workflowProcessDefinitionEpersonRest.setIsacknowledgement(obj.getIsacknowledgement());
        return workflowProcessDefinitionEpersonRest;
    }

    public WorkflowProcessEpersonRest convertByHistory(WorkflowProcessEperson obj, Projection projection) {
        WorkflowProcessEpersonRest workflowProcessDefinitionEpersonRest = new WorkflowProcessEpersonRest();
        if (obj.getePerson() != null) {
            workflowProcessDefinitionEpersonRest.setePersonRest(ePersonConverter.convert(obj.getePerson(), projection));
        }
        if (obj.getDepartment() != null) {
            workflowProcessDefinitionEpersonRest.setDepartmentRest(workFlowProcessMasterValueConverter.convert(obj.getDepartment(), projection));
        }
        if (obj.getOffice() != null) {
            workflowProcessDefinitionEpersonRest.setOfficeRest(workFlowProcessMasterValueConverter.convert(obj.getOffice(), projection));
        }
        if (obj.getUsertype() != null) {
            workflowProcessDefinitionEpersonRest.setUserType(workFlowProcessMasterValueConverter.convert(obj.getUsertype(), projection));
        }
        if (obj.getResponsebyallusers() != null) {
            workflowProcessDefinitionEpersonRest.setResponsebyallusersRest(workFlowProcessMasterValueConverter.convert(obj.getResponsebyallusers(), projection));
        }
        if(obj.getInitiator()!=null) {
            workflowProcessDefinitionEpersonRest.setInitiator(obj.getInitiator());
        }
        if(obj.getRemark()!=null){
            workflowProcessDefinitionEpersonRest.setRemark(obj.getRemark());
        }

        workflowProcessDefinitionEpersonRest.setAssignDate(obj.getAssignDate());
        workflowProcessDefinitionEpersonRest.setIndex(obj.getIndex());
        workflowProcessDefinitionEpersonRest.setIssequence(obj.getIssequence());
        workflowProcessDefinitionEpersonRest.setSequence(obj.getSequence());
        workflowProcessDefinitionEpersonRest.setOwner(obj.getOwner());
        workflowProcessDefinitionEpersonRest.setIsrefer(obj.getIsrefer());
        workflowProcessDefinitionEpersonRest.setIsapproved(obj.getIsapproved());
        workflowProcessDefinitionEpersonRest.setIsacknowledgement(obj.getIsacknowledgement());
        workflowProcessDefinitionEpersonRest.setIssignnote(obj.getIssignnote());
        workflowProcessDefinitionEpersonRest.setIsdraftreview(obj.getIsdraftreview());
        return workflowProcessDefinitionEpersonRest;
    }

    @Override
    protected WorkflowProcessEpersonRest newInstance() {
        return new WorkflowProcessEpersonRest();
    }

    @Override
    public Class<WorkflowProcessEperson> getModelClass() {
        return WorkflowProcessEperson.class;
    }

    public WorkflowProcessEperson convert(Context context, WorkflowProcessEpersonRest rest) throws SQLException {
        WorkflowProcessEperson workflowProcessEperson = new WorkflowProcessEperson();
        if(rest.getePersonRest()!=null && rest.getePersonRest().getUuid()!=null&& !DateUtils.isNullOrEmptyOrBlank(rest.getePersonRest().getUuid())) {
            workflowProcessEperson.setePerson(ePersonService.find(context, UUID.fromString(rest.getePersonRest().getUuid())));
        }
        if (rest.getDepartmentRest() != null)
            workflowProcessEperson.setDepartment(workFlowProcessMasterValueConverter.convert(context, rest.getDepartmentRest()));
        if (rest.getOfficeRest() != null)
            workflowProcessEperson.setOffice(workFlowProcessMasterValueConverter.convert(context, rest.getOfficeRest()));
        if (rest.getUserType() != null) {
            workflowProcessEperson.setUsertype(workFlowProcessMasterValueConverter.convert(context, rest.getUserType()));
        }
        if (rest.getResponsebyallusersRest() != null) {
            workflowProcessEperson.setResponsebyallusers(workFlowProcessMasterValueConverter.convert(context, rest.getResponsebyallusersRest()));
        }
        if(rest.getAssignDate() != null){
            workflowProcessEperson.setAssignDate(rest.getAssignDate());
        }
        if(rest.getInitiator()!=null) {
            workflowProcessEperson.setInitiator(rest.getInitiator());
        }
        if(rest.getRemark()!=null){
            workflowProcessEperson.setRemark(rest.getRemark());
        }
        if(rest.getIndex()!=null){
            workflowProcessEperson.setIndex(rest.getIndex());
        }
        workflowProcessEperson.setIssequence(rest.getIssequence());
        workflowProcessEperson.setSequence(rest.getSequence());
        workflowProcessEperson.setIsrefer(rest.getIsrefer());
        workflowProcessEperson.setIssignnote(rest.getIssignnote());
        workflowProcessEperson.setIsdraftreview(rest.getIsdraftreview());
        return workflowProcessEperson;
    }
    public WorkflowProcessEperson convert(Context context, EPerson rest) {
        WorkflowProcessEperson workflowProcessEperson = new WorkflowProcessEperson();
        workflowProcessEperson.setePerson(rest);
        if (rest.getDepartment() != null)
            workflowProcessEperson.setDepartment(rest.getDepartment());
        if (rest.getOffice() != null)
           workflowProcessEperson.setOffice(rest.getOffice());
        return workflowProcessEperson;
    }

}
