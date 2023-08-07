/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.WorkFlowProcessInwardDetailsRest;
import org.dspace.app.rest.model.WorkFlowProcessOutwardDetailsRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.WorkFlowProcessInwardDetails;
import org.dspace.content.WorkFlowProcessMasterValue;
import org.dspace.content.WorkFlowProcessOutwardDetails;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.GroupService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.UUID;

@Component
public class WorkFlowProcessOutwardDetailsConverter extends DSpaceObjectConverter<WorkFlowProcessOutwardDetails, WorkFlowProcessOutwardDetailsRest> {


    @Autowired
    private GroupConverter groupConverter;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private GroupService groupService;
    @Autowired
    private WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;
    @Override
    public Class<WorkFlowProcessOutwardDetails> getModelClass() {
        return WorkFlowProcessOutwardDetails.class;
    }
    @Override
    protected WorkFlowProcessOutwardDetailsRest newInstance() {
        return new WorkFlowProcessOutwardDetailsRest();
    }
    @Override
    public WorkFlowProcessOutwardDetailsRest convert(WorkFlowProcessOutwardDetails obj, Projection projection) {
        WorkFlowProcessOutwardDetailsRest rest = new WorkFlowProcessOutwardDetailsRest();
        rest.setServiceprovider(obj.getServiceprovider());
        rest.setOutwardNumber(obj.getOutwardNumber());
        rest.setUuid(obj.getID().toString());
        rest.setAwbno(obj.getAwbno());
        rest.setOutwardDate(obj.getOutwardDate());
        if(obj.getOutwardDepartment()!=null){
            rest.setOutwardDepartmentRest(groupConverter.convert(obj.getOutwardDepartment(),projection));
        }
        if(obj.getOutwardmedium()!=null){
            rest.setOutwardmediumRest(workFlowProcessMasterValueConverter.convert(obj.getOutwardmedium(), projection));
        }
        return rest;
    }
    public WorkFlowProcessOutwardDetails convert(Context context,WorkFlowProcessOutwardDetailsRest rest) throws SQLException {
        WorkFlowProcessOutwardDetails obj = new WorkFlowProcessOutwardDetails();
       if(rest.getOutwardDepartmentRest()!=null && rest.getOutwardDepartmentRest().getId()!=null){
           obj.setOutwardDepartment(groupConverter.convert(context,rest.getOutwardDepartmentRest()));
       }
        if(rest.getOutwardmediumRest()!=null){
            obj.setOutwardmedium(workFlowProcessMasterValueConverter.convert(context,rest.getOutwardmediumRest()));
        }
        obj.setOutwardDate(rest.getOutwardDate());
        obj.setDispatchdate(rest.getDispatchdate());
        obj.setServiceprovider(rest.getServiceprovider());
        obj.setOutwardNumber(rest.getOutwardNumber());
        obj.setAwbno(rest.getAwbno());
        return obj;
    }
}
