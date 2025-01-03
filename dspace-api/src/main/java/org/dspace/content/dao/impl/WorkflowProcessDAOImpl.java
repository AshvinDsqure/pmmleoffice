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
import org.postgresql.core.NativeQuery;

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
    public List<WorkflowProcess> findNotCompletedByUser(Context context, UUID eperson, UUID statusid, UUID inwardoutwarttypeid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT wp FROM WorkflowProcess as wp " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as p  " +
                "left join wp.workflowStatus as st " +
                "left join wp.workflowType as t " +
                "where ep.isOwner=:isOwner and p.id=:eperson " +
                "and st.id IN(:statusid) and t.id=:draftid " +
                "and wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("isOwner", true);
        query.setParameter("isdelete", false);
        query.setParameter("eperson", eperson);
        query.setParameter("statusid", statusid);
        query.setParameter("draftid", inwardoutwarttypeid);
        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public List<WorkflowProcess> findNotCompletedByUserDraft(Context context, UUID eperson, UUID statusid, UUID draftid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT wp FROM WorkflowProcess as wp " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as p  " +
                "left join wp.workflowStatus as st " +
                "left join wp.workflowType as t " +
                "where ep.isOwner=:isOwner and p.id=:eperson " +
                "and st.id IN(:statusid) and t.id=:draftid " +
                "and wp.isdelete=:isdelete order by wp.InitDate desc");
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
    public List<WorkflowProcess> findCompletedFlow(Context context, UUID eperson, UUID statusid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT wp FROM WorkflowProcess as wp " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as p  " +
                "left join wp.workflowStatus as st " +
                "left join wp.workflowType as t " +
                "where ep.isOwner=:isOwner and p.id=:eperson " +
                "and st.id IN(:statusid) and t.id=:draftid " +
                "and wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("isOwner", true);
        query.setParameter("isdelete", false);
        query.setParameter("eperson", eperson);
        query.setParameter("statusid", statusid);
        query.setParameter("draftid", workflowtypeid);
        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public int countfindCompletedFlow(Context context, UUID eperson, UUID statusid, UUID workflowtypeid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as p  " +
                "left join wp.workflowStatus as st " +
                "left join wp.workflowType as t " +
                "where ep.isOwner=:isOwner and p.id=:eperson " +
                "and st.id IN(:statusid) and t.id=:draftid " +
                "and wp.isdelete=:isdelete ");
        query.setParameter("isOwner", true);
        query.setParameter("isdelete", false);
        query.setParameter("eperson", eperson);
        query.setParameter("statusid", statusid);
        query.setParameter("draftid", workflowtypeid);
        return count(query);
    }


    @Override
    public int countfindNotCompletedByUser(Context context, UUID eperson, UUID statusid, UUID inwardoutwarttypeid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as p " +
                "left join wp.workflowStatus as st " +
                "left join wp.workflowType as t " +
                "where ep.isOwner=:isOwner and p.id=:eperson " +
                "and st.id IN(:statusid) and t.id=:draftid " +
                "and wp.isdelete=:isdelete ");
        query.setParameter("isOwner", true);
        query.setParameter("isdelete", false);
        query.setParameter("eperson", eperson);
        query.setParameter("statusid", statusid);
        query.setParameter("draftid", inwardoutwarttypeid);
        return count(query);
    }

    @Override
    public int countfindNotCompletedByUserDraft(Context context, UUID eperson, UUID statusid, UUID draftid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as p " +
                "left join wp.workflowStatus as st " +
                "left join wp.workflowType as t " +
                "where ep.isOwner=:isOwner and p.id=:eperson " +
                "and st.id IN(:statusid) and t.id=:draftid " +
                "and wp.isdelete=:isdelete ");
        query.setParameter("isOwner", true);
        query.setParameter("isdelete", false);
        query.setParameter("eperson", eperson);
        query.setParameter("statusid", statusid);
        query.setParameter("draftid", draftid);
        return count(query);
    }

    @Override
    public int getCountByType(Context context, UUID typeid,Integer version) throws SQLException {
        Query query = createQuery(context, "SELECT count(wp) FROM WorkflowProcess as wp join wp.workflowType as t where t.id=:typeid and wp.version=:version");
        query.setParameter("typeid", typeid);
        query.setParameter("version", version);
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
//history
    @Override
    public List<WorkflowProcess> findDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT wp FROM WorkflowProcess as wp " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as p  " +
                "left join wp.workflowStatus as st " +
                "left join wp.workflowType as workflowtype " +
                "where ep.isOwner=:isOwner " +
                "and p.id=:eperson " +
                "and st.id IN(:statusinprogress) " +
                "and workflowtype.id IN (:workflowtypedraft) " +
                "and wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("isOwner", true);
        query.setParameter("eperson", eperson);
        query.setParameter("isdelete", false);
        query.setParameter("statusinprogress", statuscloseid);
        query.setParameter("workflowtypedraft", statusdraftid);
       // query.setParameter("statusdraft", statusdraft);

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
    public List<WorkflowProcess> getWorkflowAfterNoteApproved(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context,"SELECT wp FROM WorkflowProcess as wp " +
                "left join wp.priority as p " +
                "left join wp.workflowStatus as st " +
                "left join wp.workflowType as t  " +
                "left join wp.workFlowProcessDraftDetails as draft  " +
                "left join draft.documentsignator as p  " +
                "where draft.issinglatter=:issinglatter " +
                "and st.id=:statusid and t.id=:workflowtype " +
                "and p.id=:eperson and wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("issinglatter", false);
        query.setParameter("eperson", eperson);
        query.setParameter("statusid", statuscloseid);
        query.setParameter("workflowtype", workflowtypeid);
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
    public int getCountWorkflowAfterNoteApproved(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID workflowtypeid) throws SQLException {
        try {
            Query query = createQuery(context,"SELECT count(wp) FROM WorkflowProcess as wp " +
                    "left join wp.priority as p " +
                    "left join wp.workflowStatus as st " +
                    "left join wp.workflowType as t  " +
                    "left join wp.workFlowProcessDraftDetails as draft  " +
                    "left join draft.documentsignator as p  " +
                    "where draft.issinglatter=:issinglatter " +
                    "and st.id=:statusid and t.id=:workflowtype " +
                    "and p.id=:eperson and wp.isdelete=:isdelete");
            query.setParameter("issinglatter", false);
            query.setParameter("eperson", eperson);
            query.setParameter("statusid", statuscloseid);
            query.setParameter("workflowtype", workflowtypeid);
            query.setParameter("isdelete", false);
            return count(query);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    //sent tapal and efile both are include this query just status n=and workflow type change as per
    @Override
    public List<WorkflowProcess> sentTapal(Context context, UUID eperson, UUID statusid,UUID workflowtypeid,UUID statuscloseid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT DISTINCT wp FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st " +
                "join wp.workflowType as t " +
                " where ep.isOwner=:isOwner and p.id=:eperson and st.id NOT IN(:notDraft) and st.id NOT IN(:statusidclose)  and t.id IN (:workflowtype) and wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("isOwner", false);
        query.setParameter("eperson", eperson);
        query.setParameter("notDraft", statusid);
        query.setParameter("isdelete", false);
        query.setParameter("workflowtype", workflowtypeid);
        query.setParameter("statusidclose", statuscloseid);
        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public int countTapal(Context context, UUID eperson, UUID statusid,UUID workflowtypeid,UUID statusidclose) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st " +
                "join wp.workflowType as t " +
                "where ep.isOwner=:isOwner and t.id IN (:workflowtype) and p.id=:eperson and st.id NOT IN(:statusidclose) and st.id NOT IN(:notDraft) and wp.isdelete=:isdelete");
        query.setParameter("isOwner", false);
        query.setParameter("eperson", eperson);
        query.setParameter("notDraft", statusid);
        query.setParameter("isdelete", false);
        query.setParameter("workflowtype", workflowtypeid);
        query.setParameter("statusidclose",statusidclose);
        return count(query);
    }

    @Override
    public List<WorkflowProcess> closeTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT DISTINCT wp FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st " +
                "join wp.workflowType as t " +
                " where  st.id NOT IN(:notDraft) " +
                "and st.id IN(:statuscloseid) " +
                "and t.id IN (:workflowtype) " +
                "and p.id=:eperson " +
                "and wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("notDraft", statusdraftid);
        query.setParameter("statuscloseid", statuscloseid);
        query.setParameter("isdelete", false);
        query.setParameter("workflowtype", workflowtypeid);
        query.setParameter("eperson", eperson);
        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public int countCloseTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid, UUID workflowtypeid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st " +
                "join wp.workflowType as t " +
                "where  t.id IN (:workflowtype) " +
                "and st.id NOT IN(:notDraft) " +
                "and st.id IN(:statuscloseid) " +
                "and p.id=:eperson " +
                "and wp.isdelete=:isdelete");
        query.setParameter("notDraft", statusdraftid);
        query.setParameter("statuscloseid", statuscloseid);
        query.setParameter("isdelete", false);
        query.setParameter("workflowtype", workflowtypeid);
        query.setParameter("eperson", eperson);
        return count(query);
    }

    @Override
    public List<WorkflowProcess> acknowledgementTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT DISTINCT wp FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st " +
                "join wp.workflowType as t " +
                "where ep.isOwner=:isOwner " +
                "and ep.isacknowledgement=:isacknowledgement " +
                "and p.id=:eperson " +
                "and st.id NOT IN(:notDraft) " +
                "and t.id IN (:workflowtype) " +
                "and wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("isOwner", true);
        query.setParameter("isacknowledgement", true);
        query.setParameter("eperson", eperson);
        query.setParameter("notDraft", statusdraftid);
      //  query.setParameter("statuscloseid", statuscloseid);
        query.setParameter("isdelete", false);
        query.setParameter("workflowtype", workflowtypeid);
        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public int countacknowledgementTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid, UUID workflowtypeid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p " +
                "join wp.workflowStatus as st " +
                "join wp.workflowType as t " +
                "where ep.isOwner=:isOwner " +
                "and ep.isacknowledgement=:isacknowledgement " +
                "and p.id=:eperson " +
                "and st.id NOT IN(:notDraft) " +
                "and t.id IN (:workflowtype) " +
                "and wp.isdelete=:isdelete");
        query.setParameter("isOwner", true);
        query.setParameter("isacknowledgement", true);
        query.setParameter("eperson", eperson);
        query.setParameter("notDraft", statusdraftid);
        //  query.setParameter("statuscloseid", statuscloseid);
        query.setParameter("isdelete", false);
        query.setParameter("workflowtype", workflowtypeid);
        return count(query);
    }

    @Override
    public List<WorkflowProcess> dispatchTapal(Context context, UUID eperson, UUID statusdraftid, UUID statusdspachcloseid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT DISTINCT wp FROM WorkflowProcess as wp " +
                "join wp.workflowStatus as st " +
                "join wp.workflowType as t " +
                " where  st.id NOT IN(:notDraft) " +
                "and st.id IN(:statusdspachcloseid) " +
                "and t.id IN (:workflowtype) " +
                "and wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("notDraft", statusdraftid);
        query.setParameter("statusdspachcloseid", statusdspachcloseid);
        query.setParameter("isdelete", false);
        query.setParameter("workflowtype", workflowtypeid);
        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public int countdispatchTapal(Context context, UUID eperson, UUID statusdraftid, UUID statusdspachcloseid, UUID workflowtypeid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "join wp.workflowStatus as st " +
                "join wp.workflowType as t " +
                "where  t.id IN (:workflowtype) " +
                "and st.id NOT IN(:notDraft) " +
                "and st.id IN(:statusdspachcloseid) " +
                "and wp.isdelete=:isdelete");
        query.setParameter("notDraft", statusdraftid);
        query.setParameter("statusdspachcloseid", statusdspachcloseid);
        query.setParameter("isdelete", false);
        query.setParameter("workflowtype", workflowtypeid);
        return count(query);
    }

    @Override
    public List<WorkflowProcess> parkedFlow(Context context, UUID eperson, UUID statusdraftid, UUID statusparkedid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT DISTINCT wp FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st " +
                "join wp.workflowType as t " +
                "where ep.isOwner=:isOwner " +
                "and p.id=:eperson " +
                "and st.id NOT IN(:notDraft) " +
                "and st.id IN(:statusparkedid) " +
                "and t.id IN (:workflowtype) " +
                "and wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("isOwner", true);
        query.setParameter("eperson", eperson);
        query.setParameter("notDraft", statusdraftid);
        query.setParameter("statusparkedid", statusparkedid);
        query.setParameter("isdelete", false);
        query.setParameter("workflowtype", workflowtypeid);
        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public int countparkedFlow(Context context, UUID eperson, UUID statusdraftid, UUID statusparkedid, UUID workflowtypeid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p " +
                "join wp.workflowStatus as st " +
                "join wp.workflowType as t " +
                "where ep.isOwner=:isOwner " +
                "and p.id=:eperson " +
                "and st.id NOT IN(:notDraft) " +
                "and st.id IN(:notDraft) " +
                "and st.id IN (:statusparkedid) " +
                "and t.id IN (:workflowtype) " +
                "and wp.isdelete=:isdelete");
        query.setParameter("isOwner", true);
        query.setParameter("eperson", eperson);
        query.setParameter("notDraft", statusdraftid);
        query.setParameter("statusparkedid", statusparkedid);
        query.setParameter("isdelete", false);
        query.setParameter("workflowtype", workflowtypeid);
        return count(query);
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
    public static boolean isNullOrEmptyOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    @Override
    public List<WorkflowProcess> searchByFileNumberOrTapalNumber(Context context, MetadataField metadataField, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
      String filenumber="";
      String tapalnumber="";
      UUID initiator=null;
        for (Map.Entry<String, String> map : perameter.entrySet()) {
            System.out.println("key : " + map.getKey() + " value : " + map.getValue());
            if (map.getKey().equalsIgnoreCase("filenumber") && map.getValue() != null) {
                filenumber = map.getValue();
            }
            if (map.getKey().equalsIgnoreCase("tapalnumber") && map.getValue() != null) {
                tapalnumber = map.getValue();
            }
            if (map.getKey().equalsIgnoreCase("initiator") && map.getValue() != null) {
                initiator = UUID.fromString(map.getValue());
            }
        }
        StringBuffer sb = new StringBuffer("SELECT DISTINCT wp FROM " +
                "WorkflowProcess as wp " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as p left join ep.usertype as ut");
        if(!isNullOrEmptyOrBlank(filenumber)) {
                    sb.append(" left join wp.item as i  left join i.metadata as metadatavalue " +
                    "where ut.id=:initiator and p.id=:eperson AND  metadatavalue.metadataField =:metadataField AND lower(STR(metadatavalue.value)) like :filenumber");
            Query query = createQuery(context,sb.toString());
            query.setParameter("eperson", context.getCurrentUser().getID());
            query.setParameter("filenumber", "%" + filenumber.toLowerCase() + "%");
            query.setParameter("metadataField", metadataField);
            query.setParameter("initiator", initiator);
            if (0 <= offset) {
                query.setFirstResult(offset);
            }
            if (0 <= limit) {
                query.setMaxResults(limit);
            }
            return query.getResultList();
        }
        if(!isNullOrEmptyOrBlank(tapalnumber)){
            sb.append(" left join wp.workFlowProcessInwardDetails as inward  " +
                      "where ut.id=:initiator and p.id=:eperson and inward.inwardNumber=:tapalnumber");
            Query query = createQuery(context,sb.toString());
            query.setParameter("eperson", context.getCurrentUser().getID());
            query.setParameter("tapalnumber", tapalnumber);
            query.setParameter("initiator", initiator);
            if (0 <= offset) {
                query.setFirstResult(offset);
            }
            if (0 <= limit) {
                query.setMaxResults(limit);
            }
            return query.getResultList();
        }
        return null;
    }
    @Override
    public List<WorkflowProcess> getHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid,UUID workflowtypeid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT DISTINCT wp FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st " +
                "join wp.workflowType as t " +
                " where p.id=:eperson and st.id IN(:isDraft) and t.id IN (:workflowtype) and  wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("eperson", eperson);
        query.setParameter("isDraft", statusid);
        query.setParameter("workflowtype", workflowtypeid);
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
    public int countgetHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid,UUID workflowtypeid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st  " +
                "join wp.workflowType as t " +
                "where p.id=:eperson and st.id  IN(:isDraft) and t.id IN(:workflowtype) and wp.isdelete=:isdelete");
        query.setParameter("eperson", eperson);
        query.setParameter("isDraft", statusid);
        query.setParameter("workflowtype", workflowtypeid);
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
        StringBuffer sb = new StringBuffer("SELECT DISTINCT wp FROM WorkflowProcess as wp " +
                "left join wp.priority as p " +
                "left join wp.workflowStatus as st " +
                "left join wp.workflowType as t  " +
                "left join wp.workflowStatus as st  " +
                "left join wp.workFlowProcessInwardDetails as inward  " +
                "left join wp.workFlowProcessOutwardDetails as outward  " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as user " +
                "left join user.department as dpt " +
                "where ");

        int i = 0;
        for (Map.Entry<String, String> map : perameter.entrySet()) {
            System.out.println("key : " + map.getKey() + " value : " + map.getValue());
            if (map.getKey().equalsIgnoreCase("subject") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" wp.Subject like :" + map.getKey());
                } else {
                    sb.append(" and wp.Subject like :" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("status") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" st.id=:" + map.getKey());
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
            if (map.getKey().equalsIgnoreCase("inward") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" inward.inwardNumber like :" + map.getKey());

                } else {
                    sb.append(" and inward.inwardNumber like :" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("outward") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" outward.outwardNumber like :" + map.getKey());

                } else {
                    sb.append(" and outward.outwardNumber like :" + map.getKey());
                }
            }
            i++;
        }
        System.out.println("query " + sb.toString());
        Query query = createQuery(context, sb.toString());
        for (Map.Entry<String, String> map : perameter.entrySet()) {
            if (map.getKey().equalsIgnoreCase("subject") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
            } else if (map.getKey().equalsIgnoreCase("inward") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
            } else if (map.getKey().equalsIgnoreCase("outward") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
            } else {
                query.setParameter(map.getKey(), UUID.fromString(map.getValue()));
            }
        }

        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public List<WorkflowProcess> filterInwarAndOutWard(Context context,MetadataField metadataField, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
        StringBuffer sb = new StringBuffer("SELECT DISTINCT wp FROM WorkflowProcess as wp " +
                "left join wp.priority as p " +
                "left join wp.workflowStatus as st " +
                "left join wp.workflowType as t  " +
                "left join wp.workflowStatus as st  " +
                "left join wp.dispatchmode as mode  " +
                "left join wp.workflowProcessSenderDiary as sender  " +
                "left join wp.workFlowProcessInwardDetails as inward  " +
                "left join wp.item as i  " +
                "left join i.metadata as metadatavalue " +
                "left join inward.category as cat " +
                "left join inward.subcategory as subcat " +
                "left join inward.inwardmode as inwmode " +
                "left join inward.category as category " +
                "left join wp.workFlowProcessOutwardDetails as outward  " +
                "left join outward.outwardmedium as outmedium " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as user " +
                "left join user.designation as designa " +
                "left join user.department as dpt " +
                "left join user.office as offic " +
                "where ");

        int i = 0;
        for (Map.Entry<String, String> map : perameter.entrySet()) {
            System.out.println("key : " + map.getKey() + " value : " + map.getValue());
            if (map.getKey().equalsIgnoreCase("filenumber") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" metadatavalue.metadataField=:metadataField AND lower(STR(metadatavalue.value)) like:" + map.getKey());
                } else {
                    sb.append(" and metadatavalue.metadataField=:metadataField AND lower(STR(metadatavalue.value)) like:" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("status") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" st.id=:" + map.getKey());
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
            if (map.getKey().equalsIgnoreCase("categoryRest") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" cat.id=:" + map.getKey());

                } else {
                    sb.append(" and cat.id=:" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("subcategoryRest") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" subcat.id=:" + map.getKey());

                } else {
                    sb.append(" and subcat.id=:" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("officeRest") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" offic.id=:" + map.getKey());

                } else {
                    sb.append(" and offic.id=:" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("inwardmodeRest") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" inwmode.id=:" + map.getKey());

                } else {
                    sb.append(" and inwmode.id=:" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("outwardmodeRest") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" mode.id=:" + map.getKey());

                } else {
                    sb.append(" and mode.id=:" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("outwardmedium") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" mode.id=:" + map.getKey());

                } else {
                    sb.append(" and mode.id=:" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("designation") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" designa.id=:" + map.getKey());

                } else {
                    sb.append(" and designa.id=:" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("user") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" user.id=:" + map.getKey());

                } else {
                    sb.append(" and user.id=:" + map.getKey());
                }
            }
            //text start
            if (map.getKey().equalsIgnoreCase("subject") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" wp.Subject like :" + map.getKey());
                } else {
                    sb.append(" and wp.Subject like :" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("inward") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" inward.inwardNumber like :" + map.getKey());

                } else {
                    sb.append(" and inward.inwardNumber like :" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("outward") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" outward.outwardNumber like :" + map.getKey());

                } else {
                    sb.append(" and outward.outwardNumber like :" + map.getKey());
                }
            }
            //startnefiled
            if (map.getKey().equalsIgnoreCase("inwarddate") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" STR(inward.inwardDate) =:"+map.getKey());
                } else {
                    sb.append(" and STR(inward.inwardDate) =:"+map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("outwarddate") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" STR(outward.outwardDate) =:" + map.getKey());
                } else {
                    sb.append(" and STR(outward.outwardDate) =:" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("receiveddate") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" STR(inward.receivedDate) =:" + map.getKey());

                } else {
                    sb.append(" and STR(inward.receivedDate) =:" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("username") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" user.email like :" + map.getKey());

                } else {
                    sb.append(" and user.email like :" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("sendername") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" sender.sendername like :" + map.getKey());

                } else {
                    sb.append(" and sender.sendername like :" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("senderphonenumber") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" sender.contactNumber like :" + map.getKey());

                } else {
                    sb.append(" and sender.contactNumber like :" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("senderaddress") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" sender.address like :" + map.getKey());

                } else {
                    sb.append(" and sender.address like :" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("sendercity") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" sender.city like :" + map.getKey());

                } else {
                    sb.append(" and sender.city like :" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("sendercountry") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" sender.country like :" + map.getKey());

                } else {
                    sb.append(" and sender.country like :" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("senderpincode") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" sender.pincode like :" + map.getKey());

                } else {
                    sb.append(" and sender.pincode like :" + map.getKey());
                }
            }
            if (map.getKey().equalsIgnoreCase("draftid") && map.getValue() != null) {
                if (i == 0) {
                    sb.append(" t.id!=:" + map.getKey());

                } else {
                    sb.append(" and t.id!=:" + map.getKey());
                }
            }
            i++;
        }
        System.out.println("query " + sb.toString());
        Query query = createQuery(context, sb.toString());
        for (Map.Entry<String, String> map : perameter.entrySet()) {
            if (map.getKey().equalsIgnoreCase("subject") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
            } else if (map.getKey().equalsIgnoreCase("inward") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
            } else if (map.getKey().equalsIgnoreCase("outward") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
            } else if (map.getKey().equalsIgnoreCase("inwarddate") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
            } else if (map.getKey().equalsIgnoreCase("outwarddate") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
            } else if (map.getKey().equalsIgnoreCase("receiveddate") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
            } else if (map.getKey().equalsIgnoreCase("username") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
            } else if (map.getKey().equalsIgnoreCase("sendername") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
            } else if (map.getKey().equalsIgnoreCase("senderphonenumber") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
            } else if (map.getKey().equalsIgnoreCase("senderaddress") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
            } else if (map.getKey().equalsIgnoreCase("sendercity") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
            } else if (map.getKey().equalsIgnoreCase("sendercountry") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
            } else if (map.getKey().equalsIgnoreCase("senderpincode") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
            } else if (map.getKey().equalsIgnoreCase("filenumber") && map.getValue() != null) {
                query.setParameter(map.getKey(), "%" + map.getValue() + "%");
               //query.setParameter("title", "%" + map.getValue() + "%");
                query.setParameter("metadataField", metadataField);
            }else  {
                //uuid all perameter
                query.setParameter(map.getKey(), UUID.fromString(map.getValue()));
            }
        }
        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }




    @Override
    public int countfilterInwarAndOutWard(Context context,MetadataField metadataField, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {

            StringBuffer sb = new StringBuffer("SELECT count(wp) FROM WorkflowProcess as wp " +
                    "left join wp.priority as p " +
                    "left join wp.workflowStatus as st " +
                    "left join wp.workflowType as t  " +
                    "left join wp.workflowStatus as st  " +
                    "left join wp.dispatchmode as mode  " +
                    "left join wp.workflowProcessSenderDiary as sender  " +
                    "left join wp.workFlowProcessInwardDetails as inward  " +
                    "left join wp.item as i  " +
                    "left join i.metadata as metadatavalue " +
                    "left join inward.category as cat " +
                    "left join inward.subcategory as subcat " +
                    "left join inward.inwardmode as inwmode " +
                    "left join inward.category as category " +
                    "left join wp.workFlowProcessOutwardDetails as outward  " +
                    "left join outward.outwardmedium as outmedium " +
                    "left join wp.workflowProcessEpeople as ep " +
                    "left join ep.ePerson as user " +
                    "left join user.designation as designa " +
                    "left join user.department as dpt " +
                    "left join user.office as offic " +
                    "where ");

            int i = 0;
            for (Map.Entry<String, String> map : perameter.entrySet()) {
                System.out.println("key : " + map.getKey() + " value : " + map.getValue());
                if (map.getKey().equalsIgnoreCase("filenumber") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" metadatavalue.metadataField=:metadataField AND lower(STR(metadatavalue.value)) like:" + map.getKey());
                    } else {
                        sb.append(" and metadatavalue.metadataField=:metadataField AND lower(STR(metadatavalue.value)) like:" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("status") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" st.id=:" + map.getKey());
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
                if (map.getKey().equalsIgnoreCase("categoryRest") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" cat.id=:" + map.getKey());

                    } else {
                        sb.append(" and cat.id=:" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("subcategoryRest") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" subcat.id=:" + map.getKey());

                    } else {
                        sb.append(" and subcat.id=:" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("officeRest") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" offic.id=:" + map.getKey());

                    } else {
                        sb.append(" and offic.id=:" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("inwardmodeRest") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" inwmode.id=:" + map.getKey());

                    } else {
                        sb.append(" and inwmode.id=:" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("outwardmodeRest") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" mode.id=:" + map.getKey());

                    } else {
                        sb.append(" and mode.id=:" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("outwardmedium") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" mode.id=:" + map.getKey());

                    } else {
                        sb.append(" and mode.id=:" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("designation") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" designa.id=:" + map.getKey());

                    } else {
                        sb.append(" and designa.id=:" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("user") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" user.id=:" + map.getKey());

                    } else {
                        sb.append(" and user.id=:" + map.getKey());
                    }
                }
                //text start
                if (map.getKey().equalsIgnoreCase("subject") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" wp.Subject like :" + map.getKey());
                    } else {
                        sb.append(" and wp.Subject like :" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("inward") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" inward.inwardNumber like :" + map.getKey());

                    } else {
                        sb.append(" and inward.inwardNumber like :" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("outward") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" outward.outwardNumber like :" + map.getKey());

                    } else {
                        sb.append(" and outward.outwardNumber like :" + map.getKey());
                    }
                }
                //startnefiled
                if (map.getKey().equalsIgnoreCase("inwarddate") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" STR(inward.inwardDate) =:"+map.getKey());
                    } else {
                        sb.append(" and STR(inward.inwardDate) =:"+map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("outwarddate") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" STR(outward.outwardDate) =:" + map.getKey());
                    } else {
                        sb.append(" and STR(outward.outwardDate) =:" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("receiveddate") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" STR(inward.receivedDate) =:" + map.getKey());

                    } else {
                        sb.append(" and STR(inward.receivedDate) =:" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("username") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" user.email like :" + map.getKey());

                    } else {
                        sb.append(" and user.email like :" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("sendername") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" sender.sendername like :" + map.getKey());

                    } else {
                        sb.append(" and sender.sendername like :" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("senderphonenumber") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" sender.contactNumber like :" + map.getKey());

                    } else {
                        sb.append(" and sender.contactNumber like :" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("senderaddress") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" sender.address like :" + map.getKey());

                    } else {
                        sb.append(" and sender.address like :" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("sendercity") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" sender.city like :" + map.getKey());

                    } else {
                        sb.append(" and sender.city like :" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("sendercountry") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" sender.country like :" + map.getKey());

                    } else {
                        sb.append(" and sender.country like :" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("senderpincode") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" sender.pincode like :" + map.getKey());

                    } else {
                        sb.append(" and sender.pincode like :" + map.getKey());
                    }
                }
                if (map.getKey().equalsIgnoreCase("draftid") && map.getValue() != null) {
                    if (i == 0) {
                        sb.append(" t.id!=:" + map.getKey());

                    } else {
                        sb.append(" and t.id!=:" + map.getKey());
                    }
                }
                i++;
            }
            System.out.println("query " + sb.toString());
            Query query = createQuery(context, sb.toString());
            for (Map.Entry<String, String> map : perameter.entrySet()) {
                if (map.getKey().equalsIgnoreCase("subject") && map.getValue() != null) {
                    query.setParameter(map.getKey(), "%" + map.getValue() + "%");
                } else if (map.getKey().equalsIgnoreCase("inward") && map.getValue() != null) {
                    query.setParameter(map.getKey(), "%" + map.getValue() + "%");
                } else if (map.getKey().equalsIgnoreCase("outward") && map.getValue() != null) {
                    query.setParameter(map.getKey(), "%" + map.getValue() + "%");
                } else if (map.getKey().equalsIgnoreCase("inwarddate") && map.getValue() != null) {
                    query.setParameter(map.getKey(), "%" + map.getValue() + "%");
                } else if (map.getKey().equalsIgnoreCase("outwarddate") && map.getValue() != null) {
                    query.setParameter(map.getKey(), "%" + map.getValue() + "%");
                } else if (map.getKey().equalsIgnoreCase("receiveddate") && map.getValue() != null) {
                    query.setParameter(map.getKey(), "%" + map.getValue() + "%");
                } else if (map.getKey().equalsIgnoreCase("username") && map.getValue() != null) {
                    query.setParameter(map.getKey(), "%" + map.getValue() + "%");
                } else if (map.getKey().equalsIgnoreCase("sendername") && map.getValue() != null) {
                    query.setParameter(map.getKey(), "%" + map.getValue() + "%");
                } else if (map.getKey().equalsIgnoreCase("senderphonenumber") && map.getValue() != null) {
                    query.setParameter(map.getKey(), "%" + map.getValue() + "%");
                } else if (map.getKey().equalsIgnoreCase("senderaddress") && map.getValue() != null) {
                    query.setParameter(map.getKey(), "%" + map.getValue() + "%");
                } else if (map.getKey().equalsIgnoreCase("sendercity") && map.getValue() != null) {
                    query.setParameter(map.getKey(), "%" + map.getValue() + "%");
                } else if (map.getKey().equalsIgnoreCase("sendercountry") && map.getValue() != null) {
                    query.setParameter(map.getKey(), "%" + map.getValue() + "%");
                } else if (map.getKey().equalsIgnoreCase("senderpincode") && map.getValue() != null) {
                    query.setParameter(map.getKey(), "%" + map.getValue() + "%");
                } else if (map.getKey().equalsIgnoreCase("filenumber") && map.getValue() != null) {
                 query.setParameter(map.getKey(), "%" + map.getValue() + "%");
                 query.setParameter("metadataField", metadataField);
                } else {
                    //uuid all
                    query.setParameter(map.getKey(), UUID.fromString(map.getValue()));
                }
            }
            return count(query);

    }


    @Override
    public int countByTypeAndStatus(Context context, UUID typeid, UUID statusid, UUID epersonid) throws SQLException {
        Query query = createQuery(context,
                "SELECT count(wp) FROM WorkflowProcess as wp left join wp.workflowProcessEpeople as ep left join ep.ePerson as user left join wp.workflowStatus as st left join wp.workflowType as t where  t.id=:typeid and st.id=:statusid and user.id=:epersonid and ep.isOwner=:isOwner and wp.isdelete=:isdelete");
        query.setParameter("typeid", typeid);
        query.setParameter("statusid", statusid);
        query.setParameter("epersonid", epersonid);
        query.setParameter("isOwner", true);
        query.setParameter("isdelete", false);
        return count(query);
    }

    @Override
    public int countByTypeAndStatusNotwoner(Context context, UUID typeid, UUID statusid, UUID epersonid) throws SQLException {
        Query query = createQuery(context,
                "SELECT count(wp) FROM WorkflowProcess as wp left join wp.workflowProcessEpeople as ep left join ep.ePerson as user left join wp.workflowStatus as st left join wp.workflowType as t where  t.id=:typeid and st.id=:statusid and user.id=:epersonid");
        query.setParameter("typeid", typeid);
        query.setParameter("statusid", statusid);
        query.setParameter("epersonid", epersonid);
        return count(query);
    }

    @Override
    public int countByTypeAndPriority(Context context, UUID typeid, UUID priorityid, UUID epersonid,UUID statusid) throws SQLException {
        Query query = createQuery(context,
                "SELECT count(wp) FROM WorkflowProcess as wp left join wp.workflowProcessEpeople as ep left join ep.ePerson as user left join wp.priority as p left join wp.workflowType as t left join  wp.workflowStatus as st where t.id=:typeid and p.id=:priorityid and user.id=:epersonid and ep.isOwner=:isOwner and st.id=:statusid and wp.isdelete=:isdelete");
        query.setParameter("typeid", typeid);
        query.setParameter("priorityid", priorityid);
        query.setParameter("epersonid", epersonid);
        query.setParameter("isOwner", true);
        query.setParameter("isdelete", false);
        query.setParameter("statusid", statusid);
        return count(query);
    }

    @Override
    public int countByTypeAndPriorityCreted(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid) throws SQLException {
        Query query = createQuery(context,
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                        "left join wp.workflowProcessEpeople as ep " +
                        "left join ep.ePerson as user " +
                        "left join wp.priority as p " +
                        "left join wp.workflowType as t " +
                        "left join  wp.workflowStatus as st " +
                        "where t.id=:typeid " +
                        "and p.id=:priorityid " +
                        "and user.id=:epersonid " +
                        "and ep.isOwner=:isOwner " +
                        "and st.id=:statusid " +
                        "and wp.isdelete=:isdelete");
        query.setParameter("typeid", typeid);
        query.setParameter("priorityid", priorityid);
        query.setParameter("epersonid", epersonid);
        query.setParameter("isOwner", false);
        query.setParameter("isdelete", false);
        query.setParameter("statusid", statusid);
        return count(query);
    }

    @Override
    public int countByTypeAndPriorityClose(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid) throws SQLException {
        Query query = createQuery(context,
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                        "left join wp.workflowProcessEpeople as ep " +
                        "left join ep.ePerson as user " +
                        "left join wp.priority as p " +
                        "left join wp.workflowType as t " +
                        "left join  wp.workflowStatus as st " +
                        "where t.id=:typeid " +
                        "and p.id=:priorityid " +
                        "and user.id=:epersonid " +
                        "and ep.isOwner=:isOwner " +
                        "and st.id=:statusid " +
                        "and wp.isdelete=:isdelete");
        query.setParameter("typeid", typeid);
        query.setParameter("priorityid", priorityid);
        query.setParameter("epersonid", epersonid);
        query.setParameter("isOwner", false);
        query.setParameter("isdelete", false);
        query.setParameter("statusid", statusid);
        return count(query);
    }

    @Override
    public int countByTypeAndPriorityNotDraft(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st join wp.priority as pr  " +
                "join wp.workflowType as t where t.id=:typeid " +
                "and pr.id=:priorityid and  ep.isOwner=:isOwner " +
                "and p.id=:eperson and st.id NOT IN(:notDraft) " +
                "and wp.isdelete=:isdelete");
        query.setParameter("isOwner", false);
        query.setParameter("eperson", epersonid);
        query.setParameter("notDraft", statusid);
        query.setParameter("typeid", typeid);
        query.setParameter("isdelete", false);
        query.setParameter("priorityid", priorityid);

        return count(query);
    }

    @Override
    public int countByTypeAndStatusandNotDraft(Context context, UUID typeid, UUID statusid, UUID epersonid, UUID draftstatusid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st " +
                "join wp.workflowType as t " +
                "where t.id=:typeid " +
                "and  ep.isOwner=:isOwner " +
                "and p.id=:eperson " +
                "and st.id=:statusid " +
                "and st.id NOT IN(:notDraft) " +
                "and wp.isdelete=:isdelete");
        query.setParameter("isOwner", false);
        query.setParameter("eperson", epersonid);
        query.setParameter("notDraft", draftstatusid);
        query.setParameter("typeid", typeid);
        query.setParameter("isdelete", false);
        query.setParameter("statusid", statusid);
        return count(query);
    }

    @Override
    public List<WorkflowProcess> searchSubjectByWorkflowTypeandSubject(Context context, UUID workflowtypeid, String subject) throws SQLException {
        Query query = createQuery(context, "SELECT wp FROM WorkflowProcess as wp " +
                "left join wp.workflowType as t where t.id=:workflowtypeid and lower(wp.Subject)  like :subject ");
        query.setParameter("workflowtypeid", workflowtypeid);
        query.setParameter("subject", "%" + subject.toLowerCase() + "%");
        return query.getResultList();
    }


}
