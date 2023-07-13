/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.Collection;
import org.dspace.content.*;
import org.dspace.content.dao.ItemDAO;
import org.dspace.content.dao.WorkflowProcessDAO;
import org.dspace.core.AbstractHibernateDSODAO;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.hibernate.Criteria;
import org.hibernate.criterion.*;
import org.hibernate.type.StandardBasicTypes;

import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.sql.SQLException;
import java.util.*;

/**
 * Hibernate implementation of the Database Access Object interface class for the Item object.
 * This class is responsible for all database calls for the Item object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class WorkflowProcessDAOImpl extends AbstractHibernateDSODAO<WorkflowProcess> implements WorkflowProcessDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkflowProcessDAOImpl.class);

    protected WorkflowProcessDAOImpl() {
        super();
    }

    @Override
    public List<WorkflowProcess> findNotCompletedByUser(Context context, UUID eperson, UUID statusid, UUID draftid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT wp FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st join wp.workflowType as t where ep.isOwner=:isOwner and p.id=:eperson and st.id NOT IN(:statusid) and t.id NOT IN(:draftid) and wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("isOwner", true);
        query.setParameter("isdelete", false);
        query.setParameter("eperson", eperson);
        query.setParameter("statusid", statusid);
        query.setParameter("draftid", draftid);
        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }


    @Override
    public int countfindNotCompletedByUser(Context context, UUID eperson, UUID statusid, UUID draftid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st join wp.workflowType as t where ep.isOwner=:isOwner and p.id=:eperson and st.id not IN(:statusid) and t.id NOT IN(:draftid) and wp.isdelete=:isdelete");
        query.setParameter("isOwner", false);
        query.setParameter("eperson", eperson);
        query.setParameter("isdelete", false);
        query.setParameter("statusid", statusid);
        query.setParameter("draftid", draftid);
        return count(query);
    }

    @Override
    public int getCountByType(Context context, UUID typeid) throws SQLException {
        Query query = createQuery(context, "SELECT count(wp) FROM WorkflowProcess as wp join wp.workflowType as t where t.id=:typeid");
        query.setParameter("typeid", typeid);
        return count(query);
    }

    @Override
    public List<WorkflowProcess> findReferList(Context context, UUID eperson, UUID statusreferid, UUID statusdraftid, UUID statusdraft, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT wp FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st  where ep.isOwner=:isOwner and p.id=:eperson and st.id IN(:statusreferid) and wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("isOwner", true);
        query.setParameter("eperson", eperson);
        query.setParameter("isdelete", false);
        query.setParameter("statusreferid", statusreferid);

        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public int countRefer(Context context, UUID eperson, UUID statusreferid, UUID statusdraftid, UUID statusdraft) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st  where ep.isOwner=:isOwner and p.id=:eperson and st.id IN(:statusreferid) and wp.isdelete=:isdelete");
        query.setParameter("isOwner", true);
        query.setParameter("eperson", eperson);
        query.setParameter("isdelete", false);
        query.setParameter("statusreferid", statusreferid);
        return count(query);
    }

    @Override
    public List<WorkflowProcess> findDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT wp FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st join wp.workflowType as t where ep.isOwner=:isOwner and p.id=:eperson and st.id NOT IN(:statuscloseid) and st.id NOT IN(:statusdraft) and t.id IN (:statusdraftid) and wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("isOwner", true);
        query.setParameter("eperson", eperson);
        query.setParameter("isdelete", false);
        query.setParameter("statuscloseid", statuscloseid);
        query.setParameter("statusdraftid", statusdraftid);
        query.setParameter("statusdraft", statusdraft);

        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public int countfindDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st join wp.workflowType as t  where ep.isOwner=:isOwner and p.id=:eperson and st.id NOT IN(:statuscloseid) and st.id NOT IN(:statusdraft) and t.id IN (:statusdraftid) and wp.isdelete=:isdelete");
        query.setParameter("isOwner", true);
        query.setParameter("eperson", eperson);
        query.setParameter("statuscloseid", statuscloseid);
        query.setParameter("statusdraftid", statusdraftid);
        query.setParameter("statusdraft", statusdraft);
        query.setParameter("isdelete", false);
        return count(query);
    }

    @Override
    public List<WorkflowProcess> getHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT DISTINCT wp FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st  where ep.isOwner=:isOwner and p.id=:eperson and st.id NOT IN(:notDraft) and wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("isOwner", false);
        query.setParameter("eperson", eperson);
        query.setParameter("notDraft", statusid);
        query.setParameter("isdelete", false);
        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public int countgetHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st  where ep.isOwner=:isOwner and p.id=:eperson and st.id NOT IN(:notDraft) and wp.isdelete=:isdelete");
        query.setParameter("isOwner", false);
        query.setParameter("eperson", eperson);
        query.setParameter("notDraft", statusid);
        query.setParameter("isdelete", false);
        return count(query);
    }

    @Override
    public List<WorkflowProcess> getHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT wp FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st  where p.id=:eperson and st.id IN(:isDraft) and wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("eperson", eperson);
        query.setParameter("isDraft", statusid);
        query.setParameter("isdelete", false);
        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public int countgetHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st  where p.id=:eperson and st.id  IN(:isDraft) and wp.isdelete=:isdelete");
        query.setParameter("eperson", eperson);
        query.setParameter("isDraft", statusid);
        query.setParameter("isdelete", false);
        return count(query);
    }

    @Override
    public WorkflowProcess getNoteByItemsid(Context context, UUID itemid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT wp FROM WorkflowProcess as wp " +
                "join wp.item as i  where i.id=:itemid");
        query.setParameter("itemid", itemid);
        return (WorkflowProcess) query.getSingleResult();
    }

    @Override
    public List<WorkflowProcess> Filter(Context context, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
        StringBuffer sb = new StringBuffer("SELECT wp FROM WorkflowProcess as wp " +
                "left join wp.priority as p " +
                "left join wp.workflowStatus as st " +
                "left join wp.workflowType as t  " +
                "left join wp.workflowStatus as st  " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as user " +
                "left join user.department as dpt " +
                "where ");

        int i = 0;
        for (Map.Entry<String, String> map : perameter.entrySet()) {
            System.out.println("key : " + map.getKey() + " value : " + map.getValue());
            if (map.getKey().equalsIgnoreCase("subject") && map.getValue() != null) {
                if (i == 0) {
                    sb.append("wp.Subject=:" + map.getKey());
                } else {
                    sb.append("and wp.Subject=:" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("status") && map.getValue() != null) {
                if (i == 0) {
                    sb.append("st.id=:" + map.getKey());
                } else {
                    sb.append(" and st.id=:" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("type") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" t.id=:" + map.getKey());

                } else {
                    sb.append(" and t.id=:" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("priority") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" p.id=:" + map.getKey());

                } else {
                    sb.append(" and p.id=:" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("department") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" dpt.id=:" + map.getKey());

                } else {
                    sb.append(" and dpt.id=:" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("user") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" user.id=:" + map.getKey());

                } else {
                    sb.append(" and user.id=:" + map.getKey());
                }
            }

            i++;
        }
        System.out.println("query " + sb.toString());
        Query query = createQuery(context, sb.toString());
        for (Map.Entry<String, String> map : perameter.entrySet()) {
            if (map.getKey().equalsIgnoreCase("subject") && map.getValue() != null) {
                query.setParameter(map.getKey(), map.getValue());
            } else {
                query.setParameter(map.getKey(), UUID.fromString(map.getValue()));
            }
        }
        return query.getResultList();
    }

}
