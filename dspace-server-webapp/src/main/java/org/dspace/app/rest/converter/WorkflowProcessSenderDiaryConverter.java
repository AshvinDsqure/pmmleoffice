/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import com.google.gson.Gson;
import org.dspace.app.rest.model.WorkFlowProcessDefinitionRest;
import org.dspace.app.rest.model.WorkflowItemRest;
import org.dspace.app.rest.model.WorkflowProcessSenderDiaryRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.WorkflowProcessDefinition;
import org.dspace.content.WorkflowProcessSenderDiary;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class WorkflowProcessSenderDiaryConverter extends DSpaceObjectConverter<WorkflowProcessSenderDiary, WorkflowProcessSenderDiaryRest> {

    @Override
    public Class<WorkflowProcessSenderDiary> getModelClass() {

        return WorkflowProcessSenderDiary.class;
    }

    @Autowired
    ModelMapper modelMapper;


    @Override
    protected WorkflowProcessSenderDiaryRest newInstance() {
        return new WorkflowProcessSenderDiaryRest();
    }

    @Override
    public WorkflowProcessSenderDiaryRest convert(WorkflowProcessSenderDiary obj, Projection projection) {
        WorkflowProcessSenderDiaryRest rest = new WorkflowProcessSenderDiaryRest();
        if (obj.getState() != null) {
            rest.setState(obj.getState());
        }
        if (obj.getEmail() != null) {
            rest.setEmail(obj.getEmail());
        }
        if (obj.getAddress() != null) {
            rest.setAddress(obj.getAddress());
        }
        if (obj.getCity() != null) {
            rest.setCity(obj.getCity());
        }
        if (obj.getCountry() != null) {
            rest.setCountry(obj.getCountry());
        }
        if (obj.getSendername() != null) {
            rest.setSendername(obj.getSendername());
        }
        if (obj.getContactNumber() != null) {
            rest.setContactNumber(obj.getContactNumber());
        }
        if (obj.getDesignation() != null) {
            rest.setDesignation(obj.getDesignation());
        }
        if (obj.getOrganization() != null) {
            rest.setOrganization(obj.getOrganization());
        }
        if (obj.getPincode()!=null){
            rest.setPincode(obj.getPincode());
        }
        if (obj.getFax()!=null){
            rest.setFax(obj.getFax());
        }
        if (obj.getLandline()!=null){
            rest.setLandline(obj.getLandline());
        }
        rest.setUuid(obj.getID().toString());
        return rest;
    }

    public WorkflowProcessSenderDiary convert(WorkflowProcessSenderDiary obj, WorkflowProcessSenderDiaryRest rest) {
        obj = modelMapper.map(rest, WorkflowProcessSenderDiary.class);
        return obj;
    }

    public WorkflowProcessSenderDiary convert(WorkflowProcessSenderDiaryRest rest) {
        WorkflowProcessSenderDiary obj = null;
        if (rest != null) {
            obj = new WorkflowProcessSenderDiary();
            obj = modelMapper.map(rest, WorkflowProcessSenderDiary.class);
        }
        return obj;
    }

}
