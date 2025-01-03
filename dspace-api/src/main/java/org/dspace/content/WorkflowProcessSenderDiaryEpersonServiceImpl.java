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
import org.dspace.content.dao.WorkflowProcessDefinitionDAO;
import org.dspace.content.dao.WorkflowProcessSenderDiaryEpersonDAO;
import org.dspace.content.service.WorkflowProcessSenderDiaryEpersonService;
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
public class WorkflowProcessSenderDiaryEpersonServiceImpl extends DSpaceObjectServiceImpl<WorkflowProcessSenderDiaryEperson> implements WorkflowProcessSenderDiaryEpersonService {

    /**
     * log4j category
     */


    @Autowired(required = true)
    protected WorkflowProcessDefinitionDAO workflowProcessDefinitionDAO;
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);
    @Autowired(required = true)
    protected WorkflowProcessSenderDiaryEpersonDAO WorkflowProcessSenderDiaryEpersonDAO;
    protected WorkflowProcessSenderDiaryEpersonServiceImpl() {
        super();
    }
    @Override
    public WorkflowProcessSenderDiaryEperson findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }
    @Override
    public WorkflowProcessSenderDiaryEperson findByLegacyId(Context context, int id) throws SQLException {
        return WorkflowProcessSenderDiaryEpersonDAO.findByLegacyId(context,id,WorkflowProcessSenderDiaryEperson.class);
    }
    @Override
    public WorkflowProcessSenderDiaryEperson find(Context context, UUID uuid) throws SQLException {
        return WorkflowProcessSenderDiaryEpersonDAO.findByID(context,WorkflowProcessSenderDiaryEperson.class,uuid);
    }
    @Override
    public void updateLastModified(Context context, WorkflowProcessSenderDiaryEperson dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }
    @Override
    public void delete(Context context, WorkflowProcessSenderDiaryEperson dso) throws SQLException, AuthorizeException, IOException {
        WorkflowProcessSenderDiaryEpersonDAO.delete(context,dso);
    }
    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public WorkflowProcessSenderDiaryEperson create(Context context, WorkflowProcessSenderDiaryEperson WorkflowProcessSenderDiaryEperson) throws SQLException, AuthorizeException {
        WorkflowProcessSenderDiaryEperson= WorkflowProcessSenderDiaryEpersonDAO.create(context,WorkflowProcessSenderDiaryEperson);
        return WorkflowProcessSenderDiaryEperson;
    }

    @Override
    public List<WorkflowProcessSenderDiaryEperson> findAll(Context context) throws SQLException {
        return Optional.ofNullable(WorkflowProcessSenderDiaryEpersonDAO.findAll(context,WorkflowProcessSenderDiaryEperson.class)).orElse(new ArrayList<>());
    }

    @Override
    public List<WorkflowProcessSenderDiaryEperson> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return  Optional.ofNullable(WorkflowProcessSenderDiaryEpersonDAO.findAll(context,WorkflowProcessSenderDiaryEperson.class,limit,
                offset)).orElse(new ArrayList<>());
    }


}
