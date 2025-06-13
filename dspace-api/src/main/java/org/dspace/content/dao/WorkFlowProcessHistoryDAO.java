/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content.dao;

import org.dspace.content.WorkFlowProcessComment;
import org.dspace.content.WorkFlowProcessHistory;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface WorkFlowProcessHistoryDAO extends DSpaceObjectLegacySupportDAO<WorkFlowProcessHistory>{
    int countRows(Context context) throws SQLException;
    List<WorkFlowProcessHistory> getHistory(Context context, UUID workflowprocessid)  throws SQLException;
    public int countHistory(Context context,UUID workflowprocessid) throws SQLException;
    List<WorkFlowProcessHistory> getHistory(Context context,int limit)  throws SQLException;

}
