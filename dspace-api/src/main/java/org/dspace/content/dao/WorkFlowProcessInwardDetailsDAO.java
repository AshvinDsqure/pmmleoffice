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
import org.dspace.core.Context;

import java.sql.SQLException;

public interface WorkFlowProcessInwardDetailsDAO extends DSpaceObjectLegacySupportDAO<WorkFlowProcessInwardDetails>{
    int countRows(Context context) throws SQLException;

    WorkFlowProcessInwardDetails getByInwardNumber(Context context,String inwardnumber) throws SQLException;
}
