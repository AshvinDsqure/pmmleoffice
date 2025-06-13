/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.EpersonToEpersonMappingRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.EpersonToEpersonMapping;
import org.dspace.content.service.EpersonToEpersonMappingService;
import org.dspace.core.Context;
import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.UUID;

@Component
public class EpersonToEpersonMappingConverter extends DSpaceObjectConverter<EpersonToEpersonMapping, EpersonToEpersonMappingRest> {
    @Autowired
    EpersonToEpersonMappingService EpersonToEpersonMappingService;

    @Autowired
    EPersonConverter ePersonConverter;
    @Autowired
    EpersonMappingConverter epersonMappingConverter;
    @Autowired
    WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;

    @Override
    public Class<EpersonToEpersonMapping> getModelClass() {
        return EpersonToEpersonMapping.class;
    }

    @Override
    protected EpersonToEpersonMappingRest newInstance() {
        return new EpersonToEpersonMappingRest();
    }

    @Override
    public EpersonToEpersonMappingRest convert(EpersonToEpersonMapping obj, Projection projection) {
        EpersonToEpersonMappingRest rest = new EpersonToEpersonMappingRest();
        try {
            if(obj.getEperson()!=null){
                try {
                    rest.setEpersonRest(ePersonConverter.convert(obj.getEperson(), projection));
                } catch (ObjectNotFoundException ex) {
                    // Handle gracefully, maybe log and ignore
                    ex.printStackTrace();
                }
            }
            if(obj.getEpersonmapping()!=null){
               try {
                   rest.setEpersonMappingRest(epersonMappingConverter.convert(obj.getEpersonmapping(),projection));
               } catch (ObjectNotFoundException ex) {
                    // Handle gracefully, maybe log and ignore
                    ex.printStackTrace();
                }
            }
            rest.setIsactive(obj.getIsactive());
            if(obj.getID()!=null){
                rest.setUuid(obj.getID().toString());
            }
            rest.setIsdelete(obj.getIsdelete());
            return rest;
        } catch (Exception e) {
            System.out.println("error");
            e.printStackTrace();
        }
        return null;
    }

    public EpersonToEpersonMapping convertbycreatenew(Context context,EpersonToEpersonMappingRest rest) throws SQLException {
        EpersonToEpersonMapping obj = new EpersonToEpersonMapping();
        if(rest.getEpersonRest()!=null){
            obj.setEperson(ePersonConverter.convert(context,rest.getEpersonRest()));
        }
        if(rest.getEpersonMappingRest()!=null){
            obj.setEpersonmapping(epersonMappingConverter.convertbyService(context,rest.getEpersonMappingRest()));
        }
        obj.setIsdelete(rest.getIsdelete());
        obj.setIsactive(rest.getIsactive());
        return obj;
    }

    public EpersonToEpersonMapping convertbyService(Context context, EpersonToEpersonMappingRest rest) throws SQLException {
        System.out.println("id: ::::::::::"+rest.getId());
        if (rest != null && rest.getId() != null && !rest.getId().isEmpty()) {
            return EpersonToEpersonMappingService.find(context, UUID.fromString(rest.getId()));
        }
        return null;
    }
}
