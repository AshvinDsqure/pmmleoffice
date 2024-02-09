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
import org.dspace.content.dao.SubCategoryDAO;
import org.dspace.content.service.SubCategoryService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class SubCategoryServiceImpl extends DSpaceObjectServiceImpl<SubCategory> implements SubCategoryService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);
    @Autowired(required = true)
    protected SubCategoryDAO SubCategoryDAO;
    protected SubCategoryServiceImpl() {
        super();
    }
    @Override
    public SubCategory findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }
    @Override
    public SubCategory findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }
    @Override
    public void updateLastModified(Context context, SubCategory dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }
    @Override
    public void delete(Context context, SubCategory dso) throws SQLException, AuthorizeException, IOException {
        SubCategoryDAO.delete(context,dso);
    }
    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }


    @Override
    public SubCategory create(Context context, SubCategory SubCategory) throws SQLException, AuthorizeException {
        SubCategory= SubCategoryDAO.create(context,SubCategory);
        return SubCategory;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return SubCategoryDAO.countRows(context);
    }

    @Override
    public List<SubCategory> getByCountryId(Context context, UUID countryid) throws SQLException {
        return SubCategoryDAO.getByCountryId(context,countryid);
    }


    @Override
    public SubCategory find(Context context, UUID uuid) throws SQLException {
        return SubCategoryDAO.findByID(context,SubCategory.class,uuid);
    }
    @Override
    public void update(Context context, SubCategory SubCategory) throws SQLException, AuthorizeException {
        this.SubCategoryDAO.save(context, SubCategory);
    }
}