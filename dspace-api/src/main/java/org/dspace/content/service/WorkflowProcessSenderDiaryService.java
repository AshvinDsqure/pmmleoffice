/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public interface WorkflowProcessSenderDiaryService extends DSpaceObjectService<WorkflowProcessSenderDiary>, DSpaceObjectLegacySupportService<WorkflowProcessSenderDiary> {

    public WorkflowProcessSenderDiary create(Context context, WorkflowProcessSenderDiary workflowProcessSenderDiary) throws SQLException, AuthorizeException;

    public List<WorkflowProcessSenderDiary> findAll(Context context) throws SQLException;
    public WorkflowProcessSenderDiary findByWorkflowProcessSenderDiary(Context context, WorkflowProcessSenderDiary workflowProcessSenderDiary) throws SQLException;
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
    public List<WorkflowProcessSenderDiary> findAll(Context context, Integer limit, Integer offset) throws SQLException;
    public int countRows(Context context) throws SQLException;
    List<WorkflowProcessSenderDiary> searchSenderDiary(Context context, String name) throws SQLException;

    public WorkflowProcessSenderDiary findByEmailID(Context context, String emailID) throws SQLException;
}
