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
import org.springframework.beans.factory.annotation.Autowired;

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
    public List<WorkflowProcess> findNotCompletedByUser(Context context, UUID eperson, UUID statusid, UUID draftid,UUID epersontoepersonmapid, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.EPERSON.getName(), "firstname", null);
        return workflowProcessDAO.findNotCompletedByUser(context, eperson, statusid, draftid,epersontoepersonmapid,perameter, metadataFields,offset, limit);
    }

    @Override
    public List<WorkflowProcess> findNotCompletedByUserDraft(Context context, UUID eperson, UUID statusid, UUID draftid,UUID epersontoepersonmapid,HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.EPERSON.getName(), "firstname", null);
        return workflowProcessDAO.findNotCompletedByUserDraft(context, eperson, statusid, draftid,epersontoepersonmapid,perameter,metadataFields,offset, limit);
    }

    @Override
    public List<WorkflowProcess> findCompletedFlow(Context context, UUID eperson, UUID statusid, UUID workflowtypeid,UUID epersontoepersonmapid, Integer offset, Integer limit) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.EPERSON.getName(), "firstname", null);
        return workflowProcessDAO.findCompletedFlow(context,eperson,statusid,workflowtypeid,epersontoepersonmapid,offset,limit);
    }

    @Override
    public int countfindCompletedFlow(Context context, UUID eperson, UUID statusid, UUID workflowtypeid,UUID epersontoepersonmapid) throws SQLException {
        return workflowProcessDAO.countfindCompletedFlow(context,eperson,statusid,workflowtypeid,epersontoepersonmapid);
    }

    @Override
    public int countfindNotCompletedByUser(Context context, UUID eperson, UUID statusid, UUID draftid,UUID epersontoepersonmapid) throws SQLException {
        return workflowProcessDAO.countfindNotCompletedByUser(context, eperson, statusid, draftid,epersontoepersonmapid);
    }

    @Override
    public int countfindNotCompletedByUserDraft(Context context, UUID eperson, UUID statusid, UUID draftid,UUID epersontoepersonmapid) throws SQLException {
        return workflowProcessDAO.countfindNotCompletedByUserDraft(context, eperson, statusid, draftid,epersontoepersonmapid);

    }

    @Override
    public List<WorkflowProcess> getHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid,UUID epersontoepersonmapid,Integer offset, Integer limit) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.EPERSON.getName(), "firstname", null);
        return workflowProcessDAO.getHistoryByNotOwnerAndNotDraft(context, eperson, statusid,epersontoepersonmapid,metadataFields, offset, limit);
    }
    @Override
    public int countgetHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid,UUID epersontoepersonmapid) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.EPERSON.getName(), "firstname", null);
        return workflowProcessDAO.countgetHistoryByNotOwnerAndNotDraft(context, eperson, statusid,epersontoepersonmapid);
    }
    @Override
    public int countfilterInwarAndOutWard(Context context, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.DC.getName(), "title", null);
        return workflowProcessDAO.countfilterInwarAndOutWard(context,metadataFields,perameter, offset, limit);
    }

    @Override
    public List<WorkflowProcess> getHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid,UUID workflowtypeid,UUID epersontoepersonmapid,HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.DC.getName(), "firstname", null);
        return workflowProcessDAO.getHistoryByOwnerAndIsDraft(context, eperson, statusid,workflowtypeid,epersontoepersonmapid,perameter, metadataFields, offset, limit);
    }

    @Override
    public int countgetHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid,UUID workflowtypeid,UUID epersontoepersonmapid) throws SQLException {
        return workflowProcessDAO.countgetHistoryByOwnerAndIsDraft(context, eperson, statusid,workflowtypeid,epersontoepersonmapid);
    }

    @Override
    public synchronized void storeWorkFlowMataDataTOBitsream(Context context, WorkflowProcessReferenceDoc workflowProcessReferenceDoc, Item item) throws SQLException, AuthorizeException {
        Bitstream bitstream = workflowProcessReferenceDoc.getBitstream();
        if (bitstream != null) {
            // System.out.println("in store ::::storeWorkFlowMataDataTOBitsream");
            List<Bundle> bundles = item.getBundles("ORIGINAL");
            // System.out.println("bundles size"+bundles.size());
            Bundle finalBundle = null;
            if (bundles.size() == 0) {
                // System.out.println("create new Bundel");
                finalBundle = bundleService.create(context, item, "ORIGINAL");
            } else {
                finalBundle = bundles.stream().findFirst().get();
            }
            // System.out.println("finalBundle ::name "+finalBundle.getName());
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
        }else{
            // System.out.println("bitstream not::::::::::::::in thid doc ");
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
    public List<WorkflowProcess> findDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft,UUID epersontoepersonmapid,HashMap<String, String> perameter,UUID statusReject, Integer offset, Integer limit) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.EPERSON.getName(), "firstname", null);
        return workflowProcessDAO.findDraftPending(context, eperson, statuscloseid, statusdraftid, statusdraft,epersontoepersonmapid,perameter,metadataFields,statusReject, offset, limit);
    }

    @Override
    public int countfindDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft,UUID epersontoepersonmapid,UUID statusReject) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.EPERSON.getName(), "firstname", null);
        return workflowProcessDAO.countfindDraftPending(context, eperson, statuscloseid, statusdraftid, statusdraft,epersontoepersonmapid,metadataFields,statusReject);
    }

    @Override
    public List<WorkflowProcess> findFilePendingDueDate(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft, UUID epersontoepersonmapping, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.EPERSON.getName(), "firstname", null);
        return workflowProcessDAO.findFilePendingDueDate(context,eperson,statuscloseid,statusdraftid,statusdraft,epersontoepersonmapping,perameter,metadataFields,offset,limit);
    }

    @Override
    public int countfindFilePendingDueDate(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft, UUID epersontoepersonmapping) throws SQLException {
        return workflowProcessDAO.countfindFilePendingDueDate(context,eperson,statuscloseid,statusdraftid,statusdraft,epersontoepersonmapping);
    }

    @Override
    public WorkflowProcess getNoteByItemsid(Context context, UUID itemid) throws SQLException {
        return workflowProcessDAO.getNoteByItemsid(context, itemid);
    }

    @Override
    public int getCountByType(Context context, UUID typeid,Integer version) throws SQLException {
        return workflowProcessDAO.getCountByType(context, typeid,version);
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
    public int countByTypeAndStatus(Context context, UUID typeid, UUID statusid, UUID epersonid,UUID epersontoepersonmapid) throws SQLException {
        return workflowProcessDAO.countByTypeAndStatus(context, typeid, statusid, epersonid,epersontoepersonmapid);
    }


    @Override
    public int countByTypeAndStatusandNotDraft(Context context, UUID typeid, UUID statusid, UUID epersonid, UUID draftstatus,UUID epersontoepersonmapid) throws SQLException {
        return workflowProcessDAO.countByTypeAndStatusandNotDraft(context, typeid, statusid, epersonid,draftstatus,epersontoepersonmapid);
    }

    @Override
    public int countByTypeAndPriorityNotDraft(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid,UUID epersontoepersonmapid) throws SQLException {
        return workflowProcessDAO.countByTypeAndPriorityNotDraft(context,typeid,priorityid,epersonid,statusid,epersontoepersonmapid);
    }

    @Override
    public int countByTypeAndPriorityCreted(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid,UUID epersontoepersonmapid) throws SQLException {
        return workflowProcessDAO.countByTypeAndPriorityCreted(context,typeid,priorityid,epersonid,statusid,epersontoepersonmapid);

    }

    @Override
    public int countByTypeAndPriorityClose(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid,UUID epersontoepersonmapid,UUID statusdraft,UUID statusdispatch,UUID usertype) throws SQLException {
        return workflowProcessDAO.countByTypeAndPriorityClose(context,typeid,priorityid,epersonid,statusid,epersontoepersonmapid,statusdraft,statusdispatch,usertype);
    }

    @Override
    public int countByTypeAndPriorityCloseTapal(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid, UUID epersontoepersonmapid, UUID statusdraft, UUID statusdispatch, UUID usertype) throws SQLException {
        return workflowProcessDAO.countByTypeAndPriorityCloseTapal(context,typeid,priorityid,epersonid,statusid,epersontoepersonmapid,statusdraft,statusdispatch,usertype);
    }

    @Override
    public int countByTypeAndPriorityDraftTapal(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid, UUID epersontoepersonmapid) throws SQLException {
        return workflowProcessDAO.countByTypeAndPriorityDraftTapal(context,typeid,priorityid,epersonid,statusid,epersontoepersonmapid);
    }

    @Override
    public int countByTypeAndPriorityCloseSignLatter(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid, UUID epersontoepersonmapid, UUID statusdraft, UUID statusdispatch, UUID usertype) throws SQLException {
        return workflowProcessDAO.countByTypeAndPriorityCloseSignLatter(context,typeid,priorityid,epersonid,statusid,epersontoepersonmapid,statusdraft,statusdispatch,usertype);
    }

    @Override
    public int countByTypeAndPriorityPark(Context context, UUID typeid, UUID priorityid, UUID epersonid, UUID statusid, UUID epersontoepersonmapid, UUID statusdraftid) throws SQLException {
        return workflowProcessDAO.countByTypeAndPriorityPark(context,typeid,priorityid,epersonid,statusid,epersontoepersonmapid,statusdraftid);
    }

    @Override
    public int countByTypeAndPriority(Context context, UUID typeid, UUID priorityid, UUID epersonid,UUID statusid,UUID epersontoepersonmapid) throws SQLException {
        return workflowProcessDAO.countByTypeAndPriority(context, typeid, priorityid, epersonid,statusid,epersontoepersonmapid);
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
            Optional<EpersonToEpersonMapping> map= context.getCurrentUser().getEpersonToEpersonMappings().stream().filter(d->d.getIsactive()==true).findFirst();
            if (map.isPresent()) {
                EpersonToEpersonMapping ep=map.get();
                if(ep.getEpersonmapping()!=null&&ep.getEpersonmapping().getOffice()!=null&&ep.getEpersonmapping().getOffice().getPrimaryvalue()!=null){
                    senderOffice=ep.getEpersonmapping().getOffice().getPrimaryvalue();
                }
                if (currentuser != null&& ep.getEpersonmapping()!=null&&ep.getEpersonmapping().getDesignation()!=null&&ep.getEpersonmapping().getDesignation().getPrimaryvalue()!=null) {
                    senderDesignation = currentuser.getDesignation().getPrimaryvalue();
                }
                if (currentuser != null && ep.getEpersonmapping()!=null&&ep.getEpersonmapping().getDepartment()!=null&&ep.getEpersonmapping().getDepartment().getPrimaryvalue()!=null) {
                    senderDepartment = currentuser.getDepartment().getPrimaryvalue();
                }
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
    public List<WorkflowProcess> getWorkflowAfterNoteApproved(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID workflowtypeid,UUID epersontoepersonmapid,HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.EPERSON.getName(), "firstname", null);
        return workflowProcessDAO.getWorkflowAfterNoteApproved(context,eperson,statuscloseid,statusdraftid,workflowtypeid,epersontoepersonmapid,perameter,offset,limit);
    }

    @Override
    public int getCountWorkflowAfterNoteApproved(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID workflowtypeid,UUID epersontoepersonmapid) throws SQLException {
        return workflowProcessDAO.getCountWorkflowAfterNoteApproved(context,eperson,statuscloseid,statusdraftid,workflowtypeid,epersontoepersonmapid);
    }

    @Override
    public List<WorkflowProcess> searchByFilenumberOrTapaleNumber(Context context, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.DC.getName(), "title", null);
        MetadataField metadataFieldssubject = metadataFieldService.findByElement(context, MetadataSchemaEnum.DC.getName(), "subject", null);
        return workflowProcessDAO.searchByFileNumberOrTapalNumber(context,metadataFields,metadataFieldssubject, perameter, offset, limit);
    }

    @Override
    public List<WorkflowProcess> sentTapal(Context context, UUID eperson, UUID statusid, UUID workflowtypeid,UUID statuscloseid,UUID epersontoepersonmapid,HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.EPERSON.getName(), "firstname", null);
        return workflowProcessDAO.sentTapal(context,eperson,statusid,workflowtypeid,statuscloseid,epersontoepersonmapid,perameter,metadataFields,offset,limit);
    }

    @Override
    public int countTapal(Context context, UUID eperson, UUID statusid, UUID workflowtypeid,UUID statuscloseid,UUID epersontoepersonmapid,HashMap<String, String> perameter) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.EPERSON.getName(), "firstname", null);
        return workflowProcessDAO.countTapal(context,eperson,statusid,workflowtypeid,statuscloseid,epersontoepersonmapid,metadataFields,perameter);
    }

    @Override
    public List<WorkflowProcess> closeTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid,UUID statusdspatchcloseid, UUID workflowtypeid,UUID epersontoepersonmapid,UUID usertype, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.EPERSON.getName(), "firstname", null);
        return workflowProcessDAO.closeTapal(context,eperson,statusdraftid,statuscloseid,statusdspatchcloseid,workflowtypeid,epersontoepersonmapid,usertype,perameter,metadataFields,offset,limit);
    }
    @Override
    public List<WorkflowProcess> parkedFlow(Context context, UUID eperson, UUID statusdraftid, UUID statusparkedid, UUID workflowtypeid,UUID epersontoepersonmapid,HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {
        MetadataField metadataFields = metadataFieldService.findByElement(context, MetadataSchemaEnum.EPERSON.getName(), "firstname", null);
        return workflowProcessDAO.parkedFlow(context,eperson,statusdraftid,statusparkedid,workflowtypeid,epersontoepersonmapid,perameter,metadataFields,offset,limit);
    }

    @Override
    public int countCloseTapal(Context context, UUID eperson, UUID statusdraftid, UUID statuscloseid,UUID statusdspatchcloseid, UUID workflowtypeid,UUID epersontoepersonmapid,UUID usertype) throws SQLException {
        return workflowProcessDAO.countCloseTapal(context,eperson,statusdraftid,statuscloseid,statusdspatchcloseid,workflowtypeid,epersontoepersonmapid,usertype);
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
    public int countparkedFlow(Context context, UUID eperson, UUID statusdraftid, UUID statusparkedid, UUID workflowtypeid,UUID epersontoepersonmapid) throws SQLException {
        return workflowProcessDAO.countparkedFlow(context,eperson,statusdraftid,statusparkedid,workflowtypeid,epersontoepersonmapid);
    }

    @Override
    public void CallSap(Context context) {



        // System.out.println("in CallSap SAP");
        try {
            JCoDestination destination = JCoDestinationManager.getDestination("ZDMS_DOCUMENT_POST");
            JCoFunction function = destination.getRepository().getFunction("ZDMS_DOCUMENT_POST");
            if (function == null) {
                //System.out.println("in if ");
                throw new RuntimeException("ZDMS_DOCUMENT_POST not found in SAP.");
            }else{
                // System.out.println("in Else ");
                function.getImportParameterList().setValue("IT_MESSAGES", "IT_MESSAGES");
                function.execute(destination);
            }
            // System.out.println("out CallSap SAP");
        }catch (Exception e){
            // System.out.println("in error CallSap"+e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Object[]> filterDepartmentWiseCount(Context context, HashMap<String, String> parameter, String startdate, String endDate, Integer offset, Integer limit) throws SQLException {
        return workflowProcessDAO.filterDepartmentWiseCount(context,parameter,startdate,endDate,offset,limit);
    }

    @Override
    public List<Object[]> filterDepartmentWiseCountDownload(Context context, HashMap<String, String> parameter, String startdate, String endDate) throws SQLException {
        return workflowProcessDAO.filterDepartmentWiseCountDownload(context,parameter,startdate,endDate);
    }

    @Override
    public int getNextInwardNumber(Context context) throws SQLException {
        return workflowProcessDAO.getNextInwardNumber(context);
    }

    @Override
    public int getNextFileNumber(Context context) throws SQLException {
        return workflowProcessDAO.getNextFileNumber(context);
    }

    @Override
    public List<Object[]> withinDepartmentDownload(Context context, HashMap<String, String> parameter, String startdate, String endDate) throws SQLException {
        return workflowProcessDAO.withinDepartmentDownload(context,parameter,startdate,endDate);
    }

    @Override
    public List<Object[]> outerDepartmentDownload(Context context, HashMap<String, String> parameter, String startdate, String endDate) throws SQLException {
        return workflowProcessDAO.outerDepartmentDownload(context,parameter,startdate,endDate);
    }

    @Override
    public List<Object[]> stagewithinDepartmentDownload(Context context, HashMap<String, String> parameter, String startdate, String endDate) throws SQLException {
        return workflowProcessDAO.stagewithinDepartmentDownload(context,parameter,startdate,endDate);
    }

    @Override
    public List<Object[]> stageouterDepartmentDownload(Context context, HashMap<String, String> parameter, String startdate, String endDate) throws SQLException {
        return workflowProcessDAO.stageouterDepartmentDownload(context,parameter,startdate,endDate);
    }

    @Override
    public List<Object[]> NoOfFileCreatedAndClose(Context context, HashMap<String, String> parameter, String startdate, String endDate) throws SQLException {
        return workflowProcessDAO.NoOfFileCreatedAndClose(context,parameter,startdate,endDate);
    }

    @Override
    public List<Object[]> NoOfTapalCreatedAndClose(Context context, HashMap<String, String> parameter, String startdate, String endDate) throws SQLException {
        return workflowProcessDAO.NoOfTapalCreatedAndClose(context,parameter,startdate,endDate);
    }

    @Override
    public int countTapalPrkedDateRange(Context context, String startdate, String enddate) throws SQLException {
        return workflowProcessDAO.countTapalPrkedDateRange(context,startdate,enddate);
    }

    @Override
    public int countTapalStageWISE(Context context, String startdate, String enddate) throws SQLException {
        return workflowProcessDAO.countTapalStageWISE(context,startdate,enddate);
    }

    @Override
    public List<Object[]> getActiveAndDActiveUsers(Context context, String flag) throws SQLException {
        return workflowProcessDAO.getActiveAndDActiveUsers(context,flag);
    }

    @Override
    public List<Object[]> getFileByDayTakenAndDepartment(Context context, Integer daytaken, String flag,String startdate, String enddate) throws SQLException {

        if(flag!=null || flag.equalsIgnoreCase("yes")){
            flag="Complete";
        }else {
            flag="Forward";
        }
        System.out.println("flag value:::"+flag);
        return workflowProcessDAO.getFileByDayTakenAndDepartment(context,daytaken,flag,startdate,enddate);
    }

    @Override
    public List<String> getDraftMigration(Context context, Integer limit) throws SQLException {
        return workflowProcessDAO.getDraftMigration(context,limit);
    }

    public UUID getMastervalueData(Context context, String mastername, String mastervaluename) throws SQLException {
        WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterServicee.findByName(context, mastername);
        if (workFlowProcessMaster != null) {
            WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, mastervaluename, workFlowProcessMaster);
            if (workFlowProcessMasterValue != null) {
                // System.out.println(" MAster value" + workFlowProcessMasterValue.getPrimaryvalue());
                return workFlowProcessMasterValue.getID();
            }
        }
        return null;
    }

    public static String getFinancialYear() {
        LocalDate today = LocalDate.now();
        // System.out.println("todate date" + today);
        int year = today.getYear();
        int month = today.getMonthValue();
        // System.out.println("date\t" + today.getDayOfMonth());
        //System.out.println("dmonth\t" + today.getMonthValue());
        //System.out.println("year\t" + today.getYear());
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
