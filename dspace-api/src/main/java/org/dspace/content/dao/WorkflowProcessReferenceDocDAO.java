/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao;

import org.dspace.content.WorkflowProcess;
import org.dspace.content.WorkflowProcessReferenceDoc;
import org.dspace.content.WorkflowProcessReferenceDocVersion;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Database Access Object interface class for the Item object.
 * The implementation of this class is responsible for all database calls for the Item object and is autowired by spring
 * This class should only be accessed from a single service and should never be exposed outside of the API
 *
 * @author kevinvandevelde at atmire.com
 */
public interface WorkflowProcessReferenceDocDAO extends DSpaceObjectLegacySupportDAO<WorkflowProcessReferenceDoc> {

    public int countDocumentByType(Context context , UUID drafttypeid) throws SQLException;
    public List<WorkflowProcessReferenceDoc> getDocumentByType(Context context , UUID drafttypeid, Integer offset, Integer limit) throws SQLException;
    public int countDocumentByItemid(Context context ,UUID drafttypeid, UUID itemid) throws SQLException;
    public List<WorkflowProcessReferenceDoc> getDocumentByItemid(Context context ,UUID drafttypeid, UUID itemid, Integer offset, Integer limit) throws SQLException;
    public List<WorkflowProcessReferenceDoc> getDocumentByItemid(Context context ,UUID drafttypeid, UUID itemid) throws SQLException;

    public List<WorkflowProcessReferenceDoc> getDocumentByworkflowprocessid(Context context ,UUID workflowprocessid) throws SQLException;
    public List<WorkflowProcessReferenceDoc> getDocumentBySignitore(Context context ,UUID signitoreid,UUID drafttypeuuid) throws SQLException;

    public WorkflowProcessReferenceDoc findbydrafttypeandworkflowprocessAndItem(Context context,UUID item,UUID workflowprocess,UUID drafttypeid) throws SQLException;
}
