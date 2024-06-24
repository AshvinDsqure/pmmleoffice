/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import com.google.gson.Gson;
import com.itextpdf.text.DocumentException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.*;
import org.dspace.app.rest.enums.WorkFlowAction;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.enums.WorkFlowType;
import org.dspace.app.rest.enums.WorkFlowUserType;
import org.dspace.app.rest.exception.MissingParameterException;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.jbpm.JbpmServerImpl;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.repository.AbstractDSpaceRestRepository;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.app.rest.utils.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.disseminate.service.CitationDocumentService;
import org.dspace.eperson.EPerson;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
@RequestMapping("/api/" + WorkFlowProcessRest.CATEGORY
        + "/" + WorkFlowProcessRest.CATEGORY_DRAFT)
public class WorkflowProcessDraftController extends AbstractDSpaceRestRepository
        implements InitializingBean {
    private static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(WorkflowProcessDraftController.class);

    @Autowired
    WorkflowProcessService workflowProcessService;
    @Autowired
    WorkFlowProcessConverter workFlowProcessConverter;
    @Autowired
    WorkFlowProcessEpersonConverter workFlowProcessEpersonConverter;
    @Autowired
    WorkflowProcessEpersonService workflowProcessEpersonService;
    @Autowired
    private BundleService bundleService;
    @Autowired
    JbpmServerImpl jbpmServer;
    @Autowired
    BitstreamService bitstreamService;
    @Autowired
    MetadataFieldService metadataFieldService;
    @Autowired
    WorkflowProcessReferenceDocService workflowProcessReferenceDocService;
    @Autowired
    WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;

    @Autowired
    BundleRestRepository bundleRestRepository;

    @Autowired
    WorkFlowProcessMasterService workFlowProcessMasterService;
    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterValueService;

    @Autowired
    WorkflowProcessReferenceDocVersionService workflowProcessReferenceDocVersionService;

    @Autowired
    WorkflowProcessNoteService workflowProcessNoteService;
    @Autowired
    ConfigurationService configurationService;
    @Autowired
    WorkFlowProcessCommentConverter workFlowProcessCommentConverter;
    @Autowired
    WorkFlowProcessCommentService workFlowProcessCommentService;


    @Autowired
    private DiscoverableEndpointsService discoverableEndpointsService;


    @Override
    public void afterPropertiesSet() throws Exception {
        discoverableEndpointsService
                .register(this, Arrays
                        .asList(Link.of("/api/" + WorkFlowProcessRest.CATEGORY + "/" + WorkFlowProcessRest.CATEGORY_DRAFT, WorkFlowProcessRest.CATEGORY_DRAFT)));
    }

    @Autowired
    private WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @ExceptionHandler(MissingParameterException.class)
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD})
    public ResponseEntity create(@RequestBody WorkFlowProcessRest workFlowProcessRest) throws Exception {
        WorkFlowProcessRest workFlowProcessRest1 = workFlowProcessRest;
        WorkflowProcess workFlowProcess = null;
        WorkFlowProcessRest workFlowProcessRest2=null;
        try {
            HttpServletRequest request = getRequestService().getCurrentRequest().getHttpServletRequest();
            Context context = ContextUtil.obtainContext(request);
            Optional<WorkflowProcessEpersonRest> WorkflowProcessEpersonRest = Optional.ofNullable((getSubmitor(context)));
            if (!WorkflowProcessEpersonRest.isPresent()) {
                return ResponseEntity.badRequest().body("no user found");
            }
            WorkFlowType workFlowType = WorkFlowType.DRAFT;
            if (workFlowProcessRest != null && workFlowProcessRest.getId() != null) {
                workFlowProcess = workFlowProcessConverter.convertDraftwithID(workFlowProcessRest, context, UUID.fromString(workFlowProcessRest.getId()));
                if (workFlowProcess != null) {
                    System.out.println("in Draft to create flow ");
                    Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.REJECTED.getUserTypeFromMasterValue(context);
                    if (workFlowTypeStatus.isPresent()) {
                        workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
                    }
                }
            }else {
                workFlowType.setWorkFlowStatus(WorkFlowStatus.INPROGRESS);
            }
            WorkFlowAction create = WorkFlowAction.CREATE;
            workFlowType.setWorkFlowAction(create);
            // create.setComment();
            workFlowType.setProjection(utils.obtainProjection());
            workFlowProcessRest.getWorkflowProcessEpersonRests().add(WorkflowProcessEpersonRest.get());
            //perfome and stor to db
            workFlowProcessRest = workFlowType.storeWorkFlowProcess(context, workFlowProcessRest);
            WorkflowProcess workflowProcess1 = workFlowProcessConverter.convertByService(context, workFlowProcessRest);
            //create Note
            //WorkflowProcessReferenceDoc workflowProcessReferenceDoc = createNote(context, workflowProcess);
            context.commit();
            create.setComment(null);
            create.setWorkflowProcessReferenceDocs(null);
            create.setInitiator(false);
            if (workFlowProcessRest1 != null && workFlowProcessRest1.getWorkFlowProcessCommentRest() != null) {
                Context context12 = ContextUtil.obtainContext(request);
                saveComment(context12, workflowProcess1, workFlowProcessRest1, request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(workFlowProcessRest);
    }

    public void saveCommentasDraft(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest, HttpServletRequest request) throws Exception {
        System.out.println("in save comment satrt");
        if (workFlowProcessRest.getWorkFlowProcessCommentRest() != null) {
            WorkFlowProcessComment c = null;
            try {
                c = workFlowProcessCommentService.findCommentByworkflowprocessidAndissavedrafttrue(context, workflowProcess.getID());
            } catch (Exception e) {
                c = null;
                System.out.println("error"+e.getMessage());
            }
            if (c != null) {
                System.out.println("in update new draft");
                //if alredy tru then apdate
                WorkFlowProcessComment workFlowProcessComment = workFlowProcessCommentConverter.convert(context, c, workFlowProcessRest.getWorkFlowProcessCommentRest());
                workFlowProcessComment.setWorkFlowProcess(workflowProcess);
                workFlowProcessComment.setSubmitter(context.getCurrentUser());
              //  WorkFlowProcessComment workFlowProcessComment1 = workFlowProcessCommentService.create(context, workFlowProcessComment);
                System.out.println("SAVE NOTE AS DRAFT DONE ");
                WorkFlowProcessComment workFlowProcessComment2 = workFlowProcessComment;
                if (workFlowProcessRest.getWorkFlowProcessCommentRest() != null && workFlowProcessRest.getWorkFlowProcessCommentRest().getWorkflowProcessReferenceDocRest() != null) {
                    List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs = workFlowProcessRest.getWorkFlowProcessCommentRest().getWorkflowProcessReferenceDocRest().stream().filter(d -> d != null).filter(d -> d.getUuid() != null).map(d -> {
                        try {
                            WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, d);
                            workflowProcessReferenceDoc.setWorkflowprocesscomment(workFlowProcessComment2);
                            return workflowProcessReferenceDoc;
                        } catch (SQLException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());

                    if (workflowProcessReferenceDocs != null && workflowProcessReferenceDocs.size() != 0) {
                        workFlowProcessComment.setWorkflowProcessReferenceDoc(workflowProcessReferenceDocs);
                    }
                    workFlowProcessCommentService.update(context, workFlowProcessComment);
                    System.out.println(":::::::::::NOTE UPDATE AS DRAFT MWITH DOCUMENT DONE !");
                    context.commit();
                }
            } else {
                System.out.println("in create new draft");
                //create new note as draft
                WorkFlowProcessComment workFlowProcessComment = workFlowProcessCommentConverter.convert(context, workFlowProcessRest.getWorkFlowProcessCommentRest());
                workFlowProcessComment.setWorkFlowProcess(workflowProcess);
                workFlowProcessComment.setSubmitter(context.getCurrentUser());
                WorkFlowProcessComment workFlowProcessComment1 = workFlowProcessCommentService.create(context, workFlowProcessComment);
                System.out.println("SAVE NOTE AS DRAFT DONE ");
                WorkFlowProcessComment workFlowProcessComment2 = workFlowProcessComment1;
                if (workFlowProcessRest.getWorkFlowProcessCommentRest() != null && workFlowProcessRest.getWorkFlowProcessCommentRest().getWorkflowProcessReferenceDocRest() != null) {
                    List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs = workFlowProcessRest.getWorkFlowProcessCommentRest().getWorkflowProcessReferenceDocRest().stream().filter(d -> d != null).filter(d -> d.getUuid() != null).map(d -> {
                        try {
                            WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, d);
                            workflowProcessReferenceDoc.setWorkflowprocesscomment(workFlowProcessComment2);
                            return workflowProcessReferenceDoc;
                        } catch (SQLException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());

                    if (workflowProcessReferenceDocs != null && workflowProcessReferenceDocs.size() != 0) {
                        workFlowProcessComment1.setWorkflowProcessReferenceDoc(workflowProcessReferenceDocs);
                    }
                    workFlowProcessCommentService.update(context, workFlowProcessComment1);
                    System.out.println(":::::::::::NOTE UPDATE AS DRAFT MWITH DOCUMENT DONE !");
                    context.commit();
                }
            }
        }
    }

    public void saveComment(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest, HttpServletRequest request) throws Exception {
        System.out.println("in save comment satrt");
        if (workFlowProcessRest.getWorkFlowProcessCommentRest() != null) {
            WorkFlowProcessComment workFlowProcessComment = workFlowProcessCommentConverter.convert(context, workFlowProcessRest.getWorkFlowProcessCommentRest());
            workFlowProcessComment.setWorkFlowProcess(workflowProcess);
            workFlowProcessComment.setSubmitter(context.getCurrentUser());
            WorkFlowProcessComment workFlowProcessComment1 = workFlowProcessCommentService.create(context, workFlowProcessComment);

            WorkFlowProcessComment workFlowProcessComment2 = workFlowProcessComment1;
            if (workFlowProcessRest.getWorkFlowProcessCommentRest() != null && workFlowProcessRest.getWorkFlowProcessCommentRest().getWorkflowProcessReferenceDocRest() != null) {
                List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs = workFlowProcessRest.getWorkFlowProcessCommentRest().getWorkflowProcessReferenceDocRest().stream().filter(d -> d.getUuid() != null).filter(d -> d != null).map(d -> {
                    try {
                        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, d);
                        workflowProcessReferenceDoc.setWorkflowprocesscomment(workFlowProcessComment2);
                        return workflowProcessReferenceDoc;
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

                if (workflowProcessReferenceDocs != null && workflowProcessReferenceDocs.size() != 0) {
                    workFlowProcessComment1.setWorkflowProcessReferenceDoc(workflowProcessReferenceDocs);
                    // workFlowProcessCommentService.create(context, workFlowProcessComment1);
                }
                workFlowProcessCommentService.update(context, workFlowProcessComment1);
                context.commit();
                System.out.println(":::::::::::::::SAVE COMMENT DONE:::::::::::::::::::::");
                //create Note And Sing Note
                if (workflowProcess != null && workFlowProcessComment2 != null) {
                    System.out.println(":::::::::::::::SAVE COMMENT NOTE SIGN START:::::::::::::::::::::");
                    InputStream pkcs12File = null;
                    InputStream certFile = null;
                    InputStream fileInputa = null;
                    Bitstream bitstream = null;
                    Context context12 = ContextUtil.obtainContext(request);
                    DigitalSignRequet digitalSignRequet = new DigitalSignRequet();

                    try {
                        String certType = configurationService.getProperty("digital.sign.certtype");
                        String password = configurationService.getProperty("digital.sign.password");
                        String showSignature = configurationService.getProperty("digital.sign.showsignature");
                        String reason = configurationService.getProperty("digital.sign.reason");
                        String location = configurationService.getProperty("digital.sign.location");
                        String name = context12.getCurrentUser().getFullName();
                        String pageNumber = configurationService.getProperty("digital.sign.pagenumber");
                        File p12 = new File(configurationService.getProperty("digital.sign.p12File"));
                        File cert = new File(configurationService.getProperty("digital.sign.p12File"));
                        pkcs12File = new FileInputStream(p12);
                        certFile = new FileInputStream(cert);
                        if (cert != null) {
                            digitalSignRequet.setCertFileName(cert.getName());
                        }
                        if (p12 != null) {
                            digitalSignRequet.setP12FileName(p12.getName());
                        }
                        if (!isNullOrEmptyOrBlank(certType)) {
                            digitalSignRequet.setCertType(certType);
                        }
                        if (!isNullOrEmptyOrBlank(password)) {
                            digitalSignRequet.setPassword(password);
                        }
                        if (!isNullOrEmptyOrBlank(showSignature)) {
                            digitalSignRequet.setShowSignature(showSignature);
                        }
                        if (!isNullOrEmptyOrBlank(reason)) {
                            digitalSignRequet.setReason(reason);
                        }
                        if (!isNullOrEmptyOrBlank(location)) {
                            digitalSignRequet.setLocation(location);
                        }
                        if (!isNullOrEmptyOrBlank(name)) {
                            digitalSignRequet.setName(name);
                        }
                        if (!isNullOrEmptyOrBlank(pageNumber)) {
                            digitalSignRequet.setPageNumber(pageNumber);
                        }
                        if (pkcs12File != null) {
                            digitalSignRequet.setP12File(pkcs12File);
                        }
                        if (certFile != null) {
                            digitalSignRequet.setCertFile(certFile);
                        }
                        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
                        long notecount = 0;
                        if (workflowProcess.getItem() != null && workflowProcess.getItem().getName() != null) {
                            UUID statusid = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
                            notecount = workflowProcessNoteService.getNoteCountNumber(context, workflowProcess.getItem().getID(), statusid);
                        }
                        notecount = notecount + 1;
                        File tempFile1html = new File(TEMP_DIRECTORY, "Note#" + notecount + ".pdf");
                        if (!tempFile1html.exists()) {
                            try {
                                tempFile1html.createNewFile();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        //create note pdf
                        fileInputa = createFinalNoteComment(context12, workflowProcess, tempFile1html);
                        if (fileInputa != null) {
                            System.out.println("in file input");
                            digitalSignRequet.setFileInput(fileInputa);
                        }
                        digitalSignRequet.setFileInputName(tempFile1html.getName());
                        System.out.println("certFile :" + certFile);
                        System.out.println("pdf :" + fileInputa);
                        System.out.println("p12File :" + pkcs12File);
                        System.out.println("password :" + password);
                        System.out.println("certType :" + certType);
                        System.out.println("location :" + location);
                        System.out.println("pageNumber :" + pageNumber);
                        System.out.println("reason :" + reason);
                        System.out.println("name :" + name);
                        System.out.println("p12.getName() :" + p12.getName());
                        System.out.println("cert.getName() :" + cert.getName());

                        Bitstream bitstream1 = digitalSignDataForNote(context12, digitalSignRequet, bitstream);
                        if (bitstream1 != null) {
                            WorkFlowProcessComment workFlowProcessComment3 = workFlowProcessCommentService.find(context12, workFlowProcessComment2.getID());
                            if (workFlowProcessComment3 != null) {
                                WorkflowProcessReferenceDoc d = new WorkflowProcessReferenceDoc();
                                d.setBitstream(bitstream1);
                                WorkflowProcessReferenceDoc doc = workflowProcessReferenceDocService.create(context12, d);
                                if (doc != null) {
                                    workFlowProcessComment3.setNote(doc);
                                }
                                workFlowProcessCommentService.update(context12, workFlowProcessComment3);
                                context12.commit();
                                System.out.println(":::::::::::::::SAVE COMMENT NOTE SIGN DONE!:::::::::::::::::::::");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("in save comment stop");
                }
            }
        }
    }


    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "/draft")
    public ResponseEntity draft(@RequestBody WorkFlowProcessRest workFlowProcessRest, HttpServletRequest request) throws Exception {
        try {
            System.out.println("workFlowProcessRest::" + new Gson().toJson(workFlowProcessRest));
            //  HttpServletRequest request = getRequestService().getCurrentRequest().getHttpServletRequest();
            Context context = ContextUtil.obtainContext(request);
            workFlowProcessRest.getWorkflowProcessEpersonRests().clear();
            WorkFlowProcessRest workFlowProcessRest1 = workFlowProcessRest;
            Optional<WorkflowProcessEpersonRest> WorkflowProcessEpersonRest = Optional.ofNullable((getSubmitor(context)));
            if (!WorkflowProcessEpersonRest.isPresent()) {
                return ResponseEntity.badRequest().body("no user found");
            }
            WorkFlowType workFlowType = WorkFlowType.DRAFT;
            //status
            workFlowType.setWorkFlowStatus(WorkFlowStatus.DRAFT);
            WorkFlowAction create = WorkFlowAction.CREATE;

            workFlowType.setWorkFlowAction(create);
            workFlowType.setProjection(utils.obtainProjection());
            workFlowProcessRest.getWorkflowProcessEpersonRests().add(WorkflowProcessEpersonRest.get());
            //perfome and stor to db
            workFlowProcessRest = workFlowType.storeWorkFlowProcessDraft(context, workFlowProcessRest);
            WorkflowProcess workflowProcess1 = workFlowProcessConverter.convertByService(context, workFlowProcessRest);
            context.commit();
            if (workFlowProcessRest1 != null && workFlowProcessRest1.getWorkFlowProcessCommentRest() != null) {
                System.out.println("in savedraft");
                Context context12 = ContextUtil.obtainContext(request);
                saveCommentasDraft(context12, workflowProcess1, workFlowProcessRest1, request);
            }
            create.setComment(null);
            create.setWorkflowProcessReferenceDocs(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(workFlowProcessRest);
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "/savedraft")
    public ResponseEntity savedraft(@RequestBody WorkFlowProcessRest workFlowProcessRest, HttpServletRequest request) throws Exception {
        WorkflowProcess workFlowProcess = null;
        WorkFlowProcessRest workFlowProcessRest1 = workFlowProcessRest;
        System.out.println("data " + workFlowProcessRest);
        System.out.println("id " + workFlowProcessRest.getId());

        try {
            Context context = ContextUtil.obtainContext(request);
            if (workFlowProcessRest != null && workFlowProcessRest.getId() != null) {
                workFlowProcess = workFlowProcessConverter.convertDraftwithID(workFlowProcessRest, context, UUID.fromString(workFlowProcessRest.getId()));
            }
            if (workFlowProcess == null) {
                throw new RuntimeException("Workflow not found");
            }
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            WorkflowProcess workflowProcess1 = workFlowProcess;
            workflowProcessService.create(context, workFlowProcess);
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            context.commit();

            if (workFlowProcessRest1 != null && workFlowProcessRest1.getWorkFlowProcessCommentRest() != null) {
                System.out.println("in savedraft");
                Context context12 = ContextUtil.obtainContext(request);
                saveCommentasDraft(context12, workflowProcess1, workFlowProcessRest1, request);
            }
            return ResponseEntity.ok(workFlowProcessRest);
        } catch (RuntimeException e) {
            throw new UnprocessableEntityException("error in suspendTask Server..");
        } catch (AuthorizeException e) {
            throw new RuntimeException(e);
        }
    }

    public WorkflowProcessEpersonRest getSubmitor(Context context) throws SQLException {
        if (context.getCurrentUser() != null) {
            WorkflowProcessEpersonRest workflowProcessEpersonSubmitor = new WorkflowProcessEpersonRest();
            EPersonRest ePersonRest = new EPersonRest();
            ePersonRest.setUuid(context.getCurrentUser().getID().toString());
            workflowProcessEpersonSubmitor.setIndex(0);
            workflowProcessEpersonSubmitor.setSequence(0);
            Optional<WorkFlowProcessMasterValue> workFlowUserTypOptional = WorkFlowUserType.INITIATOR.getUserTypeFromMasterValue(context);
            if (workFlowUserTypOptional.isPresent()) {
                workflowProcessEpersonSubmitor.setUserType(workFlowProcessMasterValueConverter.convert(workFlowUserTypOptional.get(), utils.obtainProjection()));
            }
            workflowProcessEpersonSubmitor.setePersonRest(ePersonRest);
            return workflowProcessEpersonSubmitor;
        }
        return null;

    }

    public InputStream  createFinalNoteComment(Context context, WorkflowProcess workflowProcess, File tempFile1html) throws
            Exception {
        boolean isTextEditorFlow = false;
        System.out.println("start.......createFinalNote");
        StringBuffer sb = new StringBuffer("<!DOCTYPE html>\n" + "<html>\n" + "<head><style>@page{size:A4;margin: 0;}</style>\n" + "<title>Note</title>\n" + "</head>\n" + "<body style=\"padding-right: 20px;padding-left: 20px;background-color:#c5e6c1;\">");
        System.out.println("start.......createFinalNote" + tempFile1html.getAbsolutePath());
        //Items
        sb.append("<p> <b>Subject : " + workflowProcess.getSubject() + "</b></p>");
        if(workflowProcess.getWorkFlowProcessDraftDetails()!=null) {
            if(workflowProcess.getWorkFlowProcessDraftDetails().getIssapdoc() && workflowProcess.getWorkFlowProcessDraftDetails().getSapdocumentno()!=null && workflowProcess.getWorkFlowProcessDraftDetails().getSapdocumenttype()!=null&& workflowProcess.getWorkFlowProcessDraftDetails().getSapdocumenttype().getPrimaryvalue()!=null) {
                sb.append("<p> <b>SAP Document Type :  " + workflowProcess.getWorkFlowProcessDraftDetails().getSapdocumenttype().getPrimaryvalue() + "</b></p>");
                sb.append("<p> <b>SAP Document Number : " + workflowProcess.getWorkFlowProcessDraftDetails().getSapdocumentno() + "</b></p>");
              }
            }
        isTextEditorFlow = true;
        List<WorkFlowProcessComment> comments = workFlowProcessCommentService.getComments(context, workflowProcess.getID());
        int i = 1;
        for (WorkFlowProcessComment comment : comments) {
            sb.append("<div style=\"width:100% ;text-align: left; float:left;\">");
            //coment count
            sb.append("<p><u>Note# " + i + "</u></p>");
            //comment text
            if (comment.getComment() != null) {
                sb.append("<p>" + comment.getComment() + "</p>");
            }
            sb.append("<br><div style=\"width:100%;\"> ");
            sb.append("<div style=\"width:70%;  float:left;\"> <p><b>Attachment :</b></p> ");
            if (comment.getWorkflowProcessReferenceDoc() != null && comment.getWorkflowProcessReferenceDoc().size() != 0) {
                for (WorkflowProcessReferenceDoc workflowProcessReferenceDoc : comment.getWorkflowProcessReferenceDoc()) {
                    if (workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue() != null && !workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue().equals("PKCS12")) {
                        if (workflowProcessReferenceDoc.getBitstream() != null) {
                            String baseurl = configurationService.getProperty("dspace.server.url");
                            sb.append("<span> <a href=" + baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content>");
                            stroremetadate(workflowProcessReferenceDoc.getBitstream(), sb);
                        }
                    }
                }
            }
            sb.append("</div>");
            sb.append("<div style=\"    float: right;  width:30%\"><p> <B>Signature :</B> </p><B><span>");
            sb.append("</div></div>");
            i++;
        }
        sb.append("</body></html>");
        if (isTextEditorFlow) {
            System.out.println("::::::::::IN isTextEditorFlow :::::::::");
            FileOutputStream files = new FileOutputStream(new File(tempFile1html.getAbsolutePath()));
            System.out.println("HTML:::" + sb.toString());
            int result = PdfUtils.HtmlconvertToPdf(sb.toString(), files);
            System.out.println("HTML CONVERT DONE::::::::::::::: :" + tempFile1html.getAbsolutePath());
            InputStream outputfile = new FileInputStream(new File(tempFile1html.getAbsolutePath()));
            // Bitstream bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, outputfile, "", tempFile1html.getName());
            return outputfile;
        }
        return null;
    }

    public static boolean isNullOrEmptyOrBlank(String str) {
        return str == null || str.trim().isEmpty();
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

                try {
                    sb.append("<br>" + DateUtils.strDateToString(date));
                } catch (Exception e) {
                }
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

    public Bitstream digitalSignDataForNote(Context context, DigitalSignRequet requestModel, Bitstream bitstream) {
        //CloseableHttpClient httpClient = HttpClients.createDefault();
        System.out.println("::::::::::::digitalSignDataForNote::::::::::::::::::::::");
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        File tempsingpdf = new File(TEMP_DIRECTORY, "sign" + ".pdf");
        if (!tempsingpdf.exists()) {
            try {
                tempsingpdf.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        HttpClient httpClient = HttpClients.createDefault();
        try {
            String url = configurationService.getProperty("digital.sign.url");
            HttpPost httpPost = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            // Add parameters as form data
            builder.addTextBody("certType", requestModel.getCertType(), ContentType.TEXT_PLAIN);
            builder.addTextBody("showSignature", requestModel.getShowSignature(), ContentType.TEXT_PLAIN);
            builder.addTextBody("location", requestModel.getLocation(), ContentType.TEXT_PLAIN);
            builder.addTextBody("reason", requestModel.getReason(), ContentType.TEXT_PLAIN);
            builder.addTextBody("pageNumber", requestModel.getPageNumber(), ContentType.TEXT_PLAIN);
            builder.addTextBody("name", requestModel.getName(), ContentType.TEXT_PLAIN);
            builder.addTextBody("password", requestModel.getPassword(), ContentType.TEXT_PLAIN);
            // Add a binary file
            builder.addBinaryBody("fileInput", requestModel.getFileInput(), ContentType.APPLICATION_OCTET_STREAM, requestModel.getFileInputName());
            builder.addBinaryBody("p12File", requestModel.getP12File(), ContentType.APPLICATION_OCTET_STREAM, requestModel.getP12FileName());
            builder.addBinaryBody("certFile", requestModel.getCertFile(), ContentType.APPLICATION_OCTET_STREAM, requestModel.getCertFileName());
            // Build the multipart entity
            httpPost.setEntity(builder.build());
            // Execute the request
            try {
                // Execute the request and get the response
                HttpResponse response = httpClient.execute(httpPost);
                System.out.println("Response :::::::::::::" + response);
                // Check the response status code and content
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
                    HttpHeaders headers = new HttpHeaders();
                    for (org.apache.http.Header header : response.getAllHeaders()) {
                        headers.add(header.getName(), header.getValue());
                    }
                    byte[] responseBody = EntityUtils.toByteArray(entity);
                    byte[] s = responseBody;
                    try (FileOutputStream fos = new FileOutputStream(new File(tempsingpdf.getAbsolutePath()))) {
                        fos.write(responseBody);
                        fos.close();
                        fos.flush();
                    }
                    System.out.println("file path" + tempsingpdf.getAbsolutePath());
                    FileInputStream pdfFileInputStream = new FileInputStream(new File(tempsingpdf.getAbsolutePath()));
                    Bitstream bitstreampdfsing = bundleRestRepository.processBitstreamCreationWithoutBundle(context, pdfFileInputStream, "", tempsingpdf.getName());
                    if (bitstreampdfsing != null) {
                        System.out.println("::::::::::::digitalSignDataForNote:::::::::::::::DONe!:::::::");
                        return bitstreampdfsing;
                    }
                    // Process the response content here
                } else {
                    System.out.println("errot with " + statusCode);
                    HttpEntity entity = response.getEntity();
                    byte[] responseBody = EntityUtils.toByteArray(entity);
                    return null;

                }
            } catch (IOException e) {
                System.out.println("error" + e.getMessage());
                e.printStackTrace();

            }
        } catch (Exception e) {
            System.out.println("error" + e.getMessage());
            e.printStackTrace();
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


}
