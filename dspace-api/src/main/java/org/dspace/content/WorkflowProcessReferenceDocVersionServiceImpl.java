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
import org.dspace.content.dao.WorkflowProcessDAO;
import org.dspace.content.dao.WorkflowProcessReferenceDocVersionDAO;
import org.dspace.content.service.WorkflowProcessReferenceDocVersionService;
import org.dspace.content.service.WorkflowProcessService;
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
public class WorkflowProcessReferenceDocVersionServiceImpl extends DSpaceObjectServiceImpl<WorkflowProcessReferenceDocVersion> implements WorkflowProcessReferenceDocVersionService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);

    @Autowired(required = true)
    protected WorkflowProcessReferenceDocVersionDAO WorkflowProcessReferenceDocVersionDAO;

    protected WorkflowProcessReferenceDocVersionServiceImpl() {
        super();
    }

    @Override
    public WorkflowProcessReferenceDocVersion findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }

    @Override
    public WorkflowProcessReferenceDocVersion findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }

    @Override
    public WorkflowProcessReferenceDocVersion find(Context context, UUID uuid) throws SQLException {
        return WorkflowProcessReferenceDocVersionDAO.findByID(context, WorkflowProcessReferenceDocVersion.class, uuid);
    }

    @Override
    public void updateLastModified(Context context, WorkflowProcessReferenceDocVersion dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }

    @Override
    public void delete(Context context, WorkflowProcessReferenceDocVersion dso) throws SQLException, AuthorizeException, IOException {
        WorkflowProcessReferenceDocVersionDAO.delete(context, dso);
    }

    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public WorkflowProcessReferenceDocVersion create(Context context, WorkflowProcessReferenceDocVersion WorkflowProcessReferenceDocVersion) throws SQLException, AuthorizeException {
        return WorkflowProcessReferenceDocVersionDAO.create(context, WorkflowProcessReferenceDocVersion);

    }

    @Override
    public List<WorkflowProcessReferenceDocVersion> findAll(Context context) throws SQLException {
        return Optional.ofNullable(WorkflowProcessReferenceDocVersionDAO.findAll(context, WorkflowProcessReferenceDocVersion.class)).orElse(new ArrayList<>());
    }

    @Override
    public List<WorkflowProcessReferenceDocVersion> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return Optional.ofNullable(WorkflowProcessReferenceDocVersionDAO.findAll(context, WorkflowProcessReferenceDocVersion.class, limit,
                offset)).orElse(new ArrayList<>());
    }

    @Override
    public int countRow(Context context) throws SQLException {
        return WorkflowProcessReferenceDocVersionDAO.countRows(context);
    }

    @Override
    public int countDocumentID(Context context, UUID documentid) throws SQLException {
        return 0;
    }

    @Override
    public List<WorkflowProcessReferenceDocVersion> getDocVersionBydocumentID(Context context, UUID documentid, Integer offset, Integer limit) throws SQLException {
        return WorkflowProcessReferenceDocVersionDAO.getDocVersionBydocumentID(context, documentid, offset, limit);
    }

    @Override
    public WorkflowProcessReferenceDocVersion findByCreator(Context context, UUID uuid,UUID documentid) throws SQLException {
        try {
            WorkflowProcessReferenceDocVersion v = WorkflowProcessReferenceDocVersionDAO.findByCreator(context, uuid,documentid);
            if (v != null) {
                return v;
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("version" + e.getMessage());
            return null;
        }
    }
}
