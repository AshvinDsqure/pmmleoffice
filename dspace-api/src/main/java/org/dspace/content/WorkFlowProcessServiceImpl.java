/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoFunction;
import org.apache.logging.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.dao.WorkflowProcessDAO;
import org.dspace.content.service.*;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.eperson.EPerson;
import org.dspace.event.Event;
import org.dspace.workflow.WorkflowType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Service implementation for the Item object.
 * This class is responsible for all business logic calls for the Item object and is autowired by spring.
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class WorkFlowProcessServiceImpl extends DSpaceObjectServiceImpl<WorkflowProcess> implements WorkflowProcessService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);

    @Autowired(required = true)
    protected WorkflowProcessDAO workflowProcessDAO;
    @Autowired
    private BundleService bundleService;
    @Autowired
    private BitstreamService bitstreamService;


    protected WorkFlowProcessMasterValueService workFlowProcessMasterValueService;

    protected WorkFlowProcessMasterService workFlowProcessMasterServicee;


    protected WorkFlowProcessServiceImpl() {
        super();
    }

    @Override
    public WorkflowProcess findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }

    @Override
    public WorkflowProcess findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }

    @Override
    public WorkflowProcess find(Context context, UUID uuid) throws SQLException {

        return workflowProcessDAO.findByID(context, WorkflowProcess.class, uuid);
    }

    @Override
    public void updateLastModified(Context context, WorkflowProcess dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new org.dspace.event.Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }

    @Override
    public void delete(Context context, WorkflowProcess dso) throws SQLException, AuthorizeException, IOException {

    }

    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public WorkflowProcess create(Context context, WorkflowProcess workflowProcess) throws SQLException, AuthorizeException {
        workflowProcess = workflowProcessDAO.create(context, workflowProcess);
        return workflowProcess;
    }

    @Override
    public List<WorkflowProcess> findAll(Context context) throws SQLException {
        return Optional.ofNullable(workflowProcessDAO.findAll(context, WorkflowProcess.class)).orElse(new ArrayList<>());
    }

    @Override
    public List<WorkflowProcess> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return Optional.ofNullable(workflowProcessDAO.findAll(context, WorkflowProcess.class, limit,
                offset)).orElse(new ArrayList<>());
    }

    @Override
    public List<WorkflowProcess> findNotCompletedByUser(Context context, UUID eperson, UUID statusid, UUID draftid, Integer offset, Integer limit) throws SQLException {
        return workflowProcessDAO.findNotCompletedByUser(context, eperson, statusid, draftid, offset, limit);
    }

    @Override
    public List<WorkflowProcess> findCompletedFlow(Context context, UUID eperson, UUID statusid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException {
        return workflowProcessDAO.findCompletedFlow(context,eperson,statusid,workflowtypeid,offset,limit);
    }

    @Override
    public int countfindCompletedFlow(Context context, UUID eperson, UUID statusid, UUID workflowtypeid) throws SQLException {
        return workflowProcessDAO.countfindCompletedFlow(context,eperson,statusid,workflowtypeid);
    }

    @Override
    public int countfindNotCompletedByUser(Context context, UUID eperson, UUID statusid, UUID draftid) throws SQLException {
        return workflowProcessDAO.countfindNotCompletedByUser(context, eperson, statusid, draftid);
    }

    @Override
    public List<WorkflowProcess> getHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid, Integer offset, Integer limit) throws SQLException {
        return workflowProcessDAO.getHistoryByNotOwnerAndNotDraft(context, eperson, statusid, offset, limit);
    }

    @Override
    public int countgetHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid) throws SQLException {
        return workflowProcessDAO.countgetHistoryByNotOwnerAndNotDraft(context, eperson, statusid);
    }

    @Override
    public int countfilterInwarAndOutWard(Context context, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.DC.getName(), "title", null);
        return workflowProcessDAO.countfilterInwarAndOutWard(context,metadataFields,perameter, offset, limit);
    }

    @Override
    public List<WorkflowProcess> getHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid,UUID workflowtypeid, Integer offset, Integer limit) throws SQLException {
        return workflowProcessDAO.getHistoryByOwnerAndIsDraft(context, eperson, statusid,workflowtypeid, offset, limit);
    }

    @Override
    public int countgetHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid,UUID workflowtypeid) throws SQLException {
        return workflowProcessDAO.countgetHistoryByOwnerAndIsDraft(context, eperson, statusid,workflowtypeid);
    }

    @Override
    public void storeWorkFlowMataDataTOBitsream(Context context, WorkflowProcessReferenceDoc workflowProcessReferenceDoc, Item item) throws SQLException, AuthorizeException {
        Bitstream bitstream = workflowProcessReferenceDoc.getBitstream();
        if (bitstream != null) {
            List<Bundle> bundles = item.getBundles("ORIGINAL");
            Bundle finalBundle = null;
            if (bundles.size() == 0) {
                finalBundle = bundleService.create(context, item, "ORIGINAL");
            } else {
                finalBundle = bundles.stream().findFirst().get();
            }
            Bundle finalBundle1 = finalBundle;
            bundleService.addBitstream(context, finalBundle1, bitstream);
            if (workflowProcessReferenceDoc.getWorkFlowProcessReferenceDocType() != null) {
                bitstreamService.addMetadata(context, bitstream, "dc", "doc", "type", null, workflowProcessReferenceDoc.getWorkFlowProcessReferenceDocType().getPrimaryvalue());
            }
            if (workflowProcessReferenceDoc.getReferenceNumber() != null) {
                bitstreamService.addMetadata(context, bitstream, "dc", "ref", "number", null, workflowProcessReferenceDoc.getReferenceNumber());
            }
            if (workflowProcessReferenceDoc.getSubject() != null) {
                bitstreamService.addMetadata(context, bitstream, "dc", "description", null, null, workflowProcessReferenceDoc.getSubject());
            }
            if (workflowProcessReferenceDoc.getLatterCategory() != null) {
                bitstreamService.addMetadata(context, bitstream, "dc", "letter", "category", null, workflowProcessReferenceDoc.getLatterCategory().getPrimaryvalue());
                bitstreamService.addMetadata(context, bitstream, "dc", "letter", "categoryhi", null, workflowProcessReferenceDoc.getLatterCategory().getSecondaryvalue());
            }
            if (workflowProcessReferenceDoc.getInitdate() != null) {
                bitstreamService.addMetadata(context, bitstream, "dc", "date", null, null, workflowProcessReferenceDoc.getInitdate().toString());
            }
            if (workflowProcessReferenceDoc.getSubject() != null) {
                bitstreamService.addMetadata(context, bitstream, "dc", "subject", null, null, workflowProcessReferenceDoc.getSubject());
            }
            if (workflowProcessReferenceDoc.getPage() != null) {
                bitstreamService.addMetadata(context, bitstream, "dc", "page", null, null, workflowProcessReferenceDoc.getPage().toString());
            }
        }

    }

    @Override
    public void storeWorkFlowMataDataTOBitsream(Context context, WorkflowProcessReferenceDoc workflowProcessReferenceDoc) throws SQLException, AuthorizeException {
        Bitstream bitstream = workflowProcessReferenceDoc.getBitstream();
        if (bitstream != null) {
            if (workflowProcessReferenceDoc.getWorkFlowProcessReferenceDocType() != null) {
                bitstreamService.addMetadata(context, bitstream, "dc", "doc", "type", null, workflowProcessReferenceDoc.getWorkFlowProcessReferenceDocType().getPrimaryvalue());
            }
            if (workflowProcessReferenceDoc.getReferenceNumber() != null) {
                bitstreamService.addMetadata(context, bitstream, "dc", "ref", "number", null, workflowProcessReferenceDoc.getReferenceNumber());
            }
            if (workflowProcessReferenceDoc.getSubject() != null) {
                bitstreamService.addMetadata(context, bitstream, "dc", "description", null, null, workflowProcessReferenceDoc.getSubject());
            }
            if (workflowProcessReferenceDoc.getLatterCategory() != null) {
                bitstreamService.addMetadata(context, bitstream, "dc", "letter", "category", null, workflowProcessReferenceDoc.getLatterCategory().getPrimaryvalue());
                bitstreamService.addMetadata(context, bitstream, "dc", "letter", "categoryhi", null, workflowProcessReferenceDoc.getLatterCategory().getSecondaryvalue());
            }
            if (workflowProcessReferenceDoc.getInitdate() != null) {
                bitstreamService.addMetadata(context, bitstream, "dc", "date", null, null, workflowProcessReferenceDoc.getInitdate().toString());
            }
            if (workflowProcessReferenceDoc.getSubject() != null) {
                bitstreamService.addMetadata(context, bitstream, "dc", "subject", null, null, workflowProcessReferenceDoc.getSubject());
            }
            if (workflowProcessReferenceDoc.getPage() != null) {
                bitstreamService.addMetadata(context, bitstream, "dc", "page", null, null, workflowProcessReferenceDoc.getPage().toString());
            }
        }
    }

    @Override
    public List<WorkflowProcess> findDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft, Integer offset, Integer limit) throws SQLException {
        return workflowProcessDAO.findDraftPending(context, eperson, statuscloseid, statusdraftid, statusdraft, offset, limit);
    }

    @Override
    public int countfindDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft) throws SQLException {
        return workflowProcessDAO.countfindDraftPending(context, eperson, statuscloseid, statusdraftid, statusdraft);
    }

    @Override
    public WorkflowProcess getNoteByItemsid(Context context, UUID itemid) throws SQLException {
        return workflowProcessDAO.getNoteByItemsid(context, itemid);
    }

    @Override
    public int getCountByType(Context context, UUID typeid) throws SQLException {
        return workflowProcessDAO.getCountByType(context, typeid);
    }

    @Override
    public List<WorkflowProcess> Filter(Context context, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {

        return workflowProcessDAO.Filter(context, perameter, offset, limit);
    }

    @Override
    public List<WorkflowProcess> findReferList(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft, Integer offset, Integer limit) throws SQLException {
        return workflowProcessDAO.findReferList(context, eperson, statuscloseid, statusdraftid, statusdraft, offset, limit);
    }

    @Override
    public int countRefer(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft) throws SQLException {
        return workflowProcessDAO.countRefer(context, eperson, statuscloseid, statusdraftid, statusdraft);
    }

    @Override
    public int countByTypeAndStatus(Context context, UUID typeid, UUID statusid, UUID epersonid) throws SQLException {
        return workflowProcessDAO.countByTypeAndStatus(context, typeid, statusid, epersonid);
    }

    @Override
    public int countByTypeAndStatusNotwoner(Context context, UUID typeid, UUID statusid, UUID epersonid) throws SQLException {
        return workflowProcessDAO.countByTypeAndStatusNotwoner(context, typeid, statusid, epersonid);

    }

    @Override
    public int countByTypeAndPriority(Context context, UUID typeid, UUID priorityid, UUID epersonid) throws SQLException {
        return workflowProcessDAO.countByTypeAndPriority(context, typeid, priorityid, epersonid);
    }

    @Override
    public void
    sendEmail(Context context, HttpServletRequest request, String recipientEmail, String recipientName, String subject, List<Bitstream> bitstreams,List<String> recipientEmails,String body) throws IOException, MessagingException, SQLException, AuthorizeException {
        System.out.println("in sendEmail");
        {
            EPerson currentuser = context.getCurrentUser();
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
            Email email = Email.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "electronical"));
            email.addArgument(subject);                   //0
            if(recipientEmails!=null){
            email.addRecipient(recipientEmail);}
            if(recipientEmails!=null){
                for (String emailid:recipientEmails) {
                    email.addRecipient(emailid);
                    System.out.println("sent email for "+emailid);
                }
            }
            email.addArgument(recipientName);             //1
            email.addArgument(senderName);                //2
            email.addArgument(senderEmail);               //3
            email.addArgument(senderDesignation);         //4
            email.addArgument(senderDepartment);          //5
            email.addArgument(senderOffice);              //6
            email.addArgument(body);                      //7

            for (Bitstream bitstream : bitstreams) {
                if (bitstreamService.retrieve(context, bitstream) != null) {
                    email.addAttachment(bitstreamService.retrieve(context, bitstream), bitstream.getName(), bitstream.getFormat(context).getMIMEType());
                }
            }
            email.send();
        }
    }

    @Override
    public List<WorkflowProcess> searchSubjectByWorkflowTypeandSubject(Context context, UUID workflowtypeid, String subject) throws SQLException {
        return workflowProcessDAO.searchSubjectByWorkflowTypeandSubject(context, workflowtypeid, subject);
    }

    @Override
    public List<WorkflowProcess> filterInwarAndOutWard(Context context, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.DC.getName(), "title", null);
        return workflowProcessDAO.filterInwarAndOutWard(context,metadataFields, perameter, offset, limit);
    }

    @Override
    public List<WorkflowProcess> searchByFilenumberOrTapaleNumber(Context context, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.DC.getName(), "title", null);
        return workflowProcessDAO.searchByFileNumberOrTapalNumber(context,metadataFields, perameter, offset, limit);
    }

    @Override
    public List<WorkflowProcess> sentTapal(Context context, UUID eperson, UUID statusid, UUID workflowtypeid,UUID statuscloseid, Integer offset, Integer limit) throws SQLException {
        return workflowProcessDAO.sentTapal(context,eperson,statusid,workflowtypeid,statuscloseid,offset,limit);
    }

    @Override
    public int countTapal(Context context, UUID eperson, UUID statusid, UUID workflowtypeid,UUID statuscloseid) throws SQLException {
        return workflowProcessDAO.countTapal(context,eperson,statusid,workflowtypeid,statuscloseid);
    }

    @Override
    public List<WorkflowProcess> closeTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException {
        return workflowProcessDAO.closeTapal(context,eperson,statusdraftid,statuscloseid,workflowtypeid,offset,limit);
    }

    @Override
    public int countCloseTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid, UUID workflowtypeid) throws SQLException {
        return workflowProcessDAO.countCloseTapal(context,eperson,statusdraftid,statuscloseid,workflowtypeid);
    }

    @Override
    public List<WorkflowProcess> acknowledgementTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException {
        return workflowProcessDAO.acknowledgementTapal(context,eperson,statusdraftid,statuscloseid,workflowtypeid,offset,limit);
    }

    @Override
    public int countacknowledgementTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid, UUID workflowtypeid) throws SQLException {
        return workflowProcessDAO.countacknowledgementTapal(context, eperson, statusdraftid, statuscloseid, workflowtypeid);
    }

    @Override
    public List<WorkflowProcess> dispatchTapal(Context context, UUID eperson, UUID statusdraftid, UUID statusdspachcloseid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException {
        return workflowProcessDAO.dispatchTapal(context,eperson,statusdraftid,statusdspachcloseid,workflowtypeid,offset,limit);
    }

    @Override
    public int countdispatchTapal(Context context, UUID eperson, UUID statusdraftid, UUID statusdspachcloseid, UUID workflowtypeid) throws SQLException {
        return workflowProcessDAO.countdispatchTapal(context,eperson,statusdraftid,statusdspachcloseid,workflowtypeid);
    }

    @Override
    public List<WorkflowProcess> parkedFlow(Context context, UUID eperson, UUID statusdraftid, UUID statusparkedid, UUID workflowtypeid, Integer offset, Integer limit) throws SQLException {
        return workflowProcessDAO.parkedFlow(context,eperson,statusdraftid,statusparkedid,workflowtypeid,offset,limit);
    }

    @Override
    public int countparkedFlow(Context context, UUID eperson, UUID statusdraftid, UUID statusparkedid, UUID workflowtypeid) throws SQLException {
        return workflowProcessDAO.countparkedFlow(context,eperson,statusdraftid,statusparkedid,workflowtypeid);
    }

    @Override
    public void CallSap(Context context) {



        System.out.println("in CallSap SAP");
        try {
            JCoDestination destination = JCoDestinationManager.getDestination("ZDMS_DOCUMENT_POST");
            JCoFunction function = destination.getRepository().getFunction("ZDMS_DOCUMENT_POST");
            if (function == null) {
                System.out.println("in if ");
                throw new RuntimeException("ZDMS_DOCUMENT_POST not found in SAP.");
            }else{
                System.out.println("in Else ");
                function.getImportParameterList().setValue("IT_MESSAGES", "IT_MESSAGES");
                function.execute(destination);
            }
            System.out.println("out CallSap SAP");
        }catch (Exception e){
            System.out.println("in error CallSap"+e.getMessage());
            e.printStackTrace();
        }
    }


    public UUID getMastervalueData(Context context, String mastername, String mastervaluename) throws SQLException {
        WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterServicee.findByName(context, mastername);
        if (workFlowProcessMaster != null) {
            WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, mastervaluename, workFlowProcessMaster);
            if (workFlowProcessMasterValue != null) {
                System.out.println(" MAster value" + workFlowProcessMasterValue.getPrimaryvalue());
                return workFlowProcessMasterValue.getID();
            }
        }
        return null;
    }

    public static String getFinancialYear() {
        LocalDate today = LocalDate.now();
        System.out.println("todate date" + today);
        int year = today.getYear();
        int month = today.getMonthValue();
        System.out.println("date\t" + today.getDayOfMonth());
        System.out.println("dmonth\t" + today.getMonthValue());
        System.out.println("year\t" + today.getYear());
        String financialYear;
        String financialYears;
        if (month <= 1) {
            financialYear = String.format("%d-%d", year - 1, year);
        } else {
            financialYear = String.format("%d-%d", year, year + 1);
        }
        String s[] = financialYear.split("-");
        financialYears = s[0].toString().substring(2) + "-" + s[1].toString().substring(2);
        return financialYears;
    }


}
