/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkFlowProcessMaster;
import org.dspace.content.WorkFlowProcessMasterValue;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Service interface class for the LatterCategory object.
 * The implementation of this class is responsible for all business logic calls for the Item object and is autowired
 * by spring
 *
 * @author ashvinmajethiya
 */


public interface WorkFlowProcessMasterValueService extends DSpaceObjectService<WorkFlowProcessMasterValue>,DSpaceObjectLegacySupportService<WorkFlowProcessMasterValue> {
    public List<WorkFlowProcessMasterValue> findAll(Context context) throws SQLException;
    public List<WorkFlowProcessMasterValue> searchByDepartment(Context context, UUID masterid, String search) throws SQLException;

    public WorkFlowProcessMasterValue create(Context context, WorkFlowProcessMasterValue workFlowProcessMasterValue) throws SQLException, AuthorizeException;
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
    public List<WorkFlowProcessMasterValue> findAll(Context context, Integer limit, Integer offset) throws SQLException;
    public int countRows(Context context) throws SQLException;

    List<WorkFlowProcessMasterValue> findByType(Context context,String type,Integer offset,Integer limit)throws SQLException;
    WorkFlowProcessMasterValue findByName(Context context, String name, WorkFlowProcessMaster workFlowProcessMaster)throws SQLException;
    public int  countfindByType(Context context,String type)throws SQLException;
    public int  countfindByMasterID(Context context,UUID masterid)throws SQLException;
}
