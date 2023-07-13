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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Database Access Object interface class for the Item object.
 * The implementation of this class is responsible for all database calls for the Item object and is autowired by spring
 * This class should only be accessed from a single service and should never be exposed outside of the API
 *
 * @author kevinvandevelde at atmire.com
 */
public interface WorkflowProcessDAO extends DSpaceObjectLegacySupportDAO<WorkflowProcess> {
    List<WorkflowProcess> findNotCompletedByUser(Context context, UUID eperson, UUID statusid,UUID draftid, Integer offset, Integer limit) throws SQLException;
    List<WorkflowProcess> findDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid,UUID statusdraft, Integer offset, Integer limit) throws SQLException;
    int countfindDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid,UUID statusdraft) throws SQLException;
    List<WorkflowProcess> getHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid, Integer offset, Integer limit) throws SQLException;

    int countgetHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid) throws SQLException;

    List<WorkflowProcess> getHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid, Integer offset, Integer limit) throws SQLException;

    int countgetHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid) throws SQLException;

    int countfindNotCompletedByUser(Context context, UUID eperson, UUID statusid,UUID draftid) throws SQLException;

    int getCountByType(Context context,UUID typeid) throws SQLException;
    WorkflowProcess getNoteByItemsid(Context context, UUID itemid) throws SQLException;
    List<WorkflowProcess> Filter(Context context, HashMap<String,String> perameter ,Integer offset, Integer limit) throws SQLException;

    List<WorkflowProcess> findReferList(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid,UUID statusdraft, Integer offset, Integer limit) throws SQLException;
    int countRefer(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid,UUID statusdraft) throws SQLException;

}
