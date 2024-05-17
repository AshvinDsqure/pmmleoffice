/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.VipName;
import org.dspace.content.dao.VipNameDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class VipNameDAOImpl extends AbstractHibernateDAO<VipName> implements VipNameDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(VipNameDAOImpl.class);

    protected VipNameDAOImpl() {
        super();
    }

    @Override
    public VipName findByLegacyId(Context context, int legacyId, Class<VipName> clazz) throws SQLException {
        return null;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM VipName"));
    }

    @Override
    public List<VipName> getByCountryId(Context context, UUID vipid) throws SQLException {
        Query query = createQuery(context, "SELECT s FROM VipName s join s.vip as c where c.id=:vipid order by s.vipname ASC");
        query.setParameter("vipid",vipid);
        return query.getResultList();
    }

}
