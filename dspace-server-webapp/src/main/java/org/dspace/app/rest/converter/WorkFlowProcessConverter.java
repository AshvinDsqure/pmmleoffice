/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import com.google.gson.Gson;
import com.itextpdf.text.log.SysoCounter;
import org.dspace.app.rest.enums.WorkFlowType;
import org.dspace.app.rest.enums.WorkFlowUserType;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.*;
import org.dspace.content.enums.Dispatch;
import org.dspace.content.enums.Priority;
import org.dspace.content.enums.WorkFlowProcessReferenceDocType;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.GroupService;
import org.hibernate.internal.build.AllowSysOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This is the converter from/to the EPerson in the DSpace API data model and the
 * REST data model
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@Component
public class WorkFlowProcessConverter extends DSpaceObjectConverter<WorkflowProcess, WorkFlowProcessRest> {
    @Autowired
    ItemConverter itemConverter;
    @Autowired
    GroupService groupService;
    @Autowired
    EPersonConverter ePersonConverter;
    @Autowired
    BitstreamService bitstreamService;
    @Autowired
    WorkflowProcessSenderDiaryConverter workflowProcessSenderDiaryConverter;

    @Autowired
    WorkFlowProcessInwardDetailsService workFlowProcessInwardDetailsService;
    @Autowired
    WorkFlowProcessOutwardDetailsService workFlowProcessOutwardDetailsService;
    @Autowired
    WorkFlowProcessDraftDetailsService workFlowProcessDraftDetailsService;
    @Autowired
    WorkFlowProcessInwardDetailsConverter workFlowProcessInwardDetailsConverter;
    @Autowired
    WorkFlowProcessOutwardDetailsConverter workFlowProcessOutwardDetailsConverter;
    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;
    @Autowired
    WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;

    @Autowired
    WorkFlowProcessEpersonConverter workFlowProcessEpersonConverter;

    @Autowired
    WorkflowProcessSenderDiaryEpersonConverter workflowProcessSenderDiaryEpersonConverter;
    @Autowired
    WorkFlowProcessDraftDetailsConverter workFlowProcessDraftDetailsConverter;

    @Autowired
    WorkflowProcessNoteConverter workflowProcessNoteConverter;
    @Autowired
    WorkflowProcessService workflowProcessService;

