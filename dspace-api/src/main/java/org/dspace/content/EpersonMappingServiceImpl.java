/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content;

import org.apache.logging.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.dao.EpersonMappingDAO;
import org.dspace.content.service.EpersonMappingService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EpersonMappingServiceImpl extends DSpaceObjectServiceImpl<EpersonMapping> implements EpersonMappingService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);
    @Autowired(required = true)
    protected EpersonMappingDAO EpersonMappingDAO;
    protected EpersonMappingServiceImpl() {
        super();
    }
    @Override
    public EpersonMapping findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }
    @Override
    public EpersonMapping findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }
    @Override
    public void updateLastModified(Context context, EpersonMapping dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }
    @Override
    public void delete(Context context, EpersonMapping dso) throws SQLException, AuthorizeException, IOException {
        EpersonMappingDAO.delete(context,dso);
    }
    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }


    @Override
    public EpersonMapping create(Context context, EpersonMapping EpersonMapping) throws SQLException, AuthorizeException {
        EpersonMapping= EpersonMappingDAO.create(context,EpersonMapping);
        return EpersonMapping;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return EpersonMappingDAO.countRows(context);
    }

    @Override
    public EpersonMapping findByOfficeAndDepartmentAndDesignation(Context context, UUID office, UUID department, UUID designation) throws SQLException {
        return EpersonMappingDAO.findByOfficeAndDepartmentAndDesignation(context,office,department,designation);
    }

    @Override
    public EpersonMapping findByOfficeAndDepartmentAndDesignationTableNo(Context context, UUID office, UUID department, UUID designation, Integer tbl) throws SQLException {
        return EpersonMappingDAO.findByOfficeAndDepartmentAndDesignationTableNo(context,office,department,designation,tbl);
    }

    @Override
    public List<EpersonMapping> getByCountryId(Context context, UUID countryid) throws SQLException {
        return EpersonMappingDAO.getByCountryId(context,countryid);
    }


    @Override
    public EpersonMapping find(Context context, UUID uuid) throws SQLException {
        return EpersonMappingDAO.findByID(context,EpersonMapping.class,uuid);
    }
    @Override
    public void update(Context context, EpersonMapping EpersonMapping) throws SQLException, AuthorizeException {
        this.EpersonMappingDAO.save(context, EpersonMapping);
    }

    @Override
    public List<EpersonMapping> findAll(Context context) throws SQLException {
        return Optional.ofNullable(EpersonMappingDAO.findAll(context,EpersonMapping.class)).orElse(new ArrayList<>());
    }

    @Override
    public List<EpersonMapping> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return  Optional.ofNullable(EpersonMappingDAO.findAll(context,EpersonMapping.class,limit,
                offset)).orElse(new ArrayList<>());
    }

    @Override
    public List<EpersonMapping> findByOffice(Context context, UUID office) throws SQLException {
        return EpersonMappingDAO.findByOffice(context,office);
    }

    @Override
    public List<EpersonMapping> findOfficeAndDepartment(Context context, UUID office, UUID department) throws SQLException {
        return EpersonMappingDAO.findOfficeAndDepartment(context,office,department);
    }

    @Override
    public List<EpersonMapping> getByOfficeAndDepartmentAndDesignation(Context context, UUID office, UUID department, UUID designation) throws SQLException {
        return EpersonMappingDAO.getByOfficeAndDepartmentAndDesignation(context,office,department,designation);
    }
}