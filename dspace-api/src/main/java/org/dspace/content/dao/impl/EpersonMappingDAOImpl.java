/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.EpersonMapping;
import org.dspace.content.dao.EpersonMappingDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class EpersonMappingDAOImpl extends AbstractHibernateDAO<EpersonMapping> implements EpersonMappingDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(EpersonMappingDAOImpl.class);

    protected EpersonMappingDAOImpl() {
        super();
    }

    @Override
    public EpersonMapping findByLegacyId(Context context, int legacyId, Class<EpersonMapping> clazz) throws SQLException {
        return null;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM EpersonMapping"));
    }

    @Override
    public List<EpersonMapping> getByCountryId(Context context, UUID vipid) throws SQLException {
        Query query = createQuery(context, "SELECT s FROM EpersonMapping s join s.vip as c where c.id=:vipid order by s.EpersonMapping ASC");
        query.setParameter("vipid",vipid);
        return query.getResultList();
    }

    @Override
    public EpersonMapping findByOfficeAndDepartmentAndDesignation(Context context, UUID office, UUID department, UUID designation) throws SQLException {
       try {
           Query query = createQuery(context, "SELECT em FROM EpersonMapping em  where em.office.id=:office and em.department.id=:department and em.designation.id=:designation");
           query.setParameter("office", office);
           query.setParameter("department", department);
           query.setParameter("designation", designation);
           return (EpersonMapping) query.getSingleResult();
       }catch (Exception e){
           System.out.println("error :findByOfficeAndDepartmentAndDesignation "+e.getMessage());
           return null;
       }
    }

    @Override
    public EpersonMapping findByOfficeAndDepartmentAndDesignationTableNo(Context context, UUID office, UUID department, UUID designation, Integer tbl) throws SQLException {
        try {
            Query query = createQuery(context, "SELECT em FROM EpersonMapping em  where em.office.id=:office and em.department.id=:department and em.designation.id=:designation and em.tablenumber=:tbl");
            query.setParameter("office", office);
            query.setParameter("department", department);
            query.setParameter("designation", designation);
            query.setParameter("tbl", tbl);
            return (EpersonMapping) query.getSingleResult();
        }catch (Exception e){
            System.out.println("error :findByOfficeAndDepartmentAndDesignation "+e.getMessage());
            return null;
        }
    }

    @Override
    public List<EpersonMapping> findByOffice(Context context, UUID office) throws SQLException {
        try {
            Query query = createQuery(context, "SELECT DISTINCT em FROM EpersonMapping em  where em.office.id=:office");
            query.setParameter("office", office);
            return query.getResultList();
        }catch (Exception e){
            System.out.println("error :findByOfficeAndDepartmentAndDesignation "+e.getMessage());
            return null;
        }
    }

    @Override
    public List<EpersonMapping> findOfficeAndDepartment(Context context, UUID office, UUID department) throws SQLException {
        try {
            Query query = createQuery(context, "SELECT DISTINCT em FROM EpersonMapping em  where em.office.id=:office and em.department.id=:department");
            query.setParameter("office", office);
            query.setParameter("department", department);
            return query.getResultList();
        }catch (Exception e){
            System.out.println("error :findByOfficeAndDepartmentAndDesignation "+e.getMessage());
            return null;
        }
    }

    @Override
    public List<EpersonMapping> getByOfficeAndDepartmentAndDesignation(Context context, UUID office, UUID department, UUID designation) throws SQLException {
        try {
            Query query = createQuery(context, "SELECT DISTINCT em FROM EpersonMapping em  where em.office.id=:office and em.department.id=:department and em.designation.id=:designation");
            query.setParameter("office", office);
            query.setParameter("department", department);
            query.setParameter("designation", department);
            return query.getResultList();
        }catch (Exception e){
            System.out.println("error :findByOfficeAndDepartmentAndDesignation "+e.getMessage());
            return null;
        }
    }

}
