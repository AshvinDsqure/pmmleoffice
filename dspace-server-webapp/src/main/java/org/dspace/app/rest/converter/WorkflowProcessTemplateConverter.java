/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.WorkflowProcessTemplateRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.WorkflowProcessTemplate;
import org.dspace.content.service.WorkflowProcessTemplateService;
import org.dspace.core.Context;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class WorkflowProcessTemplateConverter extends DSpaceObjectConverter<WorkflowProcessTemplate, WorkflowProcessTemplateRest> {
    @Autowired
    WorkflowProcessTemplateService workflowProcessTemplateService;

    @Autowired
    EPersonConverter ePersonConverter;
    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;

    @Autowired
    EpersonToEpersonMappingConverter epersonToEpersonMappingConverter;

    @Autowired
    WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;

    @Override
    public Class<WorkflowProcessTemplate> getModelClass() {
        return WorkflowProcessTemplate.class;
    }

    @Override
    protected WorkflowProcessTemplateRest newInstance() {
        return new WorkflowProcessTemplateRest();
    }

    @Override
    public WorkflowProcessTemplateRest convert(WorkflowProcessTemplate obj, Projection projection) {
        WorkflowProcessTemplateRest rest = new WorkflowProcessTemplateRest();
        try {
            if(obj.getInitDate()!=null){
                rest.setInitDate(obj.getInitDate());
            }
            if(obj.getIndex()!=null){
                rest.setIndex(obj.getIndex());
            }
            if(obj.getTemplate()!=null){
                rest.setTemplateRest(workFlowProcessMasterValueConverter.convert(obj.getTemplate(),projection));
            }
            if(obj.getTemplatetype()!=null){
                rest.setTemplatetypeRest(workFlowProcessMasterValueConverter.convert(obj.getTemplatetype(),projection));
            }
            if(obj.getEditortext()!=null){
               rest.setEditortext(obj.getEditortext());
            }
            if(obj.getePerson()!=null){
                rest.setePersonRest(ePersonConverter.convertBYUSer(obj.getePerson(),projection));
            }
            if(obj.getEpersontoepersonmapping()!=null){
                rest.setEpersontoepersonmappingRest(epersonToEpersonMappingConverter.convert(obj.getEpersontoepersonmapping(),projection));
            }
            rest.setUuid(obj.getID().toString());
            return rest;
        } catch (Exception e) {
            System.out.println("error");
            e.printStackTrace();
        }
        return null;
    }

    public WorkflowProcessTemplate convert(Context context,WorkflowProcessTemplateRest rest) throws SQLException {
        WorkflowProcessTemplate obj = new WorkflowProcessTemplate();
        if(rest.getInitDate()!=null){
            obj.setInitDate(rest.getInitDate());
        }
        if(rest.getIndex()!=null){
            obj.setIndex(rest.getIndex());
        }
        if(rest.getTemplateRest()!=null){
            obj.setTemplate(workFlowProcessMasterValueConverter.convert(context,rest.getTemplateRest()));
        }
        if(rest.getePersonRest()!=null){
            obj.setePerson(ePersonConverter.convert(context,rest.getePersonRest()));
        }
        if(rest.getTemplatetypeRest()!=null){
            obj.setTemplatetype(workFlowProcessMasterValueConverter.convert(context,rest.getTemplatetypeRest()));
        }
        if(rest.getEditortext()!=null){
            obj.setEditortext(rest.getEditortext());
        }
        if(rest.getEpersontoepersonmappingRest()!=null){
            obj.setEpersontoepersonmapping(epersonToEpersonMappingConverter.convertbyService(context,rest.getEpersontoepersonmappingRest()));
        }
        return obj;
    }

    public WorkflowProcessTemplate convert(Context context,WorkflowProcessTemplate obj,WorkflowProcessTemplateRest rest) throws SQLException {
        if(rest.getInitDate()!=null){
            obj.setInitDate(rest.getInitDate());
        }
        if(rest.getIndex()!=null){
            obj.setIndex(rest.getIndex());
        }
        if(rest.getTemplateRest()!=null){
            obj.setTemplate(workFlowProcessMasterValueConverter.convert(context,rest.getTemplateRest()));
        }
        if(rest.getePersonRest()!=null){
            obj.setePerson(ePersonConverter.convert(context,rest.getePersonRest()));
        }
        if(rest.getTemplatetypeRest()!=null){
            obj.setTemplatetype(workFlowProcessMasterValueConverter.convert(context,rest.getTemplatetypeRest()));
        }
        if(rest.getEditortext()!=null){
            obj.setEditortext(rest.getEditortext());
        }
        if(rest.getEpersontoepersonmappingRest()!=null){
            obj.setEpersontoepersonmapping(epersonToEpersonMappingConverter.convertbyService(context,rest.getEpersontoepersonmappingRest()));
        }

        rest.setUuid(obj.getID().toString());
        return obj;
    }
    public WorkflowProcessTemplate convertService(Context context, WorkflowProcessTemplateRest rest) throws SQLException {
        if (rest != null && rest.getId() != null) {
            return workflowProcessTemplateService.find(context, UUID.fromString(rest.getId()));
        }
        return null;
    }
}
