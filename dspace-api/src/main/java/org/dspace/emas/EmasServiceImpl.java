/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.emas;

import org.apache.logging.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObjectServiceImpl;
import org.dspace.emas.dao.EmasDAO;
import org.dspace.emas.service.EmasService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class EmasServiceImpl extends DSpaceObjectServiceImpl<Emas> implements EmasService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Emas.class);
    @Autowired(required = true)
    protected EmasDAO EmasDAO;
    protected EmasServiceImpl() {
        super();
    }
    @Override
    public Emas findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }
    @Override
    public Emas findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }
    @Override
    public void updateLastModified(Context context, Emas dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }
    @Override
    public void delete(Context context, Emas dso) throws SQLException, AuthorizeException, IOException {
        EmasDAO.delete(context,dso);
    }
    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public Emas create(Context context, Emas Emas) throws SQLException, AuthorizeException {
        Emas= EmasDAO.create(context,Emas);
        return Emas;
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return EmasDAO.countRows(context);
    }
    @Override
    public Boolean getEmasByEperson(Context context, UUID eperson) throws SQLException {
        int i= EmasDAO.getEmasByEperson(context,eperson);
        if(i>0){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public Boolean getEmasByEpersonANDKey(Context context, UUID eperson, String key) throws SQLException {
        int i= EmasDAO.getEmasByEpersonANDKey(context,eperson,key);
        if(i>0){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public Boolean getEmasByKey(Context context, String key) throws SQLException {
        int i= EmasDAO.getEmasByKey(context,key);
        if(i>0){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public Emas find(Context context, UUID uuid) throws SQLException {
        return EmasDAO.findByID(context,Emas.class,uuid);
    }
    @Override
    public void update(Context context, Emas Emas) throws SQLException, AuthorizeException {
        this.EmasDAO.save(context, Emas);
    }
}