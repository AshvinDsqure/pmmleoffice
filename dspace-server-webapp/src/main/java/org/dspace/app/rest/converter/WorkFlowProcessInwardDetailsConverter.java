/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.WorkFlowProcessInwardDetailsRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.WorkFlowProcessInwardDetails;
import org.dspace.core.Context;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class WorkFlowProcessInwardDetailsConverter extends DSpaceObjectConverter<WorkFlowProcessInwardDetails, WorkFlowProcessInwardDetailsRest> {
    @Autowired
    ModelMapper modelMapper;

    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;

    @Override
    public Class<WorkFlowProcessInwardDetails> getModelClass() {
        return WorkFlowProcessInwardDetails.class;
    }

    @Override
    protected WorkFlowProcessInwardDetailsRest newInstance() {
        return new WorkFlowProcessInwardDetailsRest();
    }


    @Override
    public WorkFlowProcessInwardDetailsRest convert(WorkFlowProcessInwardDetails obj, Projection projection) {
        WorkFlowProcessInwardDetailsRest rest = new WorkFlowProcessInwardDetailsRest();
        if (obj.getInwardNumber() != null) {
            rest.setInwardNumber(obj.getInwardNumber());
        }
        if (obj.getInwardDate() != null) {
            rest.setInwardDate(obj.getInwardDate());
        }
        if (obj.getReceivedDate() != null) {
            rest.setReceivedDate(obj.getReceivedDate());
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
        rest.setUuid(obj.getID().toString());
        return rest;
    }

    public WorkFlowProcessInwardDetails convert(Context context, WorkFlowProcessInwardDetails obj, WorkFlowProcessInwardDetailsRest rest) throws SQLException {
        if (rest.getInwardNumber() != null) {
            obj.setInwardNumber(rest.getInwardNumber());
        }
        if (rest.getInwardDate() != null) {
            obj.setInwardDate(rest.getInwardDate());
        }
        if (rest.getReceivedDate() != null) {
            obj.setReceivedDate(rest.getReceivedDate());
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

    public WorkFlowProcessInwardDetails convert(Context context,WorkFlowProcessInwardDetailsRest rest) throws SQLException {
        WorkFlowProcessInwardDetails obj=new WorkFlowProcessInwardDetails();
        if (rest.getInwardNumber() != null) {
            obj.setInwardNumber(rest.getInwardNumber());
        }
        if (rest.getInwardDate() != null) {
            obj.setInwardDate(rest.getInwardDate());
        }
        if (rest.getReceivedDate() != null) {
            obj.setReceivedDate(rest.getReceivedDate());
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
