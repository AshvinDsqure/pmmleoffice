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
import org.dspace.content.WorkflowProcessTemplate;
import org.dspace.content.WorkflowProcessTemplate;
import org.dspace.content.WorkflowProcessReferenceDoc;
import org.dspace.content.dao.WorkflowProcessDAO;
import org.dspace.content.dao.WorkflowProcessTemplateDAO;
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
public class WorkflowProcessTemplateDAOImpl extends AbstractHibernateDSODAO<WorkflowProcessTemplate> implements WorkflowProcessTemplateDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkflowProcessTemplateDAOImpl.class);
    protected WorkflowProcessTemplateDAOImpl() {
        super();
    }


    @Override
    public List<WorkflowProcessTemplate> getWorkflowProcessByTemplate(Context context, UUID template, Integer offset, Integer limit) throws SQLException {
        try {
            Query query = createQuery(context, "SELECT t from  WorkflowProcessTemplate as t join t.template as temp where temp.id=:temp and t.isdelete=:isdelete ORDER BY t.index");
            query.setParameter("temp",template);
            query.setParameter("isdelete",false);
            return query.getResultList();
        }catch (Exception e){
            System.out.println("in error " + e.getMessage());
            return null;
        }
    }

    @Override
    public int getCountWorkflowProcessByTemplate(Context context, UUID template) throws SQLException {
        try {
            Query query = createQuery(context, "SELECT count(t) from  WorkflowProcessTemplate as t join t.template as temp where temp.id=:temp");
            query.setParameter("temp",template);
            return count(query);
        }catch (Exception e){
            System.out.println("in error " + e.getMessage());
            return 0;
        }
    }
}