    @Override
    public WorkFlowProcessRest convert(WorkflowProcess obj, Projection projection) {
//
        WorkFlowProcessRest workFlowProcessRest = new WorkFlowProcessRest();
        if (obj.getWorkflowProcessSenderDiary() != null) {
            workFlowProcessRest.setWorkflowProcessSenderDiaryRest(workflowProcessSenderDiaryConverter.convert(obj.getWorkflowProcessSenderDiary(), projection));
        }
        if (obj.getWorkFlowProcessInwardDetails() != null) {
            workFlowProcessRest.setWorkFlowProcessInwardDetailsRest(workFlowProcessInwardDetailsConverter.convert(obj.getWorkFlowProcessInwardDetails(), projection));
        }
        if (obj.getWorkFlowProcessOutwardDetails() != null) {
            workFlowProcessRest.setWorkFlowProcessOutwardDetailsRest(workFlowProcessOutwardDetailsConverter.convert(obj.getWorkFlowProcessOutwardDetails(), projection));
        }
        if (obj.getWorkFlowProcessDraftDetails() != null) {
            workFlowProcessRest.setWorkFlowProcessDraftDetailsRest(workFlowProcessDraftDetailsConverter.convert(obj.getWorkFlowProcessDraftDetails(), projection));
        }
        if (obj.getWorkflowProcessNote() != null) {
            workFlowProcessRest.setWorkflowProcessNoteRest(workflowProcessNoteConverter.convert(obj.getWorkflowProcessNote(), projection));
        }
        if (obj.getDispatchmode() != null) {
            workFlowProcessRest.setDispatchModeRest(workFlowProcessMasterValueConverter.convert(obj.getDispatchmode(), projection));
        }
        if (obj.getEligibleForFiling() != null) {
            workFlowProcessRest.setEligibleForFilingRest(workFlowProcessMasterValueConverter.convert(obj.getEligibleForFiling(), projection));
        }
        if (obj.getWorkflowType() != null) {
            workFlowProcessRest.setWorkflowType(workFlowProcessMasterValueConverter.convert(obj.getWorkflowType(), projection));
        }
        if (obj.getWorkflowStatus() != null) {
            workFlowProcessRest.setWorkflowStatus(workFlowProcessMasterValueConverter.convert(obj.getWorkflowStatus(), projection));
        }
        if (obj.getItem() != null) {
            workFlowProcessRest.setItemRest(itemConverter.convertNameOnly(obj.getItem(), projection));
        }
        if (obj.getWorkflowProcessReferenceDocs() != null) {
            workFlowProcessRest.setWorkflowProcessReferenceDocRests(obj.getWorkflowProcessReferenceDocs().stream().map(d -> {
                WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(d, projection);
                if (d.getWorkflowprocessnote() != null) {
                    rest.setWorkflowProcessNoteRest(workflowProcessNoteConverter.convert(d.getWorkflowprocessnote(), projection));
                }
                return rest;
            }).collect(Collectors.toList()));
        }
        if (obj.getWorkflowProcessSenderDiaries() != null) {
            workFlowProcessRest.setWorkflowProcessSenderDiaryRests(obj.getWorkflowProcessSenderDiaries().stream().map(d -> {
                WorkflowProcessSenderDiaryRest rest = workflowProcessSenderDiaryConverter.convert(d, projection);
                return rest;
            }).collect(Collectors.toList()));
        }
        if (obj.getItems() != null) {
            workFlowProcessRest.setItemsRests(obj.getItems().stream().map(d -> {
                ItemRest rest = itemConverter.convert(d, projection);
                if (d.getItemtype() != null) {
                    rest.setItemtypeRest(workFlowProcessMasterValueConverter.convert(d.getItemtype(), projection));
                }
                return rest;
            }).collect(Collectors.toList()));
        }
        workFlowProcessRest.setSubject(obj.getSubject());
        if (obj.getWorkflowProcessEpeople() != null) {
            Comparator<WorkflowProcessEpersonRest> comparator = (s1, s2) -> s1.getIndex().compareTo(s2.getIndex());
            List<WorkflowProcessEpersonRest> list = obj.getWorkflowProcessEpeople().stream().map(we -> {
                return workFlowProcessEpersonConverter.convert(we, projection);
            }).collect(Collectors.toList());
            list.sort(comparator);
            workFlowProcessRest.setWorkflowProcessEpersonRests(list);
        }
        workFlowProcessRest.setInitDate(obj.getInitDate());
        if (obj.getPriority() != null) {
            workFlowProcessRest.setPriorityRest(workFlowProcessMasterValueConverter.convert(obj.getPriority(), projection));
        }
        if (obj.getDispatchmode() != null)
            workFlowProcessRest.setDispatchModeRest(workFlowProcessMasterValueConverter.convert(obj.getDispatchmode(), projection));
        Optional<WorkflowProcessEperson> ownerRest = obj.getWorkflowProcessEpeople().stream().filter(w -> w.getOwner() != null).filter(w -> w.getOwner()).findFirst();
        if (ownerRest.isPresent()) {
            workFlowProcessRest.setOwner(workFlowProcessEpersonConverter.convert(ownerRest.get(), projection));
        }
        Optional<WorkflowProcessEperson> senderRest = obj.getWorkflowProcessEpeople().stream().filter(wn -> wn.getSender() != null).filter(w -> w.getSender()).findFirst();
        if (senderRest.isPresent()) {
            workFlowProcessRest.setSender(workFlowProcessEpersonConverter.convert(senderRest.get(), projection));
        }
        if (obj.getRemark() != null) {
            workFlowProcessRest.setRemark(obj.getRemark());
        }
        if (obj.getAction() != null) {
            workFlowProcessRest.setActionRest(workFlowProcessMasterValueConverter.convert(obj.getAction(), projection));
        }
        if (ownerRest.isPresent() && ownerRest.get() != null && ownerRest.get().getePerson() != null && ownerRest.get().getePerson().getFullName() != null) {
            List<WorkflowProcessEperson> ownerlist = obj.getWorkflowProcessEpeople().stream().filter(w -> w.getOwner() != null).filter(w -> w.getOwner()).collect(Collectors.toList());
            StringBuffer sb = new StringBuffer();
            int i = 0;
            for (WorkflowProcessEperson ownerRest1 : ownerlist) {
                if (ownerRest1.getePerson() != null && ownerRest1.getePerson().getFullName() != null) {
                    String currentrecipent = null;
                    if (ownerRest1.getUsertype() != null && ownerRest1.getUsertype().getPrimaryvalue() != null && ownerRest1.getUsertype().getPrimaryvalue().equalsIgnoreCase("cc")) {
                        currentrecipent = ownerRest1.getePerson().getFullName() + "(cc)";
                    } else {
                        currentrecipent = ownerRest1.getePerson().getFullName();
                    }
                    if (i == 0) {
                        sb.append(currentrecipent);
                    } else {
                        sb.append("," + currentrecipent);
                    }
                }
                i++;
            }
            workFlowProcessRest.setCurrentrecipient(sb.toString());
        }
        if (senderRest != null && senderRest.isPresent() && senderRest.get() != null && senderRest.get().getePerson() != null && senderRest.get().getePerson().getFullName() != null) {
            workFlowProcessRest.setSender(workFlowProcessEpersonConverter.convert(senderRest.get(), projection));
            List<WorkflowProcessEperson> senderlist = obj.getWorkflowProcessEpeople().stream().filter(w -> w.getSender() != null).filter(w -> w.getSender()).collect(Collectors.toList());
            StringBuffer sb = new StringBuffer();
            int i = 0;
            for (WorkflowProcessEperson sender : senderlist) {
                if (sender.getePerson() != null && sender.getePerson().getFullName() != null) {
                    if (i == 0) {
                        sb.append(sender.getePerson().getFullName());
                    } else {
                        sb.append("," + sender.getePerson().getFullName());
                    }
                }
                i++;
            }
            workFlowProcessRest.setSendername(sb.toString());
        }
        if(obj.getWorkflowProcessSenderDiaryEpeople()!=null){
            List<WorkflowProcessSenderDiaryEpersonRest> rest= obj.getWorkflowProcessSenderDiaryEpeople().stream().filter(d->d!=null)
                            .map(d->{
                          WorkflowProcessSenderDiaryEpersonRest workflowProcessSenderDiaryEperson=      workflowProcessSenderDiaryEpersonConverter.convert(d,projection);
                                return workflowProcessSenderDiaryEperson;
                            }).collect(Collectors.toList());
            workFlowProcessRest.setWorkflowProcessSenderDiaryEpersonRests(rest);
        }

        if(obj.getRemark()!=null){
            workFlowProcessRest.setRemark(obj.getRemark());
        }
        if(obj.getIsread()!=null){
            workFlowProcessRest.setIsread(obj.getIsread());
        }
        workFlowProcessRest.setIsreplydraft(obj.getIsreplydraft());
        workFlowProcessRest.setIssignnote(obj.getIssignnote());
        workFlowProcessRest.setIsinternal(obj.getIsinternal());
        workFlowProcessRest.setUuid(obj.getID().toString());
        return workFlowProcessRest;
    }

