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
import org.dspace.content.dao.CategoryDAO;
import org.dspace.content.service.CategoryService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class CategoryServiceImpl extends DSpaceObjectServiceImpl<Category> implements CategoryService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);
    @Autowired(required = true)
    protected CategoryDAO CategoryDAO;
    protected CategoryServiceImpl() {
        super();
    }
    @Override
    public Category findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }
    @Override
    public Category findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }
    @Override
    public void updateLastModified(Context context, Category dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }
    @Override
    public void delete(Context context, Category dso) throws SQLException, AuthorizeException, IOException {
        CategoryDAO.delete(context,dso);
    }
    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }


    @Override
    public Category create(Context context, Category Category) throws SQLException, AuthorizeException {
        Category= CategoryDAO.create(context,Category);
        return Category;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return CategoryDAO.countRows(context);
    }

    @Override
    public List<Category> getAll(Context context) throws SQLException {
        return CategoryDAO.getAll(context);
    }


    @Override
    public Category find(Context context, UUID uuid) throws SQLException {
        return CategoryDAO.findByID(context,Category.class,uuid);
    }
    @Override
    public void update(Context context, Category Category) throws SQLException, AuthorizeException {
        this.CategoryDAO.save(context, Category);
    }
}