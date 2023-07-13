/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.LoginCounter;
import org.dspace.content.dao.LoginCounterDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LoginCounterDAOImpl extends AbstractHibernateDAO<LoginCounter> implements LoginCounterDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(LoginCounterDAOImpl.class);
    protected LoginCounterDAOImpl() {
        super();
    }
    @Override
    public LoginCounter findByLegacyId(Context context, int legacyId, Class<LoginCounter> clazz) throws SQLException {
        return null;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM LoginCounter"));
    }

    @Override
    public LoginCounter getbyToken(Context context, String token) throws SQLException {
        Query query = createQuery(context, "SELECT * FROM LoginCounter as lc where lc.sesionid=:token");
        query.setParameter("token",token);
        return (LoginCounter) query.getSingleResult();
    }

    @Override
    public List<Object[]> filter(Context context) throws SQLException {
        Query query = createQuery(context, "SELECT lc.month,lc.year,count(*) as count FROM LoginCounter as lc group by lc.month,lc.year order by lc.month desc,lc.year desc");
        return (List<Object[]>) query.getResultList();
    }
}
