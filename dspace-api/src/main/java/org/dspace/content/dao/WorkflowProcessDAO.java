/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao;

import org.dspace.content.*;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.*;

/**
 * Database Access Object interface class for the Item object.
 * The implementation of this class is responsible for all database calls for the Item object and is autowired by spring
 * This class should only be accessed from a single service and should never be exposed outside of the API
 *
 * @author kevinvandevelde at atmire.com
 */
public interface WorkflowProcessDAO extends DSpaceObjectLegacySupportDAO<WorkflowProcess> {
    List<WorkflowProcess> findNotCompletedByUser(Context context, UUID eperson, UUID statusid, UUID draftid,UUID epersontoepersonmapid,HashMap<String, String> perameter,MetadataField metadatafield, Integer offset, Integer limit) throws SQLException;
    List<WorkflowProcess> findNotCompletedByUserDraft(Context context, UUID eperson, UUID statusid, UUID draftid,UUID epersontoepersonmapid,HashMap<String, String> perameter,MetadataField metadatafield, Integer offset, Integer limit) throws SQLException;
    int countfindNotCompletedByUserDraft(Context context, UUID eperson, UUID statusid, UUID draftid,UUID epersontoepersonmapid) throws SQLException;

    List<WorkflowProcess> findCompletedFlow(Context context, UUID eperson, UUID statusid, UUID workflowtypeid,UUID epersontoepersonmapid, Integer offset, Integer limit) throws SQLException;
    int countfindCompletedFlow(Context context, UUID eperson, UUID statusid, UUID workflowtypeid,UUID epersontoepersonmapid) throws SQLException;

