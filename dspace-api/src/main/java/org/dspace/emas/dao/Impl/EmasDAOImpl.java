 /**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
 package org.dspace.emas.dao.Impl;

 import org.apache.logging.log4j.Logger;
 import org.dspace.core.AbstractHibernateDAO;
 import org.dspace.core.Context;
 import org.dspace.emas.Emas;
 import org.dspace.emas.dao.EmasDAO;

 import javax.persistence.Query;
 import java.sql.SQLException;
 import java.util.UUID;

public class EmasDAOImpl extends AbstractHibernateDAO<Emas> implements EmasDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(EmasDAOImpl.class);

    protected EmasDAOImpl() {
        super();
    }

    @Override
    public Emas findByLegacyId(Context context, int legacyId, Class<Emas> clazz) throws SQLException {
        return null;
    }
    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM Emas"));
    }
    @Override
    public int getEmasByEperson(Context context, UUID eperson) throws SQLException {
       try {
           Query query = createQuery(context, "SELECT count(*) FROM Emas e where e.eperson.id=:eperson");
           query.setParameter("eperson", eperson);
           return count(query);
       }catch (Exception e){
           System.out.println("in error getEmasByEperson" + e.getMessage());
           return 0;
       }
    }
    @Override
    public int getEmasByEpersonANDKey(Context context, UUID eperson, String key) throws SQLException {
        try {
            Query query = createQuery(context, "SELECT count(*) FROM Emas e where e.eperson.id=:eperson AND e.key=:key");
            query.setParameter("eperson", eperson);
            query.setParameter("key", key);
            return count(query);
        }catch (Exception e){
            System.out.println("in error getEmasByEpersonANDKey" + e.getMessage());
            return 0;
        }
    }
    @Override
    public int getEmasByKey(Context context, String key) throws SQLException {
        try {
            Query query = createQuery(context, "SELECT count(*) FROM Emas e where e.key=:key");
            query.setParameter("key", key);
            return count(query);
        }catch (Exception e){
            System.out.println("in error getEmasByKey" + e.getMessage());
            return 0;
        }
    }

}
