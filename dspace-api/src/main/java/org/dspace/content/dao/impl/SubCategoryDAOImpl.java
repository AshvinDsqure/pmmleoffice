/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.SubCategory;
import org.dspace.content.dao.SubCategoryDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class SubCategoryDAOImpl extends AbstractHibernateDAO<SubCategory> implements SubCategoryDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(SubCategoryDAOImpl.class);

    protected SubCategoryDAOImpl() {
        super();
    }

    @Override
    public SubCategory findByLegacyId(Context context, int legacyId, Class<SubCategory> clazz) throws SQLException {
        return null;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM SubCategory"));
    }

    @Override
    public List<SubCategory> getByCountryId(Context context, UUID vipid) throws SQLException {
        Query query = createQuery(context, "SELECT s FROM SubCategory s join s.category as c where c.id=:vipid order by s.subcategoryname");
        query.setParameter("vipid",vipid);
        return query.getResultList();
    }

}
