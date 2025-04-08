/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.WorkflowProcessSenderDiaryEperson;
import org.dspace.content.dao.WorkflowProcessSenderDiaryEpersonDAO;
import org.dspace.core.AbstractHibernateDSODAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;

/**
 * Hibernate implementation of the Database Access Object interface class for the Item object.
 * This class is responsible for all database calls for the Item object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class WorkflowProcessSenderDiaryEpersonDAOImpl extends AbstractHibernateDSODAO<WorkflowProcessSenderDiaryEperson> implements WorkflowProcessSenderDiaryEpersonDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkflowProcessDefinitionDAOImpl.class);
    protected WorkflowProcessSenderDiaryEpersonDAOImpl() {
        super();
    }

    @Override
    public List<WorkflowProcessSenderDiaryEperson> getALLData(Context context, int limit) throws SQLException {
        try {
            Query query = createQuery(context, "" +
                    "SELECT DISTINCT ep FROM WorkflowProcessSenderDiaryEperson as ep where ep.ePerson.id is not null and ep.epersontoepersonmapping is null");
            query.setMaxResults(limit);
            return query.getResultList();
        }catch (Exception e){
            e.printStackTrace();
            return null;

        }
    }
}
