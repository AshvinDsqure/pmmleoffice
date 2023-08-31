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
        rest.setUuid(obj.getID().toString());
        if (obj.getServiceprovider() != null) {
            rest.setServiceprovider(obj.getServiceprovider());
        }
        if (obj.getOutwardNumber() != null) {
            rest.setOutwardNumber(obj.getOutwardNumber());
        }
        if (obj.getAwbno() != null) {
            rest.setAwbno(obj.getAwbno());
        }
        if (obj.getOutwardDate() != null) {
            rest.setOutwardDate(obj.getOutwardDate());
        }
        if (obj.getOutwardDepartment() != null) {
            rest.setOutwardDepartmentRest(groupConverter.convert(obj.getOutwardDepartment(), projection));
        }
        if (obj.getOutwardmedium() != null) {
            rest.setOutwardmediumRest(workFlowProcessMasterValueConverter.convert(obj.getOutwardmedium(), projection));
        }
        if (obj.getCategory() != null) {
            rest.setCategoryRest(workFlowProcessMasterValueConverter.convert(obj.getCategory(), projection));
        }
        if (obj.getSubcategory() != null) {
            rest.setSubcategoryRest(workFlowProcessMasterValueConverter.convert(obj.getSubcategory(), projection));
        }
        if (obj.getLettercategory() != null) {
            rest.setLettercategoryRest(workFlowProcessMasterValueConverter.convert(obj.getLettercategory(), projection));
        }
        if (obj.getOfficelocation() != null) {
            rest.setOfficelocationRest(workFlowProcessMasterValueConverter.convert(obj.getOfficelocation(), projection));
        }
        if (obj.getCategoriesoffile() != null) {
            rest.setCategoriesoffileRest(workFlowProcessMasterValueConverter.convert(obj.getCategoriesoffile(), projection));
        }
        if (obj.getSubcategoriesoffile() != null) {
            rest.setSubcategoriesoffileRest(workFlowProcessMasterValueConverter.convert(obj.getSubcategoriesoffile(), projection));
        }
        if(obj.getDispatchdate()!=null){
            rest.setDispatchdate(obj.getDispatchdate());
        }
        return rest;
    }

    public WorkFlowProcessOutwardDetails convert(Context context, WorkFlowProcessOutwardDetailsRest rest) throws SQLException {
        WorkFlowProcessOutwardDetails obj = new WorkFlowProcessOutwardDetails();
        if (rest.getOutwardDepartmentRest() != null && rest.getOutwardDepartmentRest().getId() != null && !rest.getOutwardDepartmentRest().getId().isEmpty()) {
            obj.setOutwardDepartment(groupConverter.convert(context, rest.getOutwardDepartmentRest()));
        }
        if (rest.getOutwardmediumRest() != null) {
            obj.setOutwardmedium(workFlowProcessMasterValueConverter.convert(context, rest.getOutwardmediumRest()));
        }
        if (rest.getOutwardDate() != null) {
            obj.setOutwardDate(rest.getOutwardDate());
        }
        if (rest.getDispatchdate() != null) {
            obj.setDispatchdate(rest.getDispatchdate());
        }
        if (rest.getServiceprovider() != null) {
            obj.setServiceprovider(rest.getServiceprovider());
        }
        if (rest.getOutwardNumber() != null) {
            obj.setOutwardNumber(rest.getOutwardNumber());
        }
        if (rest.getAwbno() != null) {
            obj.setAwbno(rest.getAwbno());
        }
        if (rest.getCategory() != null) {
            obj.setCategory(workFlowProcessMasterValueConverter.convert(context, rest.getCategoryRest()));
        }
        if (rest.getSubcategoryRest() != null) {
            obj.setSubcategory(workFlowProcessMasterValueConverter.convert(context, rest.getSubcategoryRest()));
        }
        if (rest.getLettercategoryRest() != null) {
            obj.setLettercategory(workFlowProcessMasterValueConverter.convert(context, rest.getLettercategoryRest()));
        }
        if (rest.getOfficelocationRest() != null) {
            obj.setOfficelocation(workFlowProcessMasterValueConverter.convert(context, rest.getOfficelocationRest()));
        }
        if (rest.getCategoriesoffileRest() != null) {
            obj.setCategoriesoffile(workFlowProcessMasterValueConverter.convert(context, rest.getCategoriesoffileRest()));
        }
        if (rest.getSubcategoriesoffileRest() != null) {
            obj.setSubcategoriesoffile(workFlowProcessMasterValueConverter.convert(context, rest.getSubcategoriesoffileRest()));
        }


        return obj;
    }
}
