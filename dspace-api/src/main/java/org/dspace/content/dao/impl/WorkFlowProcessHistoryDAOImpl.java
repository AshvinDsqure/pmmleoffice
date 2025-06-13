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
import org.dspace.content.WorkFlowProcessHistory;
import org.dspace.content.dao.WorkFlowProcessHistoryDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class WorkFlowProcessHistoryDAOImpl  extends AbstractHibernateDAO<WorkFlowProcessHistory> implements WorkFlowProcessHistoryDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkFlowProcessHistoryDAOImpl.class);
    protected WorkFlowProcessHistoryDAOImpl() {
        super();
    }
    @Override
    public WorkFlowProcessHistory findByLegacyId(Context context, int legacyId, Class<WorkFlowProcessHistory> clazz) throws SQLException {
        return null;
    }
    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM WorkFlowProcessHistory"));
    }
    @Override
    public List<WorkFlowProcessHistory> getHistory(Context context, UUID workflowprocessid) throws SQLException {
           Query query = createQuery(context, "SELECT history FROM WorkFlowProcessHistory as history " +
                    "join history.workflowProcess  as wp " +
                    "WHERE wp.id=:workflowprocessid order by history.actionDate ASC ");
            query.setParameter("workflowprocessid", workflowprocessid);
            return query.getResultList();
        }
    @Override
    public int countHistory(Context context,UUID workflowprocessid) throws SQLException {
        Query query = createQuery(context, "SELECT count(history) FROM WorkFlowProcessHistory as history " +
                "join history.workflowProcess  as wp " +
                "WHERE wp.id=:workflowprocessid ");
        query.setParameter("workflowprocessid", workflowprocessid);
        return count(query);
    }

    @Override
    public List<WorkFlowProcessHistory> getHistory(Context context, int limit) throws SQLException {
        Query query = createQuery(context, "SELECT history FROM WorkFlowProcessHistory as history " +
                "WHERE history.isupdate=:isupdate");
        query.setParameter("isupdate",false );
        query.setMaxResults(limit);
        return query.getResultList();
    }
}
