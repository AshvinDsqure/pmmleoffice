/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

/**
 * Service interface class for the Item object.
 * The implementation of this class is responsible for all business logic calls for the Item object and is autowired
 * by spring
 *
 * @author kevinvandevelde at atmire.com
 */
public interface WorkflowProcessService extends DSpaceObjectService<WorkflowProcess>, DSpaceObjectLegacySupportService<WorkflowProcess> {

    /**
     * Create a new workflowProcess withAuthorisation is done
     * inside of this method.
     *
     * @param context DSpace context object
     * @param workflowProcess in progress workspace item
     *
     * @return the newly created item
     * @throws SQLException if database error
     * @throws AuthorizeException if authorization error
     */

    public WorkflowProcess create(Context context, WorkflowProcess workflowProcess) throws SQLException, AuthorizeException;
    /**
     * get All WorkflowProcess
     * set are included. The order of the list is indeterminate.
     *
     * @param context DSpace context object
     * @return an iterator over the items in the archive.
     * @throws SQLException if database error
     */
    public List<WorkflowProcess> findAll(Context context) throws SQLException;
    /**
     * Get All WorkflowProcess based on limit and offset
     * set are included. The order of the list is indeterminate.
     *
     * @param context DSpace context object
     * @param limit   limit
     * @param offset  offset
     * @return an iterator over the items in the archive.
     * @throws SQLException if database error
     */
    public List<WorkflowProcess> findAll(Context context, Integer limit, Integer offset) throws SQLException;
    List<WorkflowProcess> findNotCompletedByUser(Context context, UUID eperson,UUID statusid,UUID draftid,Integer offset, Integer limit)throws SQLException;
    int countfindNotCompletedByUser(Context context, UUID eperson,UUID statusid,UUID draftid)throws SQLException;

    List<WorkflowProcess> getHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid, Integer offset, Integer limit) throws SQLException;

    int countgetHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid) throws SQLException;

    List<WorkflowProcess> getHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid, Integer offset, Integer limit) throws SQLException;

    int countgetHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid) throws SQLException;
    /**
     * Store Bbitstrea from  WorkflowProcessRefranceDoc
     *
     *
     * @param context DSpace context object
     * @param  item  item
     * @param workflowProcessReferenceDoc  workflowProcessReferenceDoc
     * @throws SQLException,AuthorizeException if database error
     */
    public  void  storeWorkFlowMataDataTOBitsream(Context context,WorkflowProcessReferenceDoc workflowProcessReferenceDoc,Item item) throws SQLException, AuthorizeException ;
    public  void  storeWorkFlowMataDataTOBitsream(Context context,WorkflowProcessReferenceDoc workflowProcessReferenceDoc) throws SQLException, AuthorizeException ;
    List<WorkflowProcess> findDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid,UUID statusdraft,Integer offset, Integer limit) throws SQLException;
    int countfindDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid,UUID statusdraft) throws SQLException;
    WorkflowProcess getNoteByItemsid(Context context, UUID itemid) throws SQLException;
    int getCountByType(Context context,UUID typeid) throws SQLException;

    List<WorkflowProcess> Filter(Context context, HashMap<String,String> perameter , Integer offset, Integer limit) throws SQLException;

    List<WorkflowProcess> findReferList(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid,UUID statusdraft, Integer offset, Integer limit) throws SQLException;
    int countRefer(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid,UUID statusdraft) throws SQLException;
    int countByTypeAndStatus(Context context,UUID typeid,UUID statusid,UUID epersonid) throws SQLException;
    int countByTypeAndPriority(Context context,UUID typeid,UUID priorityid,UUID epersonid) throws SQLException;

}
