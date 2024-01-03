/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.State;
import org.dspace.content.dao.StateDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class StateDAOImpl extends AbstractHibernateDAO<State> implements StateDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(StateDAOImpl.class);

    protected StateDAOImpl() {
        super();
    }

    @Override
    public State findByLegacyId(Context context, int legacyId, Class<State> clazz) throws SQLException {
        return null;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM State"));
    }

    @Override
    public List<State> getByCountryId(Context context, UUID countryid) throws SQLException {
        Query query = createQuery(context, "SELECT s FROM State s join s.country as c where c.id=:countryid order by s.statename");
        query.setParameter("countryid",countryid);
        return query.getResultList();
    }

}
