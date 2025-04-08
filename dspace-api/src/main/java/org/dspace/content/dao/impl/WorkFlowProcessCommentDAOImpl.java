/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.WorkFlowProcessComment;
import org.dspace.content.WorkFlowProcessComment;
import org.dspace.content.WorkflowProcessSenderDiary;
import org.dspace.content.dao.WorkFlowProcessCommentDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class WorkFlowProcessCommentDAOImpl extends AbstractHibernateDAO<WorkFlowProcessComment> implements WorkFlowProcessCommentDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkFlowProcessCommentDAOImpl.class);
    protected WorkFlowProcessCommentDAOImpl() {
        super();
    }
    @Override
    public WorkFlowProcessComment findByLegacyId(Context context, int legacyId, Class<WorkFlowProcessComment> clazz) throws SQLException {
        return null;
    }
    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM WorkFlowProcessComment"));
    }
    @Override
    public List<WorkFlowProcessComment> getComments(Context context, UUID workflowprocessid) throws SQLException {
        Query query = createQuery(context, "SELECT c FROM WorkFlowProcessComment as c join c.workFlowProcess  as wp  WHERE wp.id=:workflowprocessid order by c.actionDate ASC");
        query.setParameter("workflowprocessid", workflowprocessid);
        return query.getResultList();
    }
    @Override
    public int countComment(Context context, UUID workflowprocessid) throws SQLException {
        Query query = createQuery(context, "SELECT count(c) FROM WorkFlowProcessComment as c join c.workFlowProcess  as wp WHERE wp.id=:workflowprocessid");
        query.setParameter("workflowprocessid", workflowprocessid);
        return count(query);
    }

    @Override
    public WorkFlowProcessComment findCommentByworkflowprocessidAndissavedrafttrue(Context context, UUID workflowprocessid) throws SQLException {
        Query query = createQuery(context, "SELECT c FROM WorkFlowProcessComment as c join c.workFlowProcess  as wp  WHERE wp.id=:workflowprocessid and c.isdraftsave=:isdraftsave");
        query.setParameter("workflowprocessid", workflowprocessid);
        query.setParameter("isdraftsave", true);
        return (WorkFlowProcessComment) query.getSingleResult();
    }
    @Override
    public WorkFlowProcessComment findCommentBySubmiterandWorkflowProcessID(Context context, UUID submiter, UUID workflowprocessid) throws SQLException {
        Query query = createQuery(context, "SELECT c FROM WorkFlowProcessComment as c join c.workFlowProcess  as wp  join c.submitter as sub  WHERE wp.id=:workflowprocessid and sub.id=:submiter");
        query.setParameter("workflowprocessid", workflowprocessid);
        query.setParameter("submiter", submiter);
        return (WorkFlowProcessComment) query.getSingleResult();
    }
}
