/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.WorkFlowProcessInwardDetails;
import org.dspace.content.WorkflowProcessSenderDiary;
import org.dspace.content.dao.WorkFlowProcessInwardDetailsDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class WorkFlowProcessInwardDetailsDAOImpl extends AbstractHibernateDAO<WorkFlowProcessInwardDetails> implements WorkFlowProcessInwardDetailsDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkFlowProcessInwardDetailsDAOImpl.class);
    protected WorkFlowProcessInwardDetailsDAOImpl() {
        super();
    }
    @Override
    public WorkFlowProcessInwardDetails findByLegacyId(Context context, int legacyId, Class<WorkFlowProcessInwardDetails> clazz) throws SQLException {
        return null;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM WorkFlowProcessInwardDetails"));
    }

    @Override
    public WorkFlowProcessInwardDetails getByInwardNumber(Context context, String inwardnumber) throws SQLException {
        Query query = createQuery(context, "SELECT inward from  WorkFlowProcessInwardDetails as inward where inward.inwardNumber=:inwardnumber");
        query.setParameter("inwardnumber",inwardnumber);
        return (WorkFlowProcessInwardDetails) query.getSingleResult();
    }

    @Override
    public List<WorkFlowProcessInwardDetails> searchInwardNumber(Context context, String name) throws SQLException {
        try {
            Query query = createQuery(context,"SELECT s from  WorkFlowProcessInwardDetails as s where lower(s.inwardNumber)  like :name ");
            query.setParameter("name","%"+name.toLowerCase()+"%");
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("in error " + e.getMessage());
            return null;
        }
    }
}
