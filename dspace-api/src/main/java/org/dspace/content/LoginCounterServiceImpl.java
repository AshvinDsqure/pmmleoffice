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
import org.dspace.content.dao.LoginCounterDAO;
import org.dspace.content.service.LoginCounterService;
import org.dspace.content.service.WorkFlowProcessMasterService;
import org.dspace.content.service.WorkFlowProcessMasterValueService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for the Item object.
 * This class is responsible for all business logic calls for the Item object and is autowired by spring.
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class LoginCounterServiceImpl extends DSpaceObjectServiceImpl<LoginCounter> implements LoginCounterService {


    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(LoginCounter.class);
    @Autowired(required = true)
    protected LoginCounterDAO LoginCounterDAO;
    protected LoginCounterServiceImpl() {
        super();
    }

    @Override
    public LoginCounter findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }

    @Override
    public LoginCounter findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }

    @Override
    public LoginCounter find(Context context, UUID uuid) throws SQLException {
        return LoginCounterDAO.findByID(context,LoginCounter.class,uuid);
    }

    @Override
    public void updateLastModified(Context context, LoginCounter dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }

    @Override
    public void delete(Context context, LoginCounter dso) throws SQLException, AuthorizeException, IOException {

    }

    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public LoginCounter create(Context context, LoginCounter LoginCounter) throws SQLException, AuthorizeException {
        LoginCounter= LoginCounterDAO.create(context,LoginCounter);
        return LoginCounter;
    }

    @Override
    public List<Object[]> filter(Context context) throws SQLException {
        return LoginCounterDAO.filter(context);
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return LoginCounterDAO.countRows(context);
    }

    @Override
    public LoginCounter getbyToken(Context context, String token) throws SQLException {
        return LoginCounterDAO.getbyToken(context,token);
    }

    public org.dspace.content.dao.LoginCounterDAO getLoginCounterDAO() {
        return LoginCounterDAO;
    }

    public void setLoginCounterDAO(org.dspace.content.dao.LoginCounterDAO loginCounterDAO) {
        LoginCounterDAO = loginCounterDAO;
    }
}
