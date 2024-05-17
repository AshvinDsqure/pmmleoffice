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
import org.dspace.content.dao.CityDAO;
import org.dspace.content.service.CityService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class CityServiceImpl extends DSpaceObjectServiceImpl<City> implements CityService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);
    @Autowired(required = true)
    protected CityDAO CityDAO;
    protected CityServiceImpl() {
        super();
    }
    @Override
    public City findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }
    @Override
    public City findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }
    @Override
    public void updateLastModified(Context context, City dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }
    @Override
    public void delete(Context context, City dso) throws SQLException, AuthorizeException, IOException {
        CityDAO.delete(context,dso);
    }
    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public City create(Context context, City City) throws SQLException, AuthorizeException {
        City= CityDAO.create(context,City);
        return City;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return CityDAO.countRows(context);
    }

    @Override
    public List<City> getCityByStateid(Context context, UUID stateid) throws SQLException {
        return CityDAO.getCityByStateid(context,stateid);
    }

    @Override
    public List<City> getCityByStateid(Context context, UUID stateid, String searchcity) throws SQLException {
        return CityDAO.getCityByStateid(context,stateid,searchcity);
    }

    @Override
    public City find(Context context, UUID uuid) throws SQLException {
        return CityDAO.findByID(context,City.class,uuid);
    }
    @Override
    public void update(Context context, City City) throws SQLException, AuthorizeException {
        this.CityDAO.save(context, City);
    }
}