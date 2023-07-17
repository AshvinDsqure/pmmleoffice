/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkFlowProcessOutwardDetails;
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

public interface WorkFlowProcessOutwardDetailsService extends DSpaceObjectService<WorkFlowProcessOutwardDetails>,DSpaceObjectLegacySupportService<WorkFlowProcessOutwardDetails> {
    public List<WorkFlowProcessOutwardDetails> findAll(Context context) throws SQLException;

    public WorkFlowProcessOutwardDetails create(Context context, WorkFlowProcessOutwardDetails workFlowProcessHistory) throws SQLException, AuthorizeException;
    public List<WorkFlowProcessOutwardDetails> findAll(Context context, Integer limit, Integer offset) throws SQLException;
    public int countRows(Context context) throws SQLException;
    WorkFlowProcessOutwardDetails getByOutwardNumber(Context context, String outwardnumber) throws SQLException;


}
