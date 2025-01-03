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
import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
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
    List<WorkflowProcess> findNotCompletedByUserDraft(Context context, UUID eperson,UUID statusid,UUID draftid,Integer offset, Integer limit)throws SQLException;
    List<WorkflowProcess> findCompletedFlow(Context context, UUID eperson, UUID statusid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException;
    int countfindCompletedFlow(Context context, UUID eperson, UUID statusid, UUID workflowtypeid) throws SQLException;
    int countfindNotCompletedByUser(Context context, UUID eperson,UUID statusid,UUID draftid)throws SQLException;
    int countfindNotCompletedByUserDraft(Context context, UUID eperson,UUID statusid,UUID draftid)throws SQLException;
    List<WorkflowProcess> getHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid, Integer offset, Integer limit) throws SQLException;
    int countgetHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid) throws SQLException;
    int countfilterInwarAndOutWard(Context context, HashMap<String,String> perameter ,Integer offset, Integer limit) throws SQLException;

    List<WorkflowProcess> getHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid,UUID workflowtypeid, Integer offset, Integer limit) throws SQLException;

    int countgetHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid,UUID workflowtypeid) throws SQLException;
    /**
     * Store Bbitstrea from  WorkflowProcessRefranceDoc
     *
     *
     * @param context DSpace context object
     * @param  item  item
     * @param workflowProcessReferenceDoc  workflowProcessReferenceDoc
     * @throws SQLException,AuthorizeException if database error
     */
    public   void   storeWorkFlowMataDataTOBitsream(Context context,WorkflowProcessReferenceDoc workflowProcessReferenceDoc,Item item) throws SQLException, AuthorizeException ;
    public  void  storeWorkFlowMataDataTOBitsream(Context context,WorkflowProcessReferenceDoc workflowProcessReferenceDoc) throws SQLException, AuthorizeException ;
    List<WorkflowProcess> findDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid,UUID statusdraft,Integer offset, Integer limit) throws SQLException;
    int countfindDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid,UUID statusdraft) throws SQLException;
    WorkflowProcess getNoteByItemsid(Context context, UUID itemid) throws SQLException;
    int getCountByType(Context context,UUID typeid,Integer version) throws SQLException;
    List<WorkflowProcess> Filter(Context context, HashMap<String,String> perameter , Integer offset, Integer limit) throws SQLException;
    List<WorkflowProcess> findReferList(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid,UUID statusdraft, Integer offset, Integer limit) throws SQLException;
    int countRefer(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid,UUID statusdraft) throws SQLException;
    int countByTypeAndStatus(Context context,UUID typeid,UUID statusid,UUID epersonid) throws SQLException;
    int countByTypeAndStatusNotwoner(Context context, UUID typeid, UUID statusid, UUID epersonid) throws SQLException;
    int countByTypeAndStatusandNotDraft(Context context, UUID typeid, UUID statusid, UUID epersonid,UUID draftstatus) throws SQLException;

    int countByTypeAndPriorityNotDraft(Context context, UUID typeid, UUID priorityid, UUID epersonid,UUID statusid) throws SQLException;
    int countByTypeAndPriorityCreted(Context context, UUID typeid, UUID priorityid, UUID epersonid,UUID statusid) throws SQLException;
    int countByTypeAndPriorityClose(Context context, UUID typeid, UUID priorityid, UUID epersonid,UUID statusid) throws SQLException;

    int countByTypeAndPriority(Context context,UUID typeid,UUID priorityid,UUID epersonid,UUID statusid) throws SQLException;
    public void sendEmail(Context context, HttpServletRequest request, String recipientEmail, String recipientName,String subject, List<Bitstream> bitstream,List<String> recipientEmails,String body) throws IOException, MessagingException, SQLException, AuthorizeException;
    List<WorkflowProcess> searchSubjectByWorkflowTypeandSubject(Context context,UUID workflowtypeid, String subject) throws SQLException;
    List<WorkflowProcess> filterInwarAndOutWard(Context context, HashMap<String,String> perameter ,Integer offset, Integer limit) throws SQLException;
    List<WorkflowProcess> getWorkflowAfterNoteApproved(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException;
    int getCountWorkflowAfterNoteApproved(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID workflowtypeid) throws SQLException;

    List<WorkflowProcess> searchByFilenumberOrTapaleNumber(Context context, HashMap<String,String> perameter ,Integer offset, Integer limit) throws SQLException;

    List<WorkflowProcess> sentTapal(Context context, UUID eperson, UUID statusid,UUID workflowtypeid,UUID statuscloseid,  Integer offset, Integer limit) throws SQLException;
    int countTapal(Context context, UUID eperson, UUID statusid,UUID workflowtypeid,UUID statuscloseid) throws SQLException;
    List<WorkflowProcess> closeTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid,UUID workflowtypeid,  Integer offset, Integer limit) throws SQLException;
    int countCloseTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid,UUID workflowtypeid) throws SQLException;
    List<WorkflowProcess> acknowledgementTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid,UUID workflowtypeid,  Integer offset, Integer limit) throws SQLException;
    int countacknowledgementTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid,UUID workflowtypeid) throws SQLException;
    List<WorkflowProcess> dispatchTapal(Context context, UUID eperson, UUID statusdraftid, UUID statusdspachcloseid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException;

    int countdispatchTapal(Context context, UUID eperson, UUID statusdraftid, UUID statusdspachcloseid, UUID workflowtypeid) throws SQLException;
    List<WorkflowProcess> parkedFlow(Context context, UUID eperson, UUID statusdraftid, UUID statusparkedid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException;
    int countparkedFlow(Context context, UUID eperson, UUID statusdraftid, UUID statusparkedid, UUID workflowtypeid) throws SQLException;

    public void CallSap(Context context);

}
