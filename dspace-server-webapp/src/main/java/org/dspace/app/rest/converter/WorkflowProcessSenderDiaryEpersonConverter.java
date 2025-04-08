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
public class WorkflowProcessSenderDiaryEpersonConverter extends DSpaceObjectConverter<WorkflowProcessSenderDiaryEperson, WorkflowProcessSenderDiaryEpersonRest> {
    @Autowired
    EPersonConverter ePersonConverter;
    @Autowired
    EPersonService ePersonService;
    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;

    @Autowired
    EpersonToEpersonMappingConverter epersonToEpersonMappingConverter;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public WorkflowProcessSenderDiaryEpersonRest convert(WorkflowProcessSenderDiaryEperson obj, Projection projection) {
        WorkflowProcessSenderDiaryEpersonRest workflowProcessDefinitionEpersonRest = super.convert(obj, projection);
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
        if(obj.getIndex()!=null){
            workflowProcessDefinitionEpersonRest.setIndex(obj.getIndex());
        }
        if(obj.getEpersontoepersonmapping()!=null){
            workflowProcessDefinitionEpersonRest.setEpersonToEpersonMappingRest(epersonToEpersonMappingConverter.convert(obj.getEpersontoepersonmapping(),projection));
        }
        workflowProcessDefinitionEpersonRest.setIndex(obj.getIndex());
        return workflowProcessDefinitionEpersonRest;
    }

    public WorkflowProcessSenderDiaryEpersonRest convertByHistory(WorkflowProcessSenderDiaryEperson obj, Projection projection) {
        WorkflowProcessSenderDiaryEpersonRest workflowProcessDefinitionEpersonRest = new WorkflowProcessSenderDiaryEpersonRest();
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
        if(obj.getIndex()!=null){
            workflowProcessDefinitionEpersonRest.setIndex(obj.getIndex());
        }

        if(obj.getEpersontoepersonmapping()!=null){
            workflowProcessDefinitionEpersonRest.setEpersonToEpersonMappingRest(epersonToEpersonMappingConverter.convert(obj.getEpersontoepersonmapping(),projection));
        }
      workflowProcessDefinitionEpersonRest.setIndex(obj.getIndex());
      return workflowProcessDefinitionEpersonRest;
    }

    @Override
    protected WorkflowProcessSenderDiaryEpersonRest newInstance() {
        return new WorkflowProcessSenderDiaryEpersonRest();
    }

    @Override
    public Class<WorkflowProcessSenderDiaryEperson> getModelClass() {
        return WorkflowProcessSenderDiaryEperson.class;
    }

    public WorkflowProcessSenderDiaryEperson convert(Context context, WorkflowProcessSenderDiaryEpersonRest rest) throws SQLException {
        WorkflowProcessSenderDiaryEperson obj = new WorkflowProcessSenderDiaryEperson();
        if(rest.getePersonRest()!=null && rest.getePersonRest().getUuid()!=null&& !DateUtils.isNullOrEmptyOrBlank(rest.getePersonRest().getUuid())) {
            obj.setePerson(ePersonService.find(context, UUID.fromString(rest.getePersonRest().getUuid())));
        }
        if (rest.getDepartmentRest() != null)
            obj.setDepartment(workFlowProcessMasterValueConverter.convert(context, rest.getDepartmentRest()));
        if (rest.getOfficeRest() != null)
            obj.setOffice(workFlowProcessMasterValueConverter.convert(context, rest.getOfficeRest()));
        if (rest.getUserType() != null) {
            obj.setUsertype(workFlowProcessMasterValueConverter.convert(context, rest.getUserType()));
        }
        if(rest.getIndex()!=null){
            obj.setIndex(rest.getIndex());
        }
        if(rest.getEpersonToEpersonMappingRest()!=null){
            obj.setEpersontoepersonmapping(epersonToEpersonMappingConverter.convertbyService(context,rest.getEpersonToEpersonMappingRest()));
        }
     return obj;
    }
    public WorkflowProcessSenderDiaryEperson convert(Context context,WorkflowProcessSenderDiaryEperson obj, WorkflowProcessSenderDiaryEpersonRest rest) throws SQLException {
       // WorkflowProcessSenderDiaryEperson obj = new WorkflowProcessSenderDiaryEperson();
        if(rest.getePersonRest()!=null && rest.getePersonRest().getUuid()!=null&& !DateUtils.isNullOrEmptyOrBlank(rest.getePersonRest().getUuid())) {
            obj.setePerson(ePersonService.find(context, UUID.fromString(rest.getePersonRest().getUuid())));
        }
        if (rest.getDepartmentRest() != null)
            obj.setDepartment(workFlowProcessMasterValueConverter.convert(context, rest.getDepartmentRest()));
        if (rest.getOfficeRest() != null)
            obj.setOffice(workFlowProcessMasterValueConverter.convert(context, rest.getOfficeRest()));
        if (rest.getUserType() != null) {
            obj.setUsertype(workFlowProcessMasterValueConverter.convert(context, rest.getUserType()));
        }
        if(rest.getEpersonToEpersonMappingRest()!=null){
            obj.setEpersontoepersonmapping(epersonToEpersonMappingConverter.convertbyService(context,rest.getEpersonToEpersonMappingRest()));
        }

        if(rest.getIndex()!=null){
            obj.setIndex(rest.getIndex());
        }
        return obj;
    }
    public WorkflowProcessSenderDiaryEperson convert(Context context, EPerson rest) {
        WorkflowProcessSenderDiaryEperson obj = new WorkflowProcessSenderDiaryEperson();
        obj.setePerson(rest);
        if (rest.getDepartment() != null)
            obj.setDepartment(rest.getDepartment());
        if (rest.getOffice() != null)
            obj.setOffice(rest.getOffice());
        return obj;
    }

}
