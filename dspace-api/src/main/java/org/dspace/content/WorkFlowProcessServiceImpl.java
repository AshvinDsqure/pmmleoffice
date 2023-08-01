/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.dao.WorkflowProcessDAO;
import org.dspace.content.service.*;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.disseminate.service.CitationDocumentService;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;


import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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

        return workflowProcessDAO.findByID(context,WorkflowProcess.class,uuid);
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
        workflowProcess= workflowProcessDAO.create(context,workflowProcess);
        return workflowProcess;
    }

    @Override
    public List<WorkflowProcess> findAll(Context context) throws SQLException {
        return Optional.ofNullable(workflowProcessDAO.findAll(context,WorkflowProcess.class)).orElse(new ArrayList<>());
    }

    @Override
    public List<WorkflowProcess> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return  Optional.ofNullable(workflowProcessDAO.findAll(context,WorkflowProcess.class,limit,
                offset)).orElse(new ArrayList<>());
    }

    @Override
    public List<WorkflowProcess> findNotCompletedByUser(Context context, UUID eperson,UUID statusid,UUID draftid, Integer offset, Integer limit) throws SQLException {
       return workflowProcessDAO.findNotCompletedByUser(context,eperson,statusid,draftid,offset,limit);
    }

    @Override
    public int countfindNotCompletedByUser(Context context, UUID eperson,UUID statusid,UUID draftid) throws SQLException {
        return workflowProcessDAO.countfindNotCompletedByUser(context,eperson,statusid,draftid);
    }

    @Override
    public List<WorkflowProcess> getHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid, Integer offset, Integer limit) throws SQLException {
        return workflowProcessDAO.getHistoryByNotOwnerAndNotDraft(context,eperson,statusid,offset,limit);
    }

    @Override
    public int countgetHistoryByNotOwnerAndNotDraft(Context context, UUID eperson, UUID statusid) throws SQLException {
        return workflowProcessDAO.countgetHistoryByNotOwnerAndNotDraft(context,eperson,statusid);
    }

    @Override
    public List<WorkflowProcess> getHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid, Integer offset, Integer limit) throws SQLException {
        return workflowProcessDAO.getHistoryByOwnerAndIsDraft(context,eperson,statusid,offset,limit);
    }

    @Override
    public int countgetHistoryByOwnerAndIsDraft(Context context, UUID eperson, UUID statusid) throws SQLException {
        return workflowProcessDAO.countgetHistoryByOwnerAndIsDraft(context,eperson,statusid);
    }

    @Override
    public void storeWorkFlowMataDataTOBitsream(Context context, WorkflowProcessReferenceDoc workflowProcessReferenceDoc, Item item) throws SQLException, AuthorizeException {
        Bitstream bitstream= workflowProcessReferenceDoc.getBitstream();
        if(bitstream != null) {
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
        }

    }
    @Override
    public void storeWorkFlowMataDataTOBitsream(Context context, WorkflowProcessReferenceDoc workflowProcessReferenceDoc) throws SQLException, AuthorizeException {
        Bitstream bitstream= workflowProcessReferenceDoc.getBitstream();
        if(bitstream != null) {
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
        }
    }
    @Override
    public List<WorkflowProcess> findDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft,Integer offset, Integer limit) throws SQLException {
        return workflowProcessDAO.findDraftPending(context,eperson,statuscloseid,statusdraftid,statusdraft,offset,limit);
    }
    @Override
    public int countfindDraftPending(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid,UUID statusdraft) throws SQLException {
        return workflowProcessDAO.countfindDraftPending(context,eperson,statuscloseid,statusdraftid,statusdraft);
    }
    @Override
    public WorkflowProcess getNoteByItemsid(Context context, UUID itemid) throws SQLException {
        return workflowProcessDAO.getNoteByItemsid(context,itemid);
    }

    @Override
    public int getCountByType(Context context, UUID typeid) throws SQLException {
        return workflowProcessDAO.getCountByType(context,typeid);
    }

    @Override
    public List<WorkflowProcess> Filter(Context context, HashMap<String, String> perameter, Integer offset, Integer limit) throws SQLException {

        return workflowProcessDAO.Filter(context,perameter,offset,limit);
    }

    @Override
    public List<WorkflowProcess> findReferList(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft, Integer offset, Integer limit) throws SQLException {
        return workflowProcessDAO.findReferList(context,eperson,statuscloseid,statusdraftid,statusdraft,offset,limit);
    }

    @Override
    public int countRefer(Context context, UUID eperson, UUID statuscloseid, UUID statusdraftid, UUID statusdraft) throws SQLException {
        return workflowProcessDAO.countRefer(context,eperson,statuscloseid,statusdraftid,statusdraft);
    }
    @Override
    public int countByTypeAndStatus(Context context, UUID typeid, UUID statusid,UUID epersonid) throws SQLException {
        return workflowProcessDAO.countByTypeAndStatus(context,typeid,statusid,epersonid);
    }
    @Override
    public int countByTypeAndPriority(Context context, UUID typeid, UUID priorityid,UUID epersonid) throws SQLException {
        return workflowProcessDAO.countByTypeAndPriority(context,typeid,priorityid,epersonid);
    }
}
