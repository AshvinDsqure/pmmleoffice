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
import org.dspace.app.rest.utils.DateUtils;
import org.dspace.content.WorkflowProcessDefinition;
import org.dspace.content.WorkflowProcessSenderDiary;
import org.dspace.content.service.VipNameService;
import org.dspace.content.service.VipService;
import org.dspace.core.Context;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class WorkflowProcessSenderDiaryConverter extends DSpaceObjectConverter<WorkflowProcessSenderDiary, WorkflowProcessSenderDiaryRest> {

    @Override
    public Class<WorkflowProcessSenderDiary> getModelClass() {

        return WorkflowProcessSenderDiary.class;
    }

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    WorkFlowProcessDraftDetailsConverter workFlowProcessDraftDetailsConverter;

    @Autowired
    VipService vipService;
    @Autowired
    VipNameService vipNameService;

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
        if (obj.getPincode() != null) {
            rest.setPincode(obj.getPincode());
        }
        if (obj.getFax() != null) {
            rest.setFax(obj.getFax());
        }
        if (obj.getLandline() != null) {
            rest.setLandline(obj.getLandline());
        }
        if (obj.getVip() != null && obj.getVip().getVip() != null) {
            rest.setVipRest(obj.getVip().getVip());
        }
        if (obj.getVipname() != null && obj.getVipname().getVipname() != null) {
            rest.setVipnameRest(obj.getVipname().getVipname());
        }
        if (obj.getID() != null) {
            rest.setUuid(obj.getID().toString());
        }
        if(obj.getStatus()!=null){
            rest.setStatus(obj.getStatus());
        }
        return rest;
    }

    public WorkflowProcessSenderDiary convert(Context context, WorkflowProcessSenderDiary obj, WorkflowProcessSenderDiaryRest rest) {
        if (rest.getState() != null) {
            obj.setState(rest.getState());
        }
        if (rest.getEmail() != null) {
            obj.setEmail(rest.getEmail());
        }
        if (rest.getAddress() != null) {
            obj.setAddress(rest.getAddress());
        }
        if (rest.getCity() != null) {
            obj.setCity(rest.getCity());
        }
        if (rest.getCountry() != null) {
            obj.setCountry(rest.getCountry());
        }
        if (rest.getSendername() != null) {
            obj.setSendername(rest.getSendername());
        }
        if (rest.getContactNumber() != null) {
            obj.setContactNumber(rest.getContactNumber());
        }
        if (!DateUtils.isNullOrEmptyOrBlank(rest.getDesignation())) {
            obj.setDesignation(rest.getDesignation());
        }
        if (!DateUtils.isNullOrEmptyOrBlank(rest.getOrganization())) {
            obj.setOrganization(rest.getOrganization());
        }
        if (!DateUtils.isNullOrEmptyOrBlank(rest.getPincode())) {
            obj.setPincode(rest.getPincode());
        }

        if (!DateUtils.isNullOrEmptyOrBlank(rest.getFax())) {
            System.out.println("fax"+rest.getFax());
            obj.setFax(rest.getFax());
        }
        if (!DateUtils.isNullOrEmptyOrBlank(rest.getLandline())) {
            System.out.println("fax"+rest.getLandline());
            obj.setLandline(rest.getLandline());
        }

        if (rest.getVipRest() != null) {
            try {
                obj.setVip(vipService.find(context, UUID.fromString(rest.getVipRest())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!DateUtils.isNullOrEmptyOrBlank(rest.getVipnameRest())) {
            try {
                obj.setVipname(vipNameService.find(context, UUID.fromString(rest.getVipnameRest())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(rest.getStatus()!=null){
            obj.setStatus(rest.getStatus());
        }
        return obj;
    }

    public WorkflowProcessSenderDiary convert(Context context, WorkflowProcessSenderDiaryRest rest) {
        WorkflowProcessSenderDiary obj = new WorkflowProcessSenderDiary();
        if (rest.getState() != null) {
            obj.setState(rest.getState());
        }
        if (rest.getEmail() != null) {
            obj.setEmail(rest.getEmail());
        }
        if (rest.getAddress() != null) {
            obj.setAddress(rest.getAddress());
        }
        if (rest.getCity() != null) {
            obj.setCity(rest.getCity());
        }
        if (rest.getCountry() != null) {
            obj.setCountry(rest.getCountry());
        }
        if (rest.getSendername() != null) {
            obj.setSendername(rest.getSendername());
        }
        if (rest.getContactNumber() != null) {
            obj.setContactNumber(rest.getContactNumber());
        }
        if (!DateUtils.isNullOrEmptyOrBlank(rest.getDesignation())) {
            obj.setDesignation(rest.getDesignation());
        }
        if (!DateUtils.isNullOrEmptyOrBlank(rest.getOrganization())) {
            obj.setOrganization(rest.getOrganization());
        }
        if (!DateUtils.isNullOrEmptyOrBlank(rest.getPincode())) {
            obj.setPincode(rest.getPincode());
        }
        if (!DateUtils.isNullOrEmptyOrBlank(rest.getFax())) {
            obj.setFax(rest.getFax());
        }
        if (!DateUtils.isNullOrEmptyOrBlank(rest.getLandline())) {
            obj.setLandline(rest.getLandline());
        }
        if (!DateUtils.isNullOrEmptyOrBlank(rest.getVipRest())) {
            try {
                    obj.setVip(vipService.find(context, UUID.fromString(rest.getVipRest())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!DateUtils.isNullOrEmptyOrBlank(rest.getVipnameRest())) {
            try {
                obj.setVipname(vipNameService.find(context, UUID.fromString(rest.getVipnameRest())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(rest.getStatus()!=null){
            obj.setStatus(rest.getStatus());
        }
        return obj;
    }
}
