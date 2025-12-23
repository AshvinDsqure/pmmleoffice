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
    ItemService itemService;
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
        if (obj.getWorkflowProcessSenderDiaryEpeople() != null) {
            List<WorkflowProcessSenderDiaryEpersonRest> rest = obj.getWorkflowProcessSenderDiaryEpeople().stream().filter(d -> d != null)
                    .map(d -> {
                        WorkflowProcessSenderDiaryEpersonRest workflowProcessSenderDiaryEperson = workflowProcessSenderDiaryEpersonConverter.convert(d, projection);
                        return workflowProcessSenderDiaryEperson;
                    }).collect(Collectors.toList());
            workFlowProcessRest.setWorkflowProcessSenderDiaryEpersonRests(rest);
        }

        if (obj.getRemark() != null) {
            workFlowProcessRest.setRemark(obj.getRemark());
        }
        if (obj.getIsread() != null) {
            workFlowProcessRest.setIsread(obj.getIsread());
        }
        workFlowProcessRest.setIsreplydraft(obj.getIsreplydraft());
        workFlowProcessRest.setIssignnote(obj.getIssignnote());
        workFlowProcessRest.setIsinternal(obj.getIsinternal());
        workFlowProcessRest.setIssignatorysame(obj.getIssignatorysame());
        if (obj.getIspredefineuser() != null) {
            workFlowProcessRest.setIspredefineuser(obj.getIspredefineuser());
        }

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
        //aaaa
        System.out.println("object::::::" + obj.getIsinternal());
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
        if (obj.getWorkflowTypeStr() != null) {
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
        listuser = obj.getWorkflowProcessEpersonRests().stream().filter(d -> d != null).map(we -> {
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
        if (obj.getWorkflowProcessSenderDiaryEpersonRests() != null) {
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
        if (obj.getIspredefineuser() != null) {
            workflowProcess.setIspredefineuser(obj.getIspredefineuser());

        }
        workflowProcess.setIsinternal(obj.getIsinternal());
        workflowProcess.setIssignatorysame(obj.getIssignatorysame());
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
        if (obj.getWorkFlowProcessInwardDetailsRest() != null) {
            WorkFlowProcessInwardDetails workFlowProcessInwardDetails = workFlowProcessInwardDetailsConverter.convert(context, obj.getWorkFlowProcessInwardDetailsRest());
            workflowProcess.setWorkFlowProcessInwardDetails(workFlowProcessInwardDetails);
        }
        if (obj.getWorkflowProcessNoteRest() != null) {
            workflowProcess.setWorkflowProcessNote(workflowProcessNoteConverter.convert(context, obj.getWorkflowProcessNoteRest()));
        }
        if (obj.getItemRest() != null) {
            workflowProcess.setItem(itemConverter.convert(obj.getItemRest(), context));
        }

        if (obj.getEligibleForFilingRest() != null) {
            workflowProcess.setEligibleForFiling(workFlowProcessMasterValueConverter.convert(context, obj.getEligibleForFilingRest()));
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
        if (obj.getPriorityRest() != null) {
            workflowProcess.setPriority(workFlowProcessMasterValueConverter.convert(context, obj.getPriorityRest()));
        }
        if (obj.getActionRest() != null) {
            workflowProcess.setAction(workFlowProcessMasterValueConverter.convert(context, obj.getActionRest()));
        }
        if (obj.getIsreplydraft() != null) {
            System.out.println("in getIsreplydraft");
            workflowProcess.setIsreplydraft(obj.getIsreplydraft());
        }
        if (obj.getRemark() != null) {
            workflowProcess.setRemark(obj.getRemark());
        }
        //sender diry

        WorkflowProcess workflowProcess1 =workflowProcess;
        if (obj.getWorkflowProcessSenderDiaryRests() != null && !obj.getWorkflowProcessSenderDiaryRests().isEmpty()) {
            System.out.println("sender diry save as draft::::::::::");

            // Clear and reuse existing collection
            List<WorkflowProcessSenderDiary> existingList = workflowProcess.getWorkflowProcessSenderDiaries();
            if (existingList == null) {
                existingList = new ArrayList<>();
                workflowProcess.setWorkflowProcessSenderDiaries(existingList);
            } else {
                existingList.clear(); // Important to remove orphans properly
            }
            for (WorkflowProcessSenderDiaryRest d : obj.getWorkflowProcessSenderDiaryRests()) {
                WorkflowProcessSenderDiary diary = workflowProcessSenderDiaryConverter.convert(context, d);
                diary.setWorkflowProcess(workflowProcess1);
                existingList.add(diary);
            }
        }

        // Handle WorkflowProcessSenderDiaryEpeople
        if (obj.getWorkflowProcessSenderDiaryEpersonRests() != null && !obj.getWorkflowProcessSenderDiaryEpersonRests().isEmpty()) {
            System.out.println("save getWorkflowProcessSenderDiaryEpersonRests::::::::::::::");
            List<WorkflowProcessSenderDiaryEperson> existingList = workflowProcess.getWorkflowProcessSenderDiaryEpeople();
            if (existingList != null) {
                existingList.clear(); // Important: clear the existing list to avoid orphan issues
            }
            for (WorkflowProcessSenderDiaryEpersonRest we : obj.getWorkflowProcessSenderDiaryEpersonRests()) {
                if (we != null) {
                    try {
                        WorkflowProcessSenderDiaryEperson converted = workflowProcessSenderDiaryEpersonConverter.convert(context, we);
                        converted.setWorkflowProcess(workflowProcess);
                        existingList.add(converted); // Add to original list
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        // Handle WorkflowProcessEpeople
        if (obj.getWorkflowProcessEpersonRests() != null && !obj.getWorkflowProcessEpersonRests().isEmpty()) {
            System.out.println("save getWorkflowProcessEpersonRests::::::::::::::");

            List<WorkflowProcessEperson> existingList = workflowProcess.getWorkflowProcessEpeople();
            if (existingList != null) {
                existingList.clear(); // Clear old items
            }
            for (WorkflowProcessEpersonRest we : obj.getWorkflowProcessEpersonRests()) {
                if (we != null) {
                    try {
                        we.setSequence(we.getIndex());
                        Optional<WorkFlowProcessMasterValue> userTypeOption = WorkFlowUserType.NORMAL.getUserTypeFromMasterValue(context);
                        WorkflowProcessEperson converted = workFlowProcessEpersonConverter.convert(context, we);
                        if (userTypeOption.isPresent()) {
                            converted.setUsertype(userTypeOption.get());
                        }
                        converted.setOwner(false);
                        converted.setSender(false);
                        converted.setWorkflowProcess(workflowProcess);
                        existingList.add(converted); // Add to same list
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }


        workflowProcess.setIsinternal(obj.getIsinternal());
        workflowProcess.setIssignatorysame(obj.getIssignatorysame());
        return workflowProcess;
    }

//    public WorkFlowProcessRest convertByDashbord(Context context, WorkflowProcess obj, Projection projection) {
//        WorkFlowProcessRest workFlowProcessRest = new WorkFlowProcessRest();
//        if (obj.getWorkflowType() != null && obj.getWorkflowType().getPrimaryvalue() != null) {
//            workFlowProcessRest.setWorkflowtype(obj.getWorkflowType().getPrimaryvalue());
//            workFlowProcessRest.setWorkflowType(workFlowProcessMasterValueConverter.convert(obj.getWorkflowType(), projection));
//        }
//        if (obj.getWorkflowStatus() != null && obj.getWorkflowStatus().getPrimaryvalue() != null) {
//            workFlowProcessRest.setWorkflowStatus(workFlowProcessMasterValueConverter.convert(obj.getWorkflowStatus(), projection));
//            workFlowProcessRest.setWorkflowstatus(obj.getWorkflowStatus().getPrimaryvalue());
//        }
//        if (obj.getSubject() != null) {
//            workFlowProcessRest.setSubject(obj.getSubject());
//        }
//        if (obj.getInitDate() != null) {
//            workFlowProcessRest.setInitDate(obj.getInitDate());
//        }
//        if (obj.getPriority() != null && obj.getPriority().getPrimaryvalue() != null) {
//            workFlowProcessRest.setPriorityRest(workFlowProcessMasterValueConverter.convert(obj.getPriority(), projection));
//            workFlowProcessRest.setPriority(obj.getPriority().getPrimaryvalue());
//        }
//        try {
//            if (!obj.getWorkFlowProcessHistory().isEmpty()) {
//                workFlowProcessRest.setDateRecived(obj.getWorkFlowProcessHistory().get(0).getActionDate());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Optional<WorkflowProcessEperson> ownerRest = obj.getWorkflowProcessEpeople().stream().filter(w -> w.getOwner() != null).filter(w -> w.getOwner()).findFirst();
//        if (ownerRest.isPresent() && ownerRest.get().getAssignDate() != null) {
//            workFlowProcessRest.setDueDate(ownerRest.get().getAssignDate());
//            workFlowProcessRest.setOwner(workFlowProcessEpersonConverter.convert(ownerRest.get(), projection));
//        }
//        if (ownerRest.isPresent() && ownerRest.get() != null && ownerRest.get().getePerson() != null && ownerRest.get().getePerson().getFullName() != null) {
//            List<WorkflowProcessEperson> ownerlist = obj.getWorkflowProcessEpeople().stream().filter(w -> w.getOwner() != null).filter(w -> w.getOwner()).collect(Collectors.toList());
//            StringBuffer sb = new StringBuffer();
//            int i = 0;
//            for (WorkflowProcessEperson ownerRest1 : ownerlist) {
//                if (ownerRest1.getePerson() != null && ownerRest1.getePerson().getFullName() != null) {
//                    String currentrecipent = null;
//                    if (ownerRest1.getUsertype() != null && ownerRest1.getUsertype().getPrimaryvalue() != null && ownerRest1.getUsertype().getPrimaryvalue().equalsIgnoreCase("cc")) {
//                        currentrecipent = ownerRest1.getePerson().getFullName() + "(cc)";
//                    } else {
//                        currentrecipent = ownerRest1.getePerson().getFullName();
//                    }
//                    if (i == 0) {
//                        sb.append(currentrecipent);
//                    } else {
//                        sb.append("," + currentrecipent);
//                    }
//                }
//                i++;
//            }
//            workFlowProcessRest.setCurrentrecipient(sb.toString());
//        }
//        try {
//            Optional<WorkflowProcessEperson> senderRest = obj.getWorkflowProcessEpeople().stream().filter(wn -> wn.getSender() != null).filter(w -> w.getSender()).findFirst();
//            if (senderRest != null && senderRest.isPresent() && senderRest.get() != null && senderRest.get().getePerson() != null && senderRest.get().getePerson().getFullName() != null) {
//                workFlowProcessRest.setSender(workFlowProcessEpersonConverter.convert(senderRest.get(), projection));
//                List<WorkflowProcessEperson> senderlist = obj.getWorkflowProcessEpeople().stream().filter(w -> w.getSender() != null).filter(w -> w.getSender()).collect(Collectors.toList());
//                StringBuffer sb = new StringBuffer();
//                int i = 0;
//                for (WorkflowProcessEperson sender : senderlist) {
//                    if (sender.getePerson() != null && sender.getePerson().getFullName() != null) {
//                        if (i == 0) {
//                            sb.append(sender.getePerson().getFullName());
//                        } else {
//                            sb.append("," + sender.getePerson().getFullName());
//                        }
//                    }
//                    i++;
//                }
//                workFlowProcessRest.setSendername(sb.toString());
//            }
//        } catch (Exception e) {
//            System.out.println("Errorr ::::" + e.getMessage());
//        }
//        if (obj.getWorkFlowProcessInwardDetails() != null && obj.getWorkFlowProcessInwardDetails().getInwardDate() != null) {
//            // workFlowProcessRest.setDateRecived(obj.getWorkFlowProcessInwardDetails().getInwardDate());
//        }
//        if (obj.getWorkFlowProcessInwardDetails() != null) {
//            workFlowProcessRest.setWorkFlowProcessInwardDetailsRest(workFlowProcessInwardDetailsConverter.convert(obj.getWorkFlowProcessInwardDetails(), projection));
//        }
//        if (obj.getWorkFlowProcessOutwardDetails() != null && obj.getWorkFlowProcessOutwardDetails().getOutwardDate() != null) {
//            workFlowProcessRest.setWorkFlowProcessOutwardDetailsRest(workFlowProcessOutwardDetailsConverter.convert(obj.getWorkFlowProcessOutwardDetails(), projection));
//            //workFlowProcessRest.setDateRecived(obj.getWorkFlowProcessOutwardDetails().getOutwardDate());
//        }
//        if (obj.getWorkFlowProcessDraftDetails() != null && obj.getWorkFlowProcessDraftDetails().getDraftdate() != null) {
//            workFlowProcessRest.setWorkFlowProcessDraftDetailsRest(workFlowProcessDraftDetailsConverter.convert(obj.getWorkFlowProcessDraftDetails(), projection));
//            // workFlowProcessRest.setDateRecived(obj.getWorkFlowProcessDraftDetails().getDraftdate());
//        }
//        if (obj.getDispatchmode() != null && obj.getDispatchmode().getPrimaryvalue() != null && obj.getWorkflowType().getPrimaryvalue().equalsIgnoreCase("Inward")) {
//            workFlowProcessRest.setMode(obj.getDispatchmode().getPrimaryvalue());
//        }
//        if (obj.getWorkFlowProcessOutwardDetails() != null && obj.getWorkFlowProcessOutwardDetails().getOutwardmedium() != null && obj.getWorkFlowProcessOutwardDetails().getOutwardmedium().getPrimaryvalue() != null && obj.getWorkflowType().getPrimaryvalue().equalsIgnoreCase("Outward")) {
//            workFlowProcessRest.setMode(obj.getWorkFlowProcessOutwardDetails().getOutwardmedium().getPrimaryvalue());
//        }
//        if (obj.getItem() != null) {
//            try {
//                workFlowProcessRest.setItemRest(itemConverter.convertNameOnly(obj.getItem(), projection));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        if (obj.getRemark() != null) {
//            workFlowProcessRest.setRemark(obj.getRemark());
//        }
//        if (obj.getIsread() != null) {
//            workFlowProcessRest.setIsread(obj.getIsread());
//        }
//        workFlowProcessRest.setUuid(obj.getID().toString());
//        workFlowProcessRest.setIsmode(obj.getIsmode());
//        workFlowProcessRest.setIsreplydraft(obj.getIsreplydraft());
//        workFlowProcessRest.setIssignnote(obj.getIssignnote());
//        workFlowProcessRest.setIsinternal(obj.getIsinternal());
//        workFlowProcessRest.setIssignatorysame(obj.getIssignatorysame());
//        return workFlowProcessRest;
//    }

    public WorkFlowProcessRest convertByDashbord(Context context, WorkflowProcess obj, Projection projection) {
        WorkFlowProcessRest rest = new WorkFlowProcessRest();
        if (obj == null) return rest;

        // --- Cache commonly used collections ---
        List<WorkflowProcessEperson> epeople = Optional.ofNullable(obj.getWorkflowProcessEpeople()).orElse(Collections.emptyList());
        List<WorkflowProcessEperson> owners = epeople.stream()
                .filter(Objects::nonNull)
                .filter(ep -> Boolean.TRUE.equals(ep.getOwner()))
                .collect(Collectors.toList());
        List<WorkflowProcessEperson> senders = epeople.stream()
                .filter(Objects::nonNull)
                .filter(ep -> Boolean.TRUE.equals(ep.getSender()))
                .collect(Collectors.toList());

        // --- WorkflowType, Status, Priority ---
        if(obj.getWorkflowType()!=null&&obj.getWorkflowType().getPrimaryvalue()!=null){
            rest.setWorkflowtype(obj.getWorkflowType().getPrimaryvalue());
        }
        if(obj.getWorkflowStatus()!=null&&obj.getWorkflowStatus().getPrimaryvalue()!=null){
            rest.setWorkflowstatus(obj.getWorkflowStatus().getPrimaryvalue());
        }
        if(obj.getPriority()!=null&&obj.getPriority().getPrimaryvalue()!=null){
            rest.setPriority(obj.getPriority().getPrimaryvalue());
        }
        // --- Simple Field Mappings ---
        rest.setSubject(obj.getSubject());
        rest.setInitDate(obj.getInitDate());
        rest.setRemark(obj.getRemark());
        rest.setIsread(obj.getIsread());
        rest.setUuid(String.valueOf(obj.getID()));
        rest.setIsmode(obj.getIsmode());
        rest.setIsreplydraft(obj.getIsreplydraft());
        rest.setIssignnote(obj.getIssignnote());
        rest.setIsinternal(obj.getIsinternal());
        rest.setIssignatorysame(obj.getIssignatorysame());

        // --- History ---
        obj.getWorkFlowProcessHistory().stream()
                .findFirst()
                .map(WorkFlowProcessHistory::getActionDate)
                .ifPresent(rest::setDateRecived);

        // --- Owner Section ---
        owners.stream().findFirst().ifPresent(owner -> {
            rest.setDueDate(owner.getAssignDate());
            // rest.setOwner(workFlowProcessEpersonConverter.convertByDashbord(owner, projection));

            // Build current recipients string
            String currentRecipients = owners.stream()
                    .map(o -> {
                        String name = Optional.ofNullable(o.getePerson()).map(EPerson::getFullName).orElse(null);
                        if (name == null) return null;
                        boolean isCC = Optional.ofNullable(o.getUsertype())
                                .map(t -> "cc".equalsIgnoreCase(t.getPrimaryvalue()))
                                .orElse(false);
                        return isCC ? name + "(cc)" : name;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            rest.setCurrentrecipient(currentRecipients);
        });

        // --- Department Name ---
        try {
            epeople.stream()
                    .filter(w -> w.getIndex() == 0)
                    .findFirst()
                    .map(WorkflowProcessEperson::getEpersontoepersonmapping)
                    .map(EpersonToEpersonMapping::getEpersonmapping)
                    .map(EpersonMapping::getDepartment)
                    .map(WorkFlowProcessMasterValue::getPrimaryvalue)
                    .ifPresent(rest::setDepartmentname);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        // --- Sender Section ---
        senders.stream().findFirst().ifPresent(sender -> {
            //rest.setSender(workFlowProcessEpersonConverter.convertByDashbord(sender, projection));

            String senderNames = senders.stream().filter(d->d!=null&&d.getePerson()!=null)
                    .map(s -> Optional.ofNullable(s.getePerson()).map(EPerson::getFullName).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            rest.setSendername(senderNames);
        });

        if(obj.getWorkFlowProcessInwardDetails()!=null&&obj.getWorkFlowProcessInwardDetails().getInwardNumber()!=null){
            rest.setInwardnumber(obj.getWorkFlowProcessInwardDetails().getInwardNumber());
        }

        // --- Dispatch Mode / Outward Medium ---
        String workflowType = Optional.ofNullable(obj.getWorkflowType()).map(WorkFlowProcessMasterValue::getPrimaryvalue).orElse("");
        if ("Inward".equalsIgnoreCase(workflowType)) {
            Optional.ofNullable(obj.getDispatchmode())
                    .map(WorkFlowProcessMasterValue::getPrimaryvalue)
                    .ifPresent(rest::setMode);
        } else if ("Outward".equalsIgnoreCase(workflowType)) {
            Optional.ofNullable(obj.getWorkFlowProcessOutwardDetails())
                    .map(WorkFlowProcessOutwardDetails::getOutwardmedium)
                    .map(WorkFlowProcessMasterValue::getPrimaryvalue)
                    .ifPresent(rest::setMode);
        }

        // --- Item ---
        Optional.ofNullable(obj.getItem()).ifPresent(item -> {
            try {
                rest.setFilenumber(obj.getItem().getName());
                String subject = itemService.getMetadataFirstValue(obj.getItem(), "dc", "subject", null, null);
                rest.setFilesubject(subject!=null?subject:"NA");
            } catch (Exception ignored) {}
        });

        return rest;
    }

    public WorkFlowProcessRest convertByDashbordSentTo(Context context, WorkflowProcess obj, Projection projection) {
        WorkFlowProcessRest rest = new WorkFlowProcessRest();

        if (obj == null) return rest;

        // Cache commonly used lists
        List<WorkflowProcessEperson> epeople = Optional.ofNullable(obj.getWorkflowProcessEpeople()).orElse(Collections.emptyList());
        List<WorkflowProcessEperson> owners = epeople.stream().filter(WorkflowProcessEperson::getOwner).collect(Collectors.toList());
        List<WorkflowProcessEperson> senders = epeople.stream().filter(WorkflowProcessEperson::getSender).collect(Collectors.toList());

        int totalUsers = Math.max(epeople.size() - 1, 0);
        AtomicInteger currentOwnerIndex = new AtomicInteger(-1);

        // --- Basic Master Conversions ---
        // --- WorkflowType, Status, Priority ---
        if(obj.getWorkflowType()!=null&&obj.getWorkflowType().getPrimaryvalue()!=null){
            rest.setWorkflowtype(obj.getWorkflowType().getPrimaryvalue());
        }
        if(obj.getWorkflowStatus()!=null&&obj.getWorkflowStatus().getPrimaryvalue()!=null){
            rest.setWorkflowstatus(obj.getWorkflowStatus().getPrimaryvalue());
        }
        if(obj.getPriority()!=null&&obj.getPriority().getPrimaryvalue()!=null){
            rest.setPriority(obj.getPriority().getPrimaryvalue());
        }
        rest.setSubject(obj.getSubject());
        rest.setInitDate(obj.getInitDate());
        rest.setRemark(obj.getRemark());
        rest.setIsread(obj.getIsread());
        rest.setUuid(String.valueOf(obj.getID()));
        rest.setIsmode(obj.getIsmode());
        rest.setIsreplydraft(obj.getIsreplydraft());
        rest.setIssignnote(obj.getIssignnote());
        rest.setIsinternal(obj.getIsinternal());
        rest.setIssignatorysame(obj.getIssignatorysame());

        // --- History ---
        obj.getWorkFlowProcessHistory().stream()
                .findFirst()
                .map(WorkFlowProcessHistory::getActionDate)
                .ifPresent(rest::setDateRecived);

        // --- Owner ---
        owners.stream().findFirst().ifPresent(owner -> {
            currentOwnerIndex.set(owner.getIndex());
            rest.setDueDate(owner.getAssignDate());
            //rest.setOwner(workFlowProcessEpersonConverter.convert(owner, projection));

            String currentRecipients = owners.stream()
                    .map(o -> {
                        String name = o.getePerson() != null ? o.getePerson().getFullName() : null;
                        if (name == null) return null;
                        boolean isCC = Optional.ofNullable(o.getUsertype())
                                .map(t -> "cc".equalsIgnoreCase(t.getPrimaryvalue()))
                                .orElse(false);
                        return isCC ? name + "(cc)" : name;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            rest.setCurrentrecipient(currentRecipients);
        });

        // --- Sender ---
        senders.stream().findFirst().ifPresent(sender -> {
            int currentSenderIndex = sender.getIndex();

            // Check callback logic
            if ((currentOwnerIndex.get() > currentSenderIndex && epeople.stream().anyMatch(w -> w.getOwner() && w.getIndex() == currentSenderIndex + 1))
                    || (currentSenderIndex == 0 && totalUsers == currentOwnerIndex.get())
                    || (currentOwnerIndex.get() == 0 && currentSenderIndex > 0)) {
                rest.setIscallback(true);
            }

            // rest.setSender(workFlowProcessEpersonConverter.convert(sender, projection));

            String senderNames = senders.stream()
                    .map(s -> s.getePerson() != null ? s.getePerson().getFullName() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            rest.setSendername(senderNames);
        });

//        // --- Inward / Outward / Draft Details ---
//        Optional.ofNullable(obj.getWorkFlowProcessInwardDetails())
//                .ifPresent(d -> rest.setWorkFlowProcessInwardDetailsRest(workFlowProcessInwardDetailsConverter.convert(d, projection)));
//
//        Optional.ofNullable(obj.getWorkFlowProcessOutwardDetails())
//                .ifPresent(d -> rest.setWorkFlowProcessOutwardDetailsRest(workFlowProcessOutwardDetailsConverter.convert(d, projection)));
//
//        Optional.ofNullable(obj.getWorkFlowProcessDraftDetails())
//                .ifPresent(d -> rest.setWorkFlowProcessDraftDetailsRest(workFlowProcessDraftDetailsConverter.convert(d, projection)));

        if(obj.getWorkFlowProcessInwardDetails()!=null&&obj.getWorkFlowProcessInwardDetails().getInwardNumber()!=null){
            rest.setInwardnumber(obj.getWorkFlowProcessInwardDetails().getInwardNumber());
        }
        // --- Dispatch / Mode ---
        if ("Inward".equalsIgnoreCase(Optional.ofNullable(obj.getWorkflowType()).map(WorkFlowProcessMasterValue::getPrimaryvalue).orElse(""))) {
            Optional.ofNullable(obj.getDispatchmode())
                    .map(WorkFlowProcessMasterValue::getPrimaryvalue)
                    .ifPresent(rest::setMode);
        } else if ("Outward".equalsIgnoreCase(Optional.ofNullable(obj.getWorkflowType()).map(WorkFlowProcessMasterValue::getPrimaryvalue).orElse(""))) {
            Optional.ofNullable(obj.getWorkFlowProcessOutwardDetails())
                    .map(WorkFlowProcessOutwardDetails::getOutwardmedium)
                    .map(WorkFlowProcessMasterValue::getPrimaryvalue)
                    .ifPresent(rest::setMode);
        }

        // --- Item ---
        Optional.ofNullable(obj.getItem()).ifPresent(item -> {
            try {
                rest.setFilenumber(obj.getItem().getName());
                String subject = itemService.getMetadataFirstValue(obj.getItem(), "dc", "subject", null, null);
                rest.setFilesubject(subject!=null?subject:"NA");
            } catch (Exception ignored) {}
        });

        // --- Department Name ---
        try {
            obj.getWorkflowProcessEpeople().stream()
                    .filter(w -> w.getIndex() == 0)
                    .findFirst()
                    .map(WorkflowProcessEperson::getEpersontoepersonmapping)
                    .map(EpersonToEpersonMapping::getEpersonmapping)
                    .map(EpersonMapping::getDepartment)
                    .map(WorkFlowProcessMasterValue::getPrimaryvalue)
                    .ifPresentOrElse(rest::setDepartmentname, () -> rest.setDepartmentname("NA"));
        } catch (Exception e) {
            rest.setDepartmentname("NA");
        }

        return rest;
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
