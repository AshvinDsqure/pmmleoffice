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
import org.dspace.content.WorkflowProcessDefinition;
import org.dspace.content.WorkflowProcessEperson;
import org.dspace.core.Context;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
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
public interface WorkflowProcessEpersonService extends DSpaceObjectService<WorkflowProcessEperson>, DSpaceObjectLegacySupportService<WorkflowProcessEperson> {

    /**
     * Create a new workflowProcess withAuthorisation is done
     * inside of this method.
     *
     * @param context DSpace context object
     * @param workflowProcessEperson in progress workspace item
     *
     * @return the newly created item
     * @throws SQLException if database error
     * @throws AuthorizeException if authorization error
     */

    public WorkflowProcessEperson create(Context context, WorkflowProcessEperson workflowProcessEperson) throws SQLException, AuthorizeException;
    /**
     * get All WorkflowProcess
     * set are included. The order of the list is indeterminate.
     *
     * @param context DSpace context object
     * @return an iterator over the items in the archive.
     * @throws SQLException if database error
     */
    public List<WorkflowProcessEperson> findAll(Context context) throws SQLException;
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
    public List<WorkflowProcessEperson> findAll(Context context, Integer limit, Integer offset) throws SQLException;
    public void sendEmail(Context context, String recipientEmail, String recipientName, String subject) throws IOException, MessagingException, SQLException, AuthorizeException;
    List<WorkflowProcessEperson> getALLData(Context context,int limit) throws SQLException;
    int getCountByEpersontoepersonmapping(Context context,UUID eperson, UUID epersontoepersonmapping);
    int updateWorkflowProcessEperson(Context context,UUID epersonfrom, UUID epersontoepersonmappingfrom,UUID epersonto, UUID epersontoepersonmappingto) throws SQLException;
    int getCountByEperson(Context context,UUID eperson);

}
