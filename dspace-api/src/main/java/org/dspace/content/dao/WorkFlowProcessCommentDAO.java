/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content.dao;

import org.dspace.content.WorkFlowProcessComment;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface WorkFlowProcessCommentDAO extends DSpaceObjectLegacySupportDAO<WorkFlowProcessComment>{
    int countRows(Context context) throws SQLException;

    List<WorkFlowProcessComment> getComments(Context context, UUID workflowprocessid)  throws SQLException;
    public int countComment(Context context,UUID workflowprocessid) throws SQLException;

    public WorkFlowProcessComment findCommentByworkflowprocessidAndissavedrafttrue(Context context,UUID workflowprocessid) throws SQLException;
    public WorkFlowProcessComment findCommentBySubmiterandWorkflowProcessID(Context context,UUID submiter,UUID workflowprocessid) throws SQLException;

}
