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
import org.dspace.content.dao.CountryDAO;
import org.dspace.content.service.CountryService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class CountryServiceImpl extends DSpaceObjectServiceImpl<Country> implements CountryService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);
    @Autowired(required = true)
    protected CountryDAO CountryDAO;
    protected CountryServiceImpl() {
        super();
    }
    @Override
    public Country findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }
    @Override
    public Country findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }
    @Override
    public void updateLastModified(Context context, Country dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }
    @Override
    public void delete(Context context, Country dso) throws SQLException, AuthorizeException, IOException {
        CountryDAO.delete(context,dso);
    }
    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }


    @Override
    public Country create(Context context, Country Country) throws SQLException, AuthorizeException {
        Country= CountryDAO.create(context,Country);
        return Country;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return CountryDAO.countRows(context);
    }

    @Override
    public List<Country> getAll(Context context) throws SQLException {
        return CountryDAO.getAll(context);
    }


    @Override
    public Country find(Context context, UUID uuid) throws SQLException {
        return CountryDAO.findByID(context,Country.class,uuid);
    }
    @Override
    public void update(Context context, Country Country) throws SQLException, AuthorizeException {
        this.CountryDAO.save(context, Country);
    }
}