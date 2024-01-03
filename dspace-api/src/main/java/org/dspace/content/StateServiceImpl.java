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
import org.dspace.content.dao.StateDAO;
import org.dspace.content.service.StateService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class StateServiceImpl extends DSpaceObjectServiceImpl<State> implements StateService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);
    @Autowired(required = true)
    protected StateDAO StateDAO;
    protected StateServiceImpl() {
        super();
    }
    @Override
    public State findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }
    @Override
    public State findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }
    @Override
    public void updateLastModified(Context context, State dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }
    @Override
    public void delete(Context context, State dso) throws SQLException, AuthorizeException, IOException {
        StateDAO.delete(context,dso);
    }
    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }


    @Override
    public State create(Context context, State State) throws SQLException, AuthorizeException {
        State= StateDAO.create(context,State);
        return State;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return StateDAO.countRows(context);
    }

    @Override
    public List<State> getByCountryId(Context context, UUID countryid) throws SQLException {
        return StateDAO.getByCountryId(context,countryid);
    }


    @Override
    public State find(Context context, UUID uuid) throws SQLException {
        return StateDAO.findByID(context,State.class,uuid);
    }
    @Override
    public void update(Context context, State State) throws SQLException, AuthorizeException {
        this.StateDAO.save(context, State);
    }
}