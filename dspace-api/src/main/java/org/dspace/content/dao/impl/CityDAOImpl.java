/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.City;
import org.dspace.content.State;
import org.dspace.content.dao.CityDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class CityDAOImpl extends AbstractHibernateDAO<City> implements CityDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(CityDAOImpl.class);

    protected CityDAOImpl() {
        super();
    }

    @Override
    public City findByLegacyId(Context context, int legacyId, Class<City> clazz) throws SQLException {
        return null;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM City"));
    }

    @Override
    public List<City> getCityByStateid(Context context, UUID stateid) throws SQLException {
        Query query = createQuery(context, "SELECT c FROM City c join c.state as s where s.id=:stateid order by c.cityname ASC");
        query.setParameter("stateid",stateid);
        return query.getResultList();
    }

    @Override
    public List<City> getCityByStateid(Context context, UUID stateid, String searchcity) throws SQLException {
        try {
            Query query = createQuery(context, "SELECT c from  City as c join c.state as s where s.id=:stateid and lower(c.cityname) LIKE :search");
            query.setParameter("stateid",stateid);
            query.setParameter("search", "%"+searchcity.toLowerCase()+"%");
            return query.getResultList();
        }catch (Exception e){
            System.out.println("in error " + e.getMessage());
            return null;
        }
    }

}
