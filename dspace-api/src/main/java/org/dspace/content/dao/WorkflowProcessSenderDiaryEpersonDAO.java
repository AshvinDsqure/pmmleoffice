/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao;

import org.dspace.content.WorkflowProcessEperson;
import org.dspace.content.WorkflowProcessSenderDiaryEperson;
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
public interface WorkflowProcessSenderDiaryEpersonDAO extends DSpaceObjectLegacySupportDAO<WorkflowProcessSenderDiaryEperson> {

    List<WorkflowProcessSenderDiaryEperson> getALLData(Context context, int limit) throws SQLException;
    int getCountByEpersontoepersonmapping(Context context,  UUID eperson,UUID epersontoepersonmapping);
    int updateWorkflowProcessSenderDiaryEperson(Context context,UUID epersonfrom, UUID epersontoepersonmappingfrom,UUID epersonto, UUID epersontoepersonmappingto) throws SQLException;
    int getCountByEperson(Context context,UUID eperson);

}
