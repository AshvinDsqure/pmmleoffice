/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content.dao;

import org.dspace.content.WorkFlowProcessMaster;
import org.dspace.content.WorkFlowProcessMasterValue;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface WorkFlowProcessMasterValueDAO extends DSpaceObjectLegacySupportDAO<WorkFlowProcessMasterValue>{
    int countRows(Context context) throws SQLException;

    public List<WorkFlowProcessMasterValue> findByType(Context context, String mastername,Integer offset,Integer limit) throws SQLException;

    WorkFlowProcessMasterValue findByName(Context context, String name, WorkFlowProcessMaster workFlowProcessMaster)throws SQLException;
    public int  countfindByType(Context context,String type)throws SQLException;

    public List<WorkFlowProcessMasterValue> searchByDepartment(Context context,UUID masterid, String search) throws SQLException;

}
