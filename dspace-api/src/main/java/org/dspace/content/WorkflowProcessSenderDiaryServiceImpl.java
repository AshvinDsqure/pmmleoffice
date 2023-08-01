/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.dao.WorkflowProcessDAO;
import org.dspace.content.dao.WorkflowProcessSenderDiaryDAO;
import org.dspace.content.service.WorkflowProcessSenderDiaryService;
import org.dspace.content.service.WorkflowProcessService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Service implementation for the Item object.
 * This class is responsible for all business logic calls for the Item object and is autowired by spring.
 * This class should never be accessed directly.
 *
 * @author ashivnmajethiya at atmire.com
 */
public class WorkflowProcessSenderDiaryServiceImpl extends DSpaceObjectServiceImpl<WorkflowProcessSenderDiary> implements WorkflowProcessSenderDiaryService {

    @Autowired(required = true)
    protected WorkflowProcessSenderDiaryDAO workflowProcessSenderDiaryDAO;
    protected WorkflowProcessSenderDiaryServiceImpl() {
        super();
    }

    @Override
    public WorkflowProcessSenderDiary findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }

    @Override
    public WorkflowProcessSenderDiary findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }

    @Override
    public WorkflowProcessSenderDiary find(Context context, UUID uuid) throws SQLException {
        return workflowProcessSenderDiaryDAO.findByID(context,WorkflowProcessSenderDiary.class,uuid);
    }

    @Override
    public void updateLastModified(Context context, WorkflowProcessSenderDiary dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));

    }

    @Override
    public void delete(Context context, WorkflowProcessSenderDiary dso) throws SQLException, AuthorizeException, IOException {
         workflowProcessSenderDiaryDAO.delete(context,dso);
    }

    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public WorkflowProcessSenderDiary create(Context context, WorkflowProcessSenderDiary workflowProcessSenderDiary) throws SQLException, AuthorizeException {
        WorkflowProcessSenderDiary diary = workflowProcessSenderDiaryDAO.create(context,workflowProcessSenderDiary);
        return diary; }

    @Override
    public List<WorkflowProcessSenderDiary> findAll(Context context) throws SQLException {
        return Optional.ofNullable(workflowProcessSenderDiaryDAO.findAll(context,WorkflowProcessSenderDiary.class)).orElse(new ArrayList<>());

    }

    @Override
    public WorkflowProcessSenderDiary findByWorkflowProcessSenderDiary(Context context, WorkflowProcessSenderDiary workflowProcessSenderDiary) throws SQLException {
       return workflowProcessSenderDiaryDAO.findByID(context,WorkflowProcessSenderDiary.class,UUID.randomUUID());
    }

    @Override
    public List<WorkflowProcessSenderDiary> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return  Optional.ofNullable(workflowProcessSenderDiaryDAO.findAll(context,WorkflowProcessSenderDiary.class,limit,
                offset)).orElse(new ArrayList<>());
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return workflowProcessSenderDiaryDAO.countRows(context);
    }

    @Override
    public List<WorkflowProcessSenderDiary> searchSenderDiary(Context context,String name) throws SQLException {
        return workflowProcessSenderDiaryDAO.searchSenderDiary(context,name);
    }
    @Override
    public WorkflowProcessSenderDiary findByEmailID(Context context, String emailID) throws SQLException {
        return workflowProcessSenderDiaryDAO.findByEmailID(context,emailID);
    }

    @Override
    public void update(Context context, WorkflowProcessSenderDiary workflowProcessSenderDiary) throws SQLException, AuthorizeException {

        this.workflowProcessSenderDiaryDAO.save(context, workflowProcessSenderDiary);
    }


}
