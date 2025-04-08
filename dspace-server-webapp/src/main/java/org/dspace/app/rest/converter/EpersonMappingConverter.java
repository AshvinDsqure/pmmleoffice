/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.EpersonMappingRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.EpersonMapping;
import org.dspace.content.service.EpersonMappingService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.UUID;

@Component
public class EpersonMappingConverter extends DSpaceObjectConverter<EpersonMapping, EpersonMappingRest> {
    @Autowired
    EpersonMappingService epersonMappingService;

    @Autowired
    EPersonConverter ePersonConverter;
    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;
    @Autowired
    WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;

    @Override
    public Class<EpersonMapping> getModelClass() {
        return EpersonMapping.class;
    }

    @Override
    protected EpersonMappingRest newInstance() {
        return new EpersonMappingRest();
    }

    @Override
    public EpersonMappingRest convert(EpersonMapping obj, Projection projection) {
        EpersonMappingRest rest = new EpersonMappingRest();
        try {
            if(obj.getOffice()!=null){
                rest.setOfficeRest(workFlowProcessMasterValueConverter.convert(obj.getOffice(),projection));
            }
            if(obj.getDepartment()!=null){
                rest.setDepartmentRest(workFlowProcessMasterValueConverter.convert(obj.getDepartment(),projection));
            }
            if(obj.getDesignation()!=null){
                rest.setDesignationRest(workFlowProcessMasterValueConverter.convert(obj.getDesignation(),projection));
            }
            if(obj.getTablenumber()!=null){
                rest.setTablenumber(obj.getTablenumber());
            }
            if(obj.getID()!=null){
                rest.setUuid(obj.getID().toString());
            }
            return rest;
        } catch (Exception e) {
            System.out.println("error");
            e.printStackTrace();
        }
        return null;
    }

    public EpersonMapping convertbycreatenew(Context context,EpersonMappingRest rest) throws SQLException {
        EpersonMapping obj = new EpersonMapping();
        if(rest.getOfficeRest()!=null){
            obj.setOffice(workFlowProcessMasterValueConverter.convert(context,rest.getOfficeRest()));
        }
        if(rest.getDepartmentRest()!=null){
            obj.setDepartment(workFlowProcessMasterValueConverter.convert(context,rest.getDepartmentRest()));
        }
        if(rest.getDesignationRest()!=null){
            obj.setDesignation(workFlowProcessMasterValueConverter.convert(context,rest.getDesignationRest()));
        }
        if(rest.getTablenumber()!=null){
            obj.setTablenumber(rest.getTablenumber());
        }
        return obj;
    }

    public EpersonMapping convertbyService(Context context, EpersonMappingRest rest) throws SQLException {
        if (rest != null && rest.getId() != null) {
            return epersonMappingService.find(context, UUID.fromString(rest.getId()));
        }
        return null;
    }
}
