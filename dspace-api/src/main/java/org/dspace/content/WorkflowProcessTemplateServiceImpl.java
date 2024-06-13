/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.apache.logging.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.dao.WorkflowProcessTemplateDAO;
import org.dspace.content.service.WorkFlowProcessMasterService;
import org.dspace.content.service.WorkFlowProcessMasterValueService;
import org.dspace.content.service.WorkflowProcessTemplateService;
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

/**
 * Service implementation for the Item object.
 * This class is responsible for all business logic calls for the Item object and is autowired by spring.
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class WorkflowProcessTemplateServiceImpl extends DSpaceObjectServiceImpl<WorkflowProcessTemplate> implements WorkflowProcessTemplateService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkflowProcessTemplate.class);

    @Autowired(required = true)
    protected WorkflowProcessTemplateDAO workflowProcessTemplateDAO;

    @Autowired
    WorkFlowProcessMasterService workFlowProcessMasterService;
    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterServicevalue;

    protected WorkflowProcessTemplateServiceImpl() {
        super();
    }

    @Override
    public WorkflowProcessTemplate findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }

    @Override
    public WorkflowProcessTemplate findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }

    @Override
    public WorkflowProcessTemplate find(Context context, UUID uuid) throws SQLException {
        return workflowProcessTemplateDAO.findByID(context, WorkflowProcessTemplate.class, uuid);
    }

    @Override
    public void updateLastModified(Context context, WorkflowProcessTemplate dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }

    @Override
    public void delete(Context context, WorkflowProcessTemplate dso) throws SQLException, AuthorizeException, IOException {

    }

    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public WorkflowProcessTemplate create(Context context, WorkflowProcessTemplate workflowProcessTemplate) throws SQLException, AuthorizeException {
        workflowProcessTemplate = workflowProcessTemplateDAO.create(context, workflowProcessTemplate);
        return workflowProcessTemplate;
    }

    @Override
    public List<WorkflowProcessTemplate> findAll(Context context) throws SQLException {
        return Optional.ofNullable(workflowProcessTemplateDAO.findAll(context, WorkflowProcessTemplate.class)).orElse(new ArrayList<>());
    }

    @Override
    public List<WorkflowProcessTemplate> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return Optional.ofNullable(workflowProcessTemplateDAO.findAll(context, WorkflowProcessTemplate.class, limit,
                offset)).orElse(new ArrayList<>());
    }

    @Override
    public int getCountWorkflowProcessByTemplate(Context context, UUID template) throws SQLException {
        return workflowProcessTemplateDAO.getCountWorkflowProcessByTemplate(context,template);
    }

    @Override
    public List<WorkflowProcessTemplate> getWorkflowProcessByTemplate(Context context, UUID template, Integer offset, Integer limit) throws SQLException {
        return workflowProcessTemplateDAO.getWorkflowProcessByTemplate(context,template,offset,limit);
    }
}
