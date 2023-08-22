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
import org.dspace.content.dao.WorkFlowProcessOutwardDetailsDAO;
import org.dspace.content.service.WorkFlowProcessOutwardDetailsService;
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

public class WorkFlowProcessOutwardDetailsServiceImpl extends DSpaceObjectServiceImpl<WorkFlowProcessOutwardDetails> implements WorkFlowProcessOutwardDetailsService {


    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkFlowProcessOutwardDetails.class);

    @Autowired(required = true)
    protected WorkFlowProcessOutwardDetailsDAO workFlowProcessOutwardDetailsDAO;

    protected WorkFlowProcessOutwardDetailsServiceImpl() {
        super();
    }

    @Override
    public WorkFlowProcessOutwardDetails findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }

    @Override
    public WorkFlowProcessOutwardDetails findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }



    @Override
    public void updateLastModified(Context context, WorkFlowProcessOutwardDetails dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }

    @Override
    public void delete(Context context, WorkFlowProcessOutwardDetails dso) throws SQLException, AuthorizeException, IOException {
        workFlowProcessOutwardDetailsDAO.delete(context,dso);
    }

    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public List<WorkFlowProcessOutwardDetails> findAll(Context context) throws SQLException {
        return Optional.ofNullable(workFlowProcessOutwardDetailsDAO.findAll(context,WorkFlowProcessOutwardDetails.class)).orElse(new ArrayList<>());

    }

    @Override
    public WorkFlowProcessOutwardDetails create(Context context, WorkFlowProcessOutwardDetails WorkFlowProcessOutwardDetails) throws SQLException, AuthorizeException {
        WorkFlowProcessOutwardDetails= workFlowProcessOutwardDetailsDAO.create(context,WorkFlowProcessOutwardDetails);
        return WorkFlowProcessOutwardDetails;
    }

    @Override
    public List<WorkFlowProcessOutwardDetails> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return  Optional.ofNullable(workFlowProcessOutwardDetailsDAO.findAll(context,WorkFlowProcessOutwardDetails.class,limit,
                offset)).orElse(new ArrayList<>());
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return workFlowProcessOutwardDetailsDAO.countRows(context);
    }

    @Override
    public WorkFlowProcessOutwardDetails find(Context context, UUID uuid) throws SQLException {
        return workFlowProcessOutwardDetailsDAO.findByID(context,WorkFlowProcessOutwardDetails.class,uuid);
    }

    public void update(Context context, WorkFlowProcessOutwardDetails workFlowProcessOutwardDetails) throws SQLException, AuthorizeException {
        this.workFlowProcessOutwardDetailsDAO.save(context, workFlowProcessOutwardDetails);
    }

    @Override
    public WorkFlowProcessOutwardDetails getByOutwardNumber(Context context, String outwardnumber) throws SQLException {
        return workFlowProcessOutwardDetailsDAO.getByOutwardNumber(context,outwardnumber);
    }

    @Override
    public List<WorkFlowProcessOutwardDetails> searchOutwardNumber(Context context, String name) throws SQLException {
        return workFlowProcessOutwardDetailsDAO.searchOutwardNumber(context,name);
    }
}