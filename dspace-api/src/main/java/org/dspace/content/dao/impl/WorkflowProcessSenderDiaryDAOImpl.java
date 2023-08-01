/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.WorkflowProcessNote;
import org.dspace.content.WorkflowProcessSenderDiary;
import org.dspace.content.WorkflowProcessSenderDiary_;
import org.dspace.content.dao.WorkflowProcessNoteDAO;
import org.dspace.content.dao.WorkflowProcessSenderDiaryDAO;
import org.dspace.core.AbstractHibernateDSODAO;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.storedcomponents.CollectionRole;

import javax.persistence.Column;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Hibernate implementation of the Database Access Object interface class for the WorkspaceItem object.
 * This class is responsible for all database calls for the WorkspaceItem object and is autowired by Spring.
 * This class should never be accessed directly.
 *
 * @author AshvinMajethiya at atmire.com
 */
public class WorkflowProcessSenderDiaryDAOImpl extends AbstractHibernateDSODAO<WorkflowProcessSenderDiary> implements WorkflowProcessSenderDiaryDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkflowProcessNoteDAOImpl.class);

    protected WorkflowProcessSenderDiaryDAOImpl() {
        super();
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM WorkflowProcessSenderDiary"));

    }

    @Override
    public WorkflowProcessSenderDiary findByEmailID(Context context, String search) throws SQLException {
        try {
            StringBuilder queryBuilder = new StringBuilder("SELECT s from  WorkflowProcessSenderDiary as s where email =:name");
            Query query = createQuery(context, queryBuilder.toString());
            query.setParameter("name",search);
            return (WorkflowProcessSenderDiary) query.getSingleResult();
        } catch (Exception e) {
            System.out.println("in error " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<WorkflowProcessSenderDiary> searchSenderDiary(Context context, String name) throws SQLException {
        try {
            Query query = createQuery(context,"SELECT s from  WorkflowProcessSenderDiary as s where lower(s.sendername)  like :name ");
            query.setParameter("name","%"+name.toLowerCase()+"%");
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("in error " + e.getMessage());
            return null;
        }
    }
}