    List<WorkflowProcess> findDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft,UUID epersontoepersonmapping,HashMap<String, String> perameter,MetadataField metadatafield,UUID statusReject, Integer offset, Integer limit) throws SQLException;
    int countfindDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft,UUID epersontoepersonmapping,MetadataField metadatafield,UUID statusReject) throws SQLException;

    List<WorkflowProcess> findFilePendingDueDate(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft,UUID epersontoepersonmapping,HashMap<String, String> perameter,MetadataField metadatafield, Integer offset, Integer limit) throws SQLException;

    int countfindFilePendingDueDate(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft,UUID epersontoepersonmapping) throws SQLException;


    List<WorkflowProcess> getHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid,UUID epersontoepersonmapid,MetadataField metadatafield, Integer offset, Integer limit) throws SQLException;

    int countgetHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid,UUID epersontoepersonmapid) throws SQLException;

    List<WorkflowProcess> getHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid, UUID workflowtypeid,UUID epersontoepersonmapid,HashMap<String, String> perameter,MetadataField metadatafield,Integer offset, Integer limit) throws SQLException;

    int countgetHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid,UUID workflowtypeid,UUID epersontoepersonmapid) throws SQLException;

    int countfindNotCompletedByUser(Context context, UUID eperson, UUID statusid, UUID draftid,UUID epersontoepersonmapid) throws SQLException;

    int getCountByType(Context context, UUID typeid,Integer version) throws SQLException;

    WorkflowProcess getNoteByItemsid(Context context, UUID itemid) throws SQLException;

    List<WorkflowProcess> Filter(Context context, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException;

    List<WorkflowProcess> filterInwarAndOutWard(Context context,MetadataField metadataField, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException;
    List<WorkflowProcess> getWorkflowAfterNoteApproved(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID workflowtypeid,UUID epersontoepersonmapid,HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException;
    int getCountWorkflowAfterNoteApproved(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID workflowtypeid,UUID epersontoepersonmapid) throws SQLException;

    List<WorkflowProcess> searchByFileNumberOrTapalNumber(Context context,MetadataField metadataField,MetadataField metadataFieldsubject, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException;
    int countfilterInwarAndOutWard(Context context,MetadataField metadataField, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException;
    List<WorkflowProcess> findReferList(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft, Integer offset, Integer limit) throws SQLException;

    int countRefer(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft) throws SQLException;

    int countByTypeAndStatus(Context context, UUID typeid, UUID statusid, UUID epersonid,UUID epersontoepersonmapid) throws SQLException;

    int countByTypeAndPriority(Context context, UUID typeid, UUID priorityid, UUID epersonid,UUID statusid,UUID epersontoepersonmapid) throws SQLException;
    int countByTypeAndPriorityDraftTapal(Context context, UUID typeid, UUID priorityid, UUID epersonid,UUID statusid,UUID epersontoepersonmapid) throws SQLException;

    int countByTypeAndPriorityPark(Context context, UUID typeid, UUID priorityid, UUID epersonid,UUID statusid,UUID epersontoepersonmapid,UUID statusdraftid) throws SQLException;

    int countByTypeAndPriorityCreted(Context context, UUID typeid, UUID priorityid, UUID epersonid,UUID statusid,UUID epersontoepersonmapid) throws SQLException;
    int countByTypeAndPriorityClose(Context context, UUID typeid, UUID priorityid, UUID epersonid,UUID statusid,UUID epersontoepersonmapid,UUID statusdraft,UUID statusdispatch,UUID usertype) throws SQLException;
    int countByTypeAndPriorityCloseTapal(Context context, UUID typeid, UUID priorityid, UUID epersonid,UUID statusid,UUID epersontoepersonmapid,UUID statusdraft,UUID statusdispatch,UUID usertype) throws SQLException;

    int countByTypeAndPriorityCloseSignLatter(Context context, UUID typeid, UUID priorityid, UUID epersonid,UUID statusid,UUID epersontoepersonmapid,UUID statusdraft,UUID statusdispatch,UUID usertype) throws SQLException;

    int countByTypeAndPriorityNotDraft(Context context, UUID typeid, UUID priorityid, UUID epersonid,UUID statusid,UUID epersontoepersonmapid) throws SQLException;
    int countByTypeAndStatusandNotDraft(Context context, UUID typeid, UUID statusid, UUID epersonid,UUID draftstatus,UUID epersontoepersonmapid) throws SQLException;

    List<WorkflowProcess> searchSubjectByWorkflowTypeandSubject(Context context, UUID workflowtypeid, String subject) throws SQLException;

    List<WorkflowProcess> sentTapal(Context context, UUID eperson, UUID statusid, UUID workflowtypeid,UUID statuscloseid,UUID epersontoepersonmapid,HashMap<String, String> perameter,MetadataField metadatafield, Integer offset, Integer limit) throws SQLException;

    int countTapal(Context context, UUID eperson, UUID statusid, UUID workflowtypeid,UUID statusidclose,UUID epersontoepersonmapid,MetadataField metadatafield,HashMap<String, String> perameter) throws SQLException;

    List<WorkflowProcess> closeTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid,UUID statusdspatchcloseid, UUID workflowtypeid,UUID epersontoepersonmapid,UUID usertype, HashMap<String, String> perameter,MetadataField metadatafield,Integer offset, Integer limit) throws SQLException;

    int countCloseTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid,UUID statusdspatchcloseid, UUID workflowtypeid,UUID epersontoepersonmapid,UUID usertype) throws SQLException;

    List<WorkflowProcess> acknowledgementTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException;

    int countacknowledgementTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid, UUID workflowtypeid) throws SQLException;

    List<WorkflowProcess> dispatchTapal(Context context, UUID eperson, UUID statusdraftid, UUID statusdspachcloseid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException;

    int countdispatchTapal(Context context, UUID eperson, UUID statusdraftid, UUID statusdspachcloseid, UUID workflowtypeid) throws SQLException;
    List<WorkflowProcess> parkedFlow(Context context, UUID eperson, UUID statusdraftid, UUID statusparkedid, UUID workflowtypeid,UUID epersontoepersonmapid,HashMap<String, String> perameter,MetadataField metadatafield, Integer offset, Integer limit) throws SQLException;
    int countparkedFlow(Context context, UUID eperson, UUID statusdraftid, UUID statusparkedid, UUID workflowtypeid,UUID epersontoepersonmapid) throws SQLException;
    public List<Object[]> filterDepartmentWiseCount(Context context, HashMap<String, String> parameter,String startdate,String endDate, Integer offset, Integer limit) throws SQLException;
    public List<Object[]> filterDepartmentWiseCountDownload(Context context, HashMap<String, String> parameter,String startdate,String endDate) throws SQLException;
    public List<Object[]> withinDepartmentDownload(Context context, HashMap<String, String> parameter,String startdate,String endDate) throws SQLException;
    public List<Object[]> outerDepartmentDownload(Context context, HashMap<String, String> parameter,String startdate,String endDate) throws SQLException;

    public List<Object[]> stagewithinDepartmentDownload(Context context, HashMap<String, String> parameter,String startdate,String endDate) throws SQLException;
    public List<Object[]> stageouterDepartmentDownload(Context context, HashMap<String, String> parameter,String startdate,String endDate) throws SQLException;
    public List<Object[]> NoOfFileCreatedAndClose(Context context, HashMap<String, String> parameter,String startdate,String endDate) throws SQLException;
    public List<Object[]> NoOfTapalCreatedAndClose(Context context, HashMap<String, String> parameter,String startdate,String endDate) throws SQLException;

    public int getNextInwardNumber(Context context) throws SQLException;
    public int getNextFileNumber(Context context) throws SQLException;
    public int countTapalPrkedDateRange(Context context, String startdate, String enddate) throws SQLException;
    public int countTapalStageWISE(Context context, String startdate, String enddate) throws SQLException;

    public List<Object[]> getActiveAndDActiveUsers(Context context, String flag) throws SQLException;
    public List<Object[]> getFileByDayTakenAndDepartment(Context context, Integer daytaken,String flag,String startdate, String enddate) throws SQLException;

    public List<String> getDraftMigration(Context context, Integer limit) throws SQLException;

}
