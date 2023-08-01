/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkFlowProcessDraftDetails;
import org.dspace.content.WorkFlowProcessInwardDetails;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Service interface class for the WorkFlowProcessHistory object.
 * The implementation of this class is responsible for all business logic calls for the Item object and is autowired
 * by spring
 *
 * @author ashvinmajethiya
 */

public interface WorkFlowProcessDraftDetailsService extends DSpaceObjectService<WorkFlowProcessDraftDetails>,DSpaceObjectLegacySupportService<WorkFlowProcessDraftDetails> {
    public List<WorkFlowProcessDraftDetails> findAll(Context context) throws SQLException;

    public WorkFlowProcessDraftDetails create(Context context, WorkFlowProcessDraftDetails workFlowProcessDraftDetails) throws SQLException, AuthorizeException;
    public List<WorkFlowProcessDraftDetails> findAll(Context context, Integer limit, Integer offset) throws SQLException;
    public int countRows(Context context) throws SQLException;
    WorkFlowProcessDraftDetails getbyDocumentsignator(Context context, UUID workflowprocessid) throws SQLException;

}
