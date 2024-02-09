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
import org.dspace.content.dao.VipNameDAO;
import org.dspace.content.service.VipNameService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class VipNameServiceImpl extends DSpaceObjectServiceImpl<VipName> implements VipNameService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);
    @Autowired(required = true)
    protected VipNameDAO VipNameDAO;
    protected VipNameServiceImpl() {
        super();
    }
    @Override
    public VipName findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }
    @Override
    public VipName findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }
    @Override
    public void updateLastModified(Context context, VipName dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }
    @Override
    public void delete(Context context, VipName dso) throws SQLException, AuthorizeException, IOException {
        VipNameDAO.delete(context,dso);
    }
    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }


    @Override
    public VipName create(Context context, VipName VipName) throws SQLException, AuthorizeException {
        VipName= VipNameDAO.create(context,VipName);
        return VipName;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return VipNameDAO.countRows(context);
    }

    @Override
    public List<VipName> getByCountryId(Context context, UUID countryid) throws SQLException {
        return VipNameDAO.getByCountryId(context,countryid);
    }


    @Override
    public VipName find(Context context, UUID uuid) throws SQLException {
        return VipNameDAO.findByID(context,VipName.class,uuid);
    }
    @Override
    public void update(Context context, VipName VipName) throws SQLException, AuthorizeException {
        this.VipNameDAO.save(context, VipName);
    }
}