/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkFlowProcessInwardDetails;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
/**
 * Service interface class for the WorkFlowProcessHistory object.
 * The implementation of this class is responsible for all business logic calls for the Item object and is autowired
 * by spring
 *
 * @author ashvinmajethiya
 */

public interface WorkFlowProcessInwardDetailsService extends DSpaceObjectService<WorkFlowProcessInwardDetails>,DSpaceObjectLegacySupportService<WorkFlowProcessInwardDetails> {
    public List<WorkFlowProcessInwardDetails> findAll(Context context) throws SQLException;

    public WorkFlowProcessInwardDetails create(Context context, WorkFlowProcessInwardDetails workFlowProcessHistory) throws SQLException, AuthorizeException;
    public List<WorkFlowProcessInwardDetails> findAll(Context context, Integer limit, Integer offset) throws SQLException;
    public int countRows(Context context) throws SQLException;

    WorkFlowProcessInwardDetails getByInwardNumber(Context context,String inwardnumber) throws SQLException;
    List<WorkFlowProcessInwardDetails> searchInwardNumber(Context context, String name) throws SQLException;

}