    public WorkFlowProcessRest convertFilter(WorkflowProcess obj, Projection projection) {
        WorkFlowProcessRest workFlowProcessRest = new WorkFlowProcessRest();
        if (obj.getWorkflowProcessSenderDiary() != null) {
            workFlowProcessRest.setWorkflowProcessSenderDiaryRest(workflowProcessSenderDiaryConverter.convert(obj.getWorkflowProcessSenderDiary(), projection));
        }
        if (obj.getWorkFlowProcessInwardDetails() != null) {
            workFlowProcessRest.setWorkFlowProcessInwardDetailsRest(workFlowProcessInwardDetailsConverter.convert(obj.getWorkFlowProcessInwardDetails(), projection));
        }
        if (obj.getWorkFlowProcessOutwardDetails() != null) {
            workFlowProcessRest.setWorkFlowProcessOutwardDetailsRest(workFlowProcessOutwardDetailsConverter.convert(obj.getWorkFlowProcessOutwardDetails(), projection));
        }
        if (obj.getSubject() != null) {
            workFlowProcessRest.setSubject(obj.getSubject());
        }
        if (obj.getWorkflowProcessReferenceDocs() != null) {
            workFlowProcessRest.setWorkflowProcessReferenceDocRests(obj.getWorkflowProcessReferenceDocs().stream().map(d -> {
                WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(d, projection);
                if (d.getWorkflowprocessnote() != null) {
                    rest.setWorkflowProcessNoteRest(workflowProcessNoteConverter.convert(d.getWorkflowprocessnote(), projection));
                }
                return rest;
            }).collect(Collectors.toList()));
        }
        if (obj.getRemark() != null) {
            workFlowProcessRest.setRemark(obj.getRemark());
        }
        if (obj.getAction() != null) {
            workFlowProcessRest.setActionRest(workFlowProcessMasterValueConverter.convert(obj.getAction(), projection));
        }
        workFlowProcessRest.setUuid(obj.getID().toString());
        return workFlowProcessRest;
    }

