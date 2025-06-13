/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkFlowProcessComment;
import org.dspace.content.WorkFlowProcessHistory;
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

public interface WorkFlowProcessHistoryService extends DSpaceObjectService<WorkFlowProcessHistory>,DSpaceObjectLegacySupportService<WorkFlowProcessHistory> {
    public List<WorkFlowProcessHistory> findAll(Context context) throws SQLException;

    public WorkFlowProcessHistory create(Context context, WorkFlowProcessHistory workFlowProcessHistory) throws SQLException, AuthorizeException;
    public List<WorkFlowProcessHistory> findAll(Context context, Integer limit, Integer offset) throws SQLException;
    public int countRows(Context context) throws SQLException;
    public int countHistory(Context contex,UUID workflowprocessid) throws SQLException;

    List<WorkFlowProcessHistory> getHistory(Context context, UUID workflowprocessid)  throws SQLException;

    List<WorkFlowProcessHistory> getHistory(Context context,int limit)  throws SQLException;


}
