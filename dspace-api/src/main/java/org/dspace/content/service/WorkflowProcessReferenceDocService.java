/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.WorkflowProcess;
import org.dspace.content.WorkflowProcessReferenceDoc;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface class for the Item object.
 * The implementation of this class is responsible for all business logic calls for the Item object and is autowired
 * by spring
 *
 * @author kevinvandevelde at atmire.com
 */
public interface WorkflowProcessReferenceDocService extends DSpaceObjectService<WorkflowProcessReferenceDoc>, DSpaceObjectLegacySupportService<WorkflowProcessReferenceDoc> {

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

    public WorkflowProcessReferenceDoc create(Context context, WorkflowProcessReferenceDoc workflowProcess) throws SQLException, AuthorizeException;
    /**
     * get All WorkflowProcess
     * set are included. The order of the list is indeterminate.
     *
     * @param context DSpace context object
     * @return an iterator over the items in the archive.
     * @throws SQLException if database error
     */
    public List<WorkflowProcessReferenceDoc> findAll(Context context) throws SQLException;
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
    public List<WorkflowProcessReferenceDoc> findAll(Context context, Integer limit, Integer offset) throws SQLException;
    public int countDocumentByType(Context context , UUID drafttypeid) throws SQLException;
    public List<WorkflowProcessReferenceDoc> getDocumentByType(Context context , UUID drafttypeid, Integer offset, Integer limit) throws SQLException;
    public int countDocumentByItemid(Context context , UUID itemid) throws SQLException;
    public List<WorkflowProcessReferenceDoc> getDocumentByItemid(Context context , UUID itemid, Integer offset, Integer limit) throws SQLException;
    public List<WorkflowProcessReferenceDoc> getDocumentByworkflowprocessid(Context context ,UUID workflowprocessid) throws SQLException;
    public List<WorkflowProcessReferenceDoc> getDocumentBySignitore(Context context ,UUID signitoreid, UUID drafttypeid) throws SQLException;

    public WorkflowProcessReferenceDoc findbydrafttypeandworkflowprocessAndItem(Context context,UUID item,UUID workflowprocess,UUID drafttypeid) throws SQLException;
    public List<WorkflowProcessReferenceDoc> getDocumentByItemid(Context context ,UUID drafttypeid, UUID itemid) throws SQLException;



}
