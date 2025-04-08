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
import org.dspace.content.dao.WorkflowProcessDefinitionDAO;
import org.dspace.content.dao.WorkflowProcessEpersonDAO;
import org.dspace.content.service.WorkflowProcessDefinitionService;
import org.dspace.content.service.WorkflowProcessEpersonService;
import org.dspace.content.service.WorkflowProcessService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.eperson.EPerson;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.MessagingException;
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
public class WorkflowProcessEpersonServiceImpl extends DSpaceObjectServiceImpl<WorkflowProcessEperson> implements WorkflowProcessEpersonService {

    /**
     * log4j category
     */


    @Autowired(required = true)
    protected WorkflowProcessDefinitionDAO workflowProcessDefinitionDAO;
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);
    @Autowired(required = true)
    protected WorkflowProcessEpersonDAO workflowProcessEpersonDAO;
    protected WorkflowProcessEpersonServiceImpl() {
        super();
    }
    @Override
    public WorkflowProcessEperson findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }
    @Override
    public WorkflowProcessEperson findByLegacyId(Context context, int id) throws SQLException {
        return workflowProcessEpersonDAO.findByLegacyId(context,id,WorkflowProcessEperson.class);
    }
    @Override
    public WorkflowProcessEperson find(Context context, UUID uuid) throws SQLException {
        return workflowProcessEpersonDAO.findByID(context,WorkflowProcessEperson.class,uuid);
    }
    @Override
    public void updateLastModified(Context context, WorkflowProcessEperson dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }
    @Override
    public void delete(Context context, WorkflowProcessEperson dso) throws SQLException, AuthorizeException, IOException {
        workflowProcessEpersonDAO.delete(context,dso);
    }
    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public WorkflowProcessEperson create(Context context, WorkflowProcessEperson workflowProcessEperson) throws SQLException, AuthorizeException {
        workflowProcessEperson= workflowProcessEpersonDAO.create(context,workflowProcessEperson);
        return workflowProcessEperson;
    }

    @Override
    public List<WorkflowProcessEperson> findAll(Context context) throws SQLException {
        return Optional.ofNullable(workflowProcessEpersonDAO.findAll(context,WorkflowProcessEperson.class)).orElse(new ArrayList<>());
    }

    @Override
    public List<WorkflowProcessEperson> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return  Optional.ofNullable(workflowProcessEpersonDAO.findAll(context,WorkflowProcessEperson.class,limit,
                offset)).orElse(new ArrayList<>());
    }

    @Override
    public void sendEmail(Context context, String recipientEmail, String recipientName, String subject) throws IOException, MessagingException, SQLException, AuthorizeException {
        {
            EPerson currentuser=context.getCurrentUser();
            String senderName = null;
            String senderEmail = null;
            String senderDesignation = null;
            String senderDepartment = null;
            String senderOffice = null;
            if (currentuser != null && currentuser.getFullName() != null) {
                senderName = currentuser.getFullName();
            }
            if (currentuser != null && currentuser.getDesignation() != null && currentuser.getDesignation().getPrimaryvalue() != null) {
                senderDesignation = currentuser.getDesignation().getPrimaryvalue();
            }
            if (currentuser != null && currentuser.getDepartment() != null && currentuser.getDepartment().getPrimaryvalue() != null) {
                senderDepartment = currentuser.getDepartment().getPrimaryvalue();
            }
            if (currentuser != null && currentuser.getOffice() != null && currentuser.getOffice().getPrimaryvalue() != null) {
                senderOffice = currentuser.getOffice().getPrimaryvalue();
            }
            if (currentuser != null && currentuser.getEmail() != null) {
                senderEmail = currentuser.getEmail();
            }
            Email email = Email.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "notification_to_next_user"));
            email.addArgument(subject);                   //0
            email.addRecipient(recipientEmail);
            email.addArgument(recipientName);             //1
            email.addArgument(senderName);                //2
            email.addArgument(senderEmail);               //3
            email.addArgument(senderDesignation);         //4
            email.addArgument(senderDepartment);          //5
            email.addArgument(senderOffice);              //6
            email.send();
        }
    }

    @Override
    public List<WorkflowProcessEperson> getALLData(Context context, int limit) throws SQLException {
        return workflowProcessEpersonDAO.getALLData(context,limit);
    }
}
