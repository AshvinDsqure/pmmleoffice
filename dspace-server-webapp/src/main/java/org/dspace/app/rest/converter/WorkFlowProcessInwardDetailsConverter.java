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
        if (obj.getFilereferencenumber() != null) {
            rest.setFilereferencenumber(obj.getFilereferencenumber());
        }
        if (obj.getInwardDate() != null) {
            rest.setInwardDate(obj.getInwardDate());
        }
        if (obj.getReceivedDate() != null) {
            rest.setReceivedDate(obj.getReceivedDate());
        }
        if (obj.getLatterDate() != null) {
            rest.setLatterDate(obj.getLatterDate());
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
        if (obj.getInwardmode() != null) {
            rest.setInwardmodeRest(workFlowProcessMasterValueConverter.convert(obj.getInwardmode(), projection));
        } if (obj.getVip() != null) {
            rest.setVipRest(workFlowProcessMasterValueConverter.convert(obj.getVip(), projection));
        } if (obj.getVipname() != null) {
            rest.setVipnameRest(workFlowProcessMasterValueConverter.convert(obj.getVipname(), projection));
        }if (obj.getLanguage() != null) {
            rest.setLanguageRest(workFlowProcessMasterValueConverter.convert(obj.getLanguage(), projection));
        }
        rest.setUuid(obj.getID().toString());
        return rest;
    }

    public WorkFlowProcessInwardDetails convert(Context context, WorkFlowProcessInwardDetails obj, WorkFlowProcessInwardDetailsRest rest) throws SQLException {
        if (rest.getInwardNumber() != null) {
            obj.setInwardNumber(rest.getInwardNumber());
        }
        if (rest.getFilereferencenumber() != null) {
            obj.setFilereferencenumber(rest.getFilereferencenumber());
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
        if (rest.getInwardmodeRest() != null) {
            obj.setInwardmode(workFlowProcessMasterValueConverter.convert(context, rest.getInwardmodeRest()));
        }
        if (rest.getVipRest() != null) {
            obj.setVip(workFlowProcessMasterValueConverter.convert(context, rest.getVipRest()));
        }  if (rest.getVipnameRest() != null) {
            obj.setVipname(workFlowProcessMasterValueConverter.convert(context, rest.getVipnameRest()));
        }
        if (rest.getLanguageRest() != null) {
            obj.setLanguage(workFlowProcessMasterValueConverter.convert(context, rest.getLanguageRest()));
        }
        if (rest.getLatterDate() != null) {
            obj.setLatterDate(rest.getLatterDate());
        }
        return obj;
    }

    public WorkFlowProcessInwardDetails convert(Context context,WorkFlowProcessInwardDetailsRest rest) throws SQLException {
        WorkFlowProcessInwardDetails obj=new WorkFlowProcessInwardDetails();
        if (rest.getInwardNumber() != null) {
            obj.setInwardNumber(rest.getInwardNumber());
        }
        if (rest.getFilereferencenumber() != null) {
            obj.setFilereferencenumber(rest.getFilereferencenumber());
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
        if (rest.getInwardmodeRest() != null) {
            obj.setInwardmode(workFlowProcessMasterValueConverter.convert(context, rest.getInwardmodeRest()));
        }
        if (rest.getVipRest() != null) {
            obj.setVip(workFlowProcessMasterValueConverter.convert(context, rest.getVipRest()));
        }  if (rest.getVipnameRest() != null) {
            obj.setVipname(workFlowProcessMasterValueConverter.convert(context, rest.getVipnameRest()));
        }
        if (rest.getLanguageRest() != null) {
            obj.setLanguage(workFlowProcessMasterValueConverter.convert(context, rest.getLanguageRest()));
        }
        if (rest.getLatterDate() != null) {
            obj.setLatterDate(rest.getLatterDate());
        }
        return obj;
    }

}
