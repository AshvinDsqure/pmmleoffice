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
import org.dspace.content.dao.WorkFlowProcessDraftDetailsDAO;
import org.dspace.content.service.WorkFlowProcessDraftDetailsService;
import org.dspace.content.service.WorkFlowProcessDraftDetailsService;
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

public class WorkFlowProcessDraftDetailsServiceImpl extends DSpaceObjectServiceImpl<WorkFlowProcessDraftDetails> implements WorkFlowProcessDraftDetailsService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkFlowProcessDraftDetailsServiceImpl.class);
    @Autowired(required = true)
    protected WorkFlowProcessDraftDetailsDAO workFlowProcessDraftDetailsDAO;

    protected WorkFlowProcessDraftDetailsServiceImpl() {
        super();
    }
    @Override
    public WorkFlowProcessDraftDetails findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }

    @Override
    public WorkFlowProcessDraftDetails findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }
    @Override
    public void updateLastModified(Context context, WorkFlowProcessDraftDetails dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }
    @Override
    public void delete(Context context, WorkFlowProcessDraftDetails dso) throws SQLException, AuthorizeException, IOException {
        workFlowProcessDraftDetailsDAO.delete(context,dso);
    }
    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }
    @Override
    public List<WorkFlowProcessDraftDetails> findAll(Context context) throws SQLException {
        return Optional.ofNullable(workFlowProcessDraftDetailsDAO.findAll(context,WorkFlowProcessDraftDetails.class)).orElse(new ArrayList<>());
    }
    @Override
    public WorkFlowProcessDraftDetails create(Context context, WorkFlowProcessDraftDetails WorkFlowProcessDraftDetails) throws SQLException, AuthorizeException {
        WorkFlowProcessDraftDetails= workFlowProcessDraftDetailsDAO.create(context,WorkFlowProcessDraftDetails);
        return WorkFlowProcessDraftDetails;
    }
    @Override
    public List<WorkFlowProcessDraftDetails> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return  Optional.ofNullable(workFlowProcessDraftDetailsDAO.findAll(context,WorkFlowProcessDraftDetails.class,limit,
                offset)).orElse(new ArrayList<>());
    }
    @Override
    public int countRows(Context context) throws SQLException {
        return workFlowProcessDraftDetailsDAO.countRows(context);
    }
    @Override
    public WorkFlowProcessDraftDetails find(Context context, UUID uuid) throws SQLException {
        return workFlowProcessDraftDetailsDAO.findByID(context,WorkFlowProcessDraftDetails.class,uuid);
    }
    public void update(Context context, WorkFlowProcessDraftDetails workFlowProcessDraftDetails) throws SQLException, AuthorizeException {

        this.workFlowProcessDraftDetailsDAO.save(context, workFlowProcessDraftDetails);
    }

    @Override
    public WorkFlowProcessDraftDetails getbyDocumentsignator(Context context, UUID workflowprocessid) throws SQLException {
        return workFlowProcessDraftDetailsDAO.getbyDocumentsignator(context,workflowprocessid);
    }

    @Override
    public List<WorkFlowProcessDraftDetails> getbyDocumentsignator(Context context,int limit) throws SQLException {
        return workFlowProcessDraftDetailsDAO.getbyDocumentsignator(context,limit);
    }

    @Override
    public int getCountByEpersontoepersonmapping(Context context, UUID documentsignator, UUID epersontoepersonmapping) {
        return workFlowProcessDraftDetailsDAO.getCountByEpersontoepersonmapping(context,documentsignator,epersontoepersonmapping);
    }

    @Override
    public int updateWorkFlowProcessDraftDetails(Context context, UUID epersonfrom, UUID epersontoepersonmappingfrom, UUID epersonto, UUID epersontoepersonmappingto) throws SQLException {
        return workFlowProcessDraftDetailsDAO.updateWorkFlowProcessDraftDetails(context,epersonfrom,epersontoepersonmappingfrom,epersonto,epersontoepersonmappingto);
    }

    @Override
    public int getCountByEperson(Context context, UUID documentsignator) {
       return workFlowProcessDraftDetailsDAO.getCountByEperson(context,documentsignator);
    }
}