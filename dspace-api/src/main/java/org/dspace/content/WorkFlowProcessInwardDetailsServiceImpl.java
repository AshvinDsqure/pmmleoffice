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
import org.dspace.content.dao.WorkFlowProcessInwardDetailsDAO;
import org.dspace.content.service.WorkFlowProcessInwardDetailsService;
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

public class WorkFlowProcessInwardDetailsServiceImpl extends DSpaceObjectServiceImpl<WorkFlowProcessInwardDetails> implements WorkFlowProcessInwardDetailsService {


    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkFlowProcessInwardDetails.class);

    @Autowired(required = true)
    protected WorkFlowProcessInwardDetailsDAO workFlowProcessInwardDetailsDAO;

    protected WorkFlowProcessInwardDetailsServiceImpl() {
        super();
    }

    @Override
    public WorkFlowProcessInwardDetails findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }

    @Override
    public WorkFlowProcessInwardDetails findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }



    @Override
    public void updateLastModified(Context context, WorkFlowProcessInwardDetails dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }

    @Override
    public void delete(Context context, WorkFlowProcessInwardDetails dso) throws SQLException, AuthorizeException, IOException {
        workFlowProcessInwardDetailsDAO.delete(context,dso);
    }

    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public List<WorkFlowProcessInwardDetails> findAll(Context context) throws SQLException {
        return Optional.ofNullable(workFlowProcessInwardDetailsDAO.findAll(context,WorkFlowProcessInwardDetails.class)).orElse(new ArrayList<>());

    }

    @Override
    public WorkFlowProcessInwardDetails create(Context context, WorkFlowProcessInwardDetails WorkFlowProcessInwardDetails) throws SQLException, AuthorizeException {
        WorkFlowProcessInwardDetails= workFlowProcessInwardDetailsDAO.create(context,WorkFlowProcessInwardDetails);
        return WorkFlowProcessInwardDetails;
    }

    @Override
    public List<WorkFlowProcessInwardDetails> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return  Optional.ofNullable(workFlowProcessInwardDetailsDAO.findAll(context,WorkFlowProcessInwardDetails.class,limit,
                offset)).orElse(new ArrayList<>());
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return workFlowProcessInwardDetailsDAO.countRows(context);
    }

    @Override
    public WorkFlowProcessInwardDetails find(Context context, UUID uuid) throws SQLException {
        return workFlowProcessInwardDetailsDAO.findByID(context,WorkFlowProcessInwardDetails.class,uuid);
    }

    public void update(Context context, WorkFlowProcessInwardDetails workFlowProcessInwardDetails) throws SQLException, AuthorizeException {

        this.workFlowProcessInwardDetailsDAO.save(context, workFlowProcessInwardDetails);
    }

    @Override
    public WorkFlowProcessInwardDetails getByInwardNumber(Context context, String inwardnumber) throws SQLException {
        return workFlowProcessInwardDetailsDAO.getByInwardNumber(context,inwardnumber);
    }
}