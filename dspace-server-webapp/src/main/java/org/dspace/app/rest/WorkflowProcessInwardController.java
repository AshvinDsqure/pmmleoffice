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
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.*;
import org.dspace.app.rest.enums.WorkFlowAction;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.enums.WorkFlowType;
import org.dspace.app.rest.enums.WorkFlowUserType;
import org.dspace.app.rest.exception.JBPMServerExpetion;
import org.dspace.app.rest.exception.MissingParameterException;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.jbpm.JbpmServerImpl;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.repository.AbstractDSpaceRestRepository;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.app.rest.repository.LinkRestRepository;
import org.dspace.app.rest.repository.WorkflowProcessSenderDiaryRepository;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.DateUtils;
import org.dspace.app.rest.utils.FileUtils;
import org.dspace.app.rest.utils.Utils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
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
        + "/" + WorkFlowProcessRest.CATEGORY_INWARD)
public class WorkflowProcessInwardController extends AbstractDSpaceRestRepository
        implements InitializingBean {
    private static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(WorkflowProcessInwardController.class);

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
    EPersonService ePersonService;

    @Autowired
    MetadataFieldService metadataFieldService;
    @Autowired
    private DiscoverableEndpointsService discoverableEndpointsService;

    @Override
    public void afterPropertiesSet() throws Exception {
        discoverableEndpointsService
                .register(this, Arrays
                        .asList(Link.of("/api/" + WorkFlowProcessRest.CATEGORY + "/" + WorkFlowProcessRest.CATEGORY_INWARD, WorkFlowProcessRest.CATEGORY_INWARD)));
    }

    @Autowired
    private WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;
    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterValueService;
    @Autowired
    WorkFlowProcessMasterService workFlowProcessMasterService;
    @Autowired
    protected Utils utils;
    @Autowired
    private BundleRestRepository bundleRestRepository;
    @Autowired
    private WorkFlowProcessInwardDetailsConverter workFlowProcessInwardDetailsConverter;

    @Autowired
    WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;

    @Autowired
    WorkFlowProcessDraftDetailsConverter workFlowProcessDraftDetailsConverter;

    @Autowired
    WorkflowProcessSenderDiaryConverter workflowProcessSenderDiaryConverter;


    @Autowired
    WorkflowProcessSenderDiaryService workflowProcessSenderDiaryService;
    @Autowired
    WorkflowProcessSenderDiaryEpersonConverter workflowProcessSenderDiaryEpersonConverter;

    @Autowired
    WorkflowProcessSenderDiaryEpersonService workflowProcessSenderDiaryEpersonService;

    @Autowired
    WorkflowProcessReferenceDocService workflowProcessReferenceDocService;

    @Autowired
    ItemConverter itemConverter;


    EPersonConverter ePersonConverter;

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = RequestMethod.POST,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity create(MultipartFile file, String workFlowProcessReststr) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        WorkFlowProcessRest workFlowProcessRest1 = null;
        InputStream fileInputStream = null;
        Bitstream bitstream = null;
        WorkFlowProcessDraftDetailsRest workFlowProcessDraftDetailsRest = null;
        WorkflowProcessReferenceDocRest workflowProcessReferenceDocRest = null;
        workFlowProcessRest1 = mapper.readValue(workFlowProcessReststr, WorkFlowProcessRest.class);
        WorkFlowProcessRest workFlowProcessRest = workFlowProcessRest1;
        WorkFlowProcessRest workFlowProcessRestTemp = null;
        HttpServletRequest request = getRequestService().getCurrentRequest().getHttpServletRequest();
        Context context = ContextUtil.obtainContext(request);
        context.turnOffAuthorisationSystem();
        try {
            System.out.println(":::::::::::::::::::::::::::::::::IN INWARD FLOW:::::::::::::::::::::::::::::");
            Optional<WorkflowProcessEpersonRest> initiatorEpersion = Optional.ofNullable((getSubmitor(context)));
            if (!initiatorEpersion.isPresent()) {
                return ResponseEntity.badRequest().body("no user found");
            }
            if (workFlowProcessRest.getRemark() != null && initiatorEpersion.get() != null) {
                System.out.println("store Remark in inward!");
                WorkflowProcessEpersonRest ep = initiatorEpersion.get();
                ep.setRemark(workFlowProcessRest.getRemark());
            }

            if (file != null) {

                System.out.println("IN FILE SAVE");

                WorkflowProcessReferenceDoc doc = new WorkflowProcessReferenceDoc();
                fileInputStream = file.getInputStream();
                InputStream pdfFileInputStream1 = file.getInputStream();
                bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, fileInputStream, "", file.getOriginalFilename());

                System.out.println("bitstream:only pdf:" + bitstream.getName());

                doc.setBitstream(bitstream);
                doc.setPage(FileUtils.getPageCountInPDF(pdfFileInputStream1));
                WorkFlowProcessInwardDetails workFlowProcessInwardDetails = workFlowProcessInwardDetailsConverter.convert(context, workFlowProcessRest.getWorkFlowProcessInwardDetailsRest());

                if (workFlowProcessInwardDetails.getLatterDate() != null) {
                    doc.setInitdate(workFlowProcessInwardDetails.getLatterDate());
                }
                if (workFlowProcessRest.getSubject() != null) {
                    doc.setSubject(workFlowProcessRest.getSubject());
                }
                if (workFlowProcessRest.getDocumenttypeRest() != null) {
                    doc.setWorkFlowProcessReferenceDocType(workFlowProcessMasterValueConverter.convert(context, workFlowProcessRest.getDocumenttypeRest()));
                }
                WorkFlowProcessMasterValue drafttype = getMastervalueData(context, WorkFlowType.MASTER.getAction(), WorkFlowType.INWARD.getAction());
                if (drafttype != null) {
                    doc.setDrafttype(drafttype);
                }
                if (workFlowProcessRest.getWorkFlowProcessInwardDetailsRest() != null && !DateUtils.isNullOrEmptyOrBlank(workFlowProcessRest.getWorkFlowProcessInwardDetailsRest().getFilereferencenumber())) {
                    doc.setReferenceNumber(workFlowProcessRest.getWorkFlowProcessInwardDetailsRest().getFilereferencenumber());
                }
                WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.create(context, doc);
                workflowProcessReferenceDocRest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                context.commit();
                System.out.println("OUT FILE SAVE DONE...");
            }
            WorkFlowType workFlowType = WorkFlowType.INWARD;
            workFlowType.setWorkFlowStatus(WorkFlowStatus.INPROGRESS);
            WorkFlowAction create = WorkFlowAction.CREATE;
            workFlowType.setWorkFlowAction(create);
            workFlowType.setProjection(utils.obtainProjection());
            List<WorkflowProcessEpersonRest> templist = workFlowProcessRest.getWorkflowProcessEpersonRests().stream().filter(d -> d.getIndex() != 0).collect(Collectors.toList());
            List<WorkflowProcessReferenceDocRest> tempdoclist = workFlowProcessRest.getWorkflowProcessReferenceDocRests().stream().filter(d -> d != null).filter(dd -> dd.getUuid() != null).map(d -> {
                try {
                    WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, d);
                    if (workflowProcessReferenceDoc != null) {
                        return workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                    } else {
                        return null;
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

            List<WorkflowProcessSenderDiaryRest> workflowProcessSenderDiaries = workFlowProcessRest.getWorkflowProcessSenderDiaryRests().stream().filter(d -> d != null).filter(dd -> dd.getUuid() != null).map(d -> {
                try {
                    WorkflowProcessSenderDiary workflowProcessSenderDiary = workflowProcessSenderDiaryConverter.convert(context, d);
                    return workflowProcessSenderDiaryConverter.convert(workflowProcessSenderDiary, utils.obtainProjection());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
            if (workflowProcessReferenceDocRest != null) {
                tempdoclist.add(workflowProcessReferenceDocRest);
            }
            System.out.println("::::::::::::::::DOCUMENT LIST SIZE" + tempdoclist.size());
            int i = 0;
            int cccount = 1;
            for (WorkflowProcessEpersonRest nextEpersonrest : templist) {
                Context context1 = ContextUtil.obtainContext(request);
                workFlowProcessRest.setWorkflowProcessEpersonRests(null);
                workFlowProcessRest.setWorkflowProcessReferenceDocRests(null);
                List<WorkflowProcessEpersonRest> initeatorandnextuserlist = new ArrayList<>();
                initeatorandnextuserlist.add(0, initiatorEpersion.get());
                nextEpersonrest.setIndex(1);
                initeatorandnextuserlist.add(1, nextEpersonrest);
                //  initeatorandnextuserlist.add(1, nextEpersonrest);
                workFlowProcessRest.setWorkflowProcessEpersonRests(initeatorandnextuserlist);
                if (workFlowProcessRest.getWorkFlowProcessDraftDetailsRest() != null) {
                    workFlowProcessDraftDetailsRest = workFlowProcessRest.getWorkFlowProcessDraftDetailsRest();
                }
                if (workFlowProcessRest.getWorkFlowProcessInwardDetailsRest() != null) {
                    WorkFlowProcessInwardDetailsRest workFlowProcessInwardDetailsRest = workFlowProcessRest.getWorkFlowProcessInwardDetailsRest();
                    if (DateUtils.isNullOrEmptyOrBlank(workFlowProcessInwardDetailsRest.getInwardNumber())) {
                        System.out.println("in set inward number");
                        workFlowProcessInwardDetailsRest.setInwardNumber(getInwardNumber().get("inwardnumber"));
                    }
                }
                if (i > 0) {
                    System.out.println(":::::::::::::IN MULTIPLE USER FROW CREATE  ::::::::::::::::::::::::::");
                    WorkFlowProcessInwardDetailsRest workFlowProcessInwardDetailsRest = workFlowProcessRest.getWorkFlowProcessInwardDetailsRest();
                    WorkFlowProcessMasterValue usertype = workFlowProcessMasterValueConverter.convert(context1, nextEpersonrest.getUserType());
                    if (usertype != null && usertype.getPrimaryvalue() != null && usertype.getPrimaryvalue().equalsIgnoreCase("cc")) {
                        Map<String, String> d = getCCUserTapalnumber(cccount);
                        String inwardnumber = d.get("inwardnumber");
                        if (inwardnumber != null) {
                            System.out.println(":::::::::::::cc user inward number.:::::::::" + inwardnumber);
                            workFlowProcessInwardDetailsRest.setInwardNumber(inwardnumber);
                            cccount++;
                        }
                    } else {
                        workFlowProcessInwardDetailsRest.setInwardNumber(getInwardNumber().get("inwardnumber"));
                    }
                    workFlowProcessRest.setWorkFlowProcessInwardDetailsRest(workFlowProcessInwardDetailsRest);
                    //when multiple flow we nned to create doc
                    List<WorkflowProcessReferenceDocRest> doclist = tempdoclist.stream().filter(d -> d != null).filter(d -> d.getUuid() != null).map(d -> {
                        try {
                            WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convert(d, context1);
                            workflowProcessReferenceDoc = workflowProcessReferenceDocService.create(context1, workflowProcessReferenceDoc);
                            System.out.println("document create success " + workflowProcessReferenceDoc.getID());
                            return workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                        } catch (SQLException | AuthorizeException e) {
                            throw new RuntimeException(e);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());
                    //sender
                    List<WorkflowProcessSenderDiaryRest> tmpWorkflowProcessSenderDiaryRests = workflowProcessSenderDiaries.stream().filter(d -> d != null).filter(d -> d.getUuid() != null).map(d -> {
                        try {
                            WorkflowProcessSenderDiary workflowProcessSenderDiary = workflowProcessSenderDiaryConverter.convert(context, d);
                            return workflowProcessSenderDiaryConverter.convert(workflowProcessSenderDiary, utils.obtainProjection());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());
                    workFlowProcessRest.setWorkflowProcessSenderDiaryRests(tmpWorkflowProcessSenderDiaryRests);
                    workFlowProcessRest.setWorkFlowProcessDraftDetailsRest(workFlowProcessDraftDetailsRest);
                    workFlowProcessRest.setWorkflowProcessReferenceDocRests(doclist);
                    workFlowProcessRestTemp = workFlowType.storeWorkFlowProcess(context, workFlowProcessRest);
                    WorkflowProcess w = workFlowProcessConverter.convertByService(context1, workFlowProcessRestTemp);
                    WorkflowProcess complateWorkflowProcess = w;
                    if (workFlowProcessRest.getIsacknowledgement()) {
                        AcknowledgementDTO acknowledgement = getAcknowledgementDTO(w);
                        if (!acknowledgement.getRecipientemail().equalsIgnoreCase("-")) {
                            sentAcknowledgementEmail(context1, null, acknowledgement);
                        }
                    }
                    context1.commit();
                    if (complateWorkflowProcess.getItem() != null) {
                        Context context12 = ContextUtil.obtainContext(request);
                        WorkFlowProcessRest workFlowProcessRest2 = complete(context12, complateWorkflowProcess.getID());
                        if (workFlowProcessRest2 != null) {
                            workFlowProcessRestTemp = workFlowProcessRest2;
                            context12.commit();
                        }
                    }
                } else {
                    System.out.println(":::::::::::::IN SINGLE USER FROW CREATE  ::::::::::::::::::::::::::");
                    workFlowProcessRest.setWorkflowProcessReferenceDocRests(tempdoclist);
                    workFlowProcessRestTemp = workFlowType.storeWorkFlowProcess(context, workFlowProcessRest);
                    WorkflowProcess w = workFlowProcessConverter.convertByService(context1, workFlowProcessRestTemp);
                    WorkflowProcess complateWorkflowProcess = w;
                    if (workFlowProcessRest.getIsacknowledgement()) {
                        AcknowledgementDTO acknowledgement = getAcknowledgementDTO(w);
                        sentAcknowledgementEmail(context1, null, acknowledgement);
                    }
                    context1.commit();
                    if (complateWorkflowProcess.getItem() != null) {
                        //System.out.println("In Putin File");
                        Context context12 = ContextUtil.obtainContext(request);
                        WorkFlowProcessRest workFlowProcessRest2 = complete(context12, complateWorkflowProcess.getID());
                        if (workFlowProcessRest2 != null) {
                            workFlowProcessRestTemp = workFlowProcessRest2;
                            context12.commit();
                        }
                    }
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(workFlowProcessRestTemp);
    }


    public WorkFlowProcessRest complete(Context context, UUID uuid) throws Exception {
        log.info("in complete Action start!");
        WorkFlowProcessRest workFlowProcessRest = null;

        WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
        try {
            Item item = workFlowProcess.getItem();
            if (item != null) {

                System.out.println("size" + workFlowProcess.getWorkflowProcessReferenceDocs().size());
                workFlowProcess.getWorkflowProcessReferenceDocs().forEach(wd -> {
                    try {
                        if (wd.getDrafttype() != null && wd.getDrafttype().getPrimaryvalue() != null && wd.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Inward")) {
                            workflowProcessService.storeWorkFlowMataDataTOBitsream(context, wd, item);
                            System.out.println("tapal store in file " + item.getName());
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

            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }

            workflowProcessService.create(context, workFlowProcess);
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction COMPLETE = WorkFlowAction.COMPLETE;
            COMPLETE.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            log.info("in complete Action stop!");
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("in complete Action error!" + e.getMessage());
            throw new RuntimeException(e);
        }
        return workFlowProcessRest;
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = RequestMethod.POST,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}, value = "/createAndPutInFile")
    public ResponseEntity createAndPutInFile(MultipartFile file, String workFlowProcessReststr) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        WorkFlowProcessRest workFlowProcessRest = null;
        InputStream fileInputStream = null;
        Bitstream bitstream = null;
        WorkFlowProcessDraftDetailsRest workFlowProcessDraftDetailsRest = null;
        WorkflowProcessReferenceDocRest workflowProcessReferenceDocRest = null;
        workFlowProcessRest = mapper.readValue(workFlowProcessReststr, WorkFlowProcessRest.class);
        System.out.println("workFlowProcessReststr::" + workFlowProcessRest);

        HttpServletRequest request = getRequestService().getCurrentRequest().getHttpServletRequest();
        Context context = ContextUtil.obtainContext(request);
        context.turnOffAuthorisationSystem();
        try {
            System.out.println(":::::::::::::::::::::::::::::::::IN INWARD FLOW:::::::::::::::::::::::::::::");
            Optional<WorkflowProcessEpersonRest> initiatorEpersion = Optional.ofNullable((getSubmitor(context)));
            if (!initiatorEpersion.isPresent()) {
                return ResponseEntity.badRequest().body("no user found");
            }
            if (file != null) {
                System.out.println("IN FILE SAVE");
                WorkflowProcessReferenceDoc doc = new WorkflowProcessReferenceDoc();
                fileInputStream = file.getInputStream();
                bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, fileInputStream, "", file.getOriginalFilename());
                System.out.println("bitstream:only pdf:" + bitstream.getName());
                doc.setBitstream(bitstream);
                WorkFlowProcessInwardDetails workFlowProcessInwardDetails = workFlowProcessInwardDetailsConverter.convert(context, workFlowProcessRest.getWorkFlowProcessInwardDetailsRest());
                if (workFlowProcessInwardDetails.getLatterDate() != null) {
                    doc.setInitdate(workFlowProcessInwardDetails.getLatterDate());
                }
                if (workFlowProcessRest.getSubject() != null) {
                    doc.setSubject(workFlowProcessRest.getSubject());
                }
                if (workFlowProcessRest.getDocumenttypeRest() != null) {
                    doc.setWorkFlowProcessReferenceDocType(workFlowProcessMasterValueConverter.convert(context, workFlowProcessRest.getDocumenttypeRest()));
                }
                WorkFlowProcessMasterValue drafttype = getMastervalueData(context, WorkFlowType.MASTER.getAction(), WorkFlowType.INWARD.getAction());
                if (drafttype != null) {
                    doc.setDrafttype(drafttype);
                }
                if (workFlowProcessRest.getWorkFlowProcessInwardDetailsRest() != null && !DateUtils.isNullOrEmptyOrBlank(workFlowProcessRest.getWorkFlowProcessInwardDetailsRest().getFilereferencenumber())) {
                    doc.setReferenceNumber(workFlowProcessRest.getWorkFlowProcessInwardDetailsRest().getFilereferencenumber());
                }
                WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.create(context, doc);
                workflowProcessReferenceDocRest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                context.commit();
                System.out.println("OUT FILE SAVE DONE...");
            }
            WorkFlowType workFlowType = WorkFlowType.INWARD;
            workFlowType.setWorkFlowStatus(WorkFlowStatus.INPROGRESS);
            WorkFlowAction create = WorkFlowAction.CREATE;
            workFlowType.setWorkFlowAction(create);
            workFlowType.setProjection(utils.obtainProjection());
            List<WorkflowProcessReferenceDocRest> tempdoclist = workFlowProcessRest.getWorkflowProcessReferenceDocRests().stream().filter(d -> d != null).filter(dd -> dd.getUuid() != null).map(d -> {
                try {
                    WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, d);
                    return workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
            if (workflowProcessReferenceDocRest != null) {
                tempdoclist.add(workflowProcessReferenceDocRest);
            }
            System.out.println("::::::::::::::::DOCUMENT LIST SIZE" + tempdoclist.size());
            List<WorkflowProcessEpersonRest> initeatorandnextuserlist = new ArrayList<>();
            initeatorandnextuserlist.add(0, initiatorEpersion.get());
            initeatorandnextuserlist.add(1, initiatorEpersion.get());
            workFlowProcessRest.setWorkflowProcessEpersonRests(initeatorandnextuserlist);
            if (workFlowProcessRest.getWorkFlowProcessInwardDetailsRest() != null) {
                WorkFlowProcessInwardDetailsRest workFlowProcessInwardDetailsRest = workFlowProcessRest.getWorkFlowProcessInwardDetailsRest();
                if (DateUtils.isNullOrEmptyOrBlank(workFlowProcessInwardDetailsRest.getInwardNumber())) {
                    System.out.println("in set inward number");
                    workFlowProcessInwardDetailsRest.setInwardNumber(getInwardNumber().get("inwardnumber"));
                }
            }
            System.out.println(":::::::::::::IN SINGLE USER FROW CREATE  ::::::::::::::::::::::::::");
            workFlowProcessRest.setWorkflowProcessReferenceDocRests(tempdoclist);
            workFlowProcessRest = workFlowType.storeWorkFlowProcess(context, workFlowProcessRest);
            WorkflowProcess w = workFlowProcessConverter.convertByService(context, workFlowProcessRest);
            WorkflowProcess complateWorkflowProcess = w;
            if (workFlowProcessRest.getIsacknowledgement()) {
                AcknowledgementDTO acknowledgement = getAcknowledgementDTO(w);
                sentAcknowledgementEmail(context, null, acknowledgement);
            }
            context.commit();
            if (complateWorkflowProcess.getItem() != null) {
                Context context12 = ContextUtil.obtainContext(request);
                WorkFlowProcessRest workFlowProcessRest2 = complete(context12, complateWorkflowProcess.getID());
                if (workFlowProcessRest2 != null) {
                    workFlowProcessRest = workFlowProcessRest2;
                    context12.commit();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(workFlowProcessRest);
    }


    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = RequestMethod.POST,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}, value = "/forward")
    public WorkFlowProcessRest forward(MultipartFile file, String workFlowProcessReststr) throws IOException, SQLException, AuthorizeException {
        WorkFlowProcessRest workFlowProcessRest = null;
        HttpServletRequest request = getRequestService().getCurrentRequest().getHttpServletRequest();
        Context context = ContextUtil.obtainContext(request);
        context.turnOffAuthorisationSystem();
        InputStream fileInputStream = null;
        Bitstream bitstream = null;
        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = null;
        WorkflowProcessReferenceDocRest workflowProcessReferenceDocRest = null;
        String remark="";
        log.info("in Forward Action start");
        try {
            ObjectMapper mapper = new ObjectMapper();
            workFlowProcessRest = mapper.readValue(workFlowProcessReststr, WorkFlowProcessRest.class);
            String comment = workFlowProcessRest.getComment();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, UUID.fromString(workFlowProcessRest.getUuid()));
            WorkflowProcess  workFlowProcessfinal=workFlowProcess;
            if (workFlowProcess != null) {
                Optional<WorkflowProcessEperson> e = workFlowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getePerson().getID().equals(context.getCurrentUser().getID())).findFirst();
                if (e.isPresent() &&workFlowProcessRest.getComment()!=null) {
                    System.out.println("remark added ");
                    WorkflowProcessEperson ee = e.get();
                    ee.setRemark(workFlowProcessRest.getComment());
                    remark=workFlowProcessRest.getComment();
                    workflowProcessEpersonService.update(context, ee);
                    workFlowProcess.setRemark(workFlowProcessRest.getComment());
                }
                if(workFlowProcessRest.getIsinternal()!=null){
                    System.out.println("in getIsinternal"+workFlowProcessRest.getIsinternal());
                    workFlowProcess.setIsinternal(workFlowProcessRest.getIsinternal());
                }

            }
            if (file != null && workFlowProcess != null) {
                WorkFlowProcessMasterValue electronic = getMastervalueData(context, "Dispatch Mode", "Electronic");
                WorkFlowProcessInwardDetails workFlowProcessInwardDetails = workFlowProcess.getWorkFlowProcessInwardDetails();
                if (workFlowProcessInwardDetails != null) {
                    if (electronic != null) {
                        workFlowProcessInwardDetails.setInwardmode(electronic);
                    }
                }
                if (electronic != null) {
                    workFlowProcess.setDispatchmode(electronic);
                }
                System.out.println("IN FILE SAVE");
                WorkflowProcessReferenceDoc doc = new WorkflowProcessReferenceDoc();
                fileInputStream = file.getInputStream();
                bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, fileInputStream, "", file.getOriginalFilename());
                System.out.println("bitstream:only pdf:" + bitstream.getName());
                doc.setBitstream(bitstream);
                //             WorkFlowProcessInwardDetails workFlowProcessInwardDetails = workFlowProcessInwardDetailsConverter.convert(context, workFlowProcessRest.getWorkFlowProcessInwardDetailsRest());
//                if (workFlowProcessInwardDetails.getLatterDate() != null) {
//                    doc.setInitdate(workFlowProcessInwardDetails.getLatterDate());
//                }

                if (workFlowProcess.getSubject() != null) {
                    doc.setSubject(workFlowProcessRest.getSubject());
                }
                WorkFlowProcessMasterValue drafttype = getMastervalueData(context, WorkFlowType.MASTER.getAction(), WorkFlowType.INWARD.getAction());
                if (drafttype != null) {
                    doc.setDrafttype(drafttype);
                }
                doc.setWorkflowProcess(workFlowProcess);
                workflowProcessReferenceDocService.create(context, doc);
                System.out.println("OUT FILE SAVE DONE...");
            }
            if (workFlowProcessRest != null && workFlowProcessRest.getItemRest() != null) {
                workFlowProcess.setItem(itemConverter.convert(workFlowProcessRest.getItemRest(), context));
            }
            if (workFlowProcessRest.getDispatchModeRest() != null) {
                workFlowProcess.setDispatchmode(workFlowProcessMasterValueConverter.convert(context, workFlowProcessRest.getDispatchModeRest()));
            }

            if (workFlowProcessRest.getWorkflowProcessSenderDiaryRests() != null) {
                System.out.println("::::::::External SenderDiary ::::::::::save");
                List<WorkflowProcessSenderDiary> workflowProcessSenderDiaries = workFlowProcess.getWorkflowProcessSenderDiaries();
                for (WorkflowProcessSenderDiaryRest workflowProcessSenderDiaryrest : workFlowProcessRest.getWorkflowProcessSenderDiaryRests()) {
                    WorkflowProcessSenderDiary workflowProcessSenderDiary = workflowProcessSenderDiaryConverter.convert(context, workflowProcessSenderDiaryrest);
                    workflowProcessSenderDiary.setWorkflowProcess(workFlowProcessfinal);
                    workflowProcessSenderDiaryService.create(context,workflowProcessSenderDiary);
                    System.out.println("::::::::External SenderDiary ::::::::::save");

                }
            }
            if (workFlowProcess != null && workFlowProcessRest.getWorkflowProcessSenderDiaryEpersonRests() != null && workFlowProcessRest.getWorkflowProcessSenderDiaryEpersonRests().size() != 0) {
                System.out.println("in sender ::::::eper::::::dirys");
                for (WorkflowProcessSenderDiaryEpersonRest rest : workFlowProcessRest.getWorkflowProcessSenderDiaryEpersonRests()) {
                    WorkflowProcessSenderDiaryEperson workflowProcessSenderDiary = workflowProcessSenderDiaryEpersonConverter.convert(context, rest);
                    workflowProcessSenderDiary.setWorkflowProcess(workFlowProcessfinal);
                    workflowProcessSenderDiaryEpersonService.create(context, workflowProcessSenderDiary);
                }
            }
            EPerson inisiator = workFlowProcess.getWorkflowProcessEpeople().stream().filter(i -> i.getIndex() == 0).map(d -> d.getePerson()).findFirst().get();
            List<String> olduser = null;
            boolean isIntitiator = false;
            List<WorkflowProcessEperson> olduserlistuuid = workFlowProcess.getWorkflowProcessEpeople().stream().filter(d -> !d.getIssequence()).collect(Collectors.toList());
            List<WorkflowProcessEperson> olduserlistuuidissequenstrue = workFlowProcess.getWorkflowProcessEpeople().stream().collect(Collectors.toList());
            if (olduserlistuuid != null && olduserlistuuid.size() != 0) {
                olduser = olduserlistuuid.stream()
                        .filter(d -> d.getePerson() != null)
                        .filter(d -> d.getePerson().getID() != null)
                        .filter(d -> !d.getIssequence())
                        .map(d -> d.getePerson().getID().toString()).collect(Collectors.toList());
            }

            if (workFlowProcessRest.getWorkflowProcessEpersonRests() != null) {
                List<WorkflowProcessEpersonRest>    workflowProcessEpersonRestList = workFlowProcessRest.getWorkflowProcessEpersonRests().stream().filter(d->d!=null).filter(d -> !d.getIssequence()).collect(Collectors.toList());

                System.out.println("size:::::"+workflowProcessEpersonRestList.size());
                for (WorkflowProcessEpersonRest newEpesonrest : workflowProcessEpersonRestList) {
                    WorkflowProcessEperson workflowProcessEperson = workFlowProcessEpersonConverter.convert(context, newEpesonrest);
                    workflowProcessEperson.setWorkflowProcess(workFlowProcess);
                    Optional<WorkFlowProcessMasterValue> userTypeOption = WorkFlowUserType.NORMAL.getUserTypeFromMasterValue(context);
                    if (userTypeOption.isPresent()) {
                        workflowProcessEperson.setUsertype(userTypeOption.get());
                    }
                    if (newEpesonrest.getePersonRest() != null && newEpesonrest.getePersonRest().getId() != null && olduser != null && olduser.contains(newEpesonrest.getePersonRest().getId())) {
                        System.out.println(":::::::::ALLREADY USE EPERSON IN SYSTEM");
                    } else {

                        if (newEpesonrest.getePersonRest().getId().equalsIgnoreCase(inisiator.getID().toString())) {
                            System.out.println("in isIntitiator..............>");
                            isIntitiator = true;
                        } else {
                            System.out.println("ADD NEW USER IN WORKFLOWEPERSON LIST");
                            System.out.println("New user index  : " + workflowProcessEperson.getIndex());
                            workFlowProcess.setnewUser(workflowProcessEperson);
                            workflowProcessService.create(context, workFlowProcess);
                        }
                    }
                }
            }
            if (workFlowProcessRest.getWorkFlowProcessDraftDetailsRest() != null) {
                WorkFlowProcessDraftDetails workFlowProcessDraftDetails = workFlowProcessDraftDetailsConverter.convert(context, workFlowProcessRest.getWorkFlowProcessDraftDetailsRest());
                if (workFlowProcessDraftDetails != null) {
                    if (workFlowProcess.getWorkFlowProcessInwardDetails() != null) {
                        workFlowProcessDraftDetails.setReferencetapalnumber(workFlowProcess.getWorkFlowProcessInwardDetails());
                    }
                    workFlowProcess.setIsreplydraft(true);
                    workFlowProcess.setWorkFlowProcessDraftDetails(workFlowProcessDraftDetails);
                }
            }
            WorkFlowAction action = WorkFlowAction.FORWARD;
            //user not select any next user then flow go initiator
            if (isIntitiator) {
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
            if(remark!=null) {
                System.out.println("forward reemark   tapal" );
                workFlowProcessRest.setRemark(remark);
            }else {
                System.out.println("getRemark not found");
            }
            workFlowProcess.setIsread(false);
            action.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            workflowProcessService.create(context, workFlowProcess);
            context.commit();
            action.setComment(null);
            action.setWorkflowProcessReferenceDocs(null);
            action.setInitiator(false);
            log.info("in Forward Action stop");
            return workFlowProcessRest;

        } catch (
                RuntimeException e) {
            e.printStackTrace();
            throw new UnprocessableEntityException("error in forwardTask Server..");
        }
    }

    public List<WorkflowProcessReferenceDoc> getCommentDocuments(Context context, WorkFlowProcessRest wrest) {
        List<WorkflowProcessReferenceDoc> docs = null;
        if (wrest.getWorkflowProcessReferenceDocRests() != null) {
            if (wrest.getWorkflowProcessReferenceDocRests() != null) {
                docs = wrest.getWorkflowProcessReferenceDocRests().stream().map(d ->
                        {
                            try {
                                return workflowProcessReferenceDocConverter.convertByService(context, d);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                ).collect(Collectors.toList());
            }
        }

        return docs;
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = RequestMethod.POST,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}, value = "/draft")
    public WorkFlowProcessRest draft(MultipartFile file, String workFlowProcessReststr) throws IOException, SQLException, AuthorizeException {
        WorkFlowProcessRest workFlowProcessRest=null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            workFlowProcessRest = mapper.readValue(workFlowProcessReststr, WorkFlowProcessRest.class);
            System.out.println("workFlowProcessRest::" + new Gson().toJson(workFlowProcessRest));
            HttpServletRequest request = getRequestService().getCurrentRequest().getHttpServletRequest();
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            workFlowProcessRest.getWorkflowProcessEpersonRests().clear();
            Optional<WorkflowProcessEpersonRest> WorkflowProcessEpersonRest = Optional.ofNullable((getSubmitor(context)));
            WorkFlowType workFlowType = WorkFlowType.INWARD;
            //status
            workFlowType.setWorkFlowStatus(WorkFlowStatus.DRAFT);
            WorkFlowAction create = WorkFlowAction.CREATE;
            //set comment
            // create.setComment(workFlowProcessRest.getComment());
            //set action

            workFlowType.setWorkFlowAction(create);
            workFlowType.setProjection(utils.obtainProjection());
            workFlowProcessRest.getWorkflowProcessEpersonRests().add(WorkflowProcessEpersonRest.get());
            //perfome and stor to db
            workFlowProcessRest = workFlowType.storeWorkFlowProcessDraft(context, workFlowProcessRest);
            context.commit();
            create.setComment(null);
            create.setWorkflowProcessReferenceDocs(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workFlowProcessRest;
    }

    public Map<String, String> getCCUserTapalnumber(int cccount) {
        String inwardnumber = null;
        HttpServletRequest request = getRequestService().getCurrentRequest().getHttpServletRequest();
        Context context = ContextUtil.obtainContext(request);
        EPerson cEPerson = context.getCurrentUser();
        try {

            StringBuffer sb = new StringBuffer();
            WorkFlowProcessMasterValue department;
            if (cEPerson != null) {
                department = workFlowProcessMasterValueService.find(context, context.getCurrentUser().getDepartment().getID());
                if (department.getPrimaryvalue() != null) {
                    sb.append("T/" + department.getSecondaryvalue());
                }
            }
            if (cEPerson.getTablenumber() != null) {
                sb.append("/" + cEPerson.getTablenumber());
            }
            int count=workflowProcessService.getCountByType(context, getMastervalueData(context, WorkFlowType.MASTER.getAction(), WorkFlowType.INWARD.getAction()).getID(),DateUtils.getVersion());
            count = count + 1;
            sb.append("/0000" + count);
            sb.append("/" + DateUtils.getFinancialYear());
            sb.append("/C_" + cccount);
            inwardnumber = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in getInwardNumber ");
        }
        Map<String, String> map = new HashMap<>();
        map.put("inwardnumber", inwardnumber);
        return map;
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/getInwardNumber")
    public Map<String, String> getInwardNumber() throws Exception {
        String inwardnumber = null;
        try {
            HttpServletRequest request = getRequestService().getCurrentRequest().getHttpServletRequest();
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            EPerson currentuser = context.getCurrentUser();
            StringBuffer sb = new StringBuffer();
            WorkFlowProcessMasterValue department;
            if (currentuser != null) {
                department = workFlowProcessMasterValueService.find(context, context.getCurrentUser().getDepartment().getID());
                if (department.getPrimaryvalue() != null) {
                    sb.append("T/" + department.getSecondaryvalue());
                }
            }
            if (currentuser.getTablenumber() != null) {
                sb.append("/" + currentuser.getTablenumber());
            }
            int count =workflowProcessService.getCountByType(context, getMastervalueData(context, WorkFlowType.MASTER.getAction(), WorkFlowType.INWARD.getAction()).getID(),DateUtils.getVersion());
            count = count + 1;
            sb.append("/0000" + count);
            sb.append("/" + DateUtils.getFinancialYear());
            inwardnumber = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in getInwardNumber ");
        }
        Map<String, String> map = new HashMap<>();
        map.put("inwardnumber", inwardnumber);
        return map;
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/getDraftNumber")
    public Map<String, String> getDraftNumber() throws Exception {
        String inwardnumber = null;
        try {
            HttpServletRequest request = getRequestService().getCurrentRequest().getHttpServletRequest();
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            EPerson currentuser = context.getCurrentUser();
            StringBuffer sb = new StringBuffer();
            WorkFlowProcessMasterValue department;
            if (currentuser != null) {
                department = workFlowProcessMasterValueService.find(context, context.getCurrentUser().getDepartment().getID());
                if (department.getPrimaryvalue() != null) {
                    sb.append(department.getSecondaryvalue());
                }
            }
          /*  if (currentuser.getTablenumber() != null) {
                sb.append("/" + currentuser.getTablenumber());
            }*/
            Random random = new Random();

            // Generate a random 4-digit number
            int randomNumber = random.nextInt(9000) + 1000;

            sb.append("/0000" + randomNumber);

            sb.append("/" + DateUtils.getFinancialYear());

            inwardnumber = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in getInwardNumber ");
        }
        Map<String, String> map = new HashMap<>();
        map.put("draftdnumber", inwardnumber);
        return map;
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

    public WorkFlowProcessMasterValue getMastervalueData(Context context, String mastername, String mastervaluename) throws SQLException {
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

    public void sentAcknowledgementEmail(Context context, Bitstream bitstream, AcknowledgementDTO acknowledgementDTO) throws Exception {
        System.out.println("In sentAcknowledgementEmail --" + acknowledgementDTO.getRecipientemail());
        Email email = Email.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "acknowledgement"));
        email.addArgument(acknowledgementDTO.getSubject());
        email.addRecipient(acknowledgementDTO.getRecipientemail());
        email.addArgument(acknowledgementDTO.getRecipientName());               //1
        email.addArgument(DateUtils.DateSTRToDateFormatedd_mm_yyyy(acknowledgementDTO.getReceiveddate()));                //2
        email.addArgument(acknowledgementDTO.getOffice());                      //3
        email.addArgument(acknowledgementDTO.getRecipientOrganization());       //4
        email.addArgument(acknowledgementDTO.getDepartment());                  //5
        email.addArgument(acknowledgementDTO.getTapalnumber());                 //6
        email.addArgument(context.getCurrentUser().getOffice().getPrimaryvalue());          //7
        email.addArgument(context.getCurrentUser().getDepartment().getPrimaryvalue());      //8
        email.addArgument(context.getCurrentUser().getDesignation().getPrimaryvalue());     //9
        if (bitstream != null) {
            email.addAttachment(bitstreamService.retrieve(context, bitstream), bitstream.getName(), bitstream.getFormat(context).getMIMEType());
        }
        email.send();
        System.out.println("sent   sentAcknowledgementEmail done..!");
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
                    }
                    if (!DateUtils.isNullOrEmptyOrBlank(recipient.getEmail())) {
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

}
