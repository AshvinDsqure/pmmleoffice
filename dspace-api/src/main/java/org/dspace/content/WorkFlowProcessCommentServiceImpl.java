/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content;

import org.apache.logging.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.dao.WorkFlowProcessCommentDAO;
import org.dspace.content.service.WorkFlowProcessCommentService;
import org.dspace.content.service.WorkFlowProcessCommentService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WorkFlowProcessCommentServiceImpl extends DSpaceObjectServiceImpl<WorkFlowProcessComment> implements WorkFlowProcessCommentService {
    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkFlowProcessCommentServiceImpl.class);
    @Autowired(required = true)
    protected WorkFlowProcessCommentDAO workFlowProcessCommentDAO;
    protected WorkFlowProcessCommentServiceImpl() {
        super();
    }
    @Override
    public WorkFlowProcessComment findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }
    @Override
    public WorkFlowProcessComment findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }
    @Override
    public void updateLastModified(Context context, WorkFlowProcessComment dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }
    @Override
    public void delete(Context context, WorkFlowProcessComment dso) throws SQLException, AuthorizeException, IOException {
        workFlowProcessCommentDAO.delete(context,dso);
    }
    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }
    @Override
    public List<WorkFlowProcessComment> findAll(Context context) throws SQLException {
        return Optional.ofNullable(workFlowProcessCommentDAO.findAll(context,WorkFlowProcessComment.class)).orElse(new ArrayList<>());
    }
    @Override
    public WorkFlowProcessComment create(Context context, WorkFlowProcessComment WorkFlowProcessComment) throws SQLException, AuthorizeException {
        WorkFlowProcessComment= workFlowProcessCommentDAO.create(context,WorkFlowProcessComment);
        return WorkFlowProcessComment;
    }
    @Override
    public List<WorkFlowProcessComment> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return  Optional.ofNullable(workFlowProcessCommentDAO.findAll(context,WorkFlowProcessComment.class,limit,
                offset)).orElse(new ArrayList<>());
    }
    @Override
    public int countRows(Context context) throws SQLException {
        return workFlowProcessCommentDAO.countRows(context);
    }

    @Override
    public List<WorkFlowProcessComment> getComments(Context context, UUID workflowprocessid) throws SQLException {
        return workFlowProcessCommentDAO.getComments(context,workflowprocessid);
    }

    @Override
    public int countComment(Context context, UUID workflowprocessid) throws SQLException {
        return workFlowProcessCommentDAO.countComment(context,workflowprocessid);
    }

    @Override
    public WorkFlowProcessComment findCommentByworkflowprocessidAndissavedrafttrue(Context context, UUID workflowprocessid) throws SQLException {
        return workFlowProcessCommentDAO.findCommentByworkflowprocessidAndissavedrafttrue(context,workflowprocessid);
    }

    @Override
    public WorkFlowProcessComment findCommentBySubmiterandWorkflowProcessID(Context context, UUID submiter, UUID workflowprocessid) throws SQLException {
        return workFlowProcessCommentDAO.findCommentBySubmiterandWorkflowProcessID(context,submiter,workflowprocessid);
    }
    @Override
    public WorkFlowProcessComment find(Context context, UUID uuid) throws SQLException {
        return workFlowProcessCommentDAO.findByID(context,WorkFlowProcessComment.class,uuid);
    }
    public void update(Context context, WorkFlowProcessComment workFlowProcessComment) throws SQLException, AuthorizeException {

        this.workFlowProcessCommentDAO.save(context, workFlowProcessComment);
    }
}