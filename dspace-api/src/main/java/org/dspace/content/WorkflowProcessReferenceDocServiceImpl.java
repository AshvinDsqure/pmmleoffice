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
import org.dspace.content.dao.WorkflowProcessDAO;
import org.dspace.content.dao.WorkflowProcessReferenceDocDAO;
import org.dspace.content.service.WorkFlowProcessMasterService;
import org.dspace.content.service.WorkFlowProcessMasterValueService;
import org.dspace.content.service.WorkflowProcessReferenceDocService;
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
public class WorkflowProcessReferenceDocServiceImpl extends DSpaceObjectServiceImpl<WorkflowProcessReferenceDoc> implements WorkflowProcessReferenceDocService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);

    @Autowired(required = true)
    protected WorkflowProcessReferenceDocDAO workflowProcessReferenceDocDAO;

    @Autowired
    WorkFlowProcessMasterService workFlowProcessMasterService;
    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterServicevalue;


    protected WorkflowProcessReferenceDocServiceImpl() {
        super();
    }

    @Override
    public WorkflowProcessReferenceDoc findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }

    @Override
    public WorkflowProcessReferenceDoc findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }

    @Override
    public WorkflowProcessReferenceDoc find(Context context, UUID uuid) throws SQLException {
        return workflowProcessReferenceDocDAO.findByID(context,WorkflowProcessReferenceDoc.class,uuid);
    }

    @Override
    public void updateLastModified(Context context, WorkflowProcessReferenceDoc dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }

    @Override
    public void delete(Context context, WorkflowProcessReferenceDoc dso) throws SQLException, AuthorizeException, IOException {
         workflowProcessReferenceDocDAO.delete(context,dso);
    }

    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public WorkflowProcessReferenceDoc create(Context context, WorkflowProcessReferenceDoc workflowProcessReferenceDoc) throws SQLException, AuthorizeException {
        return  workflowProcessReferenceDocDAO.create(context,workflowProcessReferenceDoc);

    }

    @Override
    public List<WorkflowProcessReferenceDoc> findAll(Context context) throws SQLException {
        return Optional.ofNullable(workflowProcessReferenceDocDAO.findAll(context,WorkflowProcessReferenceDoc.class)).orElse(new ArrayList<>());
    }

    @Override
    public List<WorkflowProcessReferenceDoc> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return  Optional.ofNullable(workflowProcessReferenceDocDAO.findAll(context,WorkflowProcessReferenceDoc.class,limit,
                offset)).orElse(new ArrayList<>());
    }

    @Override
    public int countDocumentByType(Context context, UUID drafttypeid) throws SQLException {
        return workflowProcessReferenceDocDAO.countDocumentByType(context,drafttypeid);
    }

    @Override
    public List<WorkflowProcessReferenceDoc> getDocumentByType(Context context, UUID drafttypeid, Integer offset, Integer limit) throws SQLException {
        return workflowProcessReferenceDocDAO.getDocumentByType(context,drafttypeid,offset,limit);
    }

    @Override
    public int countDocumentByItemid(Context context, UUID itemid) throws SQLException {

       WorkFlowProcessMaster  workFlowProcessMaster= workFlowProcessMasterService.findByName(context,"Draft Type");
        if(workFlowProcessMaster!=null){
            WorkFlowProcessMasterValue note=workFlowProcessMasterServicevalue.findByName(context,"Note",workFlowProcessMaster);
            return workflowProcessReferenceDocDAO.countDocumentByItemid(context,note.getID(),itemid);
        }
        return 0;
    }

    @Override
    public List<WorkflowProcessReferenceDoc> getDocumentByItemid(Context context, UUID itemid, Integer offset, Integer limit) throws SQLException {
        WorkFlowProcessMaster  workFlowProcessMaster= workFlowProcessMasterService.findByName(context,"Draft Type");
        if(workFlowProcessMaster!=null){
            WorkFlowProcessMasterValue note=workFlowProcessMasterServicevalue.findByName(context,"Note",workFlowProcessMaster);
            return workflowProcessReferenceDocDAO.getDocumentByItemid(context,note.getID(),itemid,offset,limit);
        }
        return null;
    }

    @Override
    public List<WorkflowProcessReferenceDoc> getDocumentByworkflowprocessid(Context context, UUID workflowprocessid) throws SQLException {
        return workflowProcessReferenceDocDAO.getDocumentByworkflowprocessid(context,workflowprocessid);
    }

    @Override
    public List<WorkflowProcessReferenceDoc> getDocumentBySignitore(Context context, UUID signitoreid) throws SQLException {
        return workflowProcessReferenceDocDAO.getDocumentBySignitore(context,signitoreid);
    }
}
