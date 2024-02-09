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
import org.dspace.content.dao.VipDAO;
import org.dspace.content.service.VipService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class VipServiceImpl extends DSpaceObjectServiceImpl<Vip> implements VipService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);
    @Autowired(required = true)
    protected VipDAO VipDAO;
    protected VipServiceImpl() {
        super();
    }
    @Override
    public Vip findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }
    @Override
    public Vip findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }
    @Override
    public void updateLastModified(Context context, Vip dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }
    @Override
    public void delete(Context context, Vip dso) throws SQLException, AuthorizeException, IOException {
        VipDAO.delete(context,dso);
    }
    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }


    @Override
    public Vip create(Context context, Vip Vip) throws SQLException, AuthorizeException {
        Vip= VipDAO.create(context,Vip);
        return Vip;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return VipDAO.countRows(context);
    }

    @Override
    public List<Vip> getAll(Context context) throws SQLException {
        return VipDAO.getAll(context);
    }


    @Override
    public Vip find(Context context, UUID uuid) throws SQLException {
        return VipDAO.findByID(context,Vip.class,uuid);
    }
    @Override
    public void update(Context context, Vip Vip) throws SQLException, AuthorizeException {
        this.VipDAO.save(context, Vip);
    }
}