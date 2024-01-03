/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.Country;
import org.dspace.content.dao.CountryDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;

public class CountryDAOImpl extends AbstractHibernateDAO<Country> implements CountryDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(CountryDAOImpl.class);

    protected CountryDAOImpl() {
        super();
    }

    @Override
    public Country findByLegacyId(Context context, int legacyId, Class<Country> clazz) throws SQLException {
        return null;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM Country"));
    }

    @Override
    public List<Country> getAll(Context c) throws SQLException {
        Query query = createQuery(c, "SELECT c FROM Country c");
        return query.getResultList();

    }

}