    public WorkflowProcess convert(WorkFlowProcessRest obj, Context context) throws Exception {
        //aa


        System.out.println("object::::::"+obj.getIsinternal());
        WorkflowProcess workflowProcess = new WorkflowProcess();
        if (obj.getWorkflowProcessSenderDiaryRest() != null) {
            workflowProcess.setWorkflowProcessSenderDiary(workflowProcessSenderDiaryConverter.convert(context, obj.getWorkflowProcessSenderDiaryRest()));
        }

        if (obj.getWorkFlowProcessInwardDetailsRest() != null) {
            workflowProcess.setWorkFlowProcessInwardDetails(workFlowProcessInwardDetailsConverter.convert(context, obj.getWorkFlowProcessInwardDetailsRest()));
        }
        if (obj.getWorkFlowProcessDraftDetailsRest() != null) {
            WorkFlowProcessDraftDetails draftDetails = workFlowProcessDraftDetailsService.create(context, workFlowProcessDraftDetailsConverter.convert(context, obj.getWorkFlowProcessDraftDetailsRest()));
            workflowProcess.setWorkFlowProcessDraftDetails(draftDetails);
        }
        if (obj.getWorkflowProcessNoteRest() != null) {
            workflowProcess.setWorkflowProcessNote(workflowProcessNoteConverter.convert(context, obj.getWorkflowProcessNoteRest()));
        }
        if (obj.getWorkFlowProcessOutwardDetailsRest() != null) {
            workflowProcess.setWorkFlowProcessOutwardDetails(workFlowProcessOutwardDetailsConverter.convert(context, obj.getWorkFlowProcessOutwardDetailsRest()));
        }
        if (obj.getDispatchModeRest() != null) {
            workflowProcess.setDispatchmode(workFlowProcessMasterValueConverter.convert(context, obj.getDispatchModeRest()));
        }
        if (obj.getEligibleForFilingRest() != null) {
            workflowProcess.setEligibleForFiling(workFlowProcessMasterValueConverter.convert(context, obj.getEligibleForFilingRest()));
        }
        if (obj.getItemRest() != null) {
            workflowProcess.setItem(itemConverter.convert(obj.getItemRest(), context));
        }
        if(obj.getWorkflowTypeStr()!=null) {
            WorkFlowType workFlowType = WorkFlowType.valueOf(obj.getWorkflowTypeStr());
            if (workFlowType != null) {
                Optional<WorkFlowProcessMasterValue> workFlowProcessMasterValue = workFlowType.getUserTypeFromMasterValue(context);
                if (workFlowProcessMasterValue.isPresent()) {
                    workflowProcess.setWorkflowType(workFlowProcessMasterValue.get());
                }
            }
        }
        workflowProcess.setSubject(obj.getSubject());
        List<WorkflowProcessEperson> listuser = new ArrayList<>();

        // set submitor...

        AtomicInteger index = new AtomicInteger(0);
        listuser = obj.getWorkflowProcessEpersonRests().stream().filter(d->d!=null).map(we -> {
            try {
                if (we.getUserType() == null) {
                    int i = index.incrementAndGet();
                    we.setSequence(i);
                }
                WorkflowProcessEperson workflowProcessEperson = workFlowProcessEpersonConverter.convert(context, we);
                Optional<WorkFlowProcessMasterValue> workFlowUserTypOptional = WorkFlowUserType.NORMAL.getUserTypeFromMasterValue(context);
                if (we.getUserType() == null) {
                    workflowProcessEperson.setUsertype(workFlowUserTypOptional.get());
                } else {
                    workflowProcessEperson.setUsertype(workFlowProcessMasterValueConverter.convert(context, we.getUserType()));
                }
                workflowProcessEperson.setOwner(false);
                workflowProcessEperson.setSender(false);
                workflowProcessEperson.setWorkflowProcess(workflowProcess);
                return workflowProcessEperson;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        workflowProcess.setWorkflowProcessEpeople(listuser);

        //
        if(obj.getWorkflowProcessSenderDiaryEpersonRests()!=null) {
            List<WorkflowProcessSenderDiaryEperson> listusersenderdiry = new ArrayList<>();
            listusersenderdiry = obj.getWorkflowProcessSenderDiaryEpersonRests().stream().filter(d -> d != null).map(we -> {
                try {
                    WorkflowProcessSenderDiaryEperson workflowProcessSenderDiaryEperson = workflowProcessSenderDiaryEpersonConverter.convert(context, we);
                    workflowProcessSenderDiaryEperson.setWorkflowProcess(workflowProcess);
                    return workflowProcessSenderDiaryEperson;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
            workflowProcess.setWorkflowProcessSenderDiaryEpeople(listusersenderdiry);
        }
        workflowProcess.setInitDate(obj.getInitDate());
        if (obj.getPriorityRest() != null) {
            workflowProcess.setPriority(workFlowProcessMasterValueConverter.convert(context, obj.getPriorityRest()));
        }
        if (obj.getDispatchModeRest() != null) {
            workflowProcess.setDispatchmode(workFlowProcessMasterValueConverter.convert(context, obj.getDispatchModeRest()));
        }
        if (obj.getRemark() != null) {
            workflowProcess.setRemark(obj.getRemark());
        }
        if (obj.getActionRest() != null) {
            workflowProcess.setAction(workFlowProcessMasterValueConverter.convert(context, obj.getActionRest()));
        }
        if (obj.getIsreplydraft() != null) {
            workflowProcess.setIsreplydraft(obj.getIsreplydraft());
        }
        workflowProcess.setIsinternal(obj.getIsinternal());
        return workflowProcess;
    }


    public WorkflowProcess convertByService(Context context, WorkFlowProcessRest rest) throws SQLException {
        WorkflowProcess workflowProcess = null;
        if (rest != null && rest.getUuid() != null && rest.getUuid().trim().length() != 0) {
            workflowProcess = workflowProcessService.find(context, UUID.fromString(rest.getUuid()));
        }
        return workflowProcess;
    }

    public WorkflowProcess convertDraftwithID(WorkFlowProcessRest obj, Context context, UUID id) throws Exception {
        WorkflowProcess workflowProcess = workflowProcessService.find(context, id);
        if (obj.getWorkFlowProcessDraftDetailsRest() != null) {
            WorkFlowProcessDraftDetails draftDetails = workFlowProcessDraftDetailsService.create(context, workFlowProcessDraftDetailsConverter.convert(context, obj.getWorkFlowProcessDraftDetailsRest()));
            workflowProcess.setWorkFlowProcessDraftDetails(draftDetails);
        }
        if (obj.getWorkflowProcessNoteRest() != null) {
            workflowProcess.setWorkflowProcessNote(workflowProcessNoteConverter.convert(context, obj.getWorkflowProcessNoteRest()));
        }
        if (obj.getItemRest() != null) {
            workflowProcess.setItem(itemConverter.convert(obj.getItemRest(), context));
        }
        if (obj.getWorkflowTypeStr() != null) {
            WorkFlowType workFlowType = WorkFlowType.valueOf(obj.getWorkflowTypeStr());
            if (workFlowType != null) {
                Optional<WorkFlowProcessMasterValue> workFlowProcessMasterValue = workFlowType.getUserTypeFromMasterValue(context);
                if (workFlowProcessMasterValue.isPresent()) {
                    workflowProcess.setWorkflowType(workFlowProcessMasterValue.get());
                }
            }
        }
        if (obj.getSubject() != null) {
            workflowProcess.setSubject(obj.getSubject());
        }
        if (obj.getInitDate() != null) {
            workflowProcess.setInitDate(obj.getInitDate());
        }
        if (obj.getPriorityRest() != null) {
            workflowProcess.setPriority(workFlowProcessMasterValueConverter.convert(context, obj.getPriorityRest()));
        }
        if (obj.getActionRest() != null) {
            workflowProcess.setAction(workFlowProcessMasterValueConverter.convert(context, obj.getActionRest()));
        }
        if (obj.getIsreplydraft() != null) {
            workflowProcess.setIsreplydraft(obj.getIsreplydraft());
        }
        if(obj.getRemark()!=null){
            workflowProcess.setRemark(obj.getRemark());
        }
        if(obj.getDueDate()!=null){
            workflowProcess.setInitDate(obj.getDueDate());
        }
        workflowProcess.setIsinternal(obj.getIsinternal());
        return workflowProcess;
    }
    public WorkFlowProcessRest convertByDashbord(Context context, WorkflowProcess obj, Projection projection) {
        WorkFlowProcessRest workFlowProcessRest = new WorkFlowProcessRest();
        if (obj.getWorkflowType() != null && obj.getWorkflowType().getPrimaryvalue() != null) {
            workFlowProcessRest.setWorkflowtype(obj.getWorkflowType().getPrimaryvalue());
            workFlowProcessRest.setWorkflowType(workFlowProcessMasterValueConverter.convert(obj.getWorkflowType(), projection));
        }
        if (obj.getWorkflowStatus() != null && obj.getWorkflowStatus().getPrimaryvalue() != null) {
            workFlowProcessRest.setWorkflowStatus(workFlowProcessMasterValueConverter.convert(obj.getWorkflowStatus(), projection));
            workFlowProcessRest.setWorkflowstatus(obj.getWorkflowStatus().getPrimaryvalue());
        }
        if(obj.getSubject()!=null){
            workFlowProcessRest.setSubject(obj.getSubject());
        }
        if(obj.getInitDate()!=null){
           workFlowProcessRest.setInitDate(obj.getInitDate());
        }
        if (obj.getPriority() != null && obj.getPriority().getPrimaryvalue() != null) {
            workFlowProcessRest.setPriorityRest(workFlowProcessMasterValueConverter.convert(obj.getPriority(), projection));
            workFlowProcessRest.setPriority(obj.getPriority().getPrimaryvalue());
        }
        Optional<WorkflowProcessEperson> ownerRest = obj.getWorkflowProcessEpeople().stream().filter(w -> w.getOwner() != null).filter(w -> w.getOwner()).findFirst();
        if (ownerRest.isPresent() && ownerRest.get().getAssignDate() != null) {
            workFlowProcessRest.setDueDate(ownerRest.get().getAssignDate());
            workFlowProcessRest.setOwner(workFlowProcessEpersonConverter.convert(ownerRest.get(), projection));
        }
        if (ownerRest.isPresent() && ownerRest.get() != null && ownerRest.get().getePerson() != null && ownerRest.get().getePerson().getFullName() != null) {
            List<WorkflowProcessEperson> ownerlist = obj.getWorkflowProcessEpeople().stream().filter(w -> w.getOwner() != null).filter(w -> w.getOwner()).collect(Collectors.toList());
            StringBuffer sb = new StringBuffer();
            int i = 0;
            for (WorkflowProcessEperson ownerRest1 : ownerlist) {
                if (ownerRest1.getePerson() != null && ownerRest1.getePerson().getFullName() != null) {
                    String currentrecipent = null;
                    if (ownerRest1.getUsertype() != null && ownerRest1.getUsertype().getPrimaryvalue() != null && ownerRest1.getUsertype().getPrimaryvalue().equalsIgnoreCase("cc")) {
                        currentrecipent = ownerRest1.getePerson().getFullName() + "(cc)";
                    } else {
                        currentrecipent = ownerRest1.getePerson().getFullName();
                    }
                    if (i == 0) {
                        sb.append(currentrecipent);
                    } else {
                        sb.append("," + currentrecipent);
                    }
                }
                i++;
            }
            workFlowProcessRest.setCurrentrecipient(sb.toString());
        }
        Optional<WorkflowProcessEperson> senderRest = obj.getWorkflowProcessEpeople().stream().filter(wn -> wn.getSender() != null).filter(w -> w.getSender()).findFirst();
        if (senderRest != null && senderRest.isPresent() && senderRest.get() != null && senderRest.get().getePerson() != null && senderRest.get().getePerson().getFullName() != null) {
            workFlowProcessRest.setSender(workFlowProcessEpersonConverter.convert(senderRest.get(), projection));
            List<WorkflowProcessEperson> senderlist = obj.getWorkflowProcessEpeople().stream().filter(w -> w.getSender() != null).filter(w -> w.getSender()).collect(Collectors.toList());
            StringBuffer sb = new StringBuffer();
            int i = 0;
            for (WorkflowProcessEperson sender : senderlist) {
                if (sender.getePerson() != null && sender.getePerson().getFullName() != null) {
                    if (i == 0) {
                        sb.append(sender.getePerson().getFullName());
                    } else {
                        sb.append("," + sender.getePerson().getFullName());
                    }
                }
                i++;
            }
            workFlowProcessRest.setSendername(sb.toString());
        }
        if (obj.getWorkFlowProcessInwardDetails() != null && obj.getWorkFlowProcessInwardDetails().getInwardDate() != null) {
            workFlowProcessRest.setDateRecived(obj.getWorkFlowProcessInwardDetails().getInwardDate());
        }
        if(obj.getWorkFlowProcessInwardDetails()!=null){
            workFlowProcessRest.setWorkFlowProcessInwardDetailsRest(workFlowProcessInwardDetailsConverter.convert(obj.getWorkFlowProcessInwardDetails(), projection));
        }
        if (obj.getWorkFlowProcessOutwardDetails() != null && obj.getWorkFlowProcessOutwardDetails().getOutwardDate() != null) {
            workFlowProcessRest.setWorkFlowProcessOutwardDetailsRest(workFlowProcessOutwardDetailsConverter.convert(obj.getWorkFlowProcessOutwardDetails(), projection));
            workFlowProcessRest.setDateRecived(obj.getWorkFlowProcessOutwardDetails().getOutwardDate());
        }
        if (obj.getWorkFlowProcessDraftDetails() != null && obj.getWorkFlowProcessDraftDetails().getDraftdate() != null) {
            workFlowProcessRest.setWorkFlowProcessDraftDetailsRest(workFlowProcessDraftDetailsConverter.convert(obj.getWorkFlowProcessDraftDetails(), projection));
            workFlowProcessRest.setDateRecived(obj.getWorkFlowProcessDraftDetails().getDraftdate());
        }
        if (obj.getDispatchmode() != null && obj.getDispatchmode().getPrimaryvalue() != null && obj.getWorkflowType().getPrimaryvalue().equalsIgnoreCase("Inward")) {
            workFlowProcessRest.setMode(obj.getDispatchmode().getPrimaryvalue());
        }
        if (obj.getWorkFlowProcessOutwardDetails() != null && obj.getWorkFlowProcessOutwardDetails().getOutwardmedium() != null && obj.getWorkFlowProcessOutwardDetails().getOutwardmedium().getPrimaryvalue() != null && obj.getWorkflowType().getPrimaryvalue().equalsIgnoreCase("Outward")) {
            workFlowProcessRest.setMode(obj.getWorkFlowProcessOutwardDetails().getOutwardmedium().getPrimaryvalue());
        }
        if (obj.getItem() != null) {
            try {
                workFlowProcessRest.setItemRest(itemConverter.convertNameOnly(obj.getItem(), projection));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (obj.getRemark() != null) {
            workFlowProcessRest.setRemark(obj.getRemark());
        }
        workFlowProcessRest.setUuid(obj.getID().toString());
        workFlowProcessRest.setIsmode(obj.getIsmode());
        workFlowProcessRest.setIsread(obj.getIsread());
        workFlowProcessRest.setIsreplydraft(obj.getIsreplydraft());
        workFlowProcessRest.setIssignnote(obj.getIssignnote());
        workFlowProcessRest.setIsinternal(obj.getIsinternal());
        return workFlowProcessRest;
    }

    public WorkFlowProcessRest convertsearchBySubject(WorkflowProcess obj) {
        WorkFlowProcessRest workFlowProcessRest = new WorkFlowProcessRest();
        if (obj.getSubject() != null) {
            workFlowProcessRest.setSubject(obj.getSubject());
        }
        return workFlowProcessRest;
    }

    @Override
    protected WorkFlowProcessRest newInstance() {
        return new WorkFlowProcessRest();
    }

    @Override
    public Class<WorkflowProcess> getModelClass() {
        return WorkflowProcess.class;
    }
}
