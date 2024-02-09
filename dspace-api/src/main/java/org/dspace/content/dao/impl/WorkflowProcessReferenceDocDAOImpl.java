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
import org.dspace.content.dao.WorkflowProcessDAO;
import org.dspace.content.dao.WorkflowProcessReferenceDocDAO;
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
public class WorkflowProcessReferenceDocDAOImpl extends AbstractHibernateDSODAO<WorkflowProcessReferenceDoc> implements WorkflowProcessReferenceDocDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkflowProcessReferenceDocDAOImpl.class);
    protected WorkflowProcessReferenceDocDAOImpl() {
        super();
    }

    @Override
    public int countDocumentByType(Context context, UUID drafttypeid) throws SQLException {
        Query query = createQuery(context, "SELECT count(d) FROM WorkflowProcessReferenceDoc as d join d.drafttype as df where df.id=:drafttypeid");
        query.setParameter("drafttypeid",drafttypeid);
    return count(query);
    }

    @Override
    public List<WorkflowProcessReferenceDoc> getDocumentByType(Context context, UUID drafttypeid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "SELECT d FROM WorkflowProcessReferenceDoc as d join d.drafttype as df where df.id=:drafttypeid order by d.initdate DESC");
        query.setParameter("drafttypeid",drafttypeid);
        if (0 <= offset) {
            query.setFirstResult(offset);
        }if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public int countDocumentByItemid(Context context, UUID drafttypeid, UUID itemid) throws SQLException {
        Query query = createQuery(context, "SELECT count(d) FROM WorkflowProcessReferenceDoc as d join d.drafttype as df join d.workflowProcess as wp join wp.item as i where df.id in(:drafttypeid) and i.id=:itemid ");
        query.setParameter("drafttypeid",drafttypeid);
        query.setParameter("itemid",itemid);
        return count(query);
    }

    @Override
    public List<WorkflowProcessReferenceDoc> getDocumentByItemid(Context context,  UUID drafttypeid,UUID itemid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "SELECT d FROM WorkflowProcessReferenceDoc as d join d.workflowProcess as wp join wp.item as i where i.id=:itemid ");
        //query.setParameter("drafttypeid",drafttypeid);
        query.setParameter("itemid",itemid);
        if (0 <= offset) {
            query.setFirstResult(offset);
        }if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public List<WorkflowProcessReferenceDoc> getDocumentByworkflowprocessid(Context context, UUID workflowprocessid) throws SQLException {
        Query query = createQuery(context, "SELECT d FROM WorkflowProcessReferenceDoc as d join d.workflowProcess as wp wp.id=:workflowprocessid");
        //query.setParameter("drafttypeid",drafttypeid);
        query.setParameter("workflowprocessid",workflowprocessid);
        return query.getResultList();
    }
    @Override
    public List<WorkflowProcessReferenceDoc> getDocumentBySignitore(Context context, UUID signitoreid,UUID drafttypeuuid) throws SQLException {
        Query query = createQuery(context, "SELECT d FROM WorkflowProcessReferenceDoc as d" +
                " left join d.drafttype as df " +
                " left join d.workflowProcessReferenceDocVersion dv " +
                " left join dv.creator as ds " +
                " where dv.issign=:issignitor " +
                " and ds.id=:signitoreid " +
                " and df.id=:drafttypeid " +
                " and dv.issign=:issignitor");
        query.setParameter("issignitor",true);
        query.setParameter("signitoreid",signitoreid);
        query.setParameter("drafttypeid",drafttypeuuid);
        return query.getResultList();
    }
}
