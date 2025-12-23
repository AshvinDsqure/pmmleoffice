/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.MetadataField;
import org.dspace.content.WorkflowProcess;
import org.dspace.content.dao.WorkflowProcessDAO;
import org.dspace.core.AbstractHibernateDSODAO;
import org.dspace.core.Context;
import org.dspace.core.OrderMappig;
import org.hibernate.Session;

import javax.persistence.Query;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
    public List<WorkflowProcess> findNotCompletedByUser(Context context, UUID eperson, UUID statusid, UUID inwardoutwarttypeid, UUID epersontoepersonmapid, HashMap<String, String> perameter, MetadataField metadatafield, Integer offset, Integer limit) throws SQLException {

        String orderby = "";
        String getgroupby1 = "";
        String issendrcondition = "";
        if (perameter != null) {
            orderby = OrderMappig.getOrderby(perameter);
            getgroupby1 = OrderMappig.getgroupby(perameter);
        }
        StringBuffer sqlb = new StringBuffer("" +
                "SELECT wp FROM WorkflowProcess as wp " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as p  " +
                "left join wp.workflowStatus as st " +
                "left join wp.priority as priority " +
                "left join wp.workFlowProcessHistory as h " +   // <-- New join
                "left join wp.workflowType as t ");
        if (OrderMappig.findkey(perameter, "isdepartment")) {
            sqlb.append(OrderMappig.getDEpartmentjoinString());
        }

        if (OrderMappig.findkey(perameter, "issender")) {
            sqlb.append(OrderMappig.getSenderJoin());
            issendrcondition = " And metadatavalue.metadataField = :metadataField ";
        } else {
            issendrcondition = " ";
        }
        sqlb.append("where ep.isOwner=:isOwner " +
                "and ep.epersontoepersonmapping.id=:epersontoepersonmapid " +
                "and p.id=:eperson " +
                "and st.id IN(:statusid) " +
                "and t.id=:draftid " +
                "and wp.isdelete=:isdelete " + issendrcondition + getgroupby1 + orderby);
        // System.out.println("sql;::findNotCompletedByUser:" + sqlb.toString());
        Query query = createQuery(context, sqlb.toString());
        query.setParameter("isOwner", true);
        query.setParameter("isdelete", false);
        query.setParameter("eperson", eperson);
        query.setParameter("statusid", statusid);
        query.setParameter("draftid", inwardoutwarttypeid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        if (OrderMappig.findkey(perameter, "issender")) {
            query.setParameter("metadataField", metadatafield);
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
    public List<WorkflowProcess> findNotCompletedByUserDraft(Context context, UUID eperson, UUID statusid, UUID draftid, UUID epersontoepersonmapid, HashMap<String, String> perameter, MetadataField metadatafield, Integer offset, Integer limit) throws SQLException {
        String issendrcondition = "";
        String orderby = "";
        String getgroupby1 = "";
        if (perameter != null) {
            orderby = OrderMappig.getOrderby(perameter);
            getgroupby1 = OrderMappig.getgroupby(perameter);
        }


        StringBuffer sqlsb = new StringBuffer("" +
                "SELECT wp FROM WorkflowProcess as wp " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as p  " +
                "left join wp.workflowStatus as st " +
                "left join wp.priority as priority " +
                "left join wp.workFlowProcessHistory as h " +   // <-- New join
                "left join wp.workflowType as t ");
        if (OrderMappig.findkey(perameter, "isdepartment")) {
            sqlsb.append(OrderMappig.getDEpartmentjoinString());
        }
        if (OrderMappig.findkey(perameter, "issender")) {
            sqlsb.append(OrderMappig.getSenderJoin());
            issendrcondition = " And metadatavalue.metadataField = :metadataField ";
        } else {
            issendrcondition = " ";
        }
        sqlsb.append(" where ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid and p.id=:eperson " +
                "and st.id IN(:statusid) and t.id=:draftid " +
                "and wp.isdelete=:isdelete " + issendrcondition + getgroupby1 + orderby);
        // System.out.println("sql;:::" + sqlsb.toString());
        Query query = createQuery(context, sqlsb.toString());
        query.setParameter("isOwner", true);
        query.setParameter("isdelete", false);
        query.setParameter("eperson", eperson);
        query.setParameter("statusid", statusid);
        query.setParameter("draftid", draftid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        if (OrderMappig.findkey(perameter, "issender")) {
            query.setParameter("metadataField", metadatafield);
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
    public List<WorkflowProcess> findCompletedFlow(Context context, UUID eperson, UUID statusid, UUID workflowtypeid, UUID epersontoepersonmapid, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT wp FROM WorkflowProcess as wp " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as p  " +
                "left join wp.workflowStatus as st " +
                "left join wp.workflowType as t " +
                "where ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid  and p.id=:eperson " +
                "and st.id IN(:statusid) and t.id=:draftid " +
                "and wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("isOwner", true);
        query.setParameter("isdelete", false);
        query.setParameter("eperson", eperson);
        query.setParameter("statusid", statusid);
        query.setParameter("draftid", workflowtypeid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);

        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public int countfindCompletedFlow(Context context, UUID eperson, UUID statusid, UUID workflowtypeid, UUID epersontoepersonmapid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(DISTINCT wp.id) FROM WorkflowProcess as wp " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as p  " +
                "left join wp.workflowStatus as st " +
                "left join wp.workflowType as t " +
                "where ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid  and p.id=:eperson " +
                "and st.id IN(:statusid) and t.id=:draftid " +
                "and wp.isdelete=:isdelete ");
        query.setParameter("isOwner", true);
        query.setParameter("isdelete", false);
        query.setParameter("eperson", eperson);
        query.setParameter("statusid", statusid);
        query.setParameter("draftid", workflowtypeid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);

        return count(query);
    }


    @Override
    public int countfindNotCompletedByUser(Context context, UUID eperson, UUID statusid, UUID inwardoutwarttypeid, UUID epersontoepersonmapid) throws SQLException {

        Query query = createQuery(context, "" +
                "SELECT count(DISTINCT wp.id) FROM WorkflowProcess as wp " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as p " +
                "left join wp.workflowStatus as st " +
                "left join wp.priority as priority " +
                "left join wp.workflowType as t " +
                "where ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid  and p.id=:eperson " +
                "and st.id IN(:statusid) and t.id=:draftid " +
                "and wp.isdelete=:isdelete ");
        query.setParameter("isOwner", true);
        query.setParameter("isdelete", false);
        query.setParameter("eperson", eperson);
        query.setParameter("statusid", statusid);
        query.setParameter("draftid", inwardoutwarttypeid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        return count(query);
    }

    @Override
    public int countfindNotCompletedByUserDraft(Context context, UUID eperson, UUID statusid, UUID draftid, UUID epersontoepersonmapid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(DISTINCT wp.id) FROM WorkflowProcess as wp " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as p " +
                "left join wp.workflowStatus as st " +
                "left join wp.priority as priority " +
                "left join wp.workflowType as t " +
                "where ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid  and p.id=:eperson " +
                "and st.id IN(:statusid) and t.id=:draftid " +
                "and wp.isdelete=:isdelete ");
        query.setParameter("isOwner", true);
        query.setParameter("isdelete", false);
        query.setParameter("eperson", eperson);
        query.setParameter("statusid", statusid);
        query.setParameter("draftid", draftid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        return count(query);
    }

    @Override
    public int getCountByType(Context context, UUID typeid, Integer version) throws SQLException {
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
                "SELECT count(DISTINCT wp.id) FROM WorkflowProcess as wp " +
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
    public List<WorkflowProcess> findDraftPending(Context context,
                                                  UUID eperson,
                                                  UUID statuscloseid,
                                                  UUID statusdraftid,
                                                  UUID statusdraft,
                                                  UUID epersontoepersonmapid,
                                                  HashMap<String, String> perameter,
                                                  MetadataField metadatafield,
                                                  UUID statusReject,
                                                  Integer offset,
                                                  Integer limit) throws SQLException {

        String orderby = "";
        String getgroupby1 = "";
        String issendrcondition = "";

        if (perameter != null) {
            orderby = OrderMappig.getOrderby(perameter);
            getgroupby1 = OrderMappig.getgroupby(perameter);
            //  System.out.println("orderby::" + orderby);
            //  System.out.println("getgroupby1::" + getgroupby1);
        }
        StringBuffer hql = new StringBuffer("" +
                "SELECT wp FROM WorkflowProcess AS wp " +
                "INNER JOIN wp.workflowProcessEpeople AS ep " +
                "INNER JOIN ep.ePerson AS p " +
                "LEFT JOIN wp.workflowStatus AS st " +
                "LEFT JOIN wp.workflowType AS workflowtype " +
                "LEFT JOIN wp.priority AS priority " +
                "LEFT JOIN wp.workFlowProcessHistory AS h ");
        // extra joins for ordering by sender's department
        if (OrderMappig.findkey(perameter, "isdepartment")) {
            hql.append(OrderMappig.getDEpartmentjoinString());
        }
        if (OrderMappig.findkey(perameter, "issender")) {
            hql.append(OrderMappig.getSenderJoin());
            issendrcondition = " And metadatavalue.metadataField = :metadataField ";
        } else {
            issendrcondition = " ";
        }
        hql.append("WHERE ep.isOwner = :isOwner " +
                "AND ep.epersontoepersonmapping.id = :epersontoepersonmapid " +
                "AND p.id = :eperson " +
                "AND (st.id IN (:statusinprogress) OR st.id IN (:statusReject)) " +
                "AND workflowtype.id = :workflowtypedraft " +
                "AND wp.isdelete = :isdelete " + issendrcondition +
                getgroupby1 + " " +
                orderby);
        Query query = createQuery(context, hql.toString());
        // Set query parameters
        query.setParameter("isOwner", true);
        query.setParameter("eperson", eperson);
        query.setParameter("isdelete", false);
        query.setParameter("statusinprogress", statuscloseid);
        query.setParameter("statusReject", statusReject);
        query.setParameter("workflowtypedraft", statusdraftid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        if (OrderMappig.findkey(perameter, "issender")) {
            query.setParameter("metadataField", metadatafield);
        }
        // Paginatination for data
        if (offset != null && offset >= 0) {
            query.setFirstResult(offset);
        }
        if (limit != null && limit > 0) {
            query.setMaxResults(limit);
        }
        List<WorkflowProcess> results = query.getResultList();
        return results;
    }

    @Override
    public int countfindDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft, UUID epersontoepersonmapid, MetadataField metadatafield,UUID statusReject) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp.id) FROM WorkflowProcess as wp " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as p  " +
                "left join wp.workflowStatus as st " +
                "left join wp.priority as priority " +
                "left join wp.workflowType as workflowtype " +
                "where ep.isOwner=:isOwner " +
                "and ep.epersontoepersonmapping.id=:epersontoepersonmapid " +
                "and p.id=:eperson " +
                "AND (st.id IN (:statusinprogress) OR st.id IN (:statusReject)) " +
                "and workflowtype.id IN (:workflowtypedraft) " +
                "and wp.isdelete=:isdelete ");
        query.setParameter("isOwner", true);
        query.setParameter("eperson", eperson);
        query.setParameter("isdelete", false);
        query.setParameter("statusinprogress", statuscloseid);
        query.setParameter("statusReject", statusReject);
        query.setParameter("workflowtypedraft", statusdraftid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        return count(query);
    }


    @Override
    public List<WorkflowProcess> findFilePendingDueDate(Context context, UUID eperson, UUID statuscloseid,
                                                        UUID statusdraftid, UUID statusdraft, UUID epersontoepersonmapping,
                                                        HashMap<String, String> perameter, MetadataField metadatafield, Integer offset, Integer limit) throws SQLException {

        String orderby = "";
        String getgroupby1 = "";
        String issendrcondition = "";
        if (perameter != null) {
            orderby = OrderMappig.getOrderby(perameter);
            getgroupby1 = OrderMappig.getgroupby(perameter);
        }
        StringBuffer hql = new StringBuffer(" " +
                "SELECT wp FROM WorkflowProcess AS wp " +
                "INNER JOIN wp.workflowProcessEpeople AS ep " +
                "INNER JOIN ep.ePerson AS p " +
                "LEFT JOIN wp.workflowStatus AS st " +
                "LEFT JOIN wp.workflowType AS workflowtype " +
                "LEFT JOIN wp.priority AS priority " +
                "LEFT JOIN wp.workFlowProcessHistory AS h ");
        // extra joins for initiator department
        if (OrderMappig.findkey(perameter, "isdepartment")) {
            hql.append(OrderMappig.getDEpartmentjoinString());
        }
        if (OrderMappig.findkey(perameter, "issender")) {
            hql.append(OrderMappig.getSenderJoin());
            issendrcondition = " And metadatavalue.metadataField = :metadataField ";
        } else {
            issendrcondition = " ";
        }
        hql.append("WHERE ep.isOwner = :isOwner " +
                "AND ep.epersontoepersonmapping.id = :epersontoepersonmapid " +
                "AND p.id = :eperson " +
                "AND st.id = :statusinprogress " +
                "AND workflowtype.id = :workflowtypedraft " +
                "AND wp.isdelete = :isdelete " +
                "AND ep.assignDate < :currentDate " + issendrcondition + getgroupby1 + orderby);


        Query query = createQuery(context, hql.toString());

        ZonedDateTime todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        Date currentDate = Date.from(todayStart.toInstant());

        query.setParameter("isOwner", true);
        query.setParameter("eperson", eperson);
        query.setParameter("isdelete", false);
        query.setParameter("statusinprogress", statuscloseid);
        query.setParameter("workflowtypedraft", statusdraftid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapping);
        query.setParameter("currentDate", currentDate);
        if (OrderMappig.findkey(perameter, "issender")) {
            query.setParameter("metadataField", metadatafield);
        }


        if (offset != null && offset >= 0) {
            query.setFirstResult(offset);
        }
        if (limit != null && limit > 0) {
            query.setMaxResults(limit);
        }

        return query.getResultList();
    }

    @Override
    public int countfindFilePendingDueDate(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft, UUID epersontoepersonmapping) throws SQLException {
        String hql = ""
                + "SELECT count(wp.id) FROM WorkflowProcess AS wp "
                + "INNER JOIN wp.workflowProcessEpeople AS ep "
                + "INNER JOIN ep.ePerson AS p "
                + "LEFT JOIN wp.workflowStatus AS st "
                + "LEFT JOIN wp.workflowType AS workflowtype "
                + "LEFT JOIN wp.priority AS priority "
                + "WHERE ep.isOwner = :isOwner "
                + "AND ep.epersontoepersonmapping.id = :epersontoepersonmapid "
                + "AND p.id = :eperson "
                + "AND st.id = :statusinprogress "
                + "AND workflowtype.id = :workflowtypedraft "
                + "AND wp.isdelete = :isdelete "
                + "AND ep.assignDate < :currentDate";

        Query query = createQuery(context, hql);

        ZonedDateTime todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        Date currentDate = Date.from(todayStart.toInstant());
        // Set query parameters
        query.setParameter("isOwner", true);
        query.setParameter("eperson", eperson);
        query.setParameter("isdelete", false);
        query.setParameter("statusinprogress", statuscloseid);
        query.setParameter("workflowtypedraft", statusdraftid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapping);
        query.setParameter("currentDate", currentDate);
        return count(query);
    }


    @Override
    public List<WorkflowProcess> getHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid, UUID epersontoepersonmapid, MetadataField metadatafield, Integer offset, Integer limit) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT DISTINCT wp FROM WorkflowProcess as wp " +
                " join wp.item AS item " +
                " join item.metadata as metadatavalue " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st  where ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid  and p.id=:eperson and st.id NOT IN(:notDraft) and wp.isdelete=:isdelete order by wp.InitDate desc");
        query.setParameter("isOwner", false);
        query.setParameter("eperson", eperson);
        query.setParameter("notDraft", statusid);
        query.setParameter("isdelete", false);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);

        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public List<WorkflowProcess> getWorkflowAfterNoteApproved(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID workflowtypeid,UUID epersontoepersonmapid,HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {

        String orderby="";
        String getgroupby1="";
        if(perameter!=null) {
            orderby = OrderMappig.getOrderby(perameter);
            getgroupby1=OrderMappig.getgroupby(perameter);
        }

        Query query = createQuery(context,"SELECT  wp FROM WorkflowProcess as wp " +
                "left join wp.priority as priority " +
                "left join wp.workFlowProcessHistory as h " +   // <-- New join
                "left join wp.workFlowProcessDraftDetails as draft  " +
                "where draft.epersontoepersonmapping.id=:epersontoepersonmapid " +
                "and draft.issinglatter=:issinglatter " +
                "and wp.workflowStatus.id=:statusid " +
                "and wp.workflowType.id=:workflowtype " +
                "and draft.documentsignator.id=:eperson " +
                "and wp.isdelete=:isdelete "+getgroupby1+orderby);
        query.setParameter("issinglatter", false);
        query.setParameter("eperson", eperson);
        query.setParameter("statusid", statuscloseid);
        query.setParameter("workflowtype", workflowtypeid);
        query.setParameter("isdelete", false);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);

        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public int getCountWorkflowAfterNoteApproved(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID workflowtypeid, UUID epersontoepersonmapid) throws SQLException {
        try {
            Query query = createQuery(context, "SELECT count(DISTINCT wp.id) FROM WorkflowProcess as wp " +
                    "join wp.workflowProcessEpeople as ep " +
                    "left join wp.workflowStatus as st " +
                    "left join wp.priority as priority " +
                    "left join wp.workflowType as t  " +
                    "left join wp.workFlowProcessDraftDetails as draft  " +
                    "left join draft.documentsignator as p  " +
                    "where ep.epersontoepersonmapping.id=:epersontoepersonmapid and draft.issinglatter=:issinglatter " +
                    "and st.id=:statusid and t.id=:workflowtype " +
                    "and p.id=:eperson and wp.isdelete=:isdelete");
            query.setParameter("issinglatter", false);
            query.setParameter("eperson", eperson);
            query.setParameter("statusid", statuscloseid);
            query.setParameter("workflowtype", workflowtypeid);
            query.setParameter("isdelete", false);
            query.setParameter("epersontoepersonmapid", epersontoepersonmapid);


            return count(query);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    //sent tapal and efile both are include this query just status n=and workflow type change as per
    @Override
    public List<WorkflowProcess> sentTapal(Context context, UUID eperson, UUID statusid, UUID workflowtypeid, UUID statuscloseid, UUID epersontoepersonmapid, HashMap<String, String> perameter, MetadataField metadatafield, Integer offset, Integer limit) throws SQLException {
        String orderby = "";
        String getgroupby1 = "";
        if (perameter != null) {
            orderby = OrderMappig.getOrderby(perameter);
            getgroupby1 = OrderMappig.getgroupby(perameter);
        }
        StringBuffer hql = new StringBuffer(" " +
                "SELECT  wp FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st " +
                "left join wp.priority as priority " +
                "left join wp.workFlowProcessHistory as h left join wp.workflowType as t ");  // <-- New join
        // extra joins for initiator department
        if (OrderMappig.findkey(perameter, "isdepartment")) {
            hql.append(OrderMappig.getDEpartmentjoinString());
        }
        if (OrderMappig.findkey(perameter, "issender")) {
            hql.append(OrderMappig.getsentoJoin());
        }
        hql.append("" +
                " where ep.issequence=:sequence and ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid and p.id=:eperson  and t.id IN (:workflowtype) and wp.isdelete=:isdelete  "+ getgroupby1 + orderby);

        Query query = createQuery(context, hql.toString());
        query.setParameter("isOwner", false);
        query.setParameter("eperson", eperson);
        query.setParameter("isdelete", false);
        query.setParameter("sequence", true);
        query.setParameter("workflowtype", workflowtypeid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        if (OrderMappig.findkey(perameter, "issender")) {
            query.setParameter("metadataField", metadatafield);
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
    public int countTapal(Context context, UUID eperson, UUID statusid, UUID workflowtypeid, UUID statuscloseid, UUID epersontoepersonmapid, MetadataField metadatafield, HashMap<String, String> perameter) throws SQLException {
        String issendrcondition = "";


        StringBuffer hql = new StringBuffer("" +
                "SELECT count(distinct wp.id) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st " +
                "left join wp.priority as priority " +
                "join wp.workflowType as t ");
        if (perameter != null && OrderMappig.findkey(perameter, "isdepartment")) {
            hql.append(OrderMappig.getDEpartmentjoinString());
        }
        if (perameter != null && OrderMappig.findkey(perameter, "issender")) {
            hql.append(OrderMappig.getsentoJoin());
        }   else {
            issendrcondition = " ";
        }
        hql.append(" where ep.issequence=:sequence and ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid and p.id=:eperson and t.id IN (:workflowtype) and wp.isdelete=:isdelete ");

        Query query = createQuery(context, hql.toString());
        query.setParameter("isOwner", false);
        query.setParameter("eperson", eperson);
        //query.setParameter("notDraft", statusid);
        query.setParameter("isdelete", false);
        query.setParameter("sequence", true);
        query.setParameter("workflowtype", workflowtypeid);
        // query.setParameter("statusidclose", statuscloseid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        if (perameter != null && OrderMappig.findkey(perameter, "issender")) {
            query.setParameter("metadataField", metadatafield);
        }
        return count(query);
    }

    @Override
    public List<WorkflowProcess> closeTapal(Context context,
                                            UUID eperson,
                                            UUID statusdraftid,
                                            UUID statuscloseid,
                                            UUID statusdspatchcloseid,
                                            UUID workflowtypeid,
                                            UUID epersontoepersonmapid,
                                            UUID usertype,
                                            HashMap<String, String> perameter,
                                            MetadataField metadatafield,
                                            Integer offset,
                                            Integer limit) throws SQLException {

        String orderby = "";
        String getgroupby1 = "";
        String issendrcondition = "";
        if (perameter != null) {
            orderby = OrderMappig.getOrderby(perameter);
            getgroupby1 = OrderMappig.getgroupby(perameter);
        }

        if (workflowtypeid.toString().equalsIgnoreCase("81eaafc9-4986-4a08-a531-af3a24b3c805")) {
            StringBuffer hql = new StringBuffer("SELECT wp FROM WorkflowProcess as wp " +
                    "LEFT join wp.workflowProcessEpeople as ep " +
                    "LEFT join ep.ePerson as p " +
                    "LEFT join wp.workflowStatus as st " +
                    "LEFT join wp.priority as priority " +
                    "LEFT join wp.workFlowProcessHistory as h " +
                    "LEFT join wp.workflowType as t ");
            if (OrderMappig.findkey(perameter, "isdepartment")) {
                hql.append(OrderMappig.getDEpartmentjoinString());
            }
            if (OrderMappig.findkey(perameter, "issender")) {
                hql.append(OrderMappig.getSenderJoin());
                issendrcondition = " And metadatavalue.metadataField = :metadataField ";
            } else {
                issendrcondition = " ";
            }
            hql.append("WHERE ep.epersontoepersonmapping.id = :epersontoepersonmapid " +
                    "AND st.id NOT IN (:notDraft) " +
                    "AND (st.id IN (:statuscloseid) OR st.id IN (:statusdspatchcloseid)) " + // fixed grouping
                    "AND t.id IN (:workflowtype) " +
                    "AND p.id = :eperson " +
                    "AND wp.isdelete = :isdelete " +
                    "AND ep.isSender = :issender " +
                    "AND ep.usertype.id = :usertype  " + issendrcondition +
                    getgroupby1 + orderby
            );
            Query query = createQuery(context, hql.toString());

            query.setParameter("notDraft", statusdraftid);
            query.setParameter("statuscloseid", statuscloseid);
            query.setParameter("statusdspatchcloseid", statusdspatchcloseid);
            query.setParameter("isdelete", false);
            query.setParameter("issender", true);
            query.setParameter("workflowtype", workflowtypeid);
            query.setParameter("eperson", eperson);
            query.setParameter("usertype", usertype);
            query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
            if (OrderMappig.findkey(perameter, "issender")) {
                query.setParameter("metadataField", metadatafield);
            }


            if (offset != null && offset >= 0) {
                query.setFirstResult(offset);
            }
            if (limit != null && limit >= 0) {
                query.setMaxResults(limit);
            }

            return query.getResultList();
        } else {
            StringBuffer hql = new StringBuffer("SELECT wp FROM WorkflowProcess as wp " +
                    "LEFT join wp.workflowProcessEpeople as ep " +
                    "LEFT join ep.ePerson as p " +
                    "LEFT join wp.workflowStatus as st " +
                    "LEFT join wp.priority as priority " +
                    "LEFT join wp.workFlowProcessHistory as h " +
                    "LEFT join wp.workflowType as t ");
            if (OrderMappig.findkey(perameter, "isdepartment")) {
                hql.append(OrderMappig.getDEpartmentjoinString());
            }
            if (OrderMappig.findkey(perameter, "issender")) {
                hql.append(OrderMappig.getSenderJoin());
                issendrcondition = " And metadatavalue.metadataField = :metadataField ";
            } else {
                issendrcondition = " ";
            }
            hql.append("WHERE ep.epersontoepersonmapping.id = :epersontoepersonmapid " +
                    "AND st.id NOT IN (:notDraft) " +
                    "AND (st.id IN (:statuscloseid) OR st.id IN (:statusdspatchcloseid)) " + // fixed grouping
                    "AND t.id IN (:workflowtype) " +
                    "AND p.id = :eperson " +
                    "AND wp.isdelete = :isdelete " +
                    "AND ep.isSender = :issender "  + issendrcondition +
                    getgroupby1 + orderby
            );
            Query query = createQuery(context, hql.toString());

            query.setParameter("notDraft", statusdraftid);
            query.setParameter("statuscloseid", statuscloseid);
            query.setParameter("statusdspatchcloseid", statusdspatchcloseid);
            query.setParameter("isdelete", false);
            query.setParameter("issender", true);
            query.setParameter("workflowtype", workflowtypeid);
            query.setParameter("eperson", eperson);
            query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
            if (OrderMappig.findkey(perameter, "issender")) {
                query.setParameter("metadataField", metadatafield);
            }


            if (offset != null && offset >= 0) {
                query.setFirstResult(offset);
            }
            if (limit != null && limit >= 0) {
                query.setMaxResults(limit);
            }

            return query.getResultList();


        }
    }


    @Override
    public int countCloseTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid, UUID statusdspatchcloseid, UUID workflowtypeid, UUID epersontoepersonmapid, UUID usertype) throws SQLException {


        if (workflowtypeid.toString().equalsIgnoreCase("81eaafc9-4986-4a08-a531-af3a24b3c805")) {

            Query query = createQuery(context,
                    "SELECT count(distinct wp.id) FROM WorkflowProcess as wp " +
                            "LEFT join wp.workflowProcessEpeople as ep " +
                            "LEFT join ep.ePerson as p " +
                            "LEFT join wp.workflowStatus as st " +
                            "LEFT join wp.priority as priority " +
                            "LEFT join wp.workFlowProcessHistory as h " +
                            "LEFT join wp.workflowType as t " +
                            "WHERE ep.epersontoepersonmapping.id = :epersontoepersonmapid " +
                            "AND st.id NOT IN (:notDraft) " +
                            "AND (st.id IN (:statuscloseid) OR st.id IN (:statusdspatchcloseid)) " + // fixed grouping
                            "AND t.id IN (:workflowtype) " +
                            "AND p.id = :eperson " +
                            "AND wp.isdelete = :isdelete " +
                            "AND ep.isSender = :issender " +
                            "AND ep.usertype.id = :usertype ");

            query.setParameter("notDraft", statusdraftid);
            query.setParameter("statuscloseid", statuscloseid);
            query.setParameter("statusdspatchcloseid", statusdspatchcloseid);
            query.setParameter("isdelete", false);
            query.setParameter("issender", true);
            query.setParameter("workflowtype", workflowtypeid);
            query.setParameter("eperson", eperson);
            query.setParameter("usertype", usertype);
            query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
            return count(query);
        }else{
            Query query = createQuery(context,
                    "SELECT count(distinct wp.id) FROM WorkflowProcess as wp " +
                            "LEFT join wp.workflowProcessEpeople as ep " +
                            "LEFT join ep.ePerson as p " +
                            "LEFT join wp.workflowStatus as st " +
                            "LEFT join wp.priority as priority " +
                            "LEFT join wp.workFlowProcessHistory as h " +
                            "LEFT join wp.workflowType as t " +
                            "WHERE ep.epersontoepersonmapping.id = :epersontoepersonmapid " +
                            "AND st.id NOT IN (:notDraft) " +
                            "AND (st.id IN (:statuscloseid) OR st.id IN (:statusdspatchcloseid)) " + // fixed grouping
                            "AND t.id IN (:workflowtype) " +
                            "AND p.id = :eperson " +
                            "AND wp.isdelete = :isdelete " +
                            "AND ep.isSender = :issender ");

            query.setParameter("notDraft", statusdraftid);
            query.setParameter("statuscloseid", statuscloseid);
            query.setParameter("statusdspatchcloseid", statusdspatchcloseid);
            query.setParameter("isdelete", false);
            query.setParameter("issender", true);
            query.setParameter("workflowtype", workflowtypeid);
            query.setParameter("eperson", eperson);
            query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
            return count(query);

        }
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
                "SELECT count(DISTINCT wp.id) FROM WorkflowProcess as wp " +
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
                "SELECT count(DISTINCT wp.id) FROM WorkflowProcess as wp " +
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
    public List<WorkflowProcess> parkedFlow(Context context, UUID eperson, UUID statusdraftid, UUID statusparkedid, UUID workflowtypeid, UUID epersontoepersonmapid, HashMap<String, String> perameter, MetadataField metadatafield, Integer offset, Integer limit) throws SQLException {
        String orderby = "";
        String getgroupby1 = "";
        String issendrcondition = "";

        if (perameter != null) {
            orderby = OrderMappig.getOrderby(perameter);
            getgroupby1 = OrderMappig.getgroupby(perameter);
        }


        StringBuffer hhql = new StringBuffer("" +
                "SELECT  wp FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st " +
                "left join wp.priority as priority " +
                "left join wp.workFlowProcessHistory as h " +   // <-- New join
                "join wp.workflowType as t ");
        if (OrderMappig.findkey(perameter, "isdepartment")) {
            hhql.append(OrderMappig.getDEpartmentjoinString());
        }
        if (OrderMappig.findkey(perameter, "issender")) {
            hhql.append(OrderMappig.getSenderJoin());
            issendrcondition = " And metadatavalue.metadataField = :metadataField ";
        } else {
            issendrcondition = " ";
        }
        hhql.append("where ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid \n" +
                "and p.id=:eperson " +
                "and st.id NOT IN(:notDraft) " +
                "and st.id IN(:statusparkedid) " +
                "and t.id IN (:workflowtype) " +
                "and wp.isdelete=:isdelete " + issendrcondition + getgroupby1 + orderby);

        // System.out.println("hql::parkedFlow::" + hhql.toString());
        Query query = createQuery(context, hhql.toString());
        query.setParameter("isOwner", true);
        query.setParameter("eperson", eperson);
        query.setParameter("notDraft", statusdraftid);
        query.setParameter("statusparkedid", statusparkedid);
        query.setParameter("isdelete", false);
        query.setParameter("workflowtype", workflowtypeid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);


        if (OrderMappig.findkey(perameter, "issender")) {
            query.setParameter("metadataField", metadatafield);
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
    public int countparkedFlow(Context context, UUID eperson, UUID statusdraftid, UUID statusparkedid, UUID workflowtypeid, UUID epersontoepersonmapid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p " +
                "join wp.workflowStatus as st " +
                "left join wp.priority as priority " +
                "join wp.workflowType as t " +
                "where ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid \n" +
                "and p.id=:eperson " +
                "and st.id NOT IN(:notDraft) " +
                "and st.id IN (:statusparkedid) " +
                "and t.id IN (:workflowtype) " +
                "and wp.isdelete=:isdelete");
        query.setParameter("isOwner", true);
        query.setParameter("eperson", eperson);
        query.setParameter("notDraft", statusdraftid);
        query.setParameter("statusparkedid", statusparkedid);
        query.setParameter("isdelete", false);
        query.setParameter("workflowtype", workflowtypeid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        return count(query);
    }


    @Override
    public List<Object[]> filterDepartmentWiseCount(Context context, HashMap<String, String> parameter, String startdate, String endDate, Integer offset, Integer limit) throws SQLException {
        StringBuffer sb = new StringBuffer("SELECT dpt.primaryvalue, COUNT(wp) " +
                "FROM WorkflowProcess as wp " +
                "LEFT JOIN wp.priority as p " +
                "LEFT JOIN wp.workflowStatus as st " +
                "LEFT JOIN wp.workflowType as t  " +
                "LEFT JOIN wp.workFlowProcessInwardDetails as inward  " +
                "LEFT JOIN wp.workFlowProcessDraftDetails as draft  " +
                "LEFT JOIN wp.workflowProcessEpeople as ep " +
                "LEFT JOIN ep.ePerson as user " +
                "LEFT JOIN user.department as dpt " +
                "WHERE 1=1 ");  // Ensures dynamic filters work properly

        int i = 0;
        for (Map.Entry<String, String> map : parameter.entrySet()) {
            if (map.getValue() != null) {
                if (i == 0) {
                    sb.append(" AND ");
                } else {
                    sb.append(" AND ");
                }
                if (map.getKey().equalsIgnoreCase("subject")) {
                    sb.append(" wp.Subject like :" + map.getKey());
                } else if (map.getKey().equalsIgnoreCase("status")) {
                    sb.append(" st.id=:" + map.getKey());
                } else if (map.getKey().equalsIgnoreCase("type")) {
                    sb.append(" t.id=:" + map.getKey());
                } else if (map.getKey().equalsIgnoreCase("priority")) {
                    sb.append(" p.id=:" + map.getKey());
                } else if (map.getKey().equalsIgnoreCase("department")) {
                    sb.append(" dpt.id=:" + map.getKey());
                } else if (map.getKey().equalsIgnoreCase("user")) {
                    sb.append(" user.id=:" + map.getKey());
                } else if (map.getKey().equalsIgnoreCase("tapal")) {
                    sb.append(" inward.inwardDate BETWEEN :startdate AND :endDate ");
                } else if (map.getKey().equalsIgnoreCase("draft")) {
                    sb.append(" draft.draftdate BETWEEN :startdate AND :endDate ");
                }
                i++;
            }
        }
        sb.append(" GROUP BY dpt.primaryvalue ");  // Grouping by department name
        // System.out.println("query ::::::::::" + sb.toString());
        Query query = createQuery(context, sb.toString());
        for (Map.Entry<String, String> map : parameter.entrySet()) {
            if (map.getValue() != null) {
                if (map.getKey().equalsIgnoreCase("tapal") || map.getKey().equalsIgnoreCase("draft")) {
                    //no need to add
                } else if (map.getKey().equalsIgnoreCase("subject")) {
                    query.setParameter(map.getKey(), "%" + map.getValue() + "%");
                } else {
                    query.setParameter(map.getKey(), UUID.fromString(map.getValue()));
                }
            }
        }
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedStartDate = dateFormat.parse(startdate);
            Date parsedEndDate = dateFormat.parse(endDate);
            Timestamp timestampStart = new Timestamp(parsedStartDate.getTime());
            Timestamp timestampEnd = new Timestamp(parsedEndDate.getTime());
            query.setParameter("startdate", timestampStart); // Assuming startdate is like "2024-11-22"
            query.setParameter("endDate", timestampEnd); // Assuming endDate is like "2024-11-22" // assume endDate is already in 'yyyy-MM-dd' format
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (0 <= offset) {
            query.setFirstResult(offset);
        }
        if (0 <= limit) {
            query.setMaxResults(limit);
        }
        return query.getResultList();  // Returns department name and count
    }

    @Override
    public List<Object[]> filterDepartmentWiseCountDownload(Context context, HashMap<String, String> parameter, String startdate, String endDate) throws SQLException {
        StringBuffer sb = new StringBuffer("SELECT dpt.primaryvalue, COUNT(wp) " +
                "FROM WorkflowProcess as wp " +
                "LEFT JOIN wp.priority as p " +
                "LEFT JOIN wp.workflowStatus as st " +
                "LEFT JOIN wp.workflowType as t  " +
                "LEFT JOIN wp.workFlowProcessInwardDetails as inward  " +
                "LEFT JOIN wp.workFlowProcessDraftDetails as draft  " +
                "LEFT JOIN wp.workflowProcessEpeople as ep " +
                "LEFT JOIN ep.ePerson as user " +
                "LEFT JOIN user.department as dpt " +
                "WHERE 1=1 ");  // Ensures dynamic filters work properly

        int i = 0;
        for (Map.Entry<String, String> map : parameter.entrySet()) {
            if (map.getValue() != null) {
                if (i == 0) {
                    sb.append(" AND ");
                } else {
                    sb.append(" AND ");
                }
                if (map.getKey().equalsIgnoreCase("subject")) {
                    sb.append(" wp.Subject like :" + map.getKey());
                } else if (map.getKey().equalsIgnoreCase("status")) {
                    sb.append(" st.id=:" + map.getKey());
                } else if (map.getKey().equalsIgnoreCase("type")) {
                    sb.append(" t.id=:" + map.getKey());
                } else if (map.getKey().equalsIgnoreCase("priority")) {
                    sb.append(" p.id=:" + map.getKey());
                } else if (map.getKey().equalsIgnoreCase("department")) {
                    sb.append(" dpt.id=:" + map.getKey());
                } else if (map.getKey().equalsIgnoreCase("user")) {
                    sb.append(" user.id=:" + map.getKey());
                } else if (map.getKey().equalsIgnoreCase("tapal")) {
                    sb.append(" inward.inwardDate BETWEEN :startdate AND :endDate ");
                } else if (map.getKey().equalsIgnoreCase("draft")) {
                    sb.append(" draft.draftdate BETWEEN :startdate AND :endDate ");
                }
                i++;
            }
        }
        sb.append(" GROUP BY dpt.primaryvalue ");  // Grouping by department name
        // System.out.println("query ::::::::::" + sb.toString());
        Query query = createQuery(context, sb.toString());
        for (Map.Entry<String, String> map : parameter.entrySet()) {
            if (map.getValue() != null) {
                if (map.getKey().equalsIgnoreCase("tapal") || map.getKey().equalsIgnoreCase("draft")) {
                    //no need to add
                } else if (map.getKey().equalsIgnoreCase("subject")) {
                    query.setParameter(map.getKey(), "%" + map.getValue() + "%");
                } else {
                    query.setParameter(map.getKey(), UUID.fromString(map.getValue()));
                }
            }
        }
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedStartDate = dateFormat.parse(startdate);
            Date parsedEndDate = dateFormat.parse(endDate);
            Timestamp timestampStart = new Timestamp(parsedStartDate.getTime());
            Timestamp timestampEnd = new Timestamp(parsedEndDate.getTime());

            query.setParameter("startdate", timestampStart); // Assuming startdate is like "2024-11-22"
            query.setParameter("endDate", timestampEnd); // Assuming endDate is like "2024-11-22" // assume endDate is already in 'yyyy-MM-dd' format
        } catch (Exception e) {
            e.printStackTrace();
        }
        return query.getResultList();  // Returns department name and count
    }

    @Override
    public List<Object[]> withinDepartmentDownload(Context context, HashMap<String, String> parameter, String startdate, String endDate) throws SQLException {

//        String queryString="WITH base AS (\n" +
//                "    SELECT wp.uuid, d.primaryvalue AS department\n" +
//                "    FROM workflowprocess wp\n" +
//                "    INNER JOIN workflowprocessmastervalue wt ON wp.workflow_type_id = wt.uuid  \n" +
//                "    INNER JOIN workflowprocessmastervalue status ON wp.workflow_status_id = status.uuid\n" +
//                "    INNER JOIN workflowprocesseperson ep ON wp.uuid = ep.workflowprocess_id\n" +
//                "    INNER JOIN workflowprocessmastervalue usertype ON usertype.uuid = ep.usetype_id\n" +
//                "    INNER JOIN eperson e ON ep.eperson = e.uuid  \n" +
//                "    INNER JOIN epersontoepersonmapping em2 ON em2.eperson = e.uuid\n" +
//                "    INNER JOIN epersonmapping em ON em2.epersonmapping = em.uuid\n" +
//                "    INNER JOIN workflowprocessmastervalue d ON d.uuid = em.department  \n" +
//                "    INNER JOIN workflowprocessdraftdetails i ON wp.workflowprocessdraftdetails_idf = i.uuid\n" +
//                "    WHERE \n" +
//                "        wt.primaryvalue = 'Draft' \n" +
//                "        AND d.primaryvalue IS NOT NULL \n" +
//                "        AND usertype.primaryvalue = 'Initiator'\n" +
//                "        AND wp.isdelete = false\n" +
//                "),\n" +
//                "action_dates AS (\n" +
//                "    SELECT \n" +
//                "        h.workflowprocess_id,\n" +
//                "        MAX(CASE WHEN act.primaryvalue = 'Create' THEN h.actiondate END) AS created_date,\n" +
//                "        MAX(CASE WHEN act.primaryvalue = 'Complete' THEN h.actiondate END) AS complete_date\n" +
//                "    FROM workflowprocesshistory h\n" +
//                "    INNER JOIN workflowprocessmastervalue act ON act.uuid = h.action\n" +
//                "    WHERE act.primaryvalue IN ('Create', 'Complete')\n" +
//                "    GROUP BY h.workflowprocess_id\n" +
//                "),\n" +
//                "filtered AS (\n" +
//                "    SELECT \n" +
//                "        ad.workflowprocess_id,\n" +
//                "        ad.created_date,\n" +
//                "        ad.complete_date,\n" +
//                "        (DATE(ad.complete_date) - DATE(ad.created_date)) AS takenday\n" +
//                "    FROM action_dates ad\n" +
//                "    WHERE \n" +
//                "        ad.complete_date BETWEEN timestamp '2025-05-01 00:00:00' AND '2025-07-22 23:59:59'\n" +
//                ")\n" +
//                "SELECT \n" +
//                "    b.department,\n" +
//                "    f.takenday,\n" +
//                "    COUNT(DISTINCT f.workflowprocess_id) AS no_of_file_closed\n" +
//                "FROM filtered f\n" +
//                "INNER JOIN base b ON b.uuid = f.workflowprocess_id\n" +
//                "GROUP BY b.department, f.takenday\n" +
//                "ORDER BY b.department, f.takenday;";


        String sql = "WITH base AS (\n" +
                "    SELECT wp.uuid, d.primaryvalue AS department\n" +
                "    FROM workflowprocess wp\n" +
                "    INNER JOIN workflowprocessmastervalue wt ON wp.workflow_type_id = wt.uuid  \n" +
                "    INNER JOIN workflowprocessmastervalue status ON wp.workflow_status_id = status.uuid\n" +
                "    INNER JOIN workflowprocesseperson ep ON wp.uuid = ep.workflowprocess_id\n" +
                "    INNER JOIN workflowprocessmastervalue usertype ON usertype.uuid = ep.usetype_id\n" +
                "    INNER JOIN eperson e ON ep.eperson = e.uuid  \n" +
                "    INNER JOIN epersontoepersonmapping em2 ON em2.eperson = e.uuid\n" +
                "    INNER JOIN epersonmapping em ON em2.epersonmapping = em.uuid\n" +
                "    INNER JOIN workflowprocessmastervalue d ON d.uuid = em.department  \n" +
                "    INNER JOIN workflowprocessdraftdetails i ON wp.workflowprocessdraftdetails_idf = i.uuid\n" +
                "    WHERE \n" +
                "        wt.primaryvalue = 'Draft' \n" +
                "        AND d.primaryvalue IS NOT NULL \n" +
                "        AND usertype.primaryvalue = 'Initiator'\n" +
                "        AND wp.isdelete = false\n" +
                "),\n" +
                "action_dates AS (\n" +
                "    SELECT \n" +
                "        h.workflowprocess_id,\n" +
                "        MAX(CASE WHEN act.primaryvalue = 'Create' THEN h.actiondate END) AS created_date,\n" +
                "        MAX(CASE WHEN act.primaryvalue = 'Complete' THEN h.actiondate END) AS complete_date\n" +
                "    FROM workflowprocesshistory h\n" +
                "    INNER JOIN workflowprocessmastervalue act ON act.uuid = h.action\n" +
                "    WHERE act.primaryvalue IN ('Create', 'Complete')\n" +
                "    GROUP BY h.workflowprocess_id\n" +
                "),\n" +
                "filtered AS (\n" +
                "    SELECT \n" +
                "        ad.workflowprocess_id,\n" +
                "        ad.created_date,\n" +
                "        ad.complete_date,\n" +
                "        (DATE(ad.complete_date) - DATE(ad.created_date)) AS takenday\n" +
                "    FROM action_dates ad\n" +
                "    WHERE \n" +
                "        ad.complete_date BETWEEN timestamp '" + startdate + " 00:00:00' AND '" + endDate + " 23:59:59'\n" +
                "),\n" +
                "history_depts AS (\n" +
                "    SELECT \n" +
                "        wp.uuid AS workflowprocess_id,\n" +
                "        d.primaryvalue AS history_dept\n" +
                "    FROM workflowprocesshistory h\n" +
                "    INNER JOIN workflowprocesseperson ep ON ep.uuid = h.workflowprocessepeople\n" +
                "    INNER JOIN eperson e ON ep.eperson = e.uuid\n" +
                "    INNER JOIN epersontoepersonmapping em2 ON em2.eperson = e.uuid\n" +
                "    INNER JOIN epersonmapping em ON em2.epersonmapping = em.uuid\n" +
                "    INNER JOIN workflowprocessmastervalue d ON d.uuid = em.department\n" +
                "    INNER JOIN workflowprocess wp ON h.workflowprocess_id = wp.uuid\n" +
                "),\n" +
                "dept_check AS (\n" +
                "    SELECT \n" +
                "        b.uuid AS workflowprocess_id,\n" +
                "        b.department AS base_dept,\n" +
                "        COUNT(DISTINCT hd.history_dept) AS total_distinct_depts,\n" +
                "        COUNT(DISTINCT CASE WHEN hd.history_dept = b.department THEN NULL ELSE hd.history_dept END) AS non_matching_depts\n" +
                "    FROM base b\n" +
                "    LEFT JOIN history_depts hd ON b.uuid = hd.workflowprocess_id\n" +
                "    GROUP BY b.uuid, b.department\n" +
                "),\n" +
                "final_tagged AS (\n" +
                "    SELECT \n" +
                "        dc.workflowprocess_id,\n" +
                "        CASE \n" +
                "            WHEN dc.non_matching_depts > 0 THEN 'Outer'\n" +
                "            ELSE 'Inner'\n" +
                "        END AS inner_outer\n" +
                "    FROM dept_check dc\n" +
                ")\n" +
                "SELECT \n" +
                "    b.department,\n" +
                "    f.takenday,\n" +
                "    COUNT(DISTINCT f.workflowprocess_id) AS no_of_file_closed,\n" +
                "\tft.inner_outer\n" +
                "FROM filtered f\n" +
                "INNER JOIN base b ON b.uuid = f.workflowprocess_id\n" +
                "LEFT JOIN final_tagged ft ON ft.workflowprocess_id = f.workflowprocess_id\n" +
                "GROUP BY b.department, f.takenday, ft.inner_outer\n" +
                "ORDER BY b.department, f.takenday;";


        org.hibernate.Query query = createSQLQuery(context, sql);
        return query.list();
    }


    @Override
    public List<Object[]> outerDepartmentDownload(Context context, HashMap<String, String> parameter, String startdate, String endDate) throws SQLException {

        String sql = "WITH base AS (\n" +
                "    SELECT wp.uuid, d.primaryvalue AS department\n" +
                "    FROM workflowprocess wp\n" +
                "    INNER JOIN workflowprocessmastervalue wt ON wp.workflow_type_id = wt.uuid  \n" +
                "    INNER JOIN workflowprocessmastervalue status ON wp.workflow_status_id = status.uuid\n" +
                "    INNER JOIN workflowprocesseperson ep ON wp.uuid = ep.workflowprocess_id\n" +
                "    INNER JOIN workflowprocessmastervalue usertype ON usertype.uuid = ep.usetype_id\n" +
                "    INNER JOIN eperson e ON ep.eperson = e.uuid  \n" +
                "    INNER JOIN epersontoepersonmapping em2 ON em2.eperson = e.uuid\n" +
                "    INNER JOIN epersonmapping em ON em2.epersonmapping = em.uuid\n" +
                "    INNER JOIN workflowprocessmastervalue d ON d.uuid = em.department  \n" +
                "    INNER JOIN workflowprocessdraftdetails i ON wp.workflowprocessdraftdetails_idf = i.uuid\n" +
                "    WHERE \n" +
                "        wt.primaryvalue = 'Draft' \n" +
                "        AND d.primaryvalue IS NOT NULL \n" +
                "        AND usertype.primaryvalue = 'Initiator'\n" +
                "        AND wp.isdelete = false\n" +
                "),\n" +
                "action_dates AS (\n" +
                "    SELECT \n" +
                "        h.workflowprocess_id,\n" +
                "        MAX(CASE WHEN act.primaryvalue = 'Create' THEN h.actiondate END) AS created_date,\n" +
                "        MAX(CASE WHEN act.primaryvalue = 'Complete' THEN h.actiondate END) AS complete_date\n" +
                "    FROM workflowprocesshistory h\n" +
                "    INNER JOIN workflowprocessmastervalue act ON act.uuid = h.action\n" +
                "    WHERE act.primaryvalue IN ('Create', 'Complete')\n" +
                "    GROUP BY h.workflowprocess_id\n" +
                "),\n" +
                "filtered AS (\n" +
                "    SELECT \n" +
                "        ad.workflowprocess_id,\n" +
                "        ad.created_date,\n" +
                "        ad.complete_date,\n" +
                "        (DATE(ad.complete_date) - DATE(ad.created_date)) AS takenday\n" +
                "    FROM action_dates ad\n" +
                "    WHERE \n" +
                "        ad.complete_date BETWEEN timestamp '" + startdate + " 00:00:00' AND '" + endDate + " 23:59:59'\n" +
                "),\n" +
                "history_depts AS (\n" +
                "    SELECT \n" +
                "        wp.uuid AS workflowprocess_id,\n" +
                "        d.primaryvalue AS history_dept\n" +
                "    FROM workflowprocesshistory h\n" +
                "    INNER JOIN workflowprocesseperson ep ON ep.uuid = h.workflowprocessepeople\n" +
                "    INNER JOIN eperson e ON ep.eperson = e.uuid\n" +
                "    INNER JOIN epersontoepersonmapping em2 ON em2.eperson = e.uuid\n" +
                "    INNER JOIN epersonmapping em ON em2.epersonmapping = em.uuid\n" +
                "    INNER JOIN workflowprocessmastervalue d ON d.uuid = em.department\n" +
                "    INNER JOIN workflowprocess wp ON h.workflowprocess_id = wp.uuid\n" +
                "),\n" +
                "dept_check AS (\n" +
                "    SELECT \n" +
                "        b.uuid AS workflowprocess_id,\n" +
                "        b.department AS base_dept,\n" +
                "        COUNT(DISTINCT hd.history_dept) AS total_distinct_depts,\n" +
                "        COUNT(DISTINCT CASE WHEN hd.history_dept = b.department THEN NULL ELSE hd.history_dept END) AS non_matching_depts\n" +
                "    FROM base b\n" +
                "    LEFT JOIN history_depts hd ON b.uuid = hd.workflowprocess_id\n" +
                "    GROUP BY b.uuid, b.department\n" +
                "),\n" +
                "final_tagged AS (\n" +
                "    SELECT \n" +
                "        dc.workflowprocess_id,\n" +
                "        CASE \n" +
                "            WHEN dc.non_matching_depts > 0 THEN 'Outer'\n" +
                "            ELSE 'Inner'\n" +
                "        END AS inner_outer\n" +
                "    FROM dept_check dc\n" +
                ")\n" +
                "SELECT \n" +
                "    b.department,\n" +
                "    f.takenday,\n" +
                "    COUNT(DISTINCT f.workflowprocess_id) AS no_of_file_closed,\n" +
                "\tft.inner_outer\n" +
                "FROM filtered f\n" +
                "INNER JOIN base b ON b.uuid = f.workflowprocess_id\n" +
                "LEFT JOIN final_tagged ft ON ft.workflowprocess_id = f.workflowprocess_id\n" +
                "GROUP BY b.department, f.takenday, ft.inner_outer\n" +
                "ORDER BY b.department, f.takenday;";
        org.hibernate.Query query = createSQLQuery(context, sql);
        return query.list();
    }

    @Override
    public List<Object[]> stagewithinDepartmentDownload(Context context, HashMap<String, String> parameter, String startdate, String endDate) throws SQLException {
        String sql = "WITH base AS ( \n" +
                "    SELECT \n" +
                "        wp.uuid, \n" +
                "        d.primaryvalue AS department \n" +
                "    FROM workflowprocess wp \n" +
                "    INNER JOIN workflowprocessmastervalue wt ON wp.workflow_type_id = wt.uuid   \n" +
                "    INNER JOIN workflowprocessmastervalue status ON wp.workflow_status_id = status.uuid \n" +
                "    INNER JOIN workflowprocesseperson ep ON wp.uuid = ep.workflowprocess_id \n" +
                "    INNER JOIN workflowprocessmastervalue usertype ON usertype.uuid = ep.usetype_id \n" +
                "    INNER JOIN eperson e ON ep.eperson = e.uuid   \n" +
                "    INNER JOIN epersontoepersonmapping em2 ON em2.eperson = e.uuid \n" +
                "    INNER JOIN epersonmapping em ON em2.epersonmapping = em.uuid \n" +
                "    INNER JOIN workflowprocessmastervalue d ON d.uuid = em.department   \n" +
                "    INNER JOIN workflowprocessdraftdetails i ON wp.workflowprocessdraftdetails_idf = i.uuid \n" +
                "    WHERE  \n" +
                "        wt.primaryvalue = 'Draft'  \n" +
                "        AND d.primaryvalue IS NOT NULL  \n" +
                "        AND usertype.primaryvalue = 'Initiator' \n" +
                "        AND wp.isdelete = false \n" +
                "), \n" +
                "closed_data AS ( \n" +
                "    SELECT DISTINCT h.workflowprocess_id \n" +
                "    FROM workflowprocesshistory h \n" +
                "    INNER JOIN workflowprocessmastervalue actions ON actions.uuid = h.action \n" +
                "    WHERE  \n" +
                "        actions.primaryvalue = 'Complete' \n" +
                "        AND h.actiondate BETWEEN timestamp '" + startdate + " 00:00:00' AND '" + endDate + " 23:59:59' \n" +
                "), \n" +
                "stage_counts AS ( \n" +
                "    SELECT \n" +
                "        ep.workflowprocess_id, \n" +
                "        COUNT(DISTINCT ep.eperson) AS stagecount\n" +
                "    FROM workflowprocesseperson ep \n" +
                "    GROUP BY ep.workflowprocess_id\n" +
                "), \n" +
                "history_departments AS ( \n" +
                "    SELECT DISTINCT \n" +
                "        h.workflowprocess_id, \n" +
                "        d.primaryvalue AS department \n" +
                "    FROM workflowprocesshistory h \n" +
                "    INNER JOIN workflowprocesseperson ep ON h.workflowprocessepeople = ep.uuid \n" +
                "    INNER JOIN eperson e ON ep.eperson = e.uuid \n" +
                "    INNER JOIN epersontoepersonmapping em2 ON em2.eperson = e.uuid \n" +
                "    INNER JOIN epersonmapping em ON em2.epersonmapping = em.uuid \n" +
                "    INNER JOIN workflowprocessmastervalue d ON d.uuid = em.department \n" +
                "), \n" +
                "department_match AS ( \n" +
                "    SELECT  \n" +
                "        b.uuid, \n" +
                "        CASE  \n" +
                "            WHEN COUNT(DISTINCT hd.department) = 1 AND MAX(hd.department) = b.department THEN 'Inner' \n" +
                "            ELSE 'Outer' \n" +
                "        END AS inner_or_outer \n" +
                "    FROM base b \n" +
                "    LEFT JOIN history_departments hd ON b.uuid = hd.workflowprocess_id \n" +
                "    GROUP BY b.uuid, b.department \n" +
                ") \n" +
                "SELECT  \n" +
                "    b.department, \n" +
                "    sc.stagecount, \n" +
                "    COUNT(DISTINCT b.uuid) AS file_count, \n" +
                "    dm.inner_or_outer \n" +
                "FROM base b \n" +
                "INNER JOIN closed_data cl ON b.uuid = cl.workflowprocess_id \n" +
                "LEFT JOIN stage_counts sc ON b.uuid = sc.workflowprocess_id \n" +
                "LEFT JOIN department_match dm ON b.uuid = dm.uuid \n" +
                "GROUP BY b.department, sc.stagecount, dm.inner_or_outer \n" +
                "ORDER BY b.department, sc.stagecount;\n";


        org.hibernate.Query query = createSQLQuery(context, sql);
        return query.list();
    }

    @Override
    public List<Object[]> stageouterDepartmentDownload(Context context, HashMap<String, String> parameter, String startdate, String endDate) throws SQLException {
        String sql = "WITH base AS ( \n" +
                "    SELECT \n" +
                "        wp.uuid, \n" +
                "        d.primaryvalue AS department \n" +
                "    FROM workflowprocess wp \n" +
                "    INNER JOIN workflowprocessmastervalue wt ON wp.workflow_type_id = wt.uuid   \n" +
                "    INNER JOIN workflowprocessmastervalue status ON wp.workflow_status_id = status.uuid \n" +
                "    INNER JOIN workflowprocesseperson ep ON wp.uuid = ep.workflowprocess_id \n" +
                "    INNER JOIN workflowprocessmastervalue usertype ON usertype.uuid = ep.usetype_id \n" +
                "    INNER JOIN eperson e ON ep.eperson = e.uuid   \n" +
                "    INNER JOIN epersontoepersonmapping em2 ON em2.eperson = e.uuid \n" +
                "    INNER JOIN epersonmapping em ON em2.epersonmapping = em.uuid \n" +
                "    INNER JOIN workflowprocessmastervalue d ON d.uuid = em.department   \n" +
                "    INNER JOIN workflowprocessdraftdetails i ON wp.workflowprocessdraftdetails_idf = i.uuid \n" +
                "    WHERE  \n" +
                "        wt.primaryvalue = 'Draft'  \n" +
                "        AND d.primaryvalue IS NOT NULL  \n" +
                "        AND usertype.primaryvalue = 'Initiator' \n" +
                "        AND wp.isdelete = false \n" +
                "), \n" +
                "closed_data AS ( \n" +
                "    SELECT DISTINCT h.workflowprocess_id \n" +
                "    FROM workflowprocesshistory h \n" +
                "    INNER JOIN workflowprocessmastervalue actions ON actions.uuid = h.action \n" +
                "    WHERE  \n" +
                "        actions.primaryvalue = 'Complete' \n" +
                "        AND h.actiondate BETWEEN timestamp '" + startdate + " 00:00:00' AND '" + endDate + " 23:59:59' \n" +
                "), \n" +
                "stage_counts AS ( \n" +
                "    SELECT \n" +
                "        ep.workflowprocess_id, \n" +
                "        COUNT(DISTINCT ep.eperson) AS stagecount\n" +
                "    FROM workflowprocesseperson ep \n" +
                "    GROUP BY ep.workflowprocess_id\n" +
                "), \n" +
                "history_departments AS ( \n" +
                "    SELECT DISTINCT \n" +
                "        h.workflowprocess_id, \n" +
                "        d.primaryvalue AS department \n" +
                "    FROM workflowprocesshistory h \n" +
                "    INNER JOIN workflowprocesseperson ep ON h.workflowprocessepeople = ep.uuid \n" +
                "    INNER JOIN eperson e ON ep.eperson = e.uuid \n" +
                "    INNER JOIN epersontoepersonmapping em2 ON em2.eperson = e.uuid \n" +
                "    INNER JOIN epersonmapping em ON em2.epersonmapping = em.uuid \n" +
                "    INNER JOIN workflowprocessmastervalue d ON d.uuid = em.department \n" +
                "), \n" +
                "department_match AS ( \n" +
                "    SELECT  \n" +
                "        b.uuid, \n" +
                "        CASE  \n" +
                "            WHEN COUNT(DISTINCT hd.department) = 1 AND MAX(hd.department) = b.department THEN 'Inner' \n" +
                "            ELSE 'Outer' \n" +
                "        END AS inner_or_outer \n" +
                "    FROM base b \n" +
                "    LEFT JOIN history_departments hd ON b.uuid = hd.workflowprocess_id \n" +
                "    GROUP BY b.uuid, b.department \n" +
                ") \n" +
                "SELECT  \n" +
                "    b.department, \n" +
                "    sc.stagecount, \n" +
                "    COUNT(DISTINCT b.uuid) AS file_count, \n" +
                "    dm.inner_or_outer \n" +
                "FROM base b \n" +
                "INNER JOIN closed_data cl ON b.uuid = cl.workflowprocess_id \n" +
                "LEFT JOIN stage_counts sc ON b.uuid = sc.workflowprocess_id \n" +
                "LEFT JOIN department_match dm ON b.uuid = dm.uuid \n" +
                "GROUP BY b.department, sc.stagecount, dm.inner_or_outer \n" +
                "ORDER BY b.department, sc.stagecount;\n";
        org.hibernate.Query query = createSQLQuery(context, sql);
        return query.list();
    }

    @Override
    public List<Object[]> NoOfFileCreatedAndClose(Context context, HashMap<String, String> parameter, String startdate, String endDate) throws SQLException {
        String sql = "WITH base AS (\n" +
                "\t  SELECT wp.uuid, d.primaryvalue AS department, status.primaryvalue AS status\n" +
                "\t  FROM workflowprocess wp\n" +
                "\t  INNER JOIN workflowprocessmastervalue wt ON wp.workflow_type_id = wt.uuid  \n" +
                "\t  INNER JOIN workflowprocessmastervalue status ON wp.workflow_status_id = status.uuid\n" +
                "\t  INNER JOIN workflowprocesseperson ep ON wp.uuid = ep.workflowprocess_id\n" +
                "\t  INNER JOIN workflowprocessmastervalue usertype ON usertype.uuid = ep.usetype_id\n" +
                "\t  INNER JOIN eperson e ON ep.eperson = e.uuid  \n" +
                "\t  INNER JOIN epersontoepersonmapping em2 ON em2.eperson = e.uuid\n" +
                "\t  INNER JOIN epersonmapping em ON em2.epersonmapping = em.uuid\n" +
                "\t  INNER JOIN workflowprocessmastervalue d ON d.uuid = em.department  \n" +
                "\t \n" +
                "\t  INNER JOIN workflowprocessdraftdetails i ON wp.workflowprocessdraftdetails_idf = i.uuid\n" +
                "\t  WHERE \n" +
                "\t\twt.primaryvalue = 'Draft' \n" +
                "\t\tAND d.primaryvalue IS NOT NULL \n" +
                "\t\tAND usertype.primaryvalue = 'Initiator'\n" +
                "\t\tAND wp.isdelete = false\n" +
                "\t),\n" +
                "\tcreated_data AS (\n" +
                "\t  SELECT DISTINCT workflowprocess_id\n" +
                "\t  FROM workflowprocesshistory h\n" +
                "\t  INNER JOIN workflowprocessmastervalue actions ON actions.uuid = h.action\n" +
                "\t  WHERE \n" +
                "\t\tactions.primaryvalue = 'Create'\n" +
                "\t\tAND h.actiondate BETWEEN timestamp '" + startdate + " 00:00:00' AND '" + endDate + " 23:59:59'\n" +
                "\t),\n" +
                "\tclosed_data AS (\n" +
                "\t  SELECT DISTINCT h.workflowprocess_id\n" +
                "\t  FROM workflowprocesshistory h\n" +
                "\t  INNER JOIN workflowprocessmastervalue actions ON actions.uuid = h.action\n" +
                "\t  WHERE \n" +
                "\t\tactions.primaryvalue = 'Complete'\n" +
                "\t\tAND h.actiondate BETWEEN timestamp '" + startdate + " 00:00:00' AND '" + endDate + " 23:59:59'\n" +
                "\t)\n" +
                "\tSELECT \n" +
                "\t\tb.department,\n" +
                "\t\tCOUNT(DISTINCT c.workflowprocess_id) AS no_of_file_created,\n" +
                "\t\tCOUNT(DISTINCT cl.workflowprocess_id) AS no_of_file_closed\n" +
                "\tFROM base b\n" +
                "\tLEFT JOIN created_data c ON b.uuid = c.workflowprocess_id\n" +
                "\tLEFT JOIN closed_data cl ON b.uuid = cl.workflowprocess_id\n" +
                "\tGROUP BY b.department\n" +
                "\tORDER BY b.department;";
        org.hibernate.Query query = createSQLQuery(context, sql);
        return query.list();
    }

    @Override
    public List<Object[]> NoOfTapalCreatedAndClose(Context context, HashMap<String, String> parameter, String startdate, String endDate) throws SQLException {
        String sql = "SELECT  \n" +
                "    d.primaryvalue AS department,\n" +
                "    \n" +
                "    COUNT(DISTINCT CASE \n" +
                "                      WHEN actions.primaryvalue = 'Create'  \n" +
                "                      THEN h.workflowprocess_id \n" +
                "                   END) AS no_of_tapal_created,\n" +
                "    \n" +
                "    COUNT(DISTINCT CASE \n" +
                "                      WHEN actions.primaryvalue IN ('Complete', 'Parked')  \n" +
                "                           OR (\n" +
                "                               actions.primaryvalue = 'Forward'\n" +
                "                               AND h.workflowprocess_id IN (\n" +
                "                                    SELECT wp.uuid\n" +
                "                                    FROM workflowprocess wp\n" +
                "                                    INNER JOIN workflowprocessmastervalue wt \n" +
                "                                            ON wp.workflow_type_id = wt.uuid\n" +
                "                                    LEFT JOIN workflowprocesshistory h2 \n" +
                "                                            ON wp.uuid = h2.workflowprocess_id\n" +
                "                                    INNER JOIN workflowprocessmastervalue actions2 \n" +
                "                                            ON actions2.uuid = h2.action\n" +
                "                                    WHERE wt.primaryvalue = 'Inward'\n" +
                "                                      AND wp.isdelete = false\n" +
                "                                      AND actions2.primaryvalue = 'Forward'\n" +
                "                                      AND wp.version = 1\n" +
                "                                      AND h2.actiondate \n" +
                "                                          BETWEEN '" + startdate + " 00:00:00' AND '" + endDate + " 23:59:59'\n" +
                "                                    GROUP BY wp.uuid\n" +
                "                                    HAVING COUNT(actions2.primaryvalue) > 3\n" +
                "                               )\n" +
                "                           )\n" +
                "                      THEN h.workflowprocess_id \n" +
                "                   END) AS no_of_tapal_closed\n" +
                "                   \n" +
                "FROM workflowprocess wp\n" +
                "INNER JOIN workflowprocessmastervalue wt \n" +
                "        ON wp.workflow_type_id = wt.uuid\n" +
                "INNER JOIN workflowprocesseperson ep \n" +
                "        ON wp.uuid = ep.workflowprocess_id\n" +
                "INNER JOIN workflowprocessmastervalue usertype \n" +
                "        ON usertype.uuid = ep.usetype_id\n" +
                "INNER JOIN eperson e \n" +
                "        ON ep.eperson = e.uuid\n" +
                "INNER JOIN epersontoepersonmapping em2 \n" +
                "        ON em2.eperson = e.uuid\n" +
                "INNER JOIN epersonmapping em \n" +
                "        ON em2.epersonmapping = em.uuid\n" +
                "INNER JOIN workflowprocessmastervalue d \n" +
                "        ON d.uuid = em.department\n" +
                "INNER JOIN workflowprocessinwarddetails i \n" +
                "        ON wp.workflowprocessinwarddetails_idf = i.uuid\n" +
                "LEFT JOIN workflowprocesshistory h \n" +
                "        ON h.workflowprocess_id = wp.uuid\n" +
                "LEFT JOIN workflowprocessmastervalue actions \n" +
                "        ON actions.uuid = h.action\n" +
                "WHERE wt.primaryvalue = 'Inward'\n" +
                "  AND d.primaryvalue IS NOT NULL\n" +
                "  AND i.inwardnumber IS NOT NULL\n" +
                "  AND usertype.primaryvalue = 'Initiator'\n" +
                "  AND wp.isdelete = false\n" +
                "  AND (\n" +
                "        (actions.primaryvalue = 'Create'   \n" +
                "            AND h.actiondate BETWEEN '" + startdate + " 00:00:00' AND '" + endDate + " 23:59:59') \n" +
                "        OR\n" +
                "        (actions.primaryvalue IN ('Complete', 'Parked', 'Forward') \n" +
                "            AND h.actiondate BETWEEN '" + startdate + " 00:00:00' AND '" + endDate + " 23:59:59')\n" +
                "      )\n" +
                "GROUP BY d.primaryvalue\n" +
                "ORDER BY d.primaryvalue;\n";
        org.hibernate.Query query = createSQLQuery(context, sql);
        return query.list();
    }

    @Override
    public int getNextInwardNumber(Context context) throws SQLException {
        String sqlQuery = "SELECT nextval('inward_seq')";
        Session session = getHibernateSession(context);  // Get Hibernate session
        Query query = session.createNativeQuery(sqlQuery);
        Object result = query.getSingleResult();          // Fetch result
        return ((Number) result).intValue();
    }

    @Override
    public int getNextFileNumber(Context context) throws SQLException {
        String sqlQuery = "SELECT nextval('file_seq')";
        Session session = getHibernateSession(context);  // Get Hibernate session
        Query query = session.createNativeQuery(sqlQuery);
        Object result = query.getSingleResult();          // Fetch result
        return ((Number) result).intValue();
    }

    @Override
    public int countTapalPrkedDateRange(Context context, String startdate, String enddate) throws SQLException {
        try {
            String sqlQuery = "SELECT count(DISTINCT wp.uuid)\n" +
                    "                     FROM workflowprocess wp\n" +
                    "                     INNER JOIN workflowprocessmastervalue wt ON wp.workflow_type_id = wt.uuid  \n" +
                    "                     INNER JOIN workflowprocessmastervalue ws ON wp.workflow_status_id=ws.uuid\n" +
                    "                     left join workflowprocesshistory h on wp.uuid=h.workflowprocess_id\n" +
                    " INNER JOIN workflowprocessmastervalue actions ON actions.uuid = h.action\n" +
                    " WHERE \n" +
                    "                    wt.primaryvalue = 'Inward'\n" +
                    "                    AND wp.isdelete = false \n" +
                    " and ws.primaryvalue='Parked'\n" +
                    " and  actions.primaryvalue = 'Parked'\n" +
                    " and wp.version=1\n" +
                    "                    AND h.actiondate BETWEEN timestamp '" + startdate + " 00:00:00' AND '" + enddate + " 23:59:59';";
            return Integer.parseInt(createSQLQuery(context, sqlQuery).uniqueResult().toString());
        } catch (Exception e) {
            System.out.println("Exception in countTapalPrkedDateRange::" + e.getMessage());
            return 0;
        }
    }

    @Override
    public int countTapalStageWISE(Context context, String startdate, String enddate) throws SQLException {

        try {
            String sqlQuery = "SELECT COUNT(*)\n" +
                    "FROM (\n" +
                    "    SELECT DISTINCT wp.uuid\n" +
                    "    FROM workflowprocess wp\n" +
                    "    INNER JOIN workflowprocessmastervalue wt ON wp.workflow_type_id = wt.uuid  \n" +
                    "    INNER JOIN workflowprocessmastervalue ws ON wp.workflow_status_id = ws.uuid\n" +
                    "    LEFT JOIN workflowprocesshistory h ON wp.uuid = h.workflowprocess_id\n" +
                    "    INNER JOIN workflowprocessmastervalue actions ON actions.uuid = h.action\n" +
                    "    WHERE wt.primaryvalue = 'Inward'\n" +
                    "      AND wp.isdelete = false \n" +
                    "      AND actions.primaryvalue = 'Forward'\n" +
                    "      AND wp.version = 1\n" +
                    "      AND h.actiondate BETWEEN timestamp '" + startdate + " 00:00:00' AND '" + enddate + " 23:59:59'\n" +
                    "    GROUP BY wp.uuid\n" +
                    "    HAVING COUNT(actions.primaryvalue) > 2\n" +
                    ") subquery;\n";
            return Integer.parseInt(createSQLQuery(context, sqlQuery).uniqueResult().toString());
        } catch (Exception e) {
            System.out.println("Exception in countTapalPrkedDateRange::" + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Object[]> getActiveAndDActiveUsers(Context context, String flag) throws SQLException {
        try {

            if (flag != null && flag.equalsIgnoreCase("yes")) {
                String sqlQuery = "SELECT \n" +
                        "    ee.email, \n" +
                        "    mvv_first.text_value AS FirstName, \n" +
                        "    mvv_last.text_value AS LastName,\n" +
                        "    COALESCE(\n" +
                        "        NULLIF(STRING_AGG(DISTINCT department.primaryvalue, ', '), ''),\n" +
                        "        'NA'\n" +
                        "    ) AS Departments,\n" +
                        "    COALESCE(\n" +
                        "        NULLIF(STRING_AGG(DISTINCT designation.primaryvalue, ', '), ''),\n" +
                        "        'NA'\n" +
                        "    ) AS Designations,COALESCE(ee.employeeid, '0') AS employeeid\n" +
                        "FROM eperson ee\n" +
                        "LEFT JOIN epersontoepersonmapping ete ON ete.eperson = ee.uuid \n" +
                        "LEFT JOIN epersonmapping em ON em.uuid = ete.epersonmapping\n" +
                        "LEFT JOIN workflowprocessmastervalue department ON department.uuid = em.department\n" +
                        "LEFT JOIN workflowprocessmastervalue designation ON designation.uuid = em.designation\n" +
                        "LEFT JOIN metadatavalue mvv_first \n" +
                        "       ON mvv_first.dspace_object_id = ee.uuid AND mvv_first.metadata_field_id = 1\n" +
                        "LEFT JOIN metadatavalue mvv_last  \n" +
                        "       ON mvv_last.dspace_object_id = ee.uuid AND mvv_last.metadata_field_id = 2\n" +
                        "WHERE ee.uuid IN (\n" +
                        "    SELECT uuid \n" +
                        "    FROM eperson e\n" +
                        "    WHERE  EXISTS (\n" +
                        "        SELECT 1 \n" +
                        "        FROM workflowprocesseperson w \n" +
                        "        WHERE w.eperson = e.uuid\n" +
                        "    )\n" +
                        ")\n" +
                        "GROUP BY ee.uuid, ee.email, mvv_first.text_value, mvv_last.text_value\n" +
                        "ORDER BY mvv_first.text_value, mvv_last.text_value;";

                return (List<Object[]>) createSQLQuery(context, sqlQuery).getResultList();
            } else {
                String sqlQuery = "SELECT \n" +
                        "    ee.email, \n" +
                        "    mvv_first.text_value AS FirstName, \n" +
                        "    mvv_last.text_value AS LastName,\n" +
                        "    COALESCE(\n" +
                        "        NULLIF(STRING_AGG(DISTINCT department.primaryvalue, ', '), ''),\n" +
                        "        'NA'\n" +
                        "    ) AS Departments,\n" +
                        "    COALESCE(\n" +
                        "        NULLIF(STRING_AGG(DISTINCT designation.primaryvalue, ', '), ''),\n" +
                        "        'NA'\n" +
                        "    ) AS Designations,COALESCE(ee.employeeid, '0') AS employeeid\n" +
                        "FROM eperson ee\n" +
                        "LEFT JOIN epersontoepersonmapping ete ON ete.eperson = ee.uuid \n" +
                        "LEFT JOIN epersonmapping em ON em.uuid = ete.epersonmapping\n" +
                        "LEFT JOIN workflowprocessmastervalue department ON department.uuid = em.department\n" +
                        "LEFT JOIN workflowprocessmastervalue designation ON designation.uuid = em.designation\n" +
                        "LEFT JOIN metadatavalue mvv_first \n" +
                        "       ON mvv_first.dspace_object_id = ee.uuid AND mvv_first.metadata_field_id = 1\n" +
                        "LEFT JOIN metadatavalue mvv_last  \n" +
                        "       ON mvv_last.dspace_object_id = ee.uuid AND mvv_last.metadata_field_id = 2\n" +
                        "WHERE ee.uuid IN (\n" +
                        "    SELECT uuid \n" +
                        "    FROM eperson e\n" +
                        "    WHERE NOT EXISTS (\n" +
                        "        SELECT 1 \n" +
                        "        FROM workflowprocesseperson w \n" +
                        "        WHERE w.eperson = e.uuid\n" +
                        "    )\n" +
                        ")\n" +
                        "GROUP BY ee.uuid, ee.email, mvv_first.text_value, mvv_last.text_value\n" +
                        "ORDER BY mvv_first.text_value, mvv_last.text_value;";

                return (List<Object[]>) createSQLQuery(context, sqlQuery).getResultList();
            }


        } catch (Exception e) {
            System.out.println("Exception in countTapalPrkedDateRange::" + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Object[]> getFileByDayTakenAndDepartment(Context context, Integer daytaken, String flag, String startdate, String enddate) throws SQLException {
        try {
            String sqlQuery = "WITH base AS ( \n" +
                    "                    SELECT wp.uuid, d.primaryvalue AS department \n" +
                    "                    FROM workflowprocess wp \n" +
                    "                    INNER JOIN workflowprocessmastervalue wt ON wp.workflow_type_id = wt.uuid   \n" +
                    "                    INNER JOIN workflowprocessmastervalue status ON wp.workflow_status_id = status.uuid \n" +
                    "                    INNER JOIN workflowprocesseperson ep ON wp.uuid = ep.workflowprocess_id \n" +
                    "                    INNER JOIN workflowprocessmastervalue usertype ON usertype.uuid = ep.usetype_id \n" +
                    "                    INNER JOIN eperson e ON ep.eperson = e.uuid   \n" +
                    "                    INNER JOIN epersontoepersonmapping em2 ON em2.eperson = e.uuid \n" +
                    "                    INNER JOIN epersonmapping em ON em2.epersonmapping = em.uuid \n" +
                    "                    INNER JOIN workflowprocessmastervalue d ON d.uuid = em.department   \n" +
                    "                    INNER JOIN workflowprocessdraftdetails i ON wp.workflowprocessdraftdetails_idf = i.uuid \n" +
                    "                    WHERE  \n" +
                    "                        wt.primaryvalue = 'Draft'  \n" +
                    "                        AND d.primaryvalue IS NOT NULL  \n" +
                    "                        AND usertype.primaryvalue = 'Initiator' \n" +
                    "                        AND wp.isdelete = false \n" +
                    "                ), \n" +
                    "                action_dates AS ( \n" +
                    "                    SELECT  \n" +
                    "                        h.workflowprocess_id, \n" +
                    "                        MAX(CASE WHEN act.primaryvalue = 'Create' THEN h.actiondate END) AS created_date, \n" +
                    "                        MAX(CASE WHEN act.primaryvalue = '" + flag + "' THEN h.actiondate END) AS complete_date \n" +
                    "                    FROM workflowprocesshistory h \n" +
                    "                    INNER JOIN workflowprocessmastervalue act ON act.uuid = h.action \n" +
                    "                    WHERE act.primaryvalue IN ('Create', '" + flag + "') \n" +
                    "                    GROUP BY h.workflowprocess_id \n" +
                    "                ), \n" +
                    "                filtered AS ( \n" +
                    "                    SELECT  \n" +
                    "                        ad.workflowprocess_id, \n" +
                    "                        ad.created_date, \n" +
                    "                        ad.complete_date, \n" +
                    "                        (DATE(ad.complete_date) - DATE(ad.created_date)) AS takenday \n" +
                    "                    FROM action_dates ad \n" +
                    "                    WHERE  \n" +
                    "                        ad.complete_date BETWEEN timestamp '" + startdate + " 00:00:00' AND '" + enddate + " 23:59:59' \n" +
                    "                ), \n" +
                    "                history_depts AS ( \n" +
                    "                    SELECT  \n" +
                    "                        wp.uuid AS workflowprocess_id, \n" +
                    "                        d.primaryvalue AS history_dept \n" +
                    "                    FROM workflowprocesshistory h \n" +
                    "                    INNER JOIN workflowprocesseperson ep ON ep.uuid = h.workflowprocessepeople \n" +
                    "                    INNER JOIN eperson e ON ep.eperson = e.uuid \n" +
                    "                    INNER JOIN epersontoepersonmapping em2 ON em2.eperson = e.uuid \n" +
                    "                    INNER JOIN epersonmapping em ON em2.epersonmapping = em.uuid \n" +
                    "                    INNER JOIN workflowprocessmastervalue d ON d.uuid = em.department \n" +
                    "                    INNER JOIN workflowprocess wp ON h.workflowprocess_id = wp.uuid \n" +
                    "                ), \n" +
                    "                dept_check AS ( \n" +
                    "                    SELECT  \n" +
                    "                        b.uuid AS workflowprocess_id, \n" +
                    "                        b.department AS base_dept, \n" +
                    "                        COUNT(DISTINCT hd.history_dept) AS total_distinct_depts, \n" +
                    "                        COUNT(DISTINCT CASE WHEN hd.history_dept = b.department THEN NULL ELSE hd.history_dept END) AS non_matching_depts \n" +
                    "                    FROM base b \n" +
                    "                    LEFT JOIN history_depts hd ON b.uuid = hd.workflowprocess_id \n" +
                    "                    GROUP BY b.uuid, b.department \n" +
                    "                ), \n" +
                    "                final_tagged AS ( \n" +
                    "                    SELECT  \n" +
                    "                        dc.workflowprocess_id, \n" +
                    "                        CASE  \n" +
                    "                            WHEN dc.non_matching_depts > 0 THEN 'Outer' \n" +
                    "                            ELSE 'Inner' \n" +
                    "                        END AS inner_outer \n" +
                    "                    FROM dept_check dc \n" +
                    "                ) \n" +
                    "                SELECT  \n" +
                    "                    b.department, \n" +
                    "                    f.takenday, \n" +
                    "                    COUNT(DISTINCT f.workflowprocess_id) AS no_of_file_closed, \n" +
                    "                ft.inner_outer \n" +
                    "                FROM filtered f \n" +
                    "                INNER JOIN base b ON b.uuid = f.workflowprocess_id \n" +
                    "                LEFT JOIN final_tagged ft ON ft.workflowprocess_id = f.workflowprocess_id \n" +
                    "               where f.takenday='" + daytaken + "'\n" +
                    "\t\t\t   GROUP BY b.department, f.takenday, ft.inner_outer \n" +
                    "                ORDER BY b.department, f.takenday;";

            return (List<Object[]>) createSQLQuery(context, sqlQuery).getResultList();
        } catch (Exception e) {
            System.out.println("ERRORO::getFileByDayTakenAndDepartment " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<String> getDraftMigration(Context context, Integer limit) throws SQLException {
        String sqlQuery =
                "select DISTINCT wp.uuid " +
                        "from workflowprocess wp " +
                        "left join workflowprocessdraftdetails wd on wd.uuid = wp.workflowprocessdraftdetails_idf " +
                        "where wp.isreplydraft = true " +
                        "and wp.workflow_type_id = '81eaafc9-4986-4a08-a531-af3a24b3c805' " +
                        "and wp.workflow_status_id not in ('0c24379b-6ca1-4636-b3da-f31bcd8f719f') " +
                        "and wd.issinglatter = false " +
                        "and wp.version = 1 " +
                        "and wp.isdelete = false and wp.isdraftmigrated = false " +
                        "limit " + limit;

        List<String> result = createSQLQuery(context, sqlQuery)
                .unwrap(org.hibernate.query.NativeQuery.class)
                .addScalar("uuid", org.hibernate.type.StringType.INSTANCE)
                .getResultList();

        return result;

    }


    @Override
    public int countgetHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid, UUID epersontoepersonmapid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(DISTINCT wp.id) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st  where ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid and p.id=:eperson and st.id NOT IN(:notDraft) and wp.isdelete=:isdelete");
        query.setParameter("isOwner", false);
        query.setParameter("eperson", eperson);
        query.setParameter("notDraft", statusid);
        query.setParameter("isdelete", false);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        return count(query);
    }

    public static boolean isNullOrEmptyOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    @Override
    public List<WorkflowProcess> searchByFileNumberOrTapalNumber(Context context, MetadataField metadataField, MetadataField metadataFieldsubject, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
        String filenumber = "";
        String tapalnumber = "";
        String subject = "";
        String workflowtypename = "";
        UUID epersontoepersonmapid = null;
        UUID workflowtype = null;
        UUID statusid = null;
        UUID userinitiator = null;
        boolean isOwner = false;
        boolean isSentto = false;
        boolean isSignlatter = false;
        boolean isclosed = false;

        for (Map.Entry<String, String> map : perameter.entrySet()) {
            if (map.getKey().equalsIgnoreCase("filenumber") && map.getValue() != null) {
                filenumber = map.getValue();
            }
            if (map.getKey().equalsIgnoreCase("tapalnumber") && map.getValue() != null) {
                tapalnumber = map.getValue();
            }
            if (map.getKey().equalsIgnoreCase("statusuuid") && map.getValue() != null) {
                statusid = UUID.fromString(map.getValue());
            }
            if (map.getKey().equalsIgnoreCase("subject") && map.getValue() != null) {
                subject = map.getValue();
            }
            if (map.getKey().equalsIgnoreCase("workflowtype") && map.getValue() != null) {
                workflowtype = UUID.fromString(map.getValue());
            }
            if (map.getKey().equalsIgnoreCase("workflowtypename") && map.getValue() != null) {
                workflowtypename = map.getValue();
            }
            if (map.getKey().equalsIgnoreCase("epersontoepersonmapid") && map.getValue() != null) {
                epersontoepersonmapid = UUID.fromString(map.getValue());
            }
            if (map.getKey().equalsIgnoreCase("tab") && map.getValue() != null) {
                if (map.getValue().equalsIgnoreCase("Inbox")) {
                    isOwner = true;
                } else if (map.getValue().equalsIgnoreCase("Draft")) {
                    isOwner = true;
                } else if (map.getValue().equalsIgnoreCase("Created")) {
                    isOwner = false;
                } else if (map.getValue().equalsIgnoreCase("Parked")) {
                    isOwner = true;
                } else if (map.getValue().equalsIgnoreCase("SignLetter")) {
                    isOwner = false;
                    isSignlatter = true;
                } else if (map.getValue().equalsIgnoreCase("SentTo")) {
                    isOwner = false;
                    isSentto = true;
                } else if (map.getValue().equalsIgnoreCase("closed")) {
                    isOwner = false;
                    isclosed = true;
                } else if (map.getValue().equalsIgnoreCase("userinitiator")) {
                    userinitiator = UUID.fromString(map.getValue());
                }
            }
        }

        // System.out.println("is woner:::" + isOwner);
        StringBuffer sb = new StringBuffer("SELECT DISTINCT wp FROM " +
                "WorkflowProcess as wp " +
                "LEFT JOIN wp.workflowStatus as st  " +
                "LEFT JOIN wp.workflowType as wt  " +
                "left join wp.workflowProcessEpeople as ep " +
                "left join ep.ePerson as p " +
                "left join wp.workFlowProcessDraftDetails as draft ");
        if (!isNullOrEmptyOrBlank(filenumber)) {
            sb.append(" left join wp.item as i  left join i.metadata as metadatavalue " +
                    "where  wt.id=:workflowtype and ep.isOwner=:isOwner and wp.isdelete=:isdelete and p.id=:eperson and metadatavalue.metadataField =:metadataField AND  ep.epersontoepersonmapping.id=:epersontoepersonmapid And lower(STR(metadatavalue.value)) like :filenumber");
            if (!isSentto) {
                sb.append(" and st.id=:statusid");
            }
            if (isSignlatter) {
                sb.append(" and draft.issinglatter=:issinglatter and draft.documentsignator.id=:eperson");
            }
            if (isclosed) {
                sb.append(" and ep.isSender=:issender");
            }
            Query query = createQuery(context, sb.toString());
            query.setParameter("eperson", context.getCurrentUser().getID());
            query.setParameter("filenumber", "%" + filenumber.toLowerCase() + "%");
            query.setParameter("metadataField", metadataField);
            if (!isSentto) {
                query.setParameter("statusid", statusid);
            }
            if (isSignlatter) {
                query.setParameter("issinglatter", false);
            }
            if (isclosed) {
                query.setParameter("issender", true);
                //  query.setParameter("usertype", userinitiator);
            }
            query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
            query.setParameter("isdelete", false);
            query.setParameter("isOwner", isOwner);
            query.setParameter("workflowtype", workflowtype);


            if (0 <= offset) {
                query.setFirstResult(offset);
            }
            if (0 <= limit) {
                query.setMaxResults(limit);
            }
            return query.getResultList();
        }
        if (!isNullOrEmptyOrBlank(tapalnumber)) {
            sb.append(" left join wp.workFlowProcessInwardDetails as inward  " +
                    "where wt.id=:workflowtype and ep.isOwner=:isOwner and p.id=:eperson and ep.epersontoepersonmapping.id=:epersontoepersonmapid And wp.isdelete=:isdelete and lower(STR(inward.inwardNumber)) like :tapalnumber");

            if (!isSentto) {
                sb.append(" and st.id=:statusid");
            }
            if (isSignlatter) {
                sb.append(" and draft.issinglatter=:issinglatter and draft.documentsignator.id=:eperson");
            }
            if (isclosed) {
                sb.append(" and ep.isSender=:issender");
            }
            Query query = createQuery(context, sb.toString());
            query.setParameter("eperson", context.getCurrentUser().getID());
            query.setParameter("tapalnumber", "%" + tapalnumber.toLowerCase() + "%");
            if (!isSentto) {
                query.setParameter("statusid", statusid);
            }
            if (isSignlatter) {
                query.setParameter("issinglatter", false);
            }
            if (isclosed) {
                query.setParameter("issender", true);
                //  query.setParameter("usertype", userinitiator);
            }
            query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
            query.setParameter("isdelete", false);
            query.setParameter("isOwner", isOwner);
            query.setParameter("workflowtype", workflowtype);

            if (0 <= offset) {
                query.setFirstResult(offset);
            }
            if (0 <= limit) {
                query.setMaxResults(limit);
            }
            return query.getResultList();
        }
        if (!isNullOrEmptyOrBlank(subject) && workflowtype != null && workflowtypename != null && workflowtypename.equalsIgnoreCase("Draft")) {
            sb.append(" left join wp.item as i  left join i.metadata as metadatavalue " +
                    "where ep.isOwner=:isOwner and wt.id=:workflowtype  and  p.id=:eperson and metadatavalue.metadataField =:metadataField AND ep.epersontoepersonmapping.id=:epersontoepersonmapid And wp.isdelete=:isdelete and lower(STR(metadatavalue.value)) like :subject");

            if (!isSentto) {
                sb.append(" and st.id=:statusid");
            }
            if (isSignlatter) {
                sb.append(" and draft.issinglatter=:issinglatter and draft.documentsignator.id=:eperson");
            }
            if (isclosed) {
                sb.append(" and ep.isSender=:issender");
            }
            // System.out.println("sql subject::Draft:HQL::::::::::" + sb.toString());
            Query query = createQuery(context, sb.toString());
            query.setParameter("eperson", context.getCurrentUser().getID());
            query.setParameter("subject", "%" + subject.toLowerCase() + "%");
            if (!isSentto) {
                query.setParameter("statusid", statusid);
            }
            if (isSignlatter) {
                query.setParameter("issinglatter", false);
            }
            if (isclosed) {
                query.setParameter("issender", true);
                //query.setParameter("usertype", userinitiator);
            }
            query.setParameter("workflowtype", workflowtype);
            query.setParameter("metadataField", metadataFieldsubject);
            query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
            query.setParameter("isdelete", false);
            query.setParameter("isOwner", isOwner);

            if (0 <= offset) {
                query.setFirstResult(offset);
            }
            if (0 <= limit) {
                query.setMaxResults(limit);
            }
            return query.getResultList();
        }

        if (!isNullOrEmptyOrBlank(subject) && workflowtype != null && workflowtypename != null && workflowtypename.equalsIgnoreCase("Inward")) {
            sb.append(" where ep.isOwner=:isOwner and wt.id=:workflowtype and p.id=:eperson and ep.epersontoepersonmapping.id=:epersontoepersonmapid And lower(wp.Subject) like :subject");
            if (!isSentto) {
                sb.append(" and st.id=:statusid");
            }
            if (isSignlatter) {
                sb.append(" and draft.issinglatter=:issinglatter and draft.documentsignator.id=:eperson");
            }
            if (isclosed) {
                sb.append(" and ep.isSender=:issender");
            }
            Query query = createQuery(context, sb.toString());
            query.setParameter("eperson", context.getCurrentUser().getID());
            query.setParameter("subject", "%" + subject.toLowerCase() + "%");
            if (!isSentto) {
                query.setParameter("statusid", statusid);
            }
            if (isSignlatter) {
                query.setParameter("issinglatter", false);
            }
            if (isclosed) {
                query.setParameter("issender", true);
                //query.setParameter("usertype", userinitiator);
            }
            query.setParameter("workflowtype", workflowtype);
            query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
            query.setParameter("isOwner", isOwner);

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
    public List<WorkflowProcess> getHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid, UUID workflowtypeid, UUID epersontoepersonmapid, HashMap<String, String> perameter, MetadataField metadatafield, Integer offset, Integer limit) throws SQLException {

        String issendrcondition = "";
        String orderby = "";
        String getgroupby1 = "";
        if (perameter != null) {
            orderby = OrderMappig.getOrderby(perameter);
            getgroupby1 = OrderMappig.getgroupby(perameter);
        }


        StringBuffer hql = new StringBuffer("" +
                "SELECT  wp FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st " +
                "left join wp.priority as priority " +
                "left join wp.workFlowProcessHistory as h " +   // <-- New join
                "join wp.workflowType as t ");
        // extra joins for initiator department
        if (OrderMappig.findkey(perameter, "isdepartment")) {
            hql.append(OrderMappig.getDEpartmentjoinString());
        }
        if (OrderMappig.findkey(perameter, "issender")) {
            hql.append(OrderMappig.getSenderJoin());
            issendrcondition = " And metadatavalue.metadataField = :metadataField ";
        } else {
            issendrcondition = " ";
        }

        hql.append(" where ep.epersontoepersonmapping.id=:epersontoepersonmapid and p.id=:eperson and st.id=:isDraft and t.id=:workflowtype and  wp.isdelete=:isdelete " + issendrcondition + getgroupby1 + orderby);
        // System.out.println("getHistoryByOwnerAndIsDraft::HQL::" + hql.toString());
        Query query = createQuery(context, hql.toString());
        query.setParameter("eperson", eperson);
        query.setParameter("isDraft", statusid);
        query.setParameter("workflowtype", workflowtypeid);
        query.setParameter("isdelete", false);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        if (OrderMappig.findkey(perameter, "issender")) {
            query.setParameter("metadataField", metadatafield);
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
    public int countgetHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid, UUID workflowtypeid, UUID epersontoepersonmapid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(DISTINCT wp.id) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st  " +
                "left join wp.priority as priority " +
                "join wp.workflowType as t " +
                "where ep.epersontoepersonmapping.id=:epersontoepersonmapid and p.id=:eperson and st.id=:isDraft and t.id=:workflowtype and wp.isdelete=:isdelete");
        query.setParameter("eperson", eperson);
        query.setParameter("isDraft", statusid);
        query.setParameter("workflowtype", workflowtypeid);
        query.setParameter("isdelete", false);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);

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
            //  System.out.println("key : " + map.getKey() + " value : " + map.getValue());
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
        // System.out.println("query " + sb.toString());
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
    public List<WorkflowProcess> filterInwarAndOutWard(Context context, MetadataField metadataField, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
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
            //  System.out.println("key : " + map.getKey() + " value : " + map.getValue());
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
                    sb.append(" STR(inward.inwardDate) =:" + map.getKey());
                } else {
                    sb.append(" and STR(inward.inwardDate) =:" + map.getKey());
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
        //  System.out.println("query " + sb.toString());
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
            } else {
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
    public int countfilterInwarAndOutWard(Context context, MetadataField metadataField, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {

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
            // System.out.println("key : " + map.getKey() + " value : " + map.getValue());
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
                    sb.append(" STR(inward.inwardDate) =:" + map.getKey());
                } else {
                    sb.append(" and STR(inward.inwardDate) =:" + map.getKey());
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
        //  System.out.println("query " + sb.toString());
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
    public int countByTypeAndStatus(Context context, UUID typeid, UUID statusid, UUID epersonid, UUID epersontoepersonmapid) throws SQLException {
        Query query = createQuery(context,
                "SELECT count(DISTINCT wp.id) FROM WorkflowProcess as wp left join wp.workflowProcessEpeople as ep" +
                        " left join ep.ePerson as user left join wp.workflowStatus as st left join wp.workflowType as t where  t.id=:typeid and st.id=:statusid and user.id=:epersonid and ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid and wp.isdelete=:isdelete");
        query.setParameter("typeid", typeid);
        query.setParameter("statusid", statusid);
        query.setParameter("epersonid", epersonid);
        query.setParameter("isOwner", true);
        query.setParameter("isdelete", false);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);

        return count(query);
    }


    @Override
    public int countByTypeAndPriority(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid, UUID epersontoepersonmapid) throws SQLException {
        Query query = createQuery(context,
                "SELECT count(DISTINCT wp.id) FROM WorkflowProcess as wp left join wp.workflowProcessEpeople as ep left join ep.ePerson as user left join wp.priority as p left join wp.workflowType as t left join  wp.workflowStatus as st where t.id=:typeid and p.id=:priorityid and user.id=:epersonid and ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid  and st.id=:statusid and wp.isdelete=:isdelete");
        query.setParameter("typeid", typeid);
        query.setParameter("priorityid", priorityid);
        query.setParameter("epersonid", epersonid);
        query.setParameter("isOwner", true);
        query.setParameter("isdelete", false);
        query.setParameter("statusid", statusid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        return count(query);
    }

    @Override
    public int countByTypeAndPriorityDraftTapal(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid, UUID epersontoepersonmapid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(DISTINCT wp.id) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st  " +
                "left join wp.priority as priority " +
                "join wp.workflowType as t " +
                "where ep.epersontoepersonmapping.id=:epersontoepersonmapid and p.id=:eperson and st.id=:isDraft and t.id=:workflowtype and wp.isdelete=:isdelete and priority.id=:priorityid ");
        query.setParameter("eperson", epersonid);
        query.setParameter("isDraft", statusid);
        query.setParameter("workflowtype", typeid);
        query.setParameter("priorityid", priorityid);
        query.setParameter("isdelete", false);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        return count(query);
    }

    @Override
    public int countByTypeAndPriorityPark(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid, UUID epersontoepersonmapid,UUID statusdraftid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(wp) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p " +
                "join wp.workflowStatus as st " +
                "left join wp.priority as priority " +
                "join wp.workflowType as t " +
                "where ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid \n" +
                "and p.id=:eperson " +
                "and st.id NOT IN(:notDraft) " +
                "and st.id IN (:statusparkedid) " +
                "and t.id IN (:workflowtype) " +
                "and wp.isdelete=:isdelete and priority.id=:priorityid");
        query.setParameter("isOwner", true);
        query.setParameter("eperson", epersonid);
        query.setParameter("priorityid", priorityid);
        query.setParameter("notDraft", statusdraftid);
        query.setParameter("statusparkedid", statusid);
        query.setParameter("isdelete", false);
        query.setParameter("workflowtype", typeid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        return count(query);
    }

    @Override
    public int countByTypeAndPriorityCreted(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid, UUID epersontoepersonmapid) throws SQLException {
        Query query = createQuery(context,
                "SELECT count(DISTINCT wp.id) FROM WorkflowProcess as wp " +
                        "left join wp.workflowProcessEpeople as ep " +
                        "left join ep.ePerson as user " +
                        "left join wp.priority as p " +
                        "left join wp.workflowType as t " +
                        "left join  wp.workflowStatus as st " +
                        "where t.id=:typeid " +
                        "and p.id=:priorityid " +
                        "and user.id=:epersonid " +
                        "and ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid " +
                        "and st.id=:statusid " +
                        "and wp.isdelete=:isdelete");
        query.setParameter("typeid", typeid);
        query.setParameter("priorityid", priorityid);
        query.setParameter("epersonid", epersonid);
        query.setParameter("isOwner", false);
        query.setParameter("isdelete", false);
        query.setParameter("statusid", statusid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);

        return count(query);
    }

    @Override
    public int countByTypeAndPriorityClose(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid, UUID epersontoepersonmapid, UUID statusdraft, UUID statusdispatch, UUID usertype) throws SQLException {

        Query query = createQuery(context,
                "SELECT count(distinct wp.id) FROM WorkflowProcess as wp " +
                        "LEFT join wp.workflowProcessEpeople as ep " +
                        "LEFT join ep.ePerson as p " +
                        "LEFT join wp.workflowStatus as st " +
                        "LEFT join wp.priority as priority " +
                        "LEFT join wp.workFlowProcessHistory as h " +
                        "LEFT join wp.workflowType as t " +
                        "WHERE ep.epersontoepersonmapping.id = :epersontoepersonmapid " +
                        "AND st.id NOT IN (:notDraft) " +
                        "AND (st.id IN (:statuscloseid) OR st.id IN (:statusdspatchcloseid)) " + // fixed grouping
                        "AND t.id IN (:workflowtype) " +
                        "AND p.id = :eperson " +
                        "AND wp.isdelete = :isdelete " +
                        "AND ep.isSender = :issender " +
                        "and priority.id=:priorityid " +
                        "AND ep.usertype.id = :usertype ");

        query.setParameter("notDraft", statusdraft);
        query.setParameter("statuscloseid", statusid);
        query.setParameter("statusdspatchcloseid", statusdispatch);
        query.setParameter("isdelete", false);
        query.setParameter("issender", true);
        query.setParameter("workflowtype", typeid);
        query.setParameter("eperson", epersonid);
        query.setParameter("usertype", usertype);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        query.setParameter("priorityid", priorityid);
        return count(query);
    }

    @Override
    public int countByTypeAndPriorityCloseTapal(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid, UUID epersontoepersonmapid, UUID statusdraft, UUID statusdispatch, UUID usertype) throws SQLException {
        // System.out.println("tapl............");
        Query query = createQuery(context,
                "SELECT count(distinct wp.id) FROM WorkflowProcess as wp " +
                        "LEFT join wp.workflowProcessEpeople as ep " +
                        "LEFT join ep.ePerson as p " +
                        "LEFT join wp.workflowStatus as st " +
                        "LEFT join wp.priority as priority " +
                        "LEFT join wp.workFlowProcessHistory as h " +
                        "LEFT join wp.workflowType as t " +
                        "WHERE ep.epersontoepersonmapping.id = :epersontoepersonmapid " +
                        "AND st.id NOT IN (:notDraft) " +
                        "AND (st.id IN (:statuscloseid) OR st.id IN (:statusdspatchcloseid)) " + // fixed grouping
                        "AND t.id IN (:workflowtype) " +
                        "AND p.id = :eperson " +
                        "AND wp.isdelete = :isdelete " +
                        "AND ep.isSender = :issender and priority.id=:priorityid ");

        query.setParameter("notDraft", statusdraft);
        query.setParameter("statuscloseid", statusid);
        query.setParameter("statusdspatchcloseid", statusdispatch);
        query.setParameter("isdelete", false);
        query.setParameter("issender", true);
        query.setParameter("workflowtype", typeid);
        query.setParameter("eperson", epersonid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        query.setParameter("priorityid", priorityid);
        return count(query);
    }

    @Override
    public int countByTypeAndPriorityCloseSignLatter(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid, UUID epersontoepersonmapid, UUID statusdraft, UUID statusdispatch, UUID usertype) throws SQLException {
        try {
            Query query = createQuery(context, "SELECT count(DISTINCT wp.id) FROM WorkflowProcess as wp " +
                    "join wp.workflowProcessEpeople as ep " +
                    "left join wp.workflowStatus as st " +
                    "left join wp.priority as priority " +
                    "left join wp.workflowType as t  " +
                    "left join wp.workFlowProcessDraftDetails as draft  " +
                    "left join draft.documentsignator as p  " +
                    "where ep.epersontoepersonmapping.id=:epersontoepersonmapid and draft.issinglatter=:issinglatter " +
                    "and st.id=:statusid and t.id=:workflowtype " +
                    "and p.id=:eperson and wp.isdelete=:isdelete and priority.id=:priorityid");
            query.setParameter("issinglatter", false);
            query.setParameter("eperson", epersonid);
            query.setParameter("statusid", statusid);
            query.setParameter("workflowtype", typeid);
            query.setParameter("priorityid", priorityid);
            query.setParameter("isdelete", false);
            query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
            return count(query);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int countByTypeAndPriorityNotDraft(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid, UUID epersontoepersonmapid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(DISTINCT wp.id) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st join wp.priority as pr  " +
                "join wp.workflowType as t where t.id=:typeid " +
                "and pr.id=:priorityid and  ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid " +
                "and p.id=:eperson and st.id NOT IN(:notDraft) " +
                "and wp.isdelete=:isdelete and ep.issequence=:sequence");
        query.setParameter("isOwner", false);
        query.setParameter("eperson", epersonid);
        query.setParameter("notDraft", statusid);
        query.setParameter("typeid", typeid);
        query.setParameter("isdelete", false);
        query.setParameter("priorityid", priorityid);
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);
        query.setParameter("sequence", true);
        return count(query);
    }

    @Override
    public int countByTypeAndStatusandNotDraft(Context context, UUID typeid, UUID statusid, UUID epersonid, UUID draftstatusid, UUID epersontoepersonmapid) throws SQLException {
        Query query = createQuery(context, "" +
                "SELECT count(DISTINCT wp.id) FROM WorkflowProcess as wp " +
                "join wp.workflowProcessEpeople as ep " +
                "join ep.ePerson as p  " +
                "join wp.workflowStatus as st " +
                "join wp.workflowType as t " +
                "where t.id=:typeid " +
                "and  ep.isOwner=:isOwner and ep.epersontoepersonmapping.id=:epersontoepersonmapid " +
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
        query.setParameter("epersontoepersonmapid", epersontoepersonmapid);

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
