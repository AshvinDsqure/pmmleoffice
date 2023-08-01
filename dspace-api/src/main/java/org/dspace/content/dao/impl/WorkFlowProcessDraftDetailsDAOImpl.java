/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.WorkFlowProcessDraftDetails;
import org.dspace.content.WorkFlowProcessDraftDetails;
import org.dspace.content.dao.WorkFlowProcessDraftDetailsDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.UUID;

public class WorkFlowProcessDraftDetailsDAOImpl extends AbstractHibernateDAO<WorkFlowProcessDraftDetails> implements WorkFlowProcessDraftDetailsDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkFlowProcessDraftDetailsDAOImpl.class);
    protected WorkFlowProcessDraftDetailsDAOImpl() {
        super();
    }
    @Override
    public WorkFlowProcessDraftDetails findByLegacyId(Context context, int legacyId, Class<WorkFlowProcessDraftDetails> clazz) throws SQLException {
        return null;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM WorkFlowProcessDraftDetails"));
    }

    @Override
    public WorkFlowProcessDraftDetails getbyDocumentsignator(Context context, UUID workflowprocessid) throws SQLException {
        Query query = createQuery(context, "SELECT wfp FROM WorkflowProcess as wp left join wp.workFlowProcessDraftDetails as wfp where  wp.id=:workflowprocessid");
        query.setParameter("workflowprocessid", workflowprocessid);
        return (WorkFlowProcessDraftDetails)query.getSingleResult();
    }
}
