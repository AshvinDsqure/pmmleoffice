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
import org.dspace.content.dao.EpersonToEpersonMappingDAO;
import org.dspace.content.service.EpersonToEpersonMappingService;
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

public class EpersonToEpersonMappingServiceImpl extends DSpaceObjectServiceImpl<EpersonToEpersonMapping> implements EpersonToEpersonMappingService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);
    @Autowired(required = true)
    protected EpersonToEpersonMappingDAO EpersonToEpersonMappingDAO;
    protected EpersonToEpersonMappingServiceImpl() {
        super();
    }
    @Override
    public EpersonToEpersonMapping findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }
    @Override
    public EpersonToEpersonMapping findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }
    @Override
    public void updateLastModified(Context context, EpersonToEpersonMapping dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }
    @Override
    public void delete(Context context, EpersonToEpersonMapping dso) throws SQLException, AuthorizeException, IOException {
        EpersonToEpersonMappingDAO.delete(context,dso);
    }
    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }


    @Override
    public EpersonToEpersonMapping create(Context context, EpersonToEpersonMapping EpersonToEpersonMapping) throws SQLException, AuthorizeException {
        EpersonToEpersonMapping= EpersonToEpersonMappingDAO.create(context,EpersonToEpersonMapping);
        return EpersonToEpersonMapping;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return EpersonToEpersonMappingDAO.countRows(context);
    }

    @Override
    public EpersonToEpersonMapping findByEpersonAndEpersonMapping(Context context, UUID epersonmapping, UUID eperson) throws SQLException {
        return EpersonToEpersonMappingDAO.findByEpersonAndEpersonMapping(context,epersonmapping,eperson);
    }

    @Override
    public EpersonToEpersonMapping findByEpersonbyEP(Context context, UUID eperson) throws SQLException {
        return EpersonToEpersonMappingDAO.findByEpersonbyEP(context,eperson);
    }

    @Override
    public List<EpersonToEpersonMapping> findByEperson(Context context, UUID eperson,Integer offset,Integer limit) throws SQLException {
        return EpersonToEpersonMappingDAO.findByEperson(context,eperson,offset,limit);
    }

    @Override
    public int countfindByEperson(Context context, UUID eperson) throws SQLException {
        return EpersonToEpersonMappingDAO.countfindByEperson(context,eperson);
    }

    @Override
    public List<EpersonToEpersonMapping> findByofficeandDepartmentanddesignation(Context context, UUID office, UUID department, UUID designation) throws SQLException {
        return EpersonToEpersonMappingDAO.findByofficeandDepartmentanddesignation(context,office,department,designation);
    }


    @Override
    public EpersonToEpersonMapping find(Context context, UUID uuid) throws SQLException {
        return EpersonToEpersonMappingDAO.findByID(context,EpersonToEpersonMapping.class,uuid);
    }
    @Override
    public void update(Context context, EpersonToEpersonMapping EpersonToEpersonMapping) throws SQLException, AuthorizeException {
        this.EpersonToEpersonMappingDAO.save(context, EpersonToEpersonMapping);
    }

    @Override
    public List<EpersonToEpersonMapping> findAll(Context context) throws SQLException {
        return Optional.ofNullable(EpersonToEpersonMappingDAO.findAll(context,EpersonToEpersonMapping.class)).orElse(new ArrayList<>());
    }

    @Override
    public List<EpersonToEpersonMapping> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return  Optional.ofNullable(EpersonToEpersonMappingDAO.findAll(context,EpersonToEpersonMapping.class,limit,
                offset)).orElse(new ArrayList<>());
    }

    @Override
    public boolean existsByEpersonToEpersonMappingId(Context context, UUID epersonmapping) {
        return EpersonToEpersonMappingDAO.existsByEpersonToEpersonMappingId(context,epersonmapping);
    }
}