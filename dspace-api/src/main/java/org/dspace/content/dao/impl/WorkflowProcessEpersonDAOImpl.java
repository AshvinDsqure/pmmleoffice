/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.WorkflowProcess;
import org.dspace.content.WorkflowProcessDefinition;
import org.dspace.content.WorkflowProcessEperson;
import org.dspace.content.dao.WorkflowProcessDAO;
import org.dspace.content.dao.WorkflowProcessDefinitionDAO;
import org.dspace.content.dao.WorkflowProcessEpersonDAO;
import org.dspace.core.AbstractHibernateDSODAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Hibernate implementation of the Database Access Object interface class for the Item object.
 * This class is responsible for all database calls for the Item object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class WorkflowProcessEpersonDAOImpl extends AbstractHibernateDSODAO<WorkflowProcessEperson> implements WorkflowProcessEpersonDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkflowProcessDefinitionDAOImpl.class);
    protected WorkflowProcessEpersonDAOImpl() {
        super();
    }


    @Override
    public List<WorkflowProcessEperson> getALLData(Context context,int limit) throws SQLException {
        try {
            Query query = createQuery(context, "" +
                    "SELECT DISTINCT ep FROM WorkflowProcessEperson as ep where ep.ePerson.id is not null and ep.epersontoepersonmapping is null");
            query.setMaxResults(limit);
            return query.getResultList();
        }catch (Exception e){
            e.printStackTrace();
            return null;

        }
    }

    @Override
    public int getCountByEpersontoepersonmapping(Context context,UUID eperson, UUID epersontoepersonmapping) {
        try {
            Query query = createQuery(context, "" +
                    "SELECT count(ep) FROM WorkflowProcessEperson as ep where ep.epersontoepersonmapping.id=:epersontoepersonmapping and ep.ePerson.id=:eperson");
            query.setParameter("epersontoepersonmapping",epersontoepersonmapping);
            query.setParameter("eperson",eperson);
            return count(query);
        }catch (Exception e){
            e.printStackTrace();
            return 0;

        }
    }

    @Override
    public int getCountByEperson(Context context, UUID eperson) {
        try {
            Query query = createQuery(context, "" +
                    "SELECT count(ep) FROM WorkflowProcessEperson as ep where ep.ePerson.id=:eperson");
            query.setParameter("eperson",eperson);
            return count(query);
        }catch (Exception e){
            e.printStackTrace();
            return 0;

        }
    }

    @Override
    public int updateWorkflowProcessEperson(Context context,UUID epersonfrom, UUID epersontoepersonmappingfrom,UUID epersonto, UUID epersontoepersonmappingto) throws SQLException {

        try {
            String sqlQuery="update workflowprocesseperson set eperson='"+epersonto+"',epersontoepersonmapping='"+epersontoepersonmappingto+"' where eperson='"+epersonfrom+"' and epersontoepersonmapping = '"+epersontoepersonmappingfrom+"';";
            System.out.println("sql:::"+sqlQuery);
            createSQLQuery(context, sqlQuery).executeUpdate();
            return 1;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }
}
