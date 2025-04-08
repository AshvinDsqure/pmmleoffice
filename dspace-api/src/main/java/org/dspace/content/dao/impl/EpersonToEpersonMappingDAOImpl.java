/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.EpersonToEpersonMapping;
import org.dspace.content.dao.EpersonToEpersonMappingDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class EpersonToEpersonMappingDAOImpl extends AbstractHibernateDAO<EpersonToEpersonMapping> implements EpersonToEpersonMappingDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(EpersonToEpersonMappingDAOImpl.class);

    protected EpersonToEpersonMappingDAOImpl() {
        super();
    }

    @Override
    public EpersonToEpersonMapping findByLegacyId(Context context, int legacyId, Class<EpersonToEpersonMapping> clazz) throws SQLException {
        return null;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM EpersonToEpersonMapping"));
    }

    @Override
    public List<EpersonToEpersonMapping> findByEperson(Context context, UUID eperson,Integer offset,Integer limit) throws SQLException {
        try {
            Query query = createQuery(context,
                    "SELECT s FROM EpersonToEpersonMapping s WHERE s.eperson.id = :eperson and s.isdelete=:isdelete");

            query.setParameter("eperson", eperson);
            query.setParameter("isdelete", false);

            if (0 <= offset) {
                query.setFirstResult(offset);
            }
            if (0 <= limit) {
                query.setMaxResults(limit);
            }
            return  query.getResultList();
        }  catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public int countfindByEperson(Context context, UUID eperson) throws SQLException {
       try {
           Query query = createQuery(context,
                   "SELECT count(s) FROM EpersonToEpersonMapping s WHERE s.eperson.id = :eperson and s.isdelete=:isdelete");

           query.setParameter("eperson", eperson);
           query.setParameter("isdelete", false);

           return count(query);
       }catch (Exception e){
           System.out.println("Error :"+e.getMessage());
           return 0;
       }
       }

    @Override
    public EpersonToEpersonMapping findByEpersonAndEpersonMapping(Context context, UUID epersonmapping, UUID eperson) throws SQLException {
        try {
            Query query = createQuery(context,
                    "SELECT s FROM EpersonToEpersonMapping s WHERE s.epersonmapping.id = :epersonmapping AND s.eperson.id = :eperson and s.isdelete=:isdelete");

            query.setParameter("eperson", eperson);
            query.setParameter("epersonmapping", epersonmapping);
            query.setParameter("isdelete", false);

            return (EpersonToEpersonMapping) query.getSingleResult();
        }  catch (Exception e) {
            System.out.println("Error:a " + e.getMessage());
            return null;
        }
    }

    @Override
    public EpersonToEpersonMapping findByEpersonbyEP(Context context, UUID eperson) throws SQLException {
        try {
            Query query = createQuery(context,
                    "SELECT s FROM EpersonToEpersonMapping s WHERE  s.eperson.id = :eperson");

            query.setParameter("eperson", eperson);

            return (EpersonToEpersonMapping) query.getSingleResult();
        }  catch (Exception e) {
            System.out.println("Error:a " + e.getMessage());
            return null;
        }
    }


    @Override
    public List<EpersonToEpersonMapping> findByofficeandDepartmentanddesignation(Context context, UUID office, UUID department, UUID designation) throws SQLException {
        try {
            Query query = createQuery(context,
                    "SELECT DISTINCT  s FROM EpersonToEpersonMapping s left join s.epersonmapping as mapping WHERE mapping.office.id=:office and mapping.department.id=:department and mapping.designation.id=:designation and s.isdelete=:isdelete");
            query.setParameter("office", office);
            query.setParameter("department", department);
            query.setParameter("designation",designation);
            query.setParameter("isdelete", false);
            return  query.getResultList();
        }  catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean existsByEpersonToEpersonMappingId(Context context, UUID epersonmapping) {
        try {
            Query query = createQuery(context,
                    "SELECT  count(s) FROM WorkflowProcessEperson s  WHERE s.epersontoepersonmapping.id=:epersontoepersonmapping");
            query.setParameter("epersontoepersonmapping", epersonmapping);
            return  count(query)>0;
        }  catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

}
