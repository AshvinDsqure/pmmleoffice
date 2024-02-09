/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.Vip;
import org.dspace.content.dao.VipDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;

public class VipDAOImpl extends AbstractHibernateDAO<Vip> implements VipDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(VipDAOImpl.class);

    protected VipDAOImpl() {
        super();
    }

    @Override
    public Vip findByLegacyId(Context context, int legacyId, Class<Vip> clazz) throws SQLException {
        return null;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM Vip"));
    }

    @Override
    public List<Vip> getAll(Context c) throws SQLException {
        Query query = createQuery(c, "SELECT c FROM Vip c");
        return query.getResultList();

    }

}
