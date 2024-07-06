/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkflowProcess;
import org.dspace.content.WorkflowProcessReferenceDocVersion;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Service interface class for the Item object.
 * The implementation of this class is responsible for all business logic calls for the Item object and is autowired
 * by spring
 *
 * @author kevinvandevelde at atmire.com
 */
public interface WorkflowProcessReferenceDocVersionService extends DSpaceObjectService<WorkflowProcessReferenceDocVersion>, DSpaceObjectLegacySupportService<WorkflowProcessReferenceDocVersion> {

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

    public WorkflowProcessReferenceDocVersion create(Context context, WorkflowProcessReferenceDocVersion workflowProcess) throws SQLException, AuthorizeException;
    /**
     * get All WorkflowProcess
     * set are included. The order of the list is indeterminate.
     *
     * @param context DSpace context object
     * @return an iterator over the items in the archive.
     * @throws SQLException if database error
     */
    public List<WorkflowProcessReferenceDocVersion> findAll(Context context) throws SQLException;
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
    public List<WorkflowProcessReferenceDocVersion> findAll(Context context, Integer limit, Integer offset) throws SQLException;
    public int countRow(Context context) throws SQLException;
    public int countDocumentID(Context context ,UUID documentid) throws SQLException;
    public List<WorkflowProcessReferenceDocVersion> getDocVersionBydocumentID(Context context ,UUID documentid,Integer offset, Integer limit) throws SQLException;
    WorkflowProcessReferenceDocVersion findByCreator(Context context,UUID uuid,UUID documentid) throws SQLException;
}
