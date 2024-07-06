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
import org.dspace.content.WorkflowProcessReferenceDoc;
import org.dspace.content.WorkflowProcessReferenceDocVersion;
import org.dspace.content.dao.WorkflowProcessDAO;
import org.dspace.content.dao.WorkflowProcessReferenceDocDAO;
import org.dspace.content.dao.WorkflowProcessReferenceDocVersionDAO;
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
public class WorkflowProcessReferenceDocVersionDAOImpl extends AbstractHibernateDSODAO<WorkflowProcessReferenceDocVersion> implements WorkflowProcessReferenceDocVersionDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkflowProcessReferenceDocDAOImpl.class);
    protected WorkflowProcessReferenceDocVersionDAOImpl() {
        super();
    }
    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) from WorkflowProcessReferenceDocVersion"));
    }

    @Override
    public int countDocumentID(Context context, UUID documentid) throws SQLException {
        Query query = createQuery(context, "SELECT count(v) FROM WorkflowProcessReferenceDoc as d join d.workflowProcessReferenceDocVersion as v  where d.id=:docid");
        query.setParameter("docid",documentid);
        return count(query);
    }

    @Override
    public WorkflowProcessReferenceDocVersion findByCreator(Context context, UUID e,UUID documentid) throws SQLException {
        Query query = createQuery(context, "SELECT v FROM WorkflowProcessReferenceDocVersion as v   where  v.creator.id=:e and v.workflowProcessReferenceDoc.id=:docid");
        query.setParameter("e",e);
        query.setParameter("docid",documentid);
        query.setMaxResults(1);
        return (WorkflowProcessReferenceDocVersion) query.getSingleResult();
    }

    @Override
    public List<WorkflowProcessReferenceDocVersion> getDocVersionBydocumentID(Context context, UUID documentid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "SELECT v FROM WorkflowProcessReferenceDocVersion as v  where v.workflowProcessReferenceDoc.id=:docid order by v.versionnumber DESC");
        query.setParameter("docid",documentid);
        if (0 <= offset) {
            query.setFirstResult(offset);
        }if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }
}
