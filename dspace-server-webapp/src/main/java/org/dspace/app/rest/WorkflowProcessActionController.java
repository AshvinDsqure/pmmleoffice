/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.*;
import org.dspace.app.rest.enums.WorkFlowAction;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.enums.WorkFlowUserType;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.jbpm.JbpmServerImpl;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.repository.AbstractDSpaceRestRepository;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.app.rest.repository.LinkRestRepository;
import org.dspace.app.rest.utils.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.disseminate.service.CitationDocumentService;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.services.ConfigurationService;
import org.dspace.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.dspace.app.rest.utils.RegexUtils.REGEX_REQUESTMAPPING_IDENTIFIER_AS_UUID;

/**
 * This is a specialized controller to provide access to the bitstream binary
 * content
 * <p>
 * The mapping for requested endpoint try to resolve a valid UUID, for example
 * <pre>
 * {@code
 * https://<dspace.server.url>/api/core/bitstreams/26453b4d-e513-44e8-8d5b-395f62972eff/content
 * }
 * </pre>
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 * @author Tom Desair (tom dot desair at atmire dot com)
 * @author Frederic Van Reet (frederic dot vanreet at atmire dot com)
 */
@RestController
@RequestMapping("/api/" + WorkFlowProcessRest.CATEGORY + REGEX_REQUESTMAPPING_IDENTIFIER_AS_UUID)
public class WorkflowProcessActionController extends AbstractDSpaceRestRepository implements LinkRestRepository {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkflowProcessActionController.class);
    private static final int BUFFER_SIZE = 4096 * 10;
    @Autowired
    WorkflowProcessService workflowProcessService;

    @Autowired
    ItemConverter itemConverter;
    @Autowired
    WorkFlowProcessConverter workFlowProcessConverter;

    @Autowired
    WorkflowProcessReferenceDocVersionService workflowProcessReferenceDocVersionService;

    @Autowired
    WorkflowProcessNoteService workflowProcessNoteService;
    @Autowired
    BundleRestRepository bundleRestRepository;

    @Autowired
    CitationDocumentService citationDocumentService;

    @Autowired
    private FeedbackService feedbackService;


    @Autowired
    ConfigurationService configurationService;
    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterValueService;
    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;
    @Autowired
    WorkflowProcessSenderDiaryConverter workflowProcessSenderDiaryConverter;

    @Autowired
    WorkFlowProcessCommentService workFlowProcessCommentService;
    @Autowired
    WorkFlowProcessMasterService workFlowProcessMasterService;
    @Autowired
    WorkflowProcessReferenceDocService workflowProcessReferenceDocService;

    @Autowired
    WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;

    @Autowired
    WorkFlowProcessEpersonConverter workFlowProcessEpersonConverter;
    @Autowired
    GroupConverter groupConverter;
    @Autowired
    WorkFlowProcessHistoryService workFlowProcessHistoryService;

    @Autowired
    WorkflowProcessEpersonService workflowProcessEpersonService;

    @Autowired
    WorkFlowProcessDraftDetailsService workFlowProcessDraftDetailsService;
    @Autowired
    private BundleService bundleService;
    @Autowired
    JbpmServerImpl jbpmServer;
    @Autowired
    BitstreamService bitstreamService;

    @Autowired
    EPersonConverter ePersonConverter;


    @Autowired
    EventService eventService;
    @Autowired
    MetadataFieldService metadataFieldService;
    @Autowired
    MetadataValueService metadataValueService;

    @Autowired
    WorkFlowProcessOutwardDetailsConverter outwardDetailsConverter;

    private static Font COURIER = new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
    private static Font COURIER_SMALL = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static Font COURIER_SMALL_FOOTER = new Font(Font.FontFamily.UNDEFINED, 10, Font.NORMAL);
    private static Font COURIER_SMALL_FOOTER1 = new Font(Font.FontFamily.COURIER, 10, Font.BOLD);

    private Boolean isslip = false;

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "forwordDraft")
    public WorkFlowProcessRest forwordDraft(@PathVariable UUID uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        WorkFlowProcessRest workFlowProcessRest = null;
        log.info("in Forward Action start");
        try {
            Context context = ContextUtil.obtainContext(request);
            ObjectMapper mapper = new ObjectMapper();
            workFlowProcessRest = mapper.readValue(request.getInputStream(), WorkFlowProcessRest.class);
            String comment = workFlowProcessRest.getComment();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            if (workFlowProcessRest != null && workFlowProcessRest.getItemRest() != null) {
                workFlowProcess.setItem(itemConverter.convert(workFlowProcessRest.getItemRest(), context));
            }
           /* if(workFlowProcessRest.getDispatchModeRest()!=null){
                String name=workFlowProcessMasterValueConverter.convert(context,workFlowProcessRest.getDispatchModeRest()).getPrimaryvalue();
                    if(name!=null &&workFlowProcess.getDispatchmode()!=null && workFlowProcess.getDispatchmode().getPrimaryvalue()!=null && workFlowProcess.getDispatchmode().getPrimaryvalue().equalsIgnoreCase(name)){

                    }
            }*/
            List<String> olduser = null;
            List<WorkflowProcessEperson> olduserlistuuid = workFlowProcess.getWorkflowProcessEpeople().stream().filter(d -> !d.getIssequence()).collect(Collectors.toList());
            List<WorkflowProcessEperson> olduserlistuuidissequenstrue = workFlowProcess.getWorkflowProcessEpeople().stream().collect(Collectors.toList());

            if (olduserlistuuid != null && olduserlistuuid.size() != 0) {
                olduser = olduserlistuuid.stream().filter(d -> d.getePerson() != null).filter(d -> d.getePerson().getID() != null).filter(d -> !d.getIssequence()).map(d -> d.getePerson().getID().toString()).collect(Collectors.toList());
            }
            if (workFlowProcessRest.getWorkflowProcessEpersonRests() != null) {
                List<WorkflowProcessEpersonRest> WorkflowProcessEpersonRestList = workFlowProcessRest.getWorkflowProcessEpersonRests().stream().filter(d -> !d.getIssequence()).collect(Collectors.toList());
                for (WorkflowProcessEpersonRest newEpesonrest : WorkflowProcessEpersonRestList) {
                    WorkflowProcessEperson workflowProcessEperson = workFlowProcessEpersonConverter.convert(context, newEpesonrest);
                    workflowProcessEperson.setWorkflowProcess(workFlowProcess);
                    Optional<WorkFlowProcessMasterValue> userTypeOption = WorkFlowUserType.NORMAL.getUserTypeFromMasterValue(context);
                    if (userTypeOption.isPresent()) {
                        workflowProcessEperson.setUsertype(userTypeOption.get());
                    }
                    if (newEpesonrest.getePersonRest() != null && newEpesonrest.getePersonRest().getId() != null && olduser != null && olduser.contains(newEpesonrest.getePersonRest().getId())) {
                        System.out.println(":::::::::ALLREADY USE EPERSON IN SYSTEM");
                    } else {
                        System.out.println("ADD NEW USER IN WORKFLOWEPERSON LIST");
                        System.out.println("New user index  : " + workflowProcessEperson.getIndex());
                        workFlowProcess.setnewUser(workflowProcessEperson);
                        workflowProcessService.create(context, workFlowProcess);
                    }
                }
            }
            WorkFlowAction action = WorkFlowAction.FORWARD;
            //user not select any next user then flow go initiator
            if (olduser == null && workFlowProcessRest.getWorkflowProcessEpersonRests().size() == 0) {
                System.out.println("::::::::::::::::::::::::::::setInitiator :::::::true::::::::::::::::::::");
                Optional<WorkflowProcessEperson> workflowPro = workFlowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getUsertype().getPrimaryvalue().equalsIgnoreCase(WorkFlowUserType.INITIATOR.getAction())).findFirst();
                if (workflowPro.isPresent()) {
                    action.setInitiator(true);
                } else {
                    action.setInitiator(false);
                }
            }
            //one flow completed after next time forward initiator to next user
            if (workFlowProcess.getWorkflowProcessEpeople() != null) {
                Optional<WorkflowProcessEperson> workflowPro = workFlowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getUsertype().getPrimaryvalue().equalsIgnoreCase(WorkFlowUserType.INITIATOR.getAction())).findFirst();
                if (workflowPro.isPresent() && workflowPro.get().getePerson().getID().toString().equalsIgnoreCase(context.getCurrentUser().getID().toString())) {
                    System.out.println("::::::::::::::::::::::::::::setInitiatorForward::::::::true::::::::::::::::::::");
                    action.setInitiatorForward(true);
                } else {
                    action.setInitiatorForward(false);
                }
            }
            if (comment != null) {
                action.setComment(comment);
                if (workFlowProcessRest.getWorkflowProcessReferenceDocRests() != null && workFlowProcessRest.getWorkflowProcessReferenceDocRests().size() != 0) {
                    List<WorkflowProcessReferenceDoc> doc = getCommentDocuments(context, workFlowProcessRest);
                    if (doc != null) {
                        action.setWorkflowProcessReferenceDocs(doc);
                    }
                }
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            action.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            context.commit();
            action.setComment(null);
            action.setWorkflowProcessReferenceDocs(null);
            action.setInitiator(false);
            log.info("in Forward Action stop");
            return workFlowProcessRest;

        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new UnprocessableEntityException("error in forwardTask Server..");
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "backward")
    public WorkFlowProcessRest backward(@PathVariable UUID uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        log.info("in Backward Action start");
        WorkFlowProcessRest workFlowProcessRest = null;
        WorkflowProcessEpersonRest workflowProcessEpersonRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            ObjectMapper mapper = new ObjectMapper();
            workflowProcessEpersonRest = mapper.readValue(request.getInputStream(), WorkflowProcessEpersonRest.class);
            String comment = workflowProcessEpersonRest.getComment();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            if (workFlowProcess.getItem() == null && workflowProcessEpersonRest.getItemRest() != null) {
                workFlowProcess.setItem(itemConverter.convert(workflowProcessEpersonRest.getItemRest(), context));
            }
            int index = workFlowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getePerson() != null).filter(d -> d.getePerson().getID() != null).filter(d -> d.getePerson().getID().toString().equalsIgnoreCase(context.getCurrentUser().getID().toString())).findFirst().get().getIndex();
            WorkflowProcessEperson workflowProcessEperson = workFlowProcess.getWorkflowProcessEpeople().get(index - 1);
            if (workflowProcessEperson != null && workflowProcessEperson.getIsrefer()) {
                System.out.println("::::::::::::::::::::::::::::REFER USER ::::::::::::::::::::::::::::::::");
                Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.REFER.getUserTypeFromMasterValue(context);
                if (workFlowTypeStatus.isPresent()) {
                    workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
                }
            } else {
                System.out.println("::::::::::::::::::::::::::::NORMAL USER ::::::::::::::::::::::::::::::::");
                Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context);
                if (workFlowTypeStatus.isPresent()) {
                    workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
                }
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction backward = WorkFlowAction.BACKWARD;
            if (comment != null) {
                backward.setComment(comment);
                if (workflowProcessEpersonRest.getWorkflowProcessReferenceDocRests() != null && workflowProcessEpersonRest.getWorkflowProcessReferenceDocRests().size() != 0) {
                    List<WorkflowProcessReferenceDoc> doc = getCommentDocumentsByEpersion(context, workflowProcessEpersonRest);
                    if (doc != null) {
                        backward.setWorkflowProcessReferenceDocs(doc);
                    }
                }
            }
            backward.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            context.commit();
            backward.setComment(null);
            backward.setWorkflowProcessReferenceDocs(null);
            log.info("in Backward Action stop!");
            return workFlowProcessRest;
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnprocessableEntityException("error in backwar Server..");
        }
    }


    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.DELETE, RequestMethod.HEAD}, value = "deleteitem")
    public void deleteItem(@PathVariable UUID uuid, HttpServletRequest request) throws Exception {
        log.info("in deleteItem Action start");
        try {
            Context context = ContextUtil.obtainContext(request);
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            if (workFlowProcess.getItem() != null) {
                workFlowProcess.setItem(null);
                workflowProcessService.update(context, workFlowProcess);
                context.commit();
                System.out.println("Item Deleted!");
            } else {
                System.out.println("Item All ready Deleted");
            }
            log.info("in deleteItem Action stop!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.DELETE, RequestMethod.HEAD}, value = "discard")
    public void discard(@PathVariable UUID uuid, HttpServletRequest request) throws Exception {
        log.info("in discard Action start!");
        try {
            Context context = ContextUtil.obtainContext(request);
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            if (workFlowProcess != null && workFlowProcess.getIsdelete() != null && !workFlowProcess.getIsdelete()) {
                workFlowProcess.setIsdelete(true);
                workflowProcessService.create(context, workFlowProcess);
                context.commit();
                System.out.println("WorkflowProcess Deleted!");
            } else {
                System.out.println("WorkflowProcess already Deleted");
            }
            log.info("in discard Action stop!");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("error in discard" + e.getMessage());
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "digitalsign")
    public WorkflowProcessReferenceDocRest digitalsign(@PathVariable UUID uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        log.info("in digitalsign Action start!");
        WorkflowProcessReferenceDocRest workflowProcessReferenceDocRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.find(context, uuid);
            if (workflowProcessReferenceDoc != null) {
                workflowProcessReferenceDoc.setIssignature(true);
            }
            WorkflowProcessReferenceDoc workflowProcessReferenceDoc1 = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
            workflowProcessReferenceDocRest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc1, utils.obtainProjection());
            if (workflowProcessReferenceDoc1.getIssignature()) {
                WorkFlowProcessHistory workFlowAction = new WorkFlowProcessHistory();
                WorkflowProcessEperson eperson = new WorkflowProcessEperson();
                WorkflowProcess workflowProcess = workflowProcessReferenceDoc1.getWorkflowProcess();
                eperson.setOwner(true);
                eperson.setePerson(context.getCurrentUser());
                eperson.setWorkflowProcess(workflowProcessReferenceDoc1.getWorkflowProcess());
                eperson.setIndex(workflowProcess.getWorkflowProcessEpeople().stream().map(d -> d.getSequence()).max(Integer::compareTo).get());
                workflowProcess.setnewUser(eperson);
                workflowProcessService.create(context, workflowProcess);
                WorkFlowProcessMaster workFlowProcessMaster = WorkFlowAction.MASTER.getMaster(context);
                WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.UPDATE.getAction(), workFlowProcessMaster);
                workFlowAction.setAction(workFlowProcessMasterValue);
                workFlowAction.setWorkflowProcessEpeople(workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get());
            }
            return workflowProcessReferenceDocRest;
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnprocessableEntityException("error in callback Server..");
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "callback")
    public WorkFlowProcessRest callback(@PathVariable UUID uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        log.info("in callback Action start!");
        WorkFlowProcessRest workFlowProcessRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction CALLBACK = WorkFlowAction.CALLBACK;
            CALLBACK.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            context.commit();
            CALLBACK.setComment(null);
            CALLBACK.setWorkflowProcessReferenceDocs(null);
            log.info("in callback Action stop!");
            return workFlowProcessRest;
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnprocessableEntityException("error in callback Server..");
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "reject")
    public WorkFlowProcessRest reject(@PathVariable UUID uuid, HttpServletRequest request) throws Exception {
        log.info("in reject Action start!");
        WorkFlowProcessRest workFlowProcessRest = null;
        WorkflowProcessEpersonRest workflowProcessEpersonRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            ObjectMapper mapper = new ObjectMapper();
            workflowProcessEpersonRest = mapper.readValue(request.getInputStream(), WorkflowProcessEpersonRest.class);
            String comment = workflowProcessEpersonRest.getComment();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            if (workFlowProcess.getItem() == null && workflowProcessEpersonRest.getItemRest() != null) {
                workFlowProcess.setItem(itemConverter.convert(workflowProcessEpersonRest.getItemRest(), context));
            }
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.REJECTED.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction reject = WorkFlowAction.REJECTED;
            if (comment != null) {
                reject.setComment(comment);
                if (workflowProcessEpersonRest.getWorkflowProcessReferenceDocRests() != null && workflowProcessEpersonRest.getWorkflowProcessReferenceDocRests().size() != 0) {
                    List<WorkflowProcessReferenceDoc> doc = getCommentDocumentsByEpersion(context, workflowProcessEpersonRest);
                    if (doc != null) {
                        reject.setWorkflowProcessReferenceDocs(doc);
                    }
                }
            }
            reject.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            workflowProcessService.create(context, workFlowProcess);
            context.commit();
            reject.setComment(null);
            reject.setWorkflowProcessReferenceDocs(null);
            log.info("in reject Action stop!");
            return workFlowProcessRest;
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Error in in reject Action" + e.getMessage());
        }
        return null;
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "refer")
    public WorkFlowProcessRest refer(@PathVariable UUID uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        log.info("in refer Action start!");
        WorkFlowProcessRest workFlowProcessRest = null;
        WorkflowProcessEpersonRest workflowProcessEpersonRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            ObjectMapper mapper = new ObjectMapper();
            workflowProcessEpersonRest = mapper.readValue(request.getInputStream(), WorkflowProcessEpersonRest.class);
            String comment = workflowProcessEpersonRest.getComment();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            WorkflowProcessEperson workflowProcessEperson = workFlowProcessEpersonConverter.convert(context, workflowProcessEpersonRest);
            workflowProcessEperson.setWorkflowProcess(workFlowProcess);
            workflowProcessEperson.setOwner(true);
            Optional<WorkFlowProcessMasterValue> userTypeOption = WorkFlowUserType.REFER.getUserTypeFromMasterValue(context);
            if (userTypeOption.isPresent()) {
                workflowProcessEperson.setUsertype(userTypeOption.get());
            }
            if (workFlowProcess.getItem() == null && workflowProcessEpersonRest.getItemRest() != null) {
                workFlowProcess.setItem(itemConverter.convert(workflowProcessEpersonRest.getItemRest(), context));
            }
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.REFER.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            workFlowProcess.setnewUser(workflowProcessEperson);
            workflowProcessService.create(context, workFlowProcess);
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction refer = WorkFlowAction.REFER;
            refer.setIsrefer(true);
            if (comment != null) {
                refer.setComment(comment);
                if (workflowProcessEpersonRest.getWorkflowProcessReferenceDocRests() != null && workflowProcessEpersonRest.getWorkflowProcessReferenceDocRests().size() != 0) {
                    List<WorkflowProcessReferenceDoc> doc = getCommentDocumentsByEpersion(context, workflowProcessEpersonRest);
                    if (doc != null) {
                        refer.setWorkflowProcessReferenceDocs(doc);
                    }
                }
            }
            refer.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            context.commit();
            refer.setComment(null);
            refer.setWorkflowProcessReferenceDocs(null);
            refer.setIsrefer(false);
            log.info("in refer Action stop!");
            return workFlowProcessRest;
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Error refer Action is" + e.getMessage());
            throw new UnprocessableEntityException("error in forwardTask Server..");
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "received")
    public WorkFlowProcessRest received(@PathVariable UUID uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        log.info("in received Action start!");
        WorkFlowProcessRest workFlowProcessRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            workFlowProcess.setIsmode(true);
            workflowProcessService.update(context, workFlowProcess);
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction received = WorkFlowAction.RECEIVED;
            received.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            context.commit();
            received.setComment(null);
            received.setWorkflowProcessReferenceDocs(null);
            received.setIsrefer(false);
            log.info("in received Action stop!");
            return workFlowProcessRest;
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            log.error("in received Action Error" + e.getMessage());
            throw new UnprocessableEntityException("error in forwardTask Server..");
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "completed")
    public WorkFlowProcessRest complete(@PathVariable UUID uuid, HttpServletRequest request) throws Exception {
        log.info("in complete Action start!");
        WorkFlowProcessRest workFlowProcessRest = null;
        WorkflowProcessEpersonRest workflowProcessEpersonRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            ObjectMapper mapper = new ObjectMapper();
            workflowProcessEpersonRest = mapper.readValue(request.getInputStream(), WorkflowProcessEpersonRest.class);
            String comment = workflowProcessEpersonRest.getComment();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            if (workFlowProcess.getEligibleForFiling() != null && workFlowProcess.getEligibleForFiling().getPrimaryvalue() == "Yes" && workFlowProcess.getItem() == null) {
                throw new ResourceNotFoundException("Item ID not found");
            }
            if (workFlowProcess.getItem() == null && workflowProcessEpersonRest.getItemRest() != null) {
                workFlowProcess.setItem(itemConverter.convert(workflowProcessEpersonRest.getItemRest(), context));
            }
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            Item item = workFlowProcess.getItem();
            if (item != null) {
                workFlowProcess.getWorkflowProcessReferenceDocs().forEach(wd -> {
                    try {
                        if (wd.getDrafttype() != null && wd.getDrafttype().getPrimaryvalue() != null && !wd.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Document")) {
                            workflowProcessService.storeWorkFlowMataDataTOBitsream(context, wd, item);
                        }
                        if (wd.getDrafttype() != null && wd.getDrafttype().getPrimaryvalue() != null && !wd.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Inward")) {
                            workflowProcessService.storeWorkFlowMataDataTOBitsream(context, wd, item);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (AuthorizeException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                System.out.println("Item note selected");
            }

            workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            if (workFlowProcess.getWorkflowType().getPrimaryvalue().equals("Draft")) {
                //document Forward To Segnitor
                documentForwardToSegnitor(context, workFlowProcess);
            }
            workflowProcessService.create(context, workFlowProcess);
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction COMPLETE = WorkFlowAction.COMPLETE;
            if (comment != null) {
                COMPLETE.setComment(comment);
                if (workflowProcessEpersonRest.getWorkflowProcessReferenceDocRests() != null && workflowProcessEpersonRest.getWorkflowProcessReferenceDocRests().size() != 0) {
                    List<WorkflowProcessReferenceDoc> doc = getCommentDocumentsByEpersion(context, workflowProcessEpersonRest);
                    if (doc != null) {
                        COMPLETE.setWorkflowProcessReferenceDocs(doc);
                    }
                }
            }
            COMPLETE.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            if (workFlowProcess.getWorkflowType().getPrimaryvalue().equals("Draft")) {
                WorkflowProcessReferenceDoc notedoc = createFinalNote(context, workFlowProcess);
                if (notedoc != null && item != null) {
                    workflowProcessService.storeWorkFlowMataDataTOBitsream(context, notedoc, item);
                }
            }
            context.commit();
            COMPLETE.setComment(null);
            COMPLETE.setWorkflowProcessReferenceDocs(null);
            log.info("in complete Action stop!");
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            log.error("in complete Action error!" + e.getMessage());
            throw new RuntimeException(e);
        }
        return workFlowProcessRest;
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "dispatch")
    public WorkFlowProcessRest dispatch(@PathVariable UUID uuid, HttpServletRequest request, @RequestBody CommentRest comment) throws IOException, SQLException, AuthorizeException {

        log.info("in dispatch Action Start!");
        WorkFlowProcessRest workFlowProcessRest = null;
        WorkflowProcessEpersonRest workflowProcessEpersonRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.DISPATCH.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            if (workFlowProcess.getWorkFlowProcessOutwardDetails() != null && workFlowProcess.getWorkFlowProcessOutwardDetails().getOutwardDepartment() != null) {
                workFlowProcess.getWorkFlowProcessOutwardDetails().getOutwardDepartment().getMembers().forEach(e -> {
                    System.out.println("eperson:::" + e.getEmail());
                    WorkflowProcessEperson workflowProcessEpersonFromGroup = workFlowProcessEpersonConverter.convert(context, e);
                    try {
                        WorkflowProcessEperson workflowProcessEpersonmax = workFlowProcess.getWorkflowProcessEpeople().stream().max(Comparator.comparing(WorkflowProcessEperson::getIndex)).orElseThrow(NoSuchElementException::new);
                        if (workflowProcessEpersonmax != null && workflowProcessEpersonmax.getIndex() != null) {
                            System.out.println("indexs: ::::::::::" + workflowProcessEpersonmax.getIndex());
                            workflowProcessEpersonFromGroup.setIndex(workflowProcessEpersonmax.getIndex() + 1);
                        }
                        Optional<WorkFlowProcessMasterValue> workFlowUserTypOptional = WorkFlowUserType.DISPATCH.getUserTypeFromMasterValue(context);
                        if (workFlowUserTypOptional.isPresent()) {
                            workflowProcessEpersonFromGroup.setUsertype(workFlowUserTypOptional.get());
                        }
                        workflowProcessEpersonFromGroup.setWorkflowProcess(workFlowProcess);
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                    workFlowProcess.setnewUser(workflowProcessEpersonFromGroup);
                });
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction DISPATCH = WorkFlowAction.DISPATCH;
            if (comment.getComment() != null) {
                DISPATCH.setComment(comment.getComment());
            }
            DISPATCH.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            workflowProcessService.create(context, workFlowProcess);
            DISPATCH.setComment(null);
            context.commit();
            log.info("in dispatch Action Stop!");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return workFlowProcessRest;
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "dispatchreplydraftCRU")
    public WorkFlowProcessRest dispatchreplydraftCRU(@PathVariable UUID uuid, HttpServletRequest request, @RequestBody WorkFlowProcessRest workFlowProcessRests) throws IOException, SQLException, AuthorizeException {
        System.out.println("in dispatchreplydraftCRU Action dispatchreplydraftCRU");
        WorkFlowProcessRest workFlowProcessRest = null;
        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = null;
        Set<WorkflowProcessReferenceDocVersion> workflowProcessReferenceDocVersions = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);

            Optional<WorkflowProcessReferenceDoc> doc = workFlowProcess.getWorkflowProcessReferenceDocs().stream().filter(d -> d.getDrafttype() != null).filter(d -> d.getDrafttype().getPrimaryvalue() != null).filter(d -> d.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reply Tapal")).findFirst();
            if (doc.isPresent()) {
                workflowProcessReferenceDoc = doc.get();
                workflowProcessReferenceDocVersions = workflowProcessReferenceDoc.getWorkflowProcessReferenceDocVersion();
            }
            if (workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest() != null && workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest().getOutwardDepartmentRest() != null) {
                Group g = groupConverter.convert(context, workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest().getOutwardDepartmentRest());
                for (EPerson e : g.getMembers()) {
                    try {
                        if (workflowProcessReferenceDoc != null) {
                            WorkflowProcessReferenceDocVersion version = new WorkflowProcessReferenceDocVersion();
                            version.setCreator(e);
                            Double versionnumber = (double) doc.get().getWorkflowProcessReferenceDocVersion().size() + 1;
                            version.setVersionnumber(versionnumber);
                            version.setIssign(true);
                            version.setWorkflowProcessReferenceDoc(workflowProcessReferenceDoc);
                            workflowProcessReferenceDocVersions.add(version);
                        }
                    } catch (Exception ee) {
                        throw new RuntimeException(ee.getMessage());
                    }
                }
                workflowProcessReferenceDoc.setWorkflowProcessReferenceDocVersion(workflowProcessReferenceDocVersions);
                workflowProcessReferenceDocService.update(context, workflowProcessReferenceDoc);
            }
            WorkFlowProcessDraftDetails draftDetails = workFlowProcess.getWorkFlowProcessDraftDetails();
            if (draftDetails != null) {
                if (draftDetails != null) {
                    draftDetails.setIsdispatchbycru(true);
                    workFlowProcessDraftDetailsService.update(context, draftDetails);
                }
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            workflowProcessService.create(context, workFlowProcess);
            context.commit();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return workFlowProcessRest;
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "dispatchbycrugetDoc")
    public Map<String, String> dispatchbycrugetDoc(@PathVariable String uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        Map<String, String> response = new HashMap<>();
        try {
            Context context = ContextUtil.obtainContext(request);
            WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.find(context, UUID.fromString(uuid));
            if (workflowProcessReferenceDoc != null) {
                WorkflowProcess workFlowProcess = workflowProcessReferenceDoc.getWorkflowProcess();
                if (workFlowProcess != null) {
                    Bitstream slip = createSlip(context, workFlowProcess);
                    if (slip != null) {
                        response.put("slipid", slip.getID().toString());
                        Bitstream bitstream = getMargedDoc(context, workFlowProcess, null);
                        if (bitstream != null) {
                            response.put("mergeddocid", bitstream.getID().toString());
                        }
                    }
                }
            }
            context.commit();
            log.info("in dispatch Stop !");
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("in dispatch Action Error" + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "dispatchbycrugetricipent")
    public RecipientDataDTO dispatchbycrugetricipent(@PathVariable String uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        RecipientDataDTO recipientDataDTO=new RecipientDataDTO();
        List<WorkflowProcessSenderDiaryRest>workflowProcessSenderDiaryRests=new ArrayList<>();
        try {
            Context context = ContextUtil.obtainContext(request);
            WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.find(context, UUID.fromString(uuid));
            if (workflowProcessReferenceDoc != null) {
                WorkflowProcess workFlowProcess = workflowProcessReferenceDoc.getWorkflowProcess();
                if (workFlowProcess != null && workFlowProcess.getWorkflowProcessSenderDiaries() != null) {
                    workflowProcessSenderDiaryRests = workFlowProcess.getWorkflowProcessSenderDiaries().stream().map(d -> {
                        return workflowProcessSenderDiaryConverter.convert(d, utils.obtainProjection());
                    }).collect(Collectors.toList());
                    recipientDataDTO.setWorkflowProcessSenderDiaryRestList(workflowProcessSenderDiaryRests);
                    if(!DateUtils.isNullOrEmptyOrBlank(workFlowProcess.getSubject())){
                        recipientDataDTO.setSubject(workFlowProcess.getSubject());
                    }
                }
            }
            log.info("in dispatch Stop !");
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("in dispatch Action Error" + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return recipientDataDTO;
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "dispatchbySelf")
    public WorkFlowProcessRest dispatchbySelf(@PathVariable String uuid, HttpServletRequest request, @RequestBody WorkFlowProcessRest workFlowProcessRests) throws IOException, SQLException, AuthorizeException {
        WorkFlowProcessRest workFlowProcessRest = null;
        String mrgeddoc = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            String comment = "";
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, UUID.fromString(uuid));
            WorkFlowProcessDraftDetails draftDetails = workFlowProcess.getWorkFlowProcessDraftDetails();
            if (workFlowProcessRests.getWorkflowProcessSenderDiaryRests() != null) {
                List<WorkflowProcessSenderDiary> workflowProcessSenderDiaries = workFlowProcess.getWorkflowProcessSenderDiaries();
                for (WorkflowProcessSenderDiaryRest workflowProcessSenderDiaryrest : workFlowProcessRests.getWorkflowProcessSenderDiaryRests()) {
                    WorkflowProcessSenderDiary workflowProcessSenderDiary = workflowProcessSenderDiaryConverter.convert(context, workflowProcessSenderDiaryrest);
                    workflowProcessSenderDiary.setWorkflowProcess(workFlowProcess);
                    workflowProcessSenderDiaries.add(workflowProcessSenderDiary);
                }
                workFlowProcess.setWorkflowProcessSenderDiaries(workflowProcessSenderDiaries);
            }
            if (workFlowProcessRests.getDispatchModeRest() != null) {
                WorkFlowProcessMasterValue dispatchMode = workFlowProcessMasterValueConverter.convert(context, workFlowProcessRests.getDispatchModeRest());
                if (dispatchMode != null && dispatchMode.getPrimaryvalue() != null && dispatchMode.getPrimaryvalue().equalsIgnoreCase("Electronic")) {
                    try {
                        System.out.println("sent email ");
                        //sent email
                        comment = sentMailElectronic(context, request, workFlowProcess, workFlowProcessRests);
                        //change
                        if (draftDetails != null) {
                            draftDetails.setIsdispatchbyself(true);
                            workFlowProcessDraftDetailsService.update(context, draftDetails);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                } else {
                    Bitstream bitstream = getMargedDoc(context, workFlowProcess, null);
                    if (bitstream != null) {
                        mrgeddoc = bitstream.getID().toString();
                        if (draftDetails != null) {
                            draftDetails.setIsdispatchbycru(true);
                            workFlowProcessDraftDetailsService.update(context, draftDetails);
                        }
                    }
                    if (workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest() != null) {
                        WorkFlowProcessOutwardDetails workFlowProcessOutwardDetails = new WorkFlowProcessOutwardDetails();
                        if (workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest().getAwbno() != null) {
                            workFlowProcessOutwardDetails.setAwbno(workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest().getAwbno());
                        }
                        if (workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest().getServiceprovider() != null) {
                            workFlowProcessOutwardDetails.setServiceprovider(workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest().getServiceprovider());
                        }
                        if (workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest().getDispatchdate() != null) {
                            workFlowProcessOutwardDetails.setDispatchdate(workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest().getDispatchdate());
                        }
                        workFlowProcess.setWorkFlowProcessOutwardDetails(workFlowProcessOutwardDetails);
                    }
                }
            }
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            workflowProcessService.create(context, workFlowProcess);
            storeWorkFlowHistory(context,workFlowProcess);
            context.commit();
            log.info("in dispatch Stop !");
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("in dispatch Action Error" + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        workFlowProcessRest.setMargeddocuuid(mrgeddoc);
        return workFlowProcessRest;
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "acknowledgement")
    public Map<String, String> acknowledgement(@PathVariable String uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        Map<String, String> map = new HashMap<>();
        try {
            Context context = ContextUtil.obtainContext(request);
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, UUID.fromString(uuid));
            AcknowledgementDTO acknowledgement = getAcknowledgementDTO(workFlowProcess);
            Bitstream bitstream = createAcknowledgement(context, acknowledgement);
           if (bitstream != null) {
                map.put("acknowledgementbitstreamid", bitstream.getID().toString());
                sentAcknowledgementEmail(context,bitstream,acknowledgement);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("in dispatch Action Error" + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return map;
    }


    public void sentAcknowledgementEmail(Context context,Bitstream bitstream, AcknowledgementDTO acknowledgementDTO) throws Exception {
        System.out.println("In sentAcknowledgementEmail --"+acknowledgementDTO.getRecipientemail());
        Email email = Email.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "acknowledgement"));
        email.addArgument(acknowledgementDTO.getSubject());
        email.addRecipient(acknowledgementDTO.getRecipientemail());
        email.addArgument(acknowledgementDTO.getRecipientName());               //1
        email.addArgument(acknowledgementDTO.getReceiveddate());                //2
        email.addArgument(acknowledgementDTO.getOffice());                      //3
        email.addArgument(acknowledgementDTO.getRecipientOrganization());       //4
        email.addArgument(acknowledgementDTO.getDepartment());                  //5
        email.addArgument(acknowledgementDTO.getTapalnumber());                 //6
        email.addArgument(context.getCurrentUser().getOffice().getPrimaryvalue());          //7
        email.addArgument(context.getCurrentUser().getDepartment().getPrimaryvalue());      //8
        email.addArgument(context.getCurrentUser().getDesignation().getPrimaryvalue());     //9
        if (bitstream != null) {
            email.addAttachment(bitstreamService.retrieve(context, bitstream), bitstream.getName(), bitstream.getFormat(context).getMIMEType());
            email.send();
        }
        System.out.println("sent   sentAcknowledgementEmail done..!");
    }
    public Bitstream createAcknowledgement(Context context, AcknowledgementDTO acknowledgement) throws Exception {
     final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        Random random = new Random();
        // Generate a random 4-digit number
        int randomNumber = random.nextInt(9000) + 1000;
        File acknowledgementfile = new File(TEMP_DIRECTORY, "Acknowledgement" + randomNumber + ".pdf");
        if (!acknowledgementfile.exists()) {
            try {
                acknowledgementfile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        StringBuffer sb = new StringBuffer("<!DOCTYPE html>\n" + "<html lang=\"en\">\n" + "<head>\n" + "    <meta charset=\"UTF-8\">\n" + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" + "    <title> </title>\n" + "\t<style>@page{size:A4;margin: 0;}\n" + "\t.content{\n" + "\tmargin-left:40px;\n" + "\t}\n" + "\t</style>\n" + "</head>\n" + "<body>");

        sb.append("<div class=\"container\">");
        sb.append("<p style=\"float:right; margin-right:100px;\">" + acknowledgement.getReceiveddate() + "</p>");
        sb.append("<div class=\"content\">");
        sb.append("<p>To,</p>");
        sb.append("<p>" + acknowledgement.getRecipientName() + ",</p>");
        sb.append("<p>" + acknowledgement.getRecipientDesignation() + ",</p>");
        sb.append("<p> " + acknowledgement.getRecipientOrganization() + ",</p>");
        sb.append("<p> " + acknowledgement.getRecipientAddress() + "</p>");
        sb.append("</br>");
        sb.append("<p><B>Subject:</B> Acknowledgement of Your Latter No. <B>" + acknowledgement.getTapalnumber() + "</B></p>");
        sb.append("<p>Dear " + acknowledgement.getRecipientName() + ",</p>");
        sb.append("Your Letter has been recived . For future communication please refer to the this no.<B>" + acknowledgement.getTapalnumber() + "<B></p>");
        sb.append("</br>");
        sb.append("<p>Regards,</p>");
        sb.append("<p>" + acknowledgement.getOffice() + ".</p>");
        sb.append("<p>" + acknowledgement.getDepartment() + ".</p>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</body></html>");
        System.out.println("::::::::::IN isTextEditorFlow :::::::::");
        FileOutputStream files = new FileOutputStream(new File(acknowledgementfile.getAbsolutePath()));
        System.out.println("HTML:::" + sb.toString());
        int result = PdfUtils.HtmlconvertToPdf(sb.toString(), files);
        if (result == 1) {
            System.out.println("HTML CONVERT DONE::::::::::::::: :" + acknowledgementfile.getAbsolutePath());
            FileInputStream outputfile = new FileInputStream(new File(acknowledgementfile.getAbsolutePath()));
            Bitstream bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, outputfile, "", acknowledgementfile.getName());
            return bitstream;
        }
        return null;
    }
    public Bitstream createSlip(Context context, WorkflowProcess workflowProcess) throws Exception {
      // AcknowledgementDTO acknowledgement = getAcknowledgementDTO(workflowProcess);
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        Random random = new Random();
        // Generate a random 4-digit number
        int randomNumber = random.nextInt(9000) + 1000;
        File acknowledgementfile = new File(TEMP_DIRECTORY, "createSlip" + randomNumber + ".pdf");
        if (!acknowledgementfile.exists()) {
            try {
                acknowledgementfile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        StringBuffer sb = new StringBuffer("<!DOCTYPE html>\n" + "<html>\n" + "<head> <style>@page{size:A4;margin: 0;}\n" + ".card {\n" + "  box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2);\n" + "  transition: 0.3s;\n" + "  width: 30%;\n" + "  margin:5px;\n" + "  padding:5px;\n" + "}\n" + "\n" + ".card:hover {\n" + "  box-shadow: 0 8px 16px 0 rgba(0,0,0,0.2);\n" + "}\n" + ".container {\n" + "  padding: 2px 16px;\n" + "display:flex;\n" + "}\n" + "</style>" + "<title>Note</title>\n" + "</head>\n" + "<body style=\"padding-right: 20px;padding-left: 20px;\">");
        sb.append("<br>");
        sb.append("<br>");
        sb.append("<br>");
        sb.append("<div class=\"container\" >");
        List<WorkflowProcessSenderDiary> workflowProcessSenderDiaries = workflowProcess.getWorkflowProcessSenderDiaries();
        if (workflowProcessSenderDiaries != null && workflowProcessSenderDiaries.size() != 0) {
            for (WorkflowProcessSenderDiary to : workflowProcessSenderDiaries) {
                sb.append("<div class=\"card\">");
                sb.append("<h4><b>To. " + (!DateUtils.isNullOrEmptyOrBlank(to.getSendername()) ? to.getSendername() : "-") + "</b></h4>");
                sb.append("<p>" + (!DateUtils.isNullOrEmptyOrBlank(to.getAddress()) ? to.getAddress() : "-") + "</p>");
                sb.append("<p>" + (!DateUtils.isNullOrEmptyOrBlank(to.getOrganization()) ? to.getOrganization() : "-") + "</p>");
                sb.append("<p>" + (!DateUtils.isNullOrEmptyOrBlank(to.getDesignation()) ? to.getDesignation() : "-") + "</p>");
                sb.append("<p>" + (!DateUtils.isNullOrEmptyOrBlank(to.getPincode()) ? to.getPincode() : "-") + "</p>");
                sb.append("</div>");
            }
        }
        sb.append("</div>");
        sb.append("</body></html>");
        System.out.println("::::::::::IN isTextEditorFlow :::::::::");
        FileOutputStream files = new FileOutputStream(new File(acknowledgementfile.getAbsolutePath()));
        System.out.println("HTML:::" + sb.toString());
        int result = PdfUtils.HtmlconvertToPdf(sb.toString(), files);
        if (result == 1) {
            System.out.println("HTML CONVERT DONE::::::::::::::: :" + acknowledgementfile.getAbsolutePath());
            FileInputStream outputfile = new FileInputStream(new File(acknowledgementfile.getAbsolutePath()));
            Bitstream bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, outputfile, "", acknowledgementfile.getName());
            return bitstream;
        }
        return null;
    }

    public AcknowledgementDTO getAcknowledgementDTO(WorkflowProcess workflowProcess) {
        try {
            AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
            if (workflowProcess != null) {
                if (workflowProcess.getWorkFlowProcessInwardDetails() != null) {
                    if (workflowProcess.getWorkFlowProcessInwardDetails().getInwardNumber() != null && !DateUtils.isNullOrEmptyOrBlank(workflowProcess.getWorkFlowProcessInwardDetails().getInwardNumber())) {
                        acknowledgementDTO.setTapalnumber(workflowProcess.getWorkFlowProcessInwardDetails().getInwardNumber());
                    } else {
                        acknowledgementDTO.setTapalnumber("-");
                    }
                }
                if (workflowProcess.getWorkFlowProcessInwardDetails() != null && workflowProcess.getWorkFlowProcessInwardDetails().getReceivedDate() != null) {

                    acknowledgementDTO.setReceiveddate(workflowProcess.getWorkFlowProcessInwardDetails().getReceivedDate().toString());
                } else {
                    acknowledgementDTO.setReceiveddate("-");
                }
            }
            if (!DateUtils.isNullOrEmptyOrBlank(workflowProcess.getSubject())) {
                acknowledgementDTO.setSubject(workflowProcess.getSubject());
            } else {
                acknowledgementDTO.setSubject("-");
            }
            if (workflowProcess.getWorkflowProcessEpeople() != null) {
                Optional<EPerson> creator = workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getIndex() == 0).map(d -> d.getePerson()).findFirst();
                if (creator.isPresent()) {
                    if (creator.get() != null) {
                        if (creator.get().getDepartment() != null && creator.get().getDepartment().getPrimaryvalue() != null) {
                            acknowledgementDTO.setDepartment(creator.get().getDepartment().getPrimaryvalue());
                        } else {
                            acknowledgementDTO.setDepartment("-");
                        }
                        if (creator.get().getOffice() != null && creator.get().getOffice().getPrimaryvalue() != null) {
                            acknowledgementDTO.setOffice(creator.get().getOffice().getPrimaryvalue());
                        } else {
                            acknowledgementDTO.setOffice("-");
                        }
                    }
                }
            }
            if (workflowProcess.getWorkflowProcessSenderDiaries() != null) {
                WorkflowProcessSenderDiary recipient = null;
                Optional<WorkflowProcessSenderDiary> workflowProcessSenderDiary = workflowProcess.getWorkflowProcessSenderDiaries().stream().filter(d -> d.getStatus() == 1).findFirst();
                if (workflowProcessSenderDiary.isPresent()) {
                    recipient = workflowProcessSenderDiary.get();
                    if (!DateUtils.isNullOrEmptyOrBlank(recipient.getSendername())) {
                        acknowledgementDTO.setRecipientName(recipient.getSendername());
                    } else {
                        acknowledgementDTO.setRecipientName("-");
                    }
                    if (!DateUtils.isNullOrEmptyOrBlank(recipient.getDesignation())) {
                        acknowledgementDTO.setRecipientDesignation(recipient.getDesignation());
                    } else {
                        acknowledgementDTO.setRecipientDesignation("-");
                    }
                    if (!DateUtils.isNullOrEmptyOrBlank(recipient.getOrganization())) {
                        acknowledgementDTO.setRecipientOrganization(recipient.getOrganization());
                    } else {
                        acknowledgementDTO.setRecipientOrganization("-");
                    }
                    if (!DateUtils.isNullOrEmptyOrBlank(recipient.getAddress())) {
                        acknowledgementDTO.setRecipientAddress(recipient.getAddress());
                    } else {
                        acknowledgementDTO.setRecipientAddress("-");
                    }if (!DateUtils.isNullOrEmptyOrBlank(recipient.getEmail())) {
                        acknowledgementDTO.setRecipientemail(recipient.getEmail());
                    } else {
                        acknowledgementDTO.setRecipientemail("-");
                    }

                }
            }
            return acknowledgementDTO;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "dispatchCompleteByCRU")
    public WorkFlowProcessRest dispatchCompleteByCRU(@PathVariable String uuid, HttpServletRequest
            request, @RequestBody @Valid WorkFlowProcessOutwardDetailsRest outwardDetailsRest) throws
            IOException, SQLException, AuthorizeException {
        log.info("in dispatch Action Start!");
        WorkFlowProcessRest workFlowProcessRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            String comment = "Dispatch Close";
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, UUID.fromString(uuid));
            if (outwardDetailsRest.getOutwardmediumRest() != null) {
                WorkFlowProcessMasterValue type = workFlowProcessMasterValueConverter.convert(context, outwardDetailsRest.getOutwardmediumRest());
                if (type != null && type.getPrimaryvalue() != null && type.getPrimaryvalue().equalsIgnoreCase("Electronic")) {
                    try {
                        workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
                        comment = sentMailElectronic(context, request, workFlowProcess, workFlowProcessRest);
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                } else {
                    WorkFlowProcessOutwardDetails workFlowProcessOutwardDetails = new WorkFlowProcessOutwardDetails();
                    workFlowProcessOutwardDetails.setAwbno(outwardDetailsRest.getAwbno());
                    workFlowProcessOutwardDetails.setServiceprovider(outwardDetailsRest.getServiceprovider());
                    workFlowProcessOutwardDetails.setDispatchdate(outwardDetailsRest.getDispatchdate());
                    workFlowProcess.setWorkFlowProcessOutwardDetails(workFlowProcessOutwardDetails);
                }
            }
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.DISPATCHCLOSE.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            WorkFlowAction COMPLETE = WorkFlowAction.DISPATCHCLOSE;
            if (comment != null) {
                COMPLETE.setComment(comment);
            }
            COMPLETE.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            workflowProcessService.create(context, workFlowProcess);
            storeWorkFlowHistory(context,workFlowProcess);
            context.commit();
            COMPLETE.setComment(null);
            log.info("in dispatch Stop !");
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("in dispatch Action Error" + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return workFlowProcessRest;
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "dispatchComplete")
    public WorkFlowProcessRest dispatchComplete(@PathVariable String uuid, HttpServletRequest
            request, @RequestBody @Valid WorkFlowProcessOutwardDetailsRest outwardDetailsRest) throws
            IOException, SQLException, AuthorizeException {
        log.info("in dispatch Action Start!");
        WorkFlowProcessRest workFlowProcessRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            String comment = "Close";
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, UUID.fromString(uuid));
            if (workFlowProcess.getWorkFlowProcessOutwardDetails() == null || workFlowProcess.getWorkFlowProcessOutwardDetails().getOutwardDepartment() == null) {
                throw new ResourceNotFoundException("Dispatch not found");
            }
            if (workFlowProcess.getWorkFlowProcessOutwardDetails() != null && workFlowProcess.getWorkFlowProcessOutwardDetails().getOutwardmedium() != null && workFlowProcess.getWorkFlowProcessOutwardDetails().getOutwardmedium().getPrimaryvalue() != null && workFlowProcess.getWorkFlowProcessOutwardDetails().getOutwardmedium().getPrimaryvalue().equalsIgnoreCase("Physical")) {
                WorkFlowProcessOutwardDetails workFlowProcessOutwardDetails = workFlowProcess.getWorkFlowProcessOutwardDetails();
                workFlowProcessOutwardDetails.setAwbno(outwardDetailsRest.getAwbno());
                workFlowProcessOutwardDetails.setServiceprovider(outwardDetailsRest.getServiceprovider());
                workFlowProcessOutwardDetails.setDispatchdate(outwardDetailsRest.getDispatchdate());
                workFlowProcess.setWorkFlowProcessOutwardDetails(workFlowProcessOutwardDetails);
            }
            if (workFlowProcess.getWorkFlowProcessOutwardDetails() != null && workFlowProcess.getWorkFlowProcessOutwardDetails().getOutwardmedium() != null && workFlowProcess.getWorkFlowProcessOutwardDetails().getOutwardmedium().getPrimaryvalue() != null && workFlowProcess.getWorkFlowProcessOutwardDetails().getOutwardmedium().getPrimaryvalue().equalsIgnoreCase("Electronic") && workFlowProcess.getWorkflowProcessSenderDiary() != null && workFlowProcess.getWorkflowProcessSenderDiary().getEmail() != null) {
                String emailid = workFlowProcess.getWorkflowProcessSenderDiary().getEmail();
                System.out.println(":::::::::::::::::::::::sent Mail for this email" + emailid);
                try {
                    feedbackService.sendEmail(context, request, emailid, "no-reply@d2t.co", "Your outward submtion successfully.", "page");
                } catch (IOException | MessagingException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction COMPLETE = WorkFlowAction.COMPLETE;
            if (comment != null) {
                COMPLETE.setComment(comment);
            }
            COMPLETE.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            workflowProcessService.create(context, workFlowProcess);
            context.commit();
            COMPLETE.setComment(null);
            log.info("in dispatch Stop !");
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("in dispatch Action Error" + e.getMessage());
        }
        return workFlowProcessRest;
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "dispatchColose")
    public WorkFlowProcessRest dispatchColose(@PathVariable String uuid, HttpServletRequest request) throws
            IOException, SQLException, AuthorizeException {
        log.info("in dispatchColose Start !");
        WorkFlowProcessRest workFlowProcessRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            String comment = null;
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, UUID.fromString(uuid));
            comment = sentMailElectronic(context, request, workFlowProcess, workFlowProcessRest);
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction COMPLETE = WorkFlowAction.COMPLETE;
            if (comment != null) {
                COMPLETE.setComment(comment);
            }
            COMPLETE.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            workflowProcessService.create(context, workFlowProcess);
            context.commit();
            COMPLETE.setComment(null);
            log.info("in dispatchColose Stop !");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return workFlowProcessRest;
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "suspend")
    public WorkFlowProcessRest suspend(@PathVariable UUID uuid, HttpServletRequest request) throws
            IOException, SQLException {
        log.info("in suspend Start !");
        WorkFlowProcessRest workFlowProcessRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            if (workFlowProcess.getWorkflowType().getPrimaryvalue().equals("Draft")) {
                createFinalNote(context, workFlowProcess);
            }
            if (workFlowProcess == null) {
                throw new RuntimeException("Workflow not found");
            }
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.SUSPEND.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction holdAction = WorkFlowAction.HOLD;

            holdAction.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            workflowProcessService.create(context, workFlowProcess);
            context.commit();
            holdAction.setComment(null);
            log.info("in suspend Stop !");
            return workFlowProcessRest;
        } catch (RuntimeException | ParseException e) {
            throw new UnprocessableEntityException("error in suspendTask Server..");
        } catch (AuthorizeException e) {
            throw new RuntimeException(e);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "resumetask")
    public WorkFlowProcessRest resumetask(@PathVariable UUID uuid, HttpServletRequest request) throws
            IOException, SQLException, AuthorizeException {
        log.info("in resumetask Start !");
        WorkFlowProcessRest workFlowProcessRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            if (workFlowProcess == null) {
                throw new RuntimeException("Workflow not found");
            }
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction unholdAction = WorkFlowAction.UNHOLD;
            unholdAction.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            workflowProcessService.create(context, workFlowProcess);
            context.commit();
            unholdAction.setComment(null);
            log.info("in resumetask Stop !");
            return workFlowProcessRest;
        } catch (RuntimeException e) {
            log.error("in resumetask Error !" + e.getMessage());
            throw new UnprocessableEntityException("error in forwardTask Server..");
        }
    }

    ///createFinalDraftDoc
    public WorkflowProcessReferenceDoc createFinalDraftDoc(Context context, WorkflowProcess workflowProcess) throws
            Exception {
        boolean isTextEditorFlow = false;
        Map<String, Object> map = new HashMap<String, Object>();
        InputStream input2 = null;
        DocToPdfConverter docToPdfConverter = null;
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        System.out.println("start.......createFinalNote");
        StringBuffer sb = new StringBuffer("<!DOCTYPE html>\n" + "<html>\n" + "<head><style>@page{size:A4;margin: 0;}</style>\n" + "<title>Note</title>\n" + "</head>\n" + "<body style=\"padding-right: 20px;padding-left: 20px;background-color:#c5e6c1;\">");
        long notecount = 0;
        if (workflowProcess.getItem() != null && workflowProcess.getItem().getName() != null) {
            UUID statusid = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            notecount = workflowProcessNoteService.getNoteCountNumber(context, workflowProcess.getItem().getID(), statusid);
            map.put("notecount", notecount);
        }
        notecount = notecount + 1;
        File tempFileDoc = new File(TEMP_DIRECTORY, "Note#" + notecount + ".pdf");
        File tempFile1html = new File(TEMP_DIRECTORY, "Note#" + notecount + ".pdf");
        if (!tempFile1html.exists()) {
            try {
                tempFile1html.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (!tempFileDoc.exists()) {
            try {
                tempFileDoc.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.out.println("start.......createFinalNote" + tempFile1html.getAbsolutePath());
        //Items
        sb.append("<p><center> <b>Note # " + notecount + "</b></center></p>");
        for (WorkflowProcessReferenceDoc workflowProcessReferenceDoc : workflowProcess.getWorkflowProcessReferenceDocs()) {
            if (workflowProcessReferenceDoc.getDrafttype() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue().equals("Reply Tapal")) {
                System.out.println("in notsheet pdf");
                List<WorkflowProcessReferenceDocVersion> versions = workflowProcessReferenceDocVersionService.getDocVersionBydocumentID(context, workflowProcessReferenceDoc.getID(), 0, 20);
                Optional<WorkflowProcessReferenceDocVersion> vvv = versions.stream().filter(d -> d.getIsactive()).findFirst();
                WorkflowProcessReferenceDocVersion version = null;
                if (versions != null) {
                    for (WorkflowProcessReferenceDocVersion v : versions) {
                        if (v.getIsactive()) {
                            System.out.println("Active version" + v.getVersionnumber());
                            version = v;
                        }
                    }
                    InputStream out = null;
                    if (version.getBitstream() != null && version.getBitstream().getName() != null) {
                        out = bitstreamService.retrieve(context, version.getBitstream());
                        if (out != null) {
                            MargedDocUtils.DocOneWrite(notecount);
                            MargedDocUtils.DocTwoWrite(out);
                            //DocToPdfConverter.copyInInputStreamToDocx(out);
                        }
                    }
                    if (version.getEditortext() != null && !version.getEditortext().isEmpty()) {
                        if (version != null) {
                            if (version.getEditortext() != null && version.getBitstream() == null) {
                                isTextEditorFlow = true;
                                sb.append("<div>" + version.getEditortext() + "</div>");
                            }
                        }
                    }
                }
            }
        }

        //manager
      /*  if (workflowProcess.getWorkflowProcessNote() != null && workflowProcess.getWorkflowProcessNote().getSubmitter() != null) {
            EPerson creator = workflowProcess.getWorkflowProcessNote().getSubmitter();
            String Designation1 = workFlowProcessMasterValueService.find(context, creator.getDesignation().getID()).getPrimaryvalue();
            List<String> aa = new ArrayList<>();
            aa.add(workflowProcess.getWorkflowProcessNote().getSubmitter().getFullName());
            if (Designation1 != null) {
                aa.add(Designation1);
                sb.append("<br><br><br><div style=\"width:100%;    text-align: right;\">\n" +
                        "<span>" + workflowProcess.getWorkflowProcessNote().getSubmitter().getFullName() + "<br>" + Designation1);
                aa.add(DateFormate(workflowProcess.getWorkflowProcessNote().getInitDate()));
                sb.append("<br>" + DateFormate(workflowProcess.getWorkflowProcessNote().getInitDate()) + "</span></div>");
            }
            map.put("creator", aa);
        }*/
      /*  Map<String, String> referencedocumentmap = null;
        sb.append("<br><br><div style=\"width:100%;\"> ");
        sb.append("<div style=\"width:70%;  float:left;\"> <p><b>Reference Documents</b></p> ");
        //Reference Documents dinamix
        List<Map<String, String>> listreferenceReference = new ArrayList<>();
        for (WorkflowProcessReferenceDoc workflowProcessReferenceDoc : workflowProcess.getWorkflowProcessReferenceDocs()) {
            referencedocumentmap = new HashMap<String, String>();
            if (workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue().equals("Reference Reply")) {
                // InputStream out = null;
                if (workflowProcessReferenceDoc.getBitstream() != null) {
                    String baseurl = configurationService.getProperty("dspace.server.url");
                    referencedocumentmap.put("name", FileUtils.getNameWithoutExtension(workflowProcessReferenceDoc.getBitstream().getName()));
                    referencedocumentmap.put("link", baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content");
                    sb.append("<span style=\"text-align: left;\"><a href=" + baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content>");
                    if (!isTextEditorFlow) {
                        stroremetadateinmap(workflowProcessReferenceDoc.getBitstream(), referencedocumentmap);
                        listreferenceReference.add(referencedocumentmap);
                    } else {
                        stroremetadate(workflowProcessReferenceDoc.getBitstream(), sb);
                    }
                }
            }

        }*/

//        sb.append("</div>");
//        map.put("Reference Documents", listreferenceReference);
//        sb.append("<div style=\"width:23%;float:right;\"> <p><b>Reference Noting</b></p> ");
//        Map<String, String> referencenottingmap = null;
//        List<Map<String, String>> listreferencenotting = new ArrayList<>();
//        for (WorkflowProcessReferenceDoc workflowProcessReferenceDoc : workflowProcess.getWorkflowProcessReferenceDocs()) {
//            referencenottingmap = new HashMap<String, String>();
//            if (workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue().equals("Reference Noting")) {
//                if (workflowProcessReferenceDoc.getBitstream() != null) {
//                    String baseurl = configurationService.getProperty("dspace.server.url");
//                    referencenottingmap.put("name1", FileUtils.getNameWithoutExtension(workflowProcessReferenceDoc.getBitstream().getName()));
//                    referencenottingmap.put("link", baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content");
//                    sb.append("<span style=\"text-align: left;\"><a href=" + baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content>");
//                    if (workflowProcessReferenceDoc.getWorkflowprocessnote() != null && workflowProcessReferenceDoc.getWorkflowprocessnote().getID() != null) {
//                        WorkflowProcessNote note = workflowProcessNoteService.find(context, workflowProcessReferenceDoc.getWorkflowprocessnote().getID());
//                        if (note != null) {
//                            StringBuffer notecreateor = new StringBuffer("Note Creator: ");
//                            if (workflowProcessReferenceDoc.getBitstream().getName() != null) {
//                                sb.append(FileUtils.getNameWithoutExtension(workflowProcessReferenceDoc.getBitstream().getName()) + "</a>");
//                            } else {
//                                sb.append("-</a>");
//                            }
//                            if (note.getSubject() != null) {
//                                referencenottingmap.put("subject", note.getSubject());
//                                sb.append("<br>" + note.getSubject() + "<br>");
//                            } else {
//                                sb.append("<br>-<br>");
//                            }
//                            if (note.getSubmitter() != null && note.getSubmitter().getFullName() != null) {
//                                sb.append("Note Creator: " + note.getSubmitter().getFullName());
//                                notecreateor.append(note.getSubmitter().getFullName() + " ");
//                            } else {
//                                sb.append("Note Creator:<br>-");
//                            }
//                            if (note.getSubmitter() != null && note.getSubmitter().getDesignation() != null && note.getSubmitter().getDesignation().getID() != null) {
//                                String Designation1 = workFlowProcessMasterValueService.find(context, note.getSubmitter().getDesignation().getID()).getPrimaryvalue();
//                                if (Designation1 != null) {
//                                    sb.append(" | " + Designation1);
//                                    notecreateor.append("|" + Designation1);
//                                } else {
//                                    sb.append(" | -");
//                                }
//                            }
//                            if (note.getInitDate() != null) {
//                                sb.append(" " + DateFormate(note.getInitDate()));
//                                notecreateor.append(" " + DateFormate(note.getInitDate()));
//                            } else {
//                                sb.append(" - ");
//                            }
//                            referencenottingmap.put("notecreateor", notecreateor.toString());
//
//                        }
//                        if (workflowProcessReferenceDoc.getItemname() != null) {
//                            sb.append("<br>" + workflowProcessReferenceDoc.getItemname());
//                            referencenottingmap.put("filename", workflowProcessReferenceDoc.getItemname());
//                        }
//                        sb.append("</span><br><br>");
//                    }
//                }
//                listreferencenotting.add(referencenottingmap);
//            }
//        }
//        sb.append("</div></div><br>");
//        map.put("Reference Noting", listreferencenotting);
//        // pending file
//        List<WorkFlowProcessComment> comments = workFlowProcessCommentService.getComments(context, workflowProcess.getID());
//        map.put("comment", comments);
//        sb.append("<h3 style=\"width:100% ;text-align: left; float:left;\"> Comments [" + comments.size() + "]</h3>");
//        int i = 1;
//        for (WorkFlowProcessComment comment : comments) {
//            sb.append("<div style=\"width:100% ;text-align: left; float:left;\">");
//            //coment count
//            sb.append("<p><b>Comment # " + i + "</b></p>");
//
//            //comment text
//            if (comment.getComment() != null) {
//                sb.append("<span>" + comment.getComment() + "</span>");
//            }
//            if (comment.getWorkflowProcessReferenceDoc() != null && comment.getWorkflowProcessReferenceDoc().size() != 0) {
//                sb.append("<br><br>");
//                sb.append("<span><b>Reference Documents.</b></span> <br>");
//                for (WorkflowProcessReferenceDoc workflowProcessReferenceDoc : comment.getWorkflowProcessReferenceDoc()) {
//                    if (workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue().equals("Comment")) {
//                        if (workflowProcessReferenceDoc.getBitstream() != null) {
//                            String baseurl = configurationService.getProperty("dspace.server.url");
//                            sb.append("<span> <a href=" + baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content>");
//                            stroremetadate(workflowProcessReferenceDoc.getBitstream(), sb);
//                        }
//                    }
//                }
//            }
//            sb.append("</div>");
//            //Comment by
//            if (comment.getComment() != null) {
//                sb.append("<div style=\"    float: right;  width:23%\"><b>Comment By</b><br><span>");
//            }
//            //MAnager
//            if (comment.getSubmitter() != null) {
//                if (comment.getSubmitter().getFullName() != null) {
//                    sb.append("<br>" + comment.getSubmitter().getFullName());
//                }
//                if (comment.getSubmitter().getDesignation() != null) {
//                    String Designation = workFlowProcessMasterValueService.find(context, comment.getSubmitter().getDesignation().getID()).getPrimaryvalue();
//                    sb.append("<br>" + Designation);
//                }
//            }
//            //
//            if (comment.getWorkFlowProcessHistory() != null && comment.getWorkFlowProcessHistory().toString() != null) {
//                sb.append("<br>" + DateFormate(comment.getWorkFlowProcessHistory().getActionDate()));
//            }
//            sb.append("</div>");
//            i++;
//        }
        sb.append("</body></html>");
        if (isTextEditorFlow) {
            System.out.println("::::::::::IN isTextEditorFlow :::::::::");
            FileOutputStream files = new FileOutputStream(new File(tempFile1html.getAbsolutePath()));
            System.out.println("HTML:::" + sb.toString());
            int result = PdfUtils.HtmlconvertToPdf(sb.toString(), files);
            System.out.println("HTML CONVERT DONE::::::::::::::: :" + tempFile1html.getAbsolutePath());
            WorkflowProcessReferenceDoc margedoc = new WorkflowProcessReferenceDoc();
            margedoc.setSubject(workflowProcess.getSubject());
            margedoc.setDescription(workflowProcess.getSubject() + " for " + FileUtils.getNameWithoutExtension(tempFile1html.getName()));
            margedoc.setInitdate(new Date());
            WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Draft Type");
            if (workFlowProcessMaster != null) {
                WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, "Note", workFlowProcessMaster);
                if (workFlowProcessMasterValue != null) {
                    margedoc.setDrafttype(workFlowProcessMasterValue);
                }
            }
            margedoc.setWorkflowProcess(workflowProcess);
            FileInputStream outputfile = new FileInputStream(new File(tempFile1html.getAbsolutePath()));
            Bitstream bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, outputfile, "", tempFile1html.getName());
            margedoc.setBitstream(bitstream);
            if (workflowProcess.getWorkflowProcessNote() != null) {
                margedoc.setWorkflowprocessnote(workflowProcess.getWorkflowProcessNote());
                for (WorkflowProcessReferenceDoc d : workflowProcess.getWorkflowProcessReferenceDocs()) {
                    if (d.getDrafttype() != null && d.getDrafttype().getPrimaryvalue() != null && !d.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Notesheet"))
                        d.setWorkflowprocessnote(workflowProcess.getWorkflowProcessNote());
                    workflowProcessReferenceDocService.update(context, d);
                }
            }
            margedoc = workflowProcessReferenceDocService.create(context, margedoc);
            return margedoc;
        } else {
            System.out.println(":::::::::In Document flow:::::::::::::::");
            MargedDocUtils.DocthreWrite(map);
            MargedDocUtils.finalwriteDocument(tempFileDoc.getAbsolutePath());
            //DocToPdfConverter.genarateDocumentFlowNote(map, tempFileDoc.getAbsolutePath(), notecount);
            System.out.println("tempFileDoc:" + tempFileDoc.getAbsolutePath());
            System.out.println("tempFileDoc :" + tempFileDoc.getAbsolutePath());
            WorkflowProcessReferenceDoc margedoc = new WorkflowProcessReferenceDoc();
            margedoc.setSubject(workflowProcess.getSubject());
            margedoc.setDescription(workflowProcess.getSubject() + " for " + tempFileDoc.getName());
            margedoc.setInitdate(new Date());
            margedoc.setReferenceNumber("" + notecount);
            WorkFlowProcessMasterValue doctype = getMastervalueData(context, "Document Type", "Invoice");
            if (doctype != null) {
                margedoc.setWorkFlowProcessReferenceDocType(doctype);
            }
            WorkFlowProcessMasterValue lattercategory = getMastervalueData(context, "Latter Category", "Latter Category 1");
            if (lattercategory != null) {
                margedoc.setLatterCategory(lattercategory);
            }
            WorkFlowProcessMasterValue Drafttype = getMastervalueData(context, "Draft Type", "Note");
            if (Drafttype != null) {
                margedoc.setDrafttype(Drafttype);
            }
            margedoc.setWorkflowProcess(workflowProcess);
            FileInputStream outputfile = new FileInputStream(new File(tempFileDoc.getAbsolutePath()));
            Bitstream bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, outputfile, "", tempFileDoc.getName());
            margedoc.setBitstream(bitstream);
            if (workflowProcess.getWorkflowProcessNote() != null) {
                margedoc.setWorkflowprocessnote(workflowProcess.getWorkflowProcessNote());
                for (WorkflowProcessReferenceDoc d : workflowProcess.getWorkflowProcessReferenceDocs()) {
                    if (d.getDrafttype() != null && d.getDrafttype().getPrimaryvalue() != null && !d.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Notesheet"))
                        d.setWorkflowprocessnote(workflowProcess.getWorkflowProcessNote());
                    workflowProcessReferenceDocService.update(context, d);
                }
            }
            margedoc = workflowProcessReferenceDocService.create(context, margedoc);
            return margedoc;
        }
    }

    public WorkflowProcessReferenceDoc createFinalNote(Context context, WorkflowProcess workflowProcess) throws
            Exception {
        boolean isTextEditorFlow = false;
        Map<String, Object> map = new HashMap<String, Object>();
        InputStream input2 = null;
        DocToPdfConverter docToPdfConverter = null;
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        System.out.println("start.......createFinalNote");
        StringBuffer sb = new StringBuffer("<!DOCTYPE html>\n" + "<html>\n" + "<head><style>@page{size:A4;margin: 0;}</style>\n" + "<title>Note</title>\n" + "</head>\n" + "<body style=\"padding-right: 20px;padding-left: 20px;background-color:#c5e6c1;\">");
        long notecount = 0;
        if (workflowProcess.getItem() != null && workflowProcess.getItem().getName() != null) {
            UUID statusid = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            notecount = workflowProcessNoteService.getNoteCountNumber(context, workflowProcess.getItem().getID(), statusid);
            map.put("notecount", notecount);
        }
        notecount = notecount + 1;
        File tempFileDoc = new File(TEMP_DIRECTORY, "Note#" + notecount + ".pdf");
        File tempFile1html = new File(TEMP_DIRECTORY, "Note#" + notecount + ".pdf");
        if (!tempFile1html.exists()) {
            try {
                tempFile1html.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (!tempFileDoc.exists()) {
            try {
                tempFileDoc.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.out.println("start.......createFinalNote" + tempFile1html.getAbsolutePath());
        //Items
        sb.append("<p><center> <b>Note # " + notecount + "</b></center></p>");
        for (WorkflowProcessReferenceDoc workflowProcessReferenceDoc : workflowProcess.getWorkflowProcessReferenceDocs()) {
            if (workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue().equals("Notesheet")) {
                System.out.println("in notsheet pdf");
                List<WorkflowProcessReferenceDocVersion> versions = workflowProcessReferenceDocVersionService.getDocVersionBydocumentID(context, workflowProcessReferenceDoc.getID(), 0, 20);
                Optional<WorkflowProcessReferenceDocVersion> vvv = versions.stream().filter(d -> d.getIsactive()).findFirst();
                WorkflowProcessReferenceDocVersion version = null;
                if (versions != null) {
                    for (WorkflowProcessReferenceDocVersion v : versions) {
                        if (v.getIsactive()) {
                            System.out.println("Active version" + v.getVersionnumber());
                            version = v;
                        }
                    }
                    InputStream out = null;
                    if (version.getBitstream() != null && version.getBitstream().getName() != null) {
                        out = bitstreamService.retrieve(context, version.getBitstream());
                        if (out != null) {
                            MargedDocUtils.DocOneWrite(notecount);
                            MargedDocUtils.DocTwoWrite(out);
                            //DocToPdfConverter.copyInInputStreamToDocx(out);
                        }
                    }
                    if (version.getEditortext() != null && !version.getEditortext().isEmpty()) {
                        if (version != null) {
                            if (version.getEditortext() != null && version.getBitstream() == null) {
                                isTextEditorFlow = true;
                                sb.append("<div>" + version.getEditortext() + "</div>");
                            }
                        }
                    }
                }
            }
        }

        //manager
        if (workflowProcess.getWorkflowProcessNote() != null && workflowProcess.getWorkflowProcessNote().getSubmitter() != null) {
            EPerson creator = workflowProcess.getWorkflowProcessNote().getSubmitter();
            String Designation1 = workFlowProcessMasterValueService.find(context, creator.getDesignation().getID()).getPrimaryvalue();
            List<String> aa = new ArrayList<>();
            aa.add(workflowProcess.getWorkflowProcessNote().getSubmitter().getFullName());
            if (Designation1 != null) {
                aa.add(Designation1);
                sb.append("<br><br><br><div style=\"width:100%;    text-align: right;\">\n" + "<span>" + workflowProcess.getWorkflowProcessNote().getSubmitter().getFullName() + "<br>" + Designation1);
                aa.add(DateFormate(workflowProcess.getWorkflowProcessNote().getInitDate()));
                sb.append("<br>" + DateFormate(workflowProcess.getWorkflowProcessNote().getInitDate()) + "</span></div>");
            }
            map.put("creator", aa);
        }
        Map<String, String> referencedocumentmap = null;
        sb.append("<br><br><div style=\"width:100%;\"> ");
        sb.append("<div style=\"width:70%;  float:left;\"> <p><b>Reference Documents</b></p> ");
        //Reference Documents dinamix
        List<Map<String, String>> listreferenceReference = new ArrayList<>();
        for (WorkflowProcessReferenceDoc workflowProcessReferenceDoc : workflowProcess.getWorkflowProcessReferenceDocs()) {
            referencedocumentmap = new HashMap<String, String>();
            if (workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue().equals("Reference Document")) {
                // InputStream out = null;
                if (workflowProcessReferenceDoc.getBitstream() != null) {
                    String baseurl = configurationService.getProperty("dspace.server.url");
                    referencedocumentmap.put("name", FileUtils.getNameWithoutExtension(workflowProcessReferenceDoc.getBitstream().getName()));
                    referencedocumentmap.put("link", baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content");
                    sb.append("<span style=\"text-align: left;\"><a href=" + baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content>");
                    if (!isTextEditorFlow) {
                        stroremetadateinmap(workflowProcessReferenceDoc.getBitstream(), referencedocumentmap);
                        listreferenceReference.add(referencedocumentmap);
                    } else {
                        stroremetadate(workflowProcessReferenceDoc.getBitstream(), sb);
                    }
                }
            }

        }

        sb.append("</div>");
        map.put("Reference Documents", listreferenceReference);
        sb.append("<div style=\"width:23%;float:right;\"> <p><b>Reference Noting</b></p> ");
        Map<String, String> referencenottingmap = null;
        List<Map<String, String>> listreferencenotting = new ArrayList<>();
        for (WorkflowProcessReferenceDoc workflowProcessReferenceDoc : workflowProcess.getWorkflowProcessReferenceDocs()) {
            referencenottingmap = new HashMap<String, String>();
            if (workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue().equals("Reference Noting")) {
                if (workflowProcessReferenceDoc.getBitstream() != null) {
                    String baseurl = configurationService.getProperty("dspace.server.url");
                    referencenottingmap.put("name1", FileUtils.getNameWithoutExtension(workflowProcessReferenceDoc.getBitstream().getName()));
                    referencenottingmap.put("link", baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content");
                    sb.append("<span style=\"text-align: left;\"><a href=" + baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content>");
                    if (workflowProcessReferenceDoc.getWorkflowprocessnote() != null && workflowProcessReferenceDoc.getWorkflowprocessnote().getID() != null) {
                        WorkflowProcessNote note = workflowProcessNoteService.find(context, workflowProcessReferenceDoc.getWorkflowprocessnote().getID());
                        if (note != null) {
                            StringBuffer notecreateor = new StringBuffer("Note Creator: ");
                            if (workflowProcessReferenceDoc.getBitstream().getName() != null) {
                                sb.append(FileUtils.getNameWithoutExtension(workflowProcessReferenceDoc.getBitstream().getName()) + "</a>");
                            } else {
                                sb.append("-</a>");
                            }
                            if (note.getSubject() != null) {
                                referencenottingmap.put("subject", note.getSubject());
                                sb.append("<br>" + note.getSubject() + "<br>");
                            } else {
                                sb.append("<br>-<br>");
                            }
                            if (note.getSubmitter() != null && note.getSubmitter().getFullName() != null) {
                                sb.append("Note Creator: " + note.getSubmitter().getFullName());
                                notecreateor.append(note.getSubmitter().getFullName() + " ");
                            } else {
                                sb.append("Note Creator:<br>-");
                            }
                            if (note.getSubmitter() != null && note.getSubmitter().getDesignation() != null && note.getSubmitter().getDesignation().getID() != null) {
                                String Designation1 = workFlowProcessMasterValueService.find(context, note.getSubmitter().getDesignation().getID()).getPrimaryvalue();
                                if (Designation1 != null) {
                                    sb.append(" | " + Designation1);
                                    notecreateor.append("|" + Designation1);
                                } else {
                                    sb.append(" | -");
                                }
                            }
                            if (note.getInitDate() != null) {
                                sb.append(" " + DateFormate(note.getInitDate()));
                                notecreateor.append(" " + DateFormate(note.getInitDate()));
                            } else {
                                sb.append(" - ");
                            }
                            referencenottingmap.put("notecreateor", notecreateor.toString());

                        }
                        if (workflowProcessReferenceDoc.getItemname() != null) {
                            sb.append("<br>" + workflowProcessReferenceDoc.getItemname());
                            referencenottingmap.put("filename", workflowProcessReferenceDoc.getItemname());
                        }
                        sb.append("</span><br><br>");
                    }
                }
                listreferencenotting.add(referencenottingmap);
            }
        }
        sb.append("</div></div><br>");
        map.put("Reference Noting", listreferencenotting);
        // pending file
        List<WorkFlowProcessComment> comments = workFlowProcessCommentService.getComments(context, workflowProcess.getID());
        map.put("comment", comments);
        sb.append("<h3 style=\"width:100% ;text-align: left; float:left;\"> Comments [" + comments.size() + "]</h3>");
        int i = 1;
        for (WorkFlowProcessComment comment : comments) {
            sb.append("<div style=\"width:100% ;text-align: left; float:left;\">");
            //coment count
            sb.append("<p><b>Comment # " + i + "</b></p>");

            //comment text
            if (comment.getComment() != null) {
                sb.append("<span>" + comment.getComment() + "</span>");
            }
            if (comment.getWorkflowProcessReferenceDoc() != null && comment.getWorkflowProcessReferenceDoc().size() != 0) {
                sb.append("<br><br>");
                sb.append("<span><b>Reference Documents.</b></span> <br>");
                for (WorkflowProcessReferenceDoc workflowProcessReferenceDoc : comment.getWorkflowProcessReferenceDoc()) {
                    if (workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue().equals("Comment")) {
                        if (workflowProcessReferenceDoc.getBitstream() != null) {
                            String baseurl = configurationService.getProperty("dspace.server.url");
                            sb.append("<span> <a href=" + baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content>");
                            stroremetadate(workflowProcessReferenceDoc.getBitstream(), sb);
                        }
                    }
                }
            }
            sb.append("</div>");
            //Comment by
            if (comment.getComment() != null) {
                sb.append("<div style=\"    float: right;  width:23%\"><b>Comment By</b><br><span>");
            }
            //MAnager
            if (comment.getSubmitter() != null) {
                if (comment.getSubmitter().getFullName() != null) {
                    sb.append("<br>" + comment.getSubmitter().getFullName());
                }
                if (comment.getSubmitter().getDesignation() != null) {
                    String Designation = workFlowProcessMasterValueService.find(context, comment.getSubmitter().getDesignation().getID()).getPrimaryvalue();
                    sb.append("<br>" + Designation);
                }
            }
            //
            if (comment.getWorkFlowProcessHistory() != null && comment.getWorkFlowProcessHistory().toString() != null) {
                sb.append("<br>" + DateFormate(comment.getWorkFlowProcessHistory().getActionDate()));
            }
            sb.append("</div>");
            i++;
        }
        sb.append("</body></html>");
        if (isTextEditorFlow) {
            System.out.println("::::::::::IN isTextEditorFlow :::::::::");
            FileOutputStream files = new FileOutputStream(new File(tempFile1html.getAbsolutePath()));
            System.out.println("HTML:::" + sb.toString());
            int result = PdfUtils.HtmlconvertToPdf(sb.toString(), files);
            System.out.println("HTML CONVERT DONE::::::::::::::: :" + tempFile1html.getAbsolutePath());
            WorkflowProcessReferenceDoc margedoc = new WorkflowProcessReferenceDoc();
            margedoc.setSubject(workflowProcess.getSubject());
            margedoc.setDescription(workflowProcess.getSubject() + " for " + FileUtils.getNameWithoutExtension(tempFile1html.getName()));
            margedoc.setInitdate(new Date());
            WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Draft Type");
            if (workFlowProcessMaster != null) {
                WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, "Note", workFlowProcessMaster);
                if (workFlowProcessMasterValue != null) {
                    margedoc.setDrafttype(workFlowProcessMasterValue);
                }
            }
            margedoc.setWorkflowProcess(workflowProcess);
            FileInputStream outputfile = new FileInputStream(new File(tempFile1html.getAbsolutePath()));
            Bitstream bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, outputfile, "", tempFile1html.getName());
            margedoc.setBitstream(bitstream);
            if (workflowProcess.getWorkflowProcessNote() != null) {
                margedoc.setWorkflowprocessnote(workflowProcess.getWorkflowProcessNote());
                for (WorkflowProcessReferenceDoc d : workflowProcess.getWorkflowProcessReferenceDocs()) {
                    if (d.getDrafttype() != null && d.getDrafttype().getPrimaryvalue() != null && !d.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Notesheet"))
                        d.setWorkflowprocessnote(workflowProcess.getWorkflowProcessNote());
                    workflowProcessReferenceDocService.update(context, d);
                }
            }
            margedoc = workflowProcessReferenceDocService.create(context, margedoc);
            return margedoc;
        } else {
            System.out.println(":::::::::In Document flow:::::::::::::::");
            MargedDocUtils.DocthreWrite(map);
            MargedDocUtils.finalwriteDocument(tempFileDoc.getAbsolutePath());
            //DocToPdfConverter.genarateDocumentFlowNote(map, tempFileDoc.getAbsolutePath(), notecount);
            System.out.println("tempFileDoc:" + tempFileDoc.getAbsolutePath());
            System.out.println("tempFileDoc :" + tempFileDoc.getAbsolutePath());
            WorkflowProcessReferenceDoc margedoc = new WorkflowProcessReferenceDoc();
            margedoc.setSubject(workflowProcess.getSubject());
            margedoc.setDescription(workflowProcess.getSubject() + " for " + tempFileDoc.getName());
            margedoc.setInitdate(new Date());
            margedoc.setReferenceNumber("" + notecount);
            WorkFlowProcessMasterValue doctype = getMastervalueData(context, "Document Type", "Invoice");
            if (doctype != null) {
                margedoc.setWorkFlowProcessReferenceDocType(doctype);
            }
            WorkFlowProcessMasterValue lattercategory = getMastervalueData(context, "Latter Category", "Latter Category 1");
            if (lattercategory != null) {
                margedoc.setLatterCategory(lattercategory);
            }
            WorkFlowProcessMasterValue Drafttype = getMastervalueData(context, "Draft Type", "Note");
            if (Drafttype != null) {
                margedoc.setDrafttype(Drafttype);
            }
            margedoc.setWorkflowProcess(workflowProcess);
            FileInputStream outputfile = new FileInputStream(new File(tempFileDoc.getAbsolutePath()));
            Bitstream bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, outputfile, "", tempFileDoc.getName());
            margedoc.setBitstream(bitstream);
            if (workflowProcess.getWorkflowProcessNote() != null) {
                margedoc.setWorkflowprocessnote(workflowProcess.getWorkflowProcessNote());
                for (WorkflowProcessReferenceDoc d : workflowProcess.getWorkflowProcessReferenceDocs()) {
                    if (d.getDrafttype() != null && d.getDrafttype().getPrimaryvalue() != null && !d.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Notesheet"))
                        d.setWorkflowprocessnote(workflowProcess.getWorkflowProcessNote());
                    workflowProcessReferenceDocService.update(context, d);
                }
            }
            margedoc = workflowProcessReferenceDocService.create(context, margedoc);
            return margedoc;
        }
    }

    public List<WorkflowProcessReferenceDoc> getCommentDocumentsByEpersion(Context
                                                                                   context, WorkflowProcessEpersonRest erest) {
        List<WorkflowProcessReferenceDoc> docs = null;
        if (erest.getWorkflowProcessReferenceDocRests() != null) {
            docs = erest.getWorkflowProcessReferenceDocRests().stream().map(d -> {
                try {
                    return workflowProcessReferenceDocConverter.convertByService(context, d);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
        }
        return docs;
    }

    public List<WorkflowProcessReferenceDoc> getCommentDocuments(Context context, WorkFlowProcessRest wrest) {
        List<WorkflowProcessReferenceDoc> docs = null;
        if (wrest.getWorkflowProcessReferenceDocRests() != null) {
            if (wrest.getWorkflowProcessReferenceDocRests() != null) {
                docs = wrest.getWorkflowProcessReferenceDocRests().stream().map(d -> {
                    try {
                        return workflowProcessReferenceDocConverter.convertByService(context, d);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
            }
        }

        return docs;
    }

    private static String DateFormate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        return formatter.format(date);
    }

    public WorkFlowProcessMasterValue getMastervalueData(Context context, String mastername, String mastervaluename) throws
            SQLException {
        WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, mastername);
        if (workFlowProcessMaster != null) {
            WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, mastervaluename, workFlowProcessMaster);
            if (workFlowProcessMasterValue != null) {
                System.out.println(" MAster value" + workFlowProcessMasterValue.getPrimaryvalue());
                return workFlowProcessMasterValue;
            }
        }
        return null;
    }

    public void stroremetadateinmap(Bitstream bitstream, Map<String, String> map) throws ParseException {
        if (bitstream.getMetadata() != null) {
            int i = 0;
            String refnumber = null;
            String doctype = null;
            String date = null;
            String lettercategory = null;
            String lettercategoryhindi = null;
            String description = null;
            StringBuffer doctyperefnumber = new StringBuffer();
            StringBuffer datelettercategory = new StringBuffer();

            for (MetadataValue metadataValue : bitstream.getMetadata()) {
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_doc_type")) {
                    doctype = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_ref_number")) {
                    refnumber = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_date")) {
                    date = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_letter_category")) {
                    lettercategory = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_letter_categoryhi")) {
                    lettercategoryhindi = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_description")) {
                    description = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_title")) {
                }
                i++;
            }

            if (doctype != null) {

                doctyperefnumber.append(doctype);
            } else {
                if (bitstream.getName() != null) {

                } else {

                }
            }
            if (refnumber != null) {

                doctyperefnumber.append("(" + refnumber + ")");
            } else {

            }
            if (date != null) {

                datelettercategory.append(DateUtils.strDateToString(date));
            } else {

            }
            if (lettercategory != null && lettercategoryhindi != null) {

                datelettercategory.append(" (" + lettercategory + "|" + lettercategoryhindi + ")");
            } else {

            }
            if (description != null) {

                map.put("description", description);
            } else {

            }
            map.put("datelettercategory", datelettercategory.toString() != null ? datelettercategory.toString() : "-");
            map.put("doctyperefnumber", doctyperefnumber.toString() != null ? doctyperefnumber.toString() : "-");

        }

    }

    public void documentForwardToSegnitor(Context context, WorkflowProcess workFlowProcess) throws Exception {
        try {
            if (workFlowProcess.getWorkFlowProcessDraftDetails() != null) {
                System.out.println("Document Going to  Documentsignator");
                //note doc
                WorkFlowProcessDraftDetails draft = workFlowProcess.getWorkFlowProcessDraftDetails();
                if (draft.getDocumentsignator() != null) {
                    List<WorkflowProcessReferenceDoc> docs = workFlowProcess.getWorkflowProcessReferenceDocs().stream().filter(a -> a.getDrafttype() != null).filter(c -> c.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Document")).collect(Collectors.toList());
                    //rply doc
                    List<WorkflowProcessReferenceDoc> replyReference = workFlowProcess.getWorkflowProcessReferenceDocs().stream().filter(a -> a.getDrafttype() != null).filter(b -> b.getDrafttype().getPrimaryvalue() != null).filter(c -> c.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reference Reply")).collect(Collectors.toList());

                    if (docs != null) {

                        for (WorkflowProcessReferenceDoc doc : docs) {
                            WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.find(context, doc.getID());
                            workflowProcessReferenceDoc.setDocumentsignator(draft.getDocumentsignator());
                            workflowProcessReferenceDoc.setIssignature(false);
                            workflowProcessReferenceDocService.update(context, workflowProcessReferenceDoc);
                            storeWorkFlowHistoryForSignaturePanding(context, doc);
                        }
                        System.out.println("Document out to  Documentsignator");
                    }
                    if (replyReference != null) {
                        System.out.println("in Reference Reply");
                        for (WorkflowProcessReferenceDoc doc : replyReference) {
                            WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.find(context, doc.getID());
                            workflowProcessReferenceDoc.setDocumentsignator(draft.getDocumentsignator());
                            workflowProcessReferenceDoc.setIssignature(false);
                            workflowProcessReferenceDocService.update(context, workflowProcessReferenceDoc);
                            storeWorkFlowHistoryForSignaturePanding(context, doc);
                        }
                        System.out.println("Document out to  Documentsignator");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void storeWorkFlowHistoryForSignaturePanding(Context context, WorkflowProcessReferenceDoc doc) throws
            Exception {
        System.out.println("::::::IN :storeWorkFlowHistoryForSignaturePanding:::::::: ");
        WorkFlowProcessHistory workFlowAction = null;
        WorkflowProcess workflowProcess = doc.getWorkflowProcess();
        workFlowAction = new WorkFlowProcessHistory();
        WorkFlowProcessMaster workFlowProcessMaster = WorkFlowAction.MASTER.getMaster(context);
        workFlowAction.setWorkflowProcessEpeople(workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get());
        WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.PENDING.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setAction(workFlowProcessMasterValue);
        workFlowAction.setWorkflowProcess(workflowProcess);
        // workflowProcess.getWorkflowProcessNote().getSubject();
        workFlowAction.setComment("Document Signature Pending By " + doc.getDocumentsignator().getFullName() + " | " + doc.getDocumentsignator().getDesignation().getPrimaryvalue());
        workFlowProcessHistoryService.create(context, workFlowAction);
        System.out.println("::::::OUT :storeWorkFlowHistoryForSignaturePanding:::: ");
    }

    public void storeWorkFlowHistory(Context context, WorkflowProcessReferenceDoc doc) throws Exception {
        System.out.println("::::::IN :storeWorkFlowHistoryForSignaturePanding:::::::: ");
        WorkFlowProcessHistory workFlowAction = null;
        WorkflowProcess workflowProcess = doc.getWorkflowProcess();
        workFlowAction = new WorkFlowProcessHistory();
        WorkFlowProcessMaster workFlowProcessMaster = WorkFlowAction.MASTER.getMaster(context);
        workFlowAction.setWorkflowProcessEpeople(workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get());
        WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.PENDING.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setAction(workFlowProcessMasterValue);
        workFlowAction.setWorkflowProcess(workflowProcess);
        // workflowProcess.getWorkflowProcessNote().getSubject();
        workFlowAction.setComment("Document Signature Pending By " + doc.getDocumentsignator().getFullName() + " | " + doc.getDocumentsignator().getDesignation().getPrimaryvalue());
        workFlowProcessHistoryService.create(context, workFlowAction);
        System.out.println("::::::OUT :storeWorkFlowHistoryForSignaturePanding:::: ");
    }
    public void storeWorkFlowHistory(Context context, WorkflowProcess workflowProcess) throws Exception {
        System.out.println("::::::IN :storeWorkFlowHistoryForSignaturePanding:::::::: ");
        WorkFlowProcessHistory workFlowAction = null;
        workFlowAction = new WorkFlowProcessHistory();
        WorkFlowProcessMaster workFlowProcessMaster = WorkFlowAction.MASTER.getMaster(context);
//        workFlowAction.setWorkflowProcessEpeople(workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get());
        WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.DISPATCHCLOSE.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setAction(workFlowProcessMasterValue);
        workFlowAction.setWorkflowProcess(workflowProcess);
        // workflowProcess.getWorkflowProcessNote().getSubject();
        workFlowAction.setComment("Dispach By "+context.getCurrentUser().getFullName()+".");
        workFlowProcessHistoryService.create(context, workFlowAction);
        System.out.println("::::::OUT :storeWorkFlowHistoryForSignaturePanding:::: ");
    }

    public void stroremetadate(Bitstream bitstream, StringBuffer sb) throws ParseException {
        if (bitstream.getMetadata() != null) {
            int i = 0;
            String refnumber = null;
            String doctype = null;
            String date = null;
            String lettercategory = null;
            String lettercategoryhindi = null;
            String description = null;
            for (MetadataValue metadataValue : bitstream.getMetadata()) {
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_doc_type")) {
                    doctype = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_ref_number")) {
                    refnumber = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_date")) {
                    date = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_letter_category")) {
                    lettercategory = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_letter_categoryhi")) {
                    lettercategoryhindi = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_description")) {
                    description = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_title")) {
                }
                i++;
            }

            if (doctype != null) {
                sb.append(doctype + "</a>");
            } else {
                if (bitstream.getName() != null) {
                    sb.append(FileUtils.getNameWithoutExtension(bitstream.getName()) + "</a>");

                } else {
                    sb.append("-</a>");
                }
            }
            if (refnumber != null) {
                sb.append(" (" + refnumber + ")");
            } else {
                sb.append("-");
            }
            if (date != null) {
                sb.append("<br>" + DateUtils.strDateToString(date));
            } else {
                sb.append("<br>-");
            }
            if (lettercategory != null && lettercategoryhindi != null) {
                sb.append(" (" + lettercategory + "|" + lettercategoryhindi + ")");
            } else {
                sb.append("(-)");
            }
            if (description != null) {
                sb.append("<br>" + description);
            } else {
                sb.append("<br> -");
            }
            sb.append("</span><br><br>");
        }

    }

    public String sentMailElectronic(Context context, HttpServletRequest request, WorkflowProcess
            workFlowProcess, WorkFlowProcessRest workFlowProcessRest) {
        EPerson currentuser = context.getCurrentUser();
        String comment = null;
        String receivername = null;
        String subject = null;
        String body = null;
        String recipientEmail = null;
        String senderName = null;
        String senderDesignation = null;
        String senderDepartment = null;
        String senderOffice = null;
        List<WorkflowProcessSenderDiary> workflowProcessSenderDiaries_to = null;
        List<WorkflowProcessSenderDiary> workflowProcessSenderDiaries_cc = null;
        List<String> emailids1 = null;

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
        if (workFlowProcessRest.getBody() != null) {
            body = workFlowProcessRest.getBody();
        }
        //outward flow
        if (workFlowProcess != null && workFlowProcess.getWorkFlowProcessOutwardDetails() != null && workFlowProcess.getWorkFlowProcessOutwardDetails().getOutwardmedium() != null && workFlowProcess.getWorkFlowProcessOutwardDetails().getOutwardmedium().getPrimaryvalue() != null && workFlowProcess.getWorkFlowProcessOutwardDetails().getOutwardmedium().getPrimaryvalue().equalsIgnoreCase("Electronic") && workFlowProcess.getWorkflowProcessSenderDiary() != null && workFlowProcess.getWorkflowProcessSenderDiary().getEmail() != null) {
            System.out.println("::::::::::::::::::::::::Sent mail.... ::::::::::::::::::::::::::::::");
            if (workFlowProcess.getWorkflowProcessSenderDiary().getEmail() != null) {
                recipientEmail = workFlowProcess.getWorkflowProcessSenderDiary().getEmail();
                System.out.println("::::::::::::::::::::::::Sent mail to  ::::::::::::::::::::::::::::::" + recipientEmail);
            }
            if (workFlowProcess.getSubject() != null) {
                subject = workFlowProcess.getSubject();
            }
            if (workFlowProcess.getWorkflowProcessSenderDiary().getSendername() != null) {
                receivername = workFlowProcess.getWorkflowProcessSenderDiary().getSendername();
                comment = "Mail sent to " + receivername + " .";
            }
            if (workFlowProcess.getWorkflowProcessSenderDiaries() != null) {
                emailids1 = workFlowProcess.getWorkflowProcessSenderDiaries().stream().filter(d -> d != null).filter(d -> d.getEmail() != null).map(d -> d.getEmail()).collect(Collectors.toList());
                System.out.println("::::::::::::::::::::::::Sent mail.... ::::::::::::::::::::::::::::::" + emailids1);

            }
            try {
                List<Bitstream> bitstreamList = null;
                if (workFlowProcess.getWorkFlowProcessDraftDetails() != null && workFlowProcess.getWorkFlowProcessDraftDetails().getReferencetapalnumber() != null) {
                    //reply tapal bistream
                    bitstreamList = workFlowProcess.getWorkflowProcessReferenceDocs().stream().filter(a -> a.getDrafttype() != null).filter(b -> b.getDrafttype().getPrimaryvalue() != null).filter(c -> c.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reference Reply")).filter(d -> d.getBitstream() != null).map(d -> d.getBitstream()).collect(Collectors.toList());
                } else {
                    //outward
                    bitstreamList = workFlowProcess.getWorkflowProcessReferenceDocs().stream().filter(d -> d.getBitstream() != null).map(dd -> dd.getBitstream()).collect(Collectors.toList());
                }
                workflowProcessService.sendEmail(context, request, recipientEmail, receivername, subject, bitstreamList, emailids1, body);
            } catch (IOException | MessagingException | SQLException | AuthorizeException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage(), e);
            }
            System.out.println("::::::::::::   Mail sent  Done !   :::::::::::");
        }
        //reply drft
        if (workFlowProcess != null && workFlowProcess.getWorkFlowProcessDraftDetails() != null) {
            if (workFlowProcess.getWorkFlowProcessDraftDetails().getSubject() != null) {
                subject = workFlowProcess.getWorkFlowProcessDraftDetails().getSubject();
            }
            //find to and cc users
            if (workFlowProcess.getWorkflowProcessSenderDiaries() != null) {
                System.out.println("in getWorkflowProcessSenderDiaries");
                comment = "Mail sent to " + receivername + " .";
                workflowProcessSenderDiaries_to = workFlowProcess.getWorkflowProcessSenderDiaries().stream().filter(d -> d != null).filter(d -> d.getEmail() != null).filter(d -> d.getStatus() != null).filter(d -> d.getStatus() == 2).collect(Collectors.toList());
                workflowProcessSenderDiaries_cc = workFlowProcess.getWorkflowProcessSenderDiaries().stream().filter(d -> d != null).filter(d -> d.getEmail() != null).filter(d -> d.getStatus() != null).filter(d -> d.getStatus() == 3).collect(Collectors.toList());

            }
            //add doc and marged doc
            try {
                List<Bitstream> bitstreamReferenceReply = null;
                List<Bitstream> bitstreamList = null;
                InputStream replyTapalInputstream = null;
                Bitstream bitstreammarged = null;
                List<InputStream> inputreplyTapalReference = new ArrayList<>();
                if (workFlowProcess.getWorkFlowProcessDraftDetails() != null && workFlowProcess.getWorkFlowProcessDraftDetails().getReferencetapalnumber() != null) {
                    //reply tapal bistream
                    Optional<Bitstream> bitstreamopt = workFlowProcess.getWorkflowProcessReferenceDocs().stream().filter(a -> a.getDrafttype() != null).filter(b -> b.getDrafttype().getPrimaryvalue() != null).filter(c -> c.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reply Tapal")).filter(d -> d.getBitstream() != null).map(d -> d.getBitstream()).findFirst();
                    if (bitstreamopt.isPresent()) {
                        replyTapalInputstream = bitstreamService.retrieve(context, bitstreamopt.get());
                    }
                    if (replyTapalInputstream != null) {
                        inputreplyTapalReference.add(replyTapalInputstream);
                    }
                    bitstreamReferenceReply = workFlowProcess.getWorkflowProcessReferenceDocs().stream().filter(a -> a.getDrafttype() != null).filter(b -> b.getDrafttype().getPrimaryvalue() != null).filter(c -> c.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reference Reply")).filter(d -> d.getBitstream() != null).map(d -> d.getBitstream()).collect(Collectors.toList());

                    if (bitstreamReferenceReply != null && !bitstreamReferenceReply.isEmpty()) {
                        for (Bitstream bitstream : bitstreamReferenceReply) {
                            InputStream inputStream = bitstreamService.retrieve(context, bitstream);
                            inputreplyTapalReference.add(inputStream);
                        }
                    }
                    final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
                    File output = new File(TEMP_DIRECTORY, "ReplyDraftSign.pdf");
                    if (!output.exists()) {
                        try {
                            output.createNewFile();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    OutputStream out = new FileOutputStream(new File(output.getAbsolutePath()));
                    MargedDocUtils.mergePdfFiles(inputreplyTapalReference, out);
                    FileInputStream outputfile = new FileInputStream(new File(output.getAbsolutePath()));
                    bitstreammarged = bundleRestRepository.processBitstreamCreationWithoutBundle(context, outputfile, "", output.getName());
                } else {
                    //outward
                    bitstreamList = workFlowProcess.getWorkflowProcessReferenceDocs().stream().filter(d -> d.getBitstream() != null).map(dd -> dd.getBitstream()).collect(Collectors.toList());
                }
                //0
                if (workflowProcessSenderDiaries_to != null) {
                    for (WorkflowProcessSenderDiary to : workflowProcessSenderDiaries_to) {
                        Email email = Email.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "draftreply"));
                        email.addArgument(subject);
                        if (!DateUtils.isNullOrEmptyOrBlank(to.getEmail())) {
                            email.addRecipient(to.getEmail());
                        }
                        if (workflowProcessSenderDiaries_cc != null) {
                            for (WorkflowProcessSenderDiary cc : workflowProcessSenderDiaries_cc) {
                                if (!DateUtils.isNullOrEmptyOrBlank(cc.getEmail())) {
                                    email.addRecipient(cc.getEmail());
                                }
                            }
                        }
                        email.addArgument(to.getSendername());        //1
                        email.addArgument(senderName);                //2
                        email.addArgument(currentuser.getEmail());    //3
                        email.addArgument(senderDesignation);         //4
                        email.addArgument(senderDepartment);          //5
                        email.addArgument(senderOffice);              //6
                        email.addArgument(body);                      //7
                        email.addAttachment(bitstreamService.retrieve(context, bitstreammarged), bitstreammarged.getName(), bitstreammarged.getFormat(context).getMIMEType());
//                        for (Bitstream bitstream : bitstreamList) {
//                            if (bitstreamService.retrieve(context, bitstream) != null) {
//                                email.addAttachment(bitstreamService.retrieve(context, bitstreammarged), bitstream.getName(), bitstream.getFormat(context).getMIMEType());
//                            }
//                        }
                        email.send();
                    }
                }

            } catch (IOException | MessagingException | SQLException | AuthorizeException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage(), e);
            }
            System.out.println("::::::::::::   Mail sent  Done !   :::::::::::");
        }
        return comment;
    }

    public Bitstream getMargedDoc(Context context, WorkflowProcess workFlowProcess, Bitstream bitstream1) {
        try {
            List<Bitstream> bitstreamReferenceReply = null;
            InputStream replyTapalInputstream = null;
            Bitstream bitstreammarged = null;
            InputStream slipInput = null;
            List<InputStream> inputreplyTapalReference = new ArrayList<>();
            if (bitstream1 != null) {
                slipInput = bitstreamService.retrieve(context, bitstream1);
                inputreplyTapalReference.add(slipInput);
            }
            if (workFlowProcess.getWorkFlowProcessDraftDetails() != null && workFlowProcess.getWorkFlowProcessDraftDetails().getReferencetapalnumber() != null) {
                //reply tapal bistream
                Optional<Bitstream> bitstreamopt = workFlowProcess.getWorkflowProcessReferenceDocs().stream().filter(a -> a.getDrafttype() != null).filter(b -> b.getDrafttype().getPrimaryvalue() != null).filter(c -> c.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reply Tapal")).filter(d -> d.getBitstream() != null).map(d -> d.getBitstream()).findFirst();
                if (bitstreamopt.isPresent()) {
                    replyTapalInputstream = bitstreamService.retrieve(context, bitstreamopt.get());
                }
                if (replyTapalInputstream != null) {
                    inputreplyTapalReference.add(replyTapalInputstream);
                }
                bitstreamReferenceReply = workFlowProcess.getWorkflowProcessReferenceDocs().stream().filter(a -> a.getDrafttype() != null).filter(b -> b.getDrafttype().getPrimaryvalue() != null).filter(c -> c.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reference Reply")).filter(d -> d.getBitstream() != null).map(d -> d.getBitstream()).collect(Collectors.toList());

                if (bitstreamReferenceReply != null && !bitstreamReferenceReply.isEmpty()) {
                    for (Bitstream bitstream : bitstreamReferenceReply) {
                        InputStream inputStream = bitstreamService.retrieve(context, bitstream);
                        inputreplyTapalReference.add(inputStream);
                    }
                }
                final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
                File output = new File(TEMP_DIRECTORY, "replyTapalmarged.pdf");
                if (!output.exists()) {
                    try {
                        output.createNewFile();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                OutputStream out = new FileOutputStream(new File(output.getAbsolutePath()));
                MargedDocUtils.mergePdfFiles(inputreplyTapalReference, out);
                FileInputStream outputfile = new FileInputStream(new File(output.getAbsolutePath()));
                bitstreammarged = bundleRestRepository.processBitstreamCreationWithoutBundle(context, outputfile, "", output.getName());
                System.out.println("out>>>>>" + output.getAbsolutePath());
                return bitstreammarged;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
