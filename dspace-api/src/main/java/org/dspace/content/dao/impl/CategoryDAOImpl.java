/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.Category;
import org.dspace.content.dao.CategoryDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;

public class CategoryDAOImpl extends AbstractHibernateDAO<Category> implements CategoryDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(CategoryDAOImpl.class);

    protected CategoryDAOImpl() {
        super();
    }

    @Override
    public Category findByLegacyId(Context context, int legacyId, Class<Category> clazz) throws SQLException {
        return null;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM Category"));
    }

    @Override
    public List<Category> getAll(Context c) throws SQLException {
        Query query = createQuery(c, "SELECT c FROM Category c ORDER BY c.categoryname ASC");
        return query.getResultList();

    }

}
