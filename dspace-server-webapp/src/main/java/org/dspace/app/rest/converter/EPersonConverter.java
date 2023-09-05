/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.EPersonRest;
import org.dspace.app.rest.model.GroupRest;
import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.EPersonService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.UUID;

/**
 * This is the converter from/to the EPerson in the DSpace API data model and the
 * REST data model
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@Component
public class EPersonConverter extends DSpaceObjectConverter<EPerson, org.dspace.app.rest.model.EPersonRest> {
    @Autowired
    ModelMapper modelMapper;

    @Autowired
    EPersonService ePersonService;

    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;
    @Override
    public EPersonRest convert(EPerson obj, Projection projection) {
        EPersonRest eperson = super.convert(obj, projection);
        eperson.setLastActive(obj.getLastActive());
        eperson.setNetid(obj.getNetid());
        eperson.setCanLogIn(obj.canLogIn());
        eperson.setRequireCertificate(obj.getRequireCertificate());
        eperson.setSelfRegistered(obj.getSelfRegistered());
        eperson.setEmail(obj.getEmail());
        if(obj.getTablenumber()!=null){
        eperson.setTablenumber(obj.getTablenumber());
        }
        if(obj.getEmployeeid()!=null) {
            eperson.setEmployeeid(obj.getEmployeeid());
        }
        if(obj.getDepartment()!=null && obj.getDepartment().getID()!=null){
        eperson.setDepartmentRest(workFlowProcessMasterValueConverter.convert(obj.getDepartment(),projection));
        }
        if(obj.getOffice()!=null && obj.getOffice().getID()!=null){
            eperson.setOfficeRest(workFlowProcessMasterValueConverter.convert(obj.getOffice(),projection));
        }
        if(obj.getDesignation()!=null){
            eperson.setDesignationRest(workFlowProcessMasterValueConverter.convert(obj.getDesignation(),projection));
        }
        if(obj.getFullName()!=null){
            if(obj.getDesignation()!=null && obj.getDesignation().getPrimaryvalue()!=null) {
                eperson.setFullname(obj.getFullName() + " (" + obj.getDesignation().getPrimaryvalue() + ").");
            }else{
                eperson.setFullname(obj.getFullName()+".");
            }
        }
        return eperson;
    }
    public EPersonRest convertBYUSer(EPerson obj, Projection projection) {
        EPersonRest eperson = new EPersonRest();
        eperson.setLastActive(obj.getLastActive());
        eperson.setNetid(obj.getNetid());
        eperson.setCanLogIn(obj.canLogIn());
        eperson.setRequireCertificate(obj.getRequireCertificate());
        eperson.setSelfRegistered(obj.getSelfRegistered());
        eperson.setEmail(obj.getEmail());
        eperson.setUuid(obj.getID().toString());
        if(obj.getTablenumber()!=null){
            eperson.setTablenumber(obj.getTablenumber());
        }
        if(obj.getEmployeeid()!=null) {
            eperson.setEmployeeid(obj.getEmployeeid());
        }
        if(obj.getDepartment()!=null && obj.getDepartment().getID()!=null){
            eperson.setDepartmentRest(workFlowProcessMasterValueConverter.convert(obj.getDepartment(),projection));
        }
        if(obj.getOffice()!=null && obj.getOffice().getID()!=null){
            eperson.setOfficeRest(workFlowProcessMasterValueConverter.convert(obj.getOffice(),projection));
        }
        if(obj.getDesignation()!=null){
            eperson.setDesignationRest(workFlowProcessMasterValueConverter.convert(obj.getDesignation(),projection));
        }
        if(obj.getFullName()!=null){
            if(obj.getDesignation()!=null && obj.getDesignation().getPrimaryvalue()!=null) {
                eperson.setFullname(obj.getFullName() + " (" + obj.getDesignation().getPrimaryvalue() + ").");
            }else{
                eperson.setFullname(obj.getFullName()+".");
            }
        }
        return eperson;
    }

    public EPerson convert(Context context, EPersonRest rest) throws SQLException {
        if(rest!=null && rest.getId()!=null){
            return ePersonService.find(context, UUID.fromString(rest.getId()));
        }
        return null;
    }
    public EPerson convert(EPersonRest obj) {
        return modelMapper.map(obj,EPerson.class);
    }
    @Override
    protected EPersonRest newInstance() {
        return new EPersonRest();
    }
    @Override
    public Class<EPerson> getModelClass() {
        return EPerson.class;
    }

}
