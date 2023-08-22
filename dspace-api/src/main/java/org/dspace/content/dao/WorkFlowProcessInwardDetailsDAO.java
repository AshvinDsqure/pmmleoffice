/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content.dao;

import org.dspace.content.WorkFlowProcessInwardDetails;
import org.dspace.content.WorkFlowProcessOutwardDetails;
import org.dspace.content.WorkflowProcessSenderDiary;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;

public interface WorkFlowProcessInwardDetailsDAO extends DSpaceObjectLegacySupportDAO<WorkFlowProcessInwardDetails>{
    int countRows(Context context) throws SQLException;

    WorkFlowProcessInwardDetails getByInwardNumber(Context context,String inwardnumber) throws SQLException;
    List<WorkFlowProcessInwardDetails> searchInwardNumber(Context context, String name) throws SQLException;
}
