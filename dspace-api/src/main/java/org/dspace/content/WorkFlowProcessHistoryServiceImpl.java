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
import org.dspace.content.dao.WorkFlowProcessHistoryDAO;
import org.dspace.content.service.WorkFlowProcessHistoryService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WorkFlowProcessHistoryServiceImpl extends DSpaceObjectServiceImpl<WorkFlowProcessHistory> implements WorkFlowProcessHistoryService {


    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);

    @Autowired(required = true)
    protected WorkFlowProcessHistoryDAO workFlowProcessHistoryDAO;

    protected WorkFlowProcessHistoryServiceImpl() {
        super();
    }

    @Override
    public WorkFlowProcessHistory findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }

    @Override
    public WorkFlowProcessHistory findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }



    @Override
    public void updateLastModified(Context context, WorkFlowProcessHistory dso) throws SQLException, AuthorizeException {

    }

    @Override
    public void delete(Context context, WorkFlowProcessHistory dso) throws SQLException, AuthorizeException, IOException {
        workFlowProcessHistoryDAO.delete(context,dso);
    }

    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public List<WorkFlowProcessHistory> findAll(Context context) throws SQLException {
        return Optional.ofNullable(workFlowProcessHistoryDAO.findAll(context,WorkFlowProcessHistory.class)).orElse(new ArrayList<>());

    }

    @Override
    public WorkFlowProcessHistory create(Context context, WorkFlowProcessHistory workFlowProcessHistory) throws SQLException, AuthorizeException {
        workFlowProcessHistory= workFlowProcessHistoryDAO.create(context,workFlowProcessHistory);
        return workFlowProcessHistory;
    }

    @Override
    public List<WorkFlowProcessHistory> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return  Optional.ofNullable(workFlowProcessHistoryDAO.findAll(context,WorkFlowProcessHistory.class,limit,
                offset)).orElse(new ArrayList<>());
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return workFlowProcessHistoryDAO.countRows(context);
    }

    @Override
    public int countHistory(Context contex, UUID workflowprocessid) throws SQLException {
        return workFlowProcessHistoryDAO.countHistory(contex,workflowprocessid);
    }

    @Override
    public List<WorkFlowProcessHistory> getHistory(Context context, UUID workflowprocessid) throws SQLException {
        return workFlowProcessHistoryDAO.getHistory(context,workflowprocessid);
    }

    @Override
    public List<WorkFlowProcessHistory> getHistory(Context context, int limit) throws SQLException {
        return workFlowProcessHistoryDAO.getHistory(context,limit);
    }


    @Override
    public WorkFlowProcessHistory find(Context context, UUID uuid) throws SQLException {
        return workFlowProcessHistoryDAO.findByID(context,WorkFlowProcessHistory.class,uuid);
    }

    public void update(Context context, WorkFlowProcessHistory workFlowProcessHistory) throws SQLException, AuthorizeException {

        this.workFlowProcessHistoryDAO.save(context, workFlowProcessHistory);
    }
}