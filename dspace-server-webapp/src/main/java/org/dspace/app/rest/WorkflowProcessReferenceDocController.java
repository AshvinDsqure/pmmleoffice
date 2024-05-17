/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.*;
import org.dspace.app.rest.enums.WorkFlowAction;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.DigitalSignRequet;
import org.dspace.app.rest.model.WorkflowProcessReferenceDocRest;
import org.dspace.app.rest.repository.AbstractDSpaceRestRepository;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.app.rest.utils.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.services.ConfigurationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller to upload bitstreams to a certain bundle, indicated by a uuid in the request
 * Usage: POST /api/core/bundles/{uuid}/bitstreams (with file and properties of file in request)
 * Example:
 * <pre>
 * {@code
 * curl https://<dspace.server.url>/api/core/bundles/d3599177-0408-403b-9f8d-d300edd79edb/bitstreams
 *  -XPOST -H 'Content-Type: multipart/form-data' \
 *  -H 'Authorization: Bearer eyJhbGciOiJI...' \
 *  -F "file=@Downloads/test.html" \
 *  -F 'properties={ "name": "test.html", "metadata": { "dc.description": [ { "value": "example file", "language": null,
 *          "authority": null, "confidence": -1, "place": 0 } ]}, "bundleName": "ORIGINAL" };type=application/json'
 * }
 * </pre>
 */
@RestController
@RequestMapping("/api/" + WorkflowProcessReferenceDocRest.CATEGORY)
public class WorkflowProcessReferenceDocController extends AbstractDSpaceRestRepository implements InitializingBean {
    private static final Logger log = LogManager.getLogger();
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    protected Utils utils;

    @Autowired
    BitstreamService bs;
    @Autowired
    private BundleRestRepository bundleRestRepository;

    @Autowired
    ConfigurationService configurationService;
    @Autowired
    private WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;
    private WorkflowProcessReferenceDocVersionConverter workflowProcessReferenceDocVersionConverter;
    @Autowired
    private WorkflowProcessService workflowProcessService;
    @Autowired
    WorkFlowProcessDraftDetailsService workFlowProcessDraftDetailsService;

    @Autowired
    private ItemConverter itemConverter;
    @Autowired
    private EPersonConverter ePersonConverter;

    @Autowired
    WorkflowProcessNoteConverter workflowProcessNoteConverter;
    @Autowired
    WorkFlowProcessCommentService workFlowProcessCommentService;
    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterValueService;
    @Autowired
    WorkFlowProcessMasterService workFlowProcessMasterService;


    @Autowired
    private WorkFlowProcessHistoryService workFlowProcessHistoryService;


    @Autowired
    private WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;

    @Autowired
    private BitstreamService bitstreamService;

    @Autowired
    private WorkflowProcessReferenceDocService workflowProcessReferenceDocService;

    @Autowired
    private WorkflowProcessReferenceDocVersionService workflowProcessReferenceDocVersionService;

    @Autowired
    private WorkflowProcessNoteService workflowProcessNoteService;
    @Autowired
    private DiscoverableEndpointsService discoverableEndpointsService;
    @Autowired
    private BundleService bundleService;

    /**
     * Method to upload a Bitstream to a Bundle with the given UUID in the URL. This will create a Bitstream with the
     * file provided in the request and attach this to the Item that matches the UUID in the URL.
     * This will only work for uploading one file, any extra files will silently be ignored
     *
     * @return The created BitstreamResource
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        discoverableEndpointsService
                .register(this, Arrays.asList(Link.of("/api/" + WorkflowProcessReferenceDocRest.CATEGORY, WorkflowProcessReferenceDocRest.CATEGORY)));
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}, value = "/bitstream")
    //@PreAuthorize("hasPermission(#uuid, 'BUNDLE', 'ADD') && hasPermission(#uuid, 'BUNDLE', 'WRITE')")
    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    public WorkflowProcessReferenceDocRest uploadBitstream(
            HttpServletRequest request,
            MultipartFile file, String workflowProcessReferenceDocRestStr) throws SQLException, AuthorizeException, IOException {
        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = null;
        WorkflowProcessReferenceDocRest workflowProcessReferenceDocRest = null;
        Bitstream bitstream = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            System.out.println("ccuser   "+context.getCurrentUser().getEmail());
            ObjectMapper mapper = new ObjectMapper();
            InputStream fileInputStream = null;
            InputStream fileInputStream1 = null;
            InputStream P12file = null;
            workflowProcessReferenceDocRest = mapper.readValue(workflowProcessReferenceDocRestStr, WorkflowProcessReferenceDocRest.class);
            workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDocRest, context);
            try {
                if (file != null && file.getInputStream() != null && file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty()) {
                    Optional<String> fileExtension = FileUtils.getExtensionByStringHandling(file.getOriginalFilename());
                    if (fileExtension.isPresent() && fileExtension.get().equalsIgnoreCase("docx") || fileExtension.get().equalsIgnoreCase("doc")) {
                        String pdfstorepathe = MargedDocUtils.ConvertDocInputstremToPDF(file.getInputStream(), FileUtils.getNameWithoutExtension(file.getOriginalFilename()));
                        FileInputStream pdfFileInputStream = new FileInputStream(new File(pdfstorepathe));
                        FileInputStream pdfFileInputStream1 = pdfFileInputStream;
                        bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, pdfFileInputStream, "", FileUtils.getNameWithoutExtension(file.getOriginalFilename()));
                        System.out.println("bitstream:doc to :pdf" + bitstream.getName());
                        workflowProcessReferenceDoc.setBitstream(bitstream);
                        workflowProcessReferenceDoc.setPage(FileUtils.getPageCountInPDF(pdfFileInputStream1));
                    }
                    if (fileExtension.isPresent() && fileExtension.get().equalsIgnoreCase("p12")) {
                        System.out.println("in nnnnnn");
                        P12file=file.getInputStream();
                        bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, file.getInputStream(), "", file.getOriginalFilename());
                        System.out.println(":::::bitstream:only p12 :::::" + bitstream.getName());
                        workflowProcessReferenceDoc.setBitstream(bitstream);
                    }
                    else {
                        fileInputStream = file.getInputStream();
                        fileInputStream1 = file.getInputStream();
                        bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, fileInputStream, "", file.getOriginalFilename());
                        System.out.println("bitstream:only pdf:" + bitstream.getName());
                        workflowProcessReferenceDoc.setBitstream(bitstream);
                        workflowProcessReferenceDoc.setPage(FileUtils.getPageCountInPDF(fileInputStream1));
                    }
                }
            } catch (IOException e) {
                log.error("Something went wrong when trying to read the inputstream from the given file in the request",
                        e);
                throw new UnprocessableEntityException("The InputStream from the file couldn't be read", e);
            }
            if (workflowProcessReferenceDocRest.getWorkFlowProcessRest() != null && workflowProcessReferenceDocRest.getWorkFlowProcessRest().getUuid() != null) {
                WorkflowProcess workflowProcess = workflowProcessService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkFlowProcessRest().getUuid()));
                Item item = workflowProcess.getItem();
                if (item != null) {
                    workflowProcess.getWorkflowProcessReferenceDocs().forEach(wd -> {
                        try {
                            workflowProcessService.storeWorkFlowMataDataTOBitsream(context, wd, item);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        } catch (AuthorizeException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc, item);
                }
                workflowProcessReferenceDoc.setWorkflowProcess(workflowProcess);
                workflowProcessService.update(context, workflowProcess);
            }
            if (workflowProcessReferenceDocRest.getDocumentsignatorRest() != null && workflowProcessReferenceDocRest.getWorkFlowProcessRest() != null && workflowProcessReferenceDocRest.getWorkFlowProcessRest().getId() != null) {
                System.out.println("::::::::::::Document Signator::Update::::::::::::::::");
                WorkFlowProcessDraftDetails workFlowProcessDraftDetails = workFlowProcessDraftDetailsService.getbyDocumentsignator(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkFlowProcessRest().getId()));
                workFlowProcessDraftDetails.setDocumentsignator(ePersonConverter.convert(context, workflowProcessReferenceDocRest.getDocumentsignatorRest()));
                workFlowProcessDraftDetailsService.update(context, workFlowProcessDraftDetails);
                System.out.println("::::::::::::Document Signator::Update:::Done!:::::::::::::");
            }
            if (workflowProcessReferenceDocRest.getItemname() != null) {
                workflowProcessReferenceDoc.setItemname(workflowProcessReferenceDocRest.getItemname());
            }
            if (workflowProcessReferenceDocRest.getWorkflowProcessNoteRest() != null) {
                workflowProcessReferenceDoc.setWorkflowprocessnote(workflowProcessNoteConverter.convert(workflowProcessReferenceDocRest.getWorkflowProcessNoteRest()));
            }
            if (workflowProcessReferenceDocRest.getDrafttypeRest() != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()) != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()).getPrimaryvalue().equalsIgnoreCase("Comment")) {
                List<WorkflowProcessReferenceDoc> docs = new ArrayList<>();
                if (workflowProcessReferenceDocRest.getUuid() != null) {
                    System.out.println("Comment if");
                    workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
                    WorkFlowProcessComment d = new WorkFlowProcessComment();
                    d.setSubmitter(context.getCurrentUser());
                    docs.add(workflowProcessReferenceDoc);
                    d.setWorkflowProcessReferenceDoc(docs);
                    if (workflowProcessReferenceDocRest.getWorkFlowProcessRest() != null && workflowProcessReferenceDocRest.getWorkFlowProcessRest().getUuid() != null) {
                        WorkflowProcess workflowProcess = workflowProcessService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkFlowProcessRest().getUuid()));
                        d.setWorkFlowProcess(workflowProcess);
                    }
                    storeVersion(context, workflowProcessReferenceDoc, bitstream, workflowProcessReferenceDocRest);
                    workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc);
                    workFlowProcessCommentService.create(context, d);
                    WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                    context.commit();
                    return rest;
                } else {
                    System.out.println("Comment else");
                    workflowProcessReferenceDoc = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
                    WorkFlowProcessComment d = new WorkFlowProcessComment();
                    d.setSubmitter(context.getCurrentUser());
                    if (workflowProcessReferenceDocRest.getWorkFlowProcessRest() != null && workflowProcessReferenceDocRest.getWorkFlowProcessRest().getUuid() != null) {
                        WorkflowProcess workflowProcess = workflowProcessService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkFlowProcessRest().getUuid()));
                        d.setWorkFlowProcess(workflowProcess);
                    }
                    docs.add(workflowProcessReferenceDoc);
                    d.setWorkflowProcessReferenceDoc(docs);
                    storeVersion(context, workflowProcessReferenceDoc, bitstream, workflowProcessReferenceDocRest);
                    workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc);
                    workFlowProcessCommentService.create(context, d);
                    WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                    context.commit();
                    return rest;
                }
            }
            //Document
            if (workflowProcessReferenceDocRest.getDrafttypeRest() != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()) != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()).getPrimaryvalue().equalsIgnoreCase("Document")) {
                System.out.println("Document in");
                if (workflowProcessReferenceDocRest.getUuid() != null && workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest() != null && workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest().getId() != null) {
                    //in version update
                    workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
                    WorkflowProcessReferenceDoc workflowProcessReferenceDocf = workflowProcessReferenceDocConverter.convertRestToDoc(context, workflowProcessReferenceDoc, workflowProcessReferenceDocRest);
                    WorkflowProcessReferenceDocVersion version = workflowProcessReferenceDocVersionService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest().getUuid()));
                    if (version != null) {
                        System.out.println("Document in version update!");
                        if (bitstream != null) {
                            version.setBitstream(bitstream);
                            workflowProcessReferenceDocf.setBitstream(bitstream);
                        }
                        version.setCreationdatetime(new Date());
                        storeWorkFlowHistoryVersionUpdate(context, workflowProcessReferenceDocf, version.getVersionnumber());
                        workflowProcessReferenceDocVersionService.update(context, version);
                        WorkflowProcessReferenceDoc d = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDocf);
                        if (workflowProcessReferenceDocRest.getWorkFlowProcessRest() != null) {
                            WorkflowProcess workflowProcess = workflowProcessService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkFlowProcessRest().getUuid()));
                            System.out.println("Doc:::::::::::::::::::::::::::::" + d);
                            Item item = workflowProcess.getItem();
                            if (item != null) {
                                System.out.println("::::::::::::: update bitstream");
                                workflowProcessService.storeWorkFlowMataDataTOBitsream(context, d, item);
                            }
                        }
                        WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDocf, utils.obtainProjection());
                        context.commit();
                        return rest;
                    }
                }
                //sognitore sing after document upload.document add in selected file.
                if (workflowProcessReferenceDocRest.getUuid() != null && workflowProcessReferenceDocRest.getIssignature()) {
                    System.out.println("sognitore sing after document upload.document");
                    workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
                    if (bitstream != null) {
                        workflowProcessReferenceDoc.setBitstream(bitstream);
                    }
                    workflowProcessReferenceDoc.setIssignature(true);
                    WorkflowProcess workflowProcess = workflowProcessReferenceDoc.getWorkflowProcess();
                    WorkflowProcessReferenceDoc finaldoc = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
                    //document push  in items
                    //  if (workflowProcess.getItem() != null) {
                    //  workflowProcessService.storeWorkFlowMataDataTOBitsream(context, finaldoc, workflowProcess.getItem());
                    //  }
                    storeWorkFlowHistoryforDocument(context, finaldoc);
                    WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                    context.commit();
                    return rest;
                }
                //create next version
                if (workflowProcessReferenceDocRest.getUuid() != null) {
                    if (workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest() == null) {
                        System.out.println("in create new next version of Document ");
                        workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
                        WorkflowProcessReferenceDoc workflowProcessReferenceDocf = workflowProcessReferenceDocConverter.convertRestToDoc(context, workflowProcessReferenceDoc, workflowProcessReferenceDocRest);
                        if (workflowProcessReferenceDocRest.getWorkFlowProcessRest() != null) {
                            workflowProcessReferenceDocf.setWorkflowProcess(workflowProcessService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getUuid())));
                        }
                        if (bitstream != null) {
                            workflowProcessReferenceDocf.setBitstream(bitstream);
                        }
                        storeVersion(context, workflowProcessReferenceDocf, bitstream, workflowProcessReferenceDocRest);
                        if (workflowProcessReferenceDoc.getWorkflowProcess() != null) {
                            System.out.println("in store histitory create new version ");
                            Item item = workflowProcessReferenceDoc.getWorkflowProcess().getItem();
                            if (item != null) {
                                System.out.println("when new version create update bitstream");
                                workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDocf, item);
                            }
                            storeWorkFlowHistoryforDocument(context, workflowProcessReferenceDoc);
                        }
                        WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                        context.commit();
                        return rest;
                    }
                } else {
                    //create fresh version
                    System.out.println("In New Document Create.");
                    workflowProcessReferenceDoc = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
                    //storeVersion(context, workflowProcessReferenceDoc, bitstream, workflowProcessReferenceDocRest);
                   // workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc);
                   WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                   /* if (workflowProcessReferenceDoc.getWorkflowprocessnote() != null && workflowProcessReferenceDoc.getWorkflowprocessnote().getID() != null) {
                        rest.setWorkflowProcessNoteRest(workflowProcessNoteConverter.convert(workflowProcessNoteService.find(context, workflowProcessReferenceDoc.getWorkflowprocessnote().getID()), utils.obtainProjection()));
                    }*/
                    context.commit();
                    return rest;
                }
            }
            //outward
            if (workflowProcessReferenceDocRest.getDrafttypeRest() != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()) != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()).getPrimaryvalue().equalsIgnoreCase("Outward")) {
                System.out.println("Outward in");
                if (workflowProcessReferenceDocRest.getUuid() != null && workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest() != null && workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest().getId() != null) {
                    //in version update
                    workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
                    WorkflowProcessReferenceDoc workflowProcessReferenceDocf = workflowProcessReferenceDocConverter.convertRestToDoc(context, workflowProcessReferenceDoc, workflowProcessReferenceDocRest);
                    WorkflowProcessReferenceDocVersion version = workflowProcessReferenceDocVersionService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest().getUuid()));
                    if (version != null) {
                        System.out.println("Outward in version update!");
                        if (bitstream != null) {
                            version.setBitstream(bitstream);
                            workflowProcessReferenceDocf.setBitstream(bitstream);
                        }
                        version.setCreationdatetime(new Date());
                        storeWorkFlowHistoryVersionUpdate(context, workflowProcessReferenceDocf, version.getVersionnumber());
                        workflowProcessReferenceDocVersionService.update(context, version);
                        WorkflowProcessReferenceDoc d = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDocf);
                        if (workflowProcessReferenceDocRest.getWorkFlowProcessRest() != null) {
                            WorkflowProcess workflowProcess = workflowProcessService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkFlowProcessRest().getUuid()));
                            System.out.println("Doc:::::::::::::::::::::::::::::" + d);
                            Item item = workflowProcess.getItem();
                            if (item != null) {
                                System.out.println("::::::::::::: update bitstream");
                                workflowProcessService.storeWorkFlowMataDataTOBitsream(context, d, item);
                            }
                        }
                        WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDocf, utils.obtainProjection());
                        context.commit();
                        return rest;
                    }
                }
                //create next version
                if (workflowProcessReferenceDocRest.getUuid() != null) {
                    if (workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest() == null) {
                        System.out.println("in create new next version of Outward ");
                        workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
                        WorkflowProcessReferenceDoc workflowProcessReferenceDocf = workflowProcessReferenceDocConverter.convertRestToDoc(context, workflowProcessReferenceDoc, workflowProcessReferenceDocRest);
                        if (workflowProcessReferenceDocRest.getWorkFlowProcessRest() != null) {
                            workflowProcessReferenceDocf.setWorkflowProcess(workflowProcessService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getUuid())));
                        }
                        if (bitstream != null) {
                            workflowProcessReferenceDocf.setBitstream(bitstream);
                        }
                        storeVersion(context, workflowProcessReferenceDocf, bitstream, workflowProcessReferenceDocRest);
                        if (workflowProcessReferenceDoc.getWorkflowProcess() != null) {
                            System.out.println("in store histitory create new version ");
                            Item item = workflowProcessReferenceDoc.getWorkflowProcess().getItem();
                            if (item != null) {
                                System.out.println("when new version create update bitstream");
                                workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDocf, item);
                            }
                            storeWorkFlowHistoryforDocument(context, workflowProcessReferenceDoc);
                        }
                        WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                        context.commit();
                        return rest;
                    }
                } else {
                    //create fresh version
                    System.out.println("In New Outward Create.");
                    workflowProcessReferenceDoc = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
                    storeVersion(context, workflowProcessReferenceDoc, bitstream, workflowProcessReferenceDocRest);
                    workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc);
                    WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                    if (workflowProcessReferenceDoc.getWorkflowprocessnote() != null && workflowProcessReferenceDoc.getWorkflowprocessnote().getID() != null) {
                        rest.setWorkflowProcessNoteRest(workflowProcessNoteConverter.convert(workflowProcessNoteService.find(context, workflowProcessReferenceDoc.getWorkflowprocessnote().getID()), utils.obtainProjection()));
                    }
                    context.commit();
                    return rest;
                }
            }
            //notsheet
            if (workflowProcessReferenceDocRest.getDrafttypeRest() != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()) != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()).getPrimaryvalue().equalsIgnoreCase("Notesheet")) {
                System.out.println("Notesheet in");
                if (workflowProcessReferenceDocRest.getUuid() != null && workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest() != null && workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest().getId() != null) {
                    workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
                    WorkflowProcessReferenceDocVersion version = workflowProcessReferenceDocVersionService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest().getUuid()));
                    if (version != null) {
                        System.out.println("Notesheet in version update!");
                        if (workflowProcessReferenceDocRest.getEditortext() != null) {
                            version.setEditortext(workflowProcessReferenceDocRest.getEditortext());
                        }
                        if (workflowProcessReferenceDocRest.getBitstreamRest() != null) {
                            version.setBitstream(bitstream);
                        }
                        version.setCreationdatetime(new Date());
                        storeWorkFlowHistoryVersionUpdate(context, workflowProcessReferenceDoc, version.getVersionnumber());
                        workflowProcessReferenceDocVersionService.update(context, version);
                        WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                        context.commit();
                        return rest;
                    }
                }
                if (workflowProcessReferenceDocRest.getUuid() != null) {
                    if (workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest() == null) {
                        System.out.println("in create new version of note ");
                        workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
                        storeVersion(context, workflowProcessReferenceDoc, bitstream, workflowProcessReferenceDocRest);
                        if (workflowProcessReferenceDoc.getWorkflowProcess() != null) {
                            System.out.println("in store histitory create new version ");
                            storeWorkFlowHistoryforDocument(context, workflowProcessReferenceDoc);
                        }
                        WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                        context.commit();
                        return rest;
                    }
                } else {
                    System.out.println("Notesheet in else ");
                    WorkflowProcessNote workflowProcessNote = new WorkflowProcessNote();
                    workflowProcessNote.setSubmitter(context.getCurrentUser());
                    if (workflowProcessReferenceDoc.getSubject() != null) {
                        workflowProcessNote.setSubject(workflowProcessReferenceDoc.getSubject());
                    }
                    WorkflowProcessNote finalw = workflowProcessNoteService.create(context, workflowProcessNote);
                    workflowProcessReferenceDoc.setWorkflowprocessnote(finalw);
                    workflowProcessReferenceDoc = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);

                    storeVersion(context, workflowProcessReferenceDoc, bitstream, workflowProcessReferenceDocRest);
                    workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc);
                    WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                    if (workflowProcessReferenceDoc.getWorkflowprocessnote() != null && workflowProcessReferenceDoc.getWorkflowprocessnote().getID() != null) {
                        rest.setWorkflowProcessNoteRest(workflowProcessNoteConverter.convert(workflowProcessNoteService.find(context, workflowProcessReferenceDoc.getWorkflowprocessnote().getID()), utils.obtainProjection()));
                    }
                    context.commit();
                    return rest;
                }
            }
            //Reply Tapal
            if (workflowProcessReferenceDocRest.getDrafttypeRest() != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()) != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()).getPrimaryvalue().equalsIgnoreCase("Reply Tapal")) {
                System.out.println("Reply Tapal in");
                if (workflowProcessReferenceDocRest.getUuid() != null && workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest() != null && workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest().getId() != null) {
                    workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
                    WorkflowProcessReferenceDocVersion version = workflowProcessReferenceDocVersionService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest().getUuid()));
                    if (version != null) {
                        System.out.println("Reply Tapal in version update!");
                        if (workflowProcessReferenceDocRest.getEditortext() != null) {
                            version.setEditortext(workflowProcessReferenceDocRest.getEditortext());
                        }
                        if (workflowProcessReferenceDocRest.getBitstreamRest() != null) {
                            version.setBitstream(bitstream);
                        }
                        version.setCreationdatetime(new Date());
                        storeWorkFlowHistoryVersionUpdate(context, workflowProcessReferenceDoc, version.getVersionnumber());
                        workflowProcessReferenceDocVersionService.update(context, version);
                        WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                        context.commit();
                        return rest;
                    }
                }
                if (workflowProcessReferenceDocRest.getUuid() != null) {
                    if (workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest() == null) {
                        System.out.println("in create new version of Reply Tapal ");
                        workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
                        storeVersion(context, workflowProcessReferenceDoc, bitstream, workflowProcessReferenceDocRest);
                        if (workflowProcessReferenceDoc.getWorkflowProcess() != null) {
                            System.out.println("in store histitory create new version ");
                            storeWorkFlowHistoryforDocument(context, workflowProcessReferenceDoc);
                        }
                        WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                        context.commit();
                        return rest;
                    }
                } else {
                    System.out.println("Reply Tapal in else ");
                    workflowProcessReferenceDoc = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
                    storeVersion(context, workflowProcessReferenceDoc, bitstream, workflowProcessReferenceDocRest);
                    workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc);
                    WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                    if (workflowProcessReferenceDoc.getWorkflowprocessnote() != null && workflowProcessReferenceDoc.getWorkflowprocessnote().getID() != null) {
                        rest.setWorkflowProcessNoteRest(workflowProcessNoteConverter.convert(workflowProcessNoteService.find(context, workflowProcessReferenceDoc.getWorkflowprocessnote().getID()), utils.obtainProjection()));
                    }
                    context.commit();
                    return rest;
                }
            }
            //PKSC12
            if (workflowProcessReferenceDocRest.getDrafttypeRest() != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()) != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()).getPrimaryvalue().equalsIgnoreCase("PKCS12")) {
                    System.out.println("PKSC12 ");
                    workflowProcessReferenceDoc = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
                    //workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc);
                    WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                    context.commit();
                    return rest;
            } else {
                if(workflowProcessReferenceDoc.getDrafttype()!=null&&workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue()!=null) {
                    System.out.println("IN Other document " + workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue());
                }
                System.out.println("IN Other document Other");

                workflowProcessReferenceDoc = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
                //workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc);
                context.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
    }

    public void storeVersion(Context context, WorkflowProcessReferenceDoc workflowProcessReferenceDoc, Bitstream bitstream, WorkflowProcessReferenceDocRest rest) throws Exception {
        WorkflowProcessReferenceDocVersion version = new WorkflowProcessReferenceDocVersion();
        version.setCreator(context.getCurrentUser());
        version.setIsactive(true);
        version.setCreationdatetime(new Date());
        if (bitstream != null) {
            version.setBitstream(bitstream);
            // workflowProcessReferenceDoc.setBitstream(bitstream);
        }
        if (rest.getEditortext() != null) {
            byte[] bytes = rest.getEditortext().getBytes("UTF-8");
            String string = new String(bytes, "UTF-8");
            version.setEditortext(string);
            // workflowProcessReferenceDoc.setEditortext(string);
        }
        Double versionnumber = (double) workflowProcessReferenceDoc.getWorkflowProcessReferenceDocVersion().size() + 1;
        version.setVersionnumber(versionnumber);
        version.setWorkflowProcessReferenceDoc(workflowProcessReferenceDoc);
        Set<WorkflowProcessReferenceDocVersion> versionsetss = new HashSet<>();
        List<WorkflowProcessReferenceDocVersion> versionset = workflowProcessReferenceDocVersionService.getDocVersionBydocumentID(context, workflowProcessReferenceDoc.getID(), 0, 100);
        System.out.println(versionset);
        for (WorkflowProcessReferenceDocVersion v : versionset) {
            if (v.getIsactive()) {
                System.out.println("in Active Version");
                WorkflowProcessReferenceDocVersion vv = workflowProcessReferenceDocVersionService.find(context, v.getID());
                if (vv != null) {
                    vv.setIsactive(false);
                    workflowProcessReferenceDocVersionService.update(context, vv);
                    versionsetss.add(vv);
                }
            } else {
                versionsetss.add(v);
            }
        }
        versionsetss.add(version);
        workflowProcessReferenceDoc.setWorkflowProcessReferenceDocVersion(versionsetss);
        workflowProcessReferenceDocService.update(context, workflowProcessReferenceDoc);
    }


    public WorkflowProcessReferenceDoc createFinalNote(Context context, WorkflowProcess workflowProcess) throws
            Exception {
        boolean isTextEditorFlow = false;

        InputStream input2 = null;
        DocToPdfConverter docToPdfConverter = null;
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        System.out.println("start.......createFinalNote");
        StringBuffer sb = new StringBuffer("<!DOCTYPE html>\n" + "<html>\n" + "<head><style>@page{size:A4;margin: 0;}</style>\n" + "<title>Note</title>\n" + "</head>\n" + "<body style=\"padding-right: 20px;padding-left: 20px;background-color:#c5e6c1;\">");
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
                        // out = bitstreamService.retrieve(context, version.getBitstream());
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
                // aa.add(DateFormate(workflowProcess.getWorkflowProcessNote().getInitDate()));
                //sb.append("<br>" + DateFormate(workflowProcess.getWorkflowProcessNote().getInitDate()) + "</span></div>");
            }
            // map.put("creator", aa);
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
                    // String baseurl = configurationService.getProperty("dspace.server.url");
                    referencedocumentmap.put("name", FileUtils.getNameWithoutExtension(workflowProcessReferenceDoc.getBitstream().getName()));
                    //  referencedocumentmap.put("link", baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content");
                    //sb.append("<span style=\"text-align: left;\"><a href=" + baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content>");
                    if (!isTextEditorFlow) {
                        // stroremetadateinmap(workflowProcessReferenceDoc.getBitstream(), referencedocumentmap);
                        listreferenceReference.add(referencedocumentmap);
                    } else {
                        // stroremetadate(workflowProcessReferenceDoc.getBitstream(), sb);
                    }
                }
            }

        }

        sb.append("</div>");
        // map.put("Reference Documents", listreferenceReference);
        sb.append("<div style=\"width:23%;float:right;\"> <p><b>Reference Noting</b></p> ");
        Map<String, String> referencenottingmap = null;
        List<Map<String, String>> listreferencenotting = new ArrayList<>();
        for (WorkflowProcessReferenceDoc workflowProcessReferenceDoc : workflowProcess.getWorkflowProcessReferenceDocs()) {
            referencenottingmap = new HashMap<String, String>();
            if (workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue().equals("Reference Noting")) {
                if (workflowProcessReferenceDoc.getBitstream() != null) {
                    //   String baseurl = configurationService.getProperty("dspace.server.url");
                    //  referencenottingmap.put("name1", FileUtils.getNameWithoutExtension(workflowProcessReferenceDoc.getBitstream().getName()));
                    //    referencenottingmap.put("link", baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content");
                    //  sb.append("<span style=\"text-align: left;\"><a href=" + baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content>");
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
                                //   sb.append(" " + DateFormate(note.getInitDate()));
                                //    notecreateor.append(" " + DateFormate(note.getInitDate()));
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
        // map.put("Reference Noting", listreferencenotting);
        // pending file
        List<WorkFlowProcessComment> comments = workFlowProcessCommentService.getComments(context, workflowProcess.getID());
        // map.put("comment", comments);
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
                            //        String baseurl = configurationService.getProperty("dspace.server.url");
                            //  sb.append("<span> <a href=" + baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content>");
                            //     stroremetadate(workflowProcessReferenceDoc.getBitstream(), sb);
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
                //      sb.append("<br>" + DateFormate(comment.getWorkFlowProcessHistory().getActionDate()));
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
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}, value = "/outward")
    //@PreAuthorize("hasPermission(#uuid, 'BUNDLE', 'ADD') && hasPermission(#uuid, 'BUNDLE', 'WRITE')")
    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    public List<WorkflowProcessReferenceDocRest> uploadBitstreamoutward(HttpServletRequest request,
                                                                        @RequestBody List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRestListstr,
                                                                        @Nullable Pageable optionalPageable) throws SQLException, AuthorizeException, IOException {
        List<WorkflowProcessReferenceDocRest> rsponce = new ArrayList<>();
        Context context = ContextUtil.obtainContext(request);
        System.out.println("workflowProcessReferenceDocRestListstr::" + workflowProcessReferenceDocRestListstr);
        System.out.println("workflowProcessReferenceDocRestList size::" + workflowProcessReferenceDocRestListstr.size());
        rsponce = workflowProcessReferenceDocRestListstr.stream().map(wrd -> {
            WorkflowProcessReferenceDocRest rest = null;
            try {
                WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convert(wrd, context);
                if (wrd.getSubject() != null) {
                    workflowProcessReferenceDoc.setSubject(wrd.getSubject());
                }
                if (wrd.getDrafttypeRest() != null) {
                    workflowProcessReferenceDoc.setDrafttype(workFlowProcessMasterValueConverter.convert(context, wrd.getDrafttypeRest()));
                }
                if (wrd.getBitstreamRest() != null && wrd.getBitstreamRest().getUuid() != null) {
                    Bitstream bitstream = bs.find(context, UUID.fromString(wrd.getBitstreamRest().getUuid()));
                    if (bitstream != null) {
                        workflowProcessReferenceDoc.setBitstream(bitstream);
                        InputStream inputStream = bitstreamService.retrieve(context, bitstream);
                        if (inputStream != null) {
                            workflowProcessReferenceDoc.setPage(FileUtils.getPageCountInPDF(inputStream));
                        }
                    }
                }
                if (wrd.getWorkFlowProcessRest() != null && wrd.getWorkFlowProcessRest().getUuid() != null) {
                    WorkflowProcess workflowProcess = workflowProcessService.find(context, UUID.fromString(wrd.getWorkFlowProcessRest().getUuid()));
                    workflowProcessReferenceDoc.setWorkflowProcess(workflowProcess);
                }
                if (wrd.getWorkflowProcessNoteRest() != null) {
                    workflowProcessReferenceDoc.setWorkflowprocessnote(workflowProcessNoteService.find(context, UUID.fromString(wrd.getWorkflowProcessNoteRest().getUuid())));
                }
                if (wrd.getItemname() != null) {
                    System.out.println("in item rest :");
                    workflowProcessReferenceDoc.setItemname(wrd.getItemname());
                }
                WorkflowProcessReferenceDoc finalworkflowProcessReferenceDoc = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
//                if (wrd.getWorkFlowProcessRest() != null && wrd.getDrafttypeRest() != null) {
//                    storeWorkFlowHistoryforDocument(context, finalworkflowProcessReferenceDoc);
//                }
               // workflowProcessService.storeWorkFlowMataDataTOBitsream(context, finalworkflowProcessReferenceDoc);
                rest = workflowProcessReferenceDocConverter.convert(finalworkflowProcessReferenceDoc, utils.obtainProjection());
                if (workflowProcessReferenceDoc.getWorkflowprocessnote() != null) {
                    rest.setWorkflowProcessNoteRest(workflowProcessNoteConverter.convert(workflowProcessReferenceDoc.getWorkflowprocessnote(), utils.obtainProjection()));
                }
                return rest;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        context.commit();
        return rsponce;
    }

    public void storeWorkFlowHistoryVersionUpdate(Context context, WorkflowProcessReferenceDoc doc, Double versionnumber) throws Exception {
        System.out.println("::::::IN :storeWorkFlowHistory::Update::version:::::: ");
        WorkFlowProcessHistory workFlowAction = null;
        WorkflowProcess workflowProcess = doc.getWorkflowProcess();
        workFlowAction = new WorkFlowProcessHistory();
        WorkFlowProcessMaster workFlowProcessMaster = WorkFlowAction.MASTER.getMaster(context);
        workFlowAction.setWorkflowProcessEpeople(workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get());
        WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.UPDATE.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setAction(workFlowProcessMasterValue);
        workFlowAction.setWorkflowProcess(workflowProcess);
        workflowProcess.getWorkflowProcessNote().getSubject();
        workFlowAction.setComment("Update Version " + versionnumber + " for " + doc.getDrafttype().getPrimaryvalue() + ".");
        workFlowProcessHistoryService.create(context, workFlowAction);
        System.out.println("::::::OUT :storeWorkFlowHistory::Update::version:::::: ");
    }

    //this hisory call when Attaged Reference Doc in view Draft note.
    public void storeWorkFlowHistoryforDocument(Context context, WorkflowProcessReferenceDoc doc) throws Exception {
        System.out.println("::::::IN :storeWorkFlowHistory::::Document:::::: ");
        WorkflowProcess workflowProcess = doc.getWorkflowProcess();
        WorkFlowProcessHistory workFlowAction = null;
        workFlowAction = new WorkFlowProcessHistory();
        WorkFlowProcessMaster workFlowProcessMaster = WorkFlowAction.MASTER.getMaster(context);
        if (doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Document") && doc.getIssignature()) {
            WorkflowProcessEperson eperson = new WorkflowProcessEperson();
            eperson.setOwner(true);
            eperson.setePerson(context.getCurrentUser());
            eperson.setWorkflowProcess(workflowProcess);
            eperson.setIndex(workflowProcess.getWorkflowProcessEpeople().stream().map(d -> d.getSequence()).max(Integer::compareTo).get());
            workflowProcess.setnewUser(eperson);
            workflowProcessService.create(context, workflowProcess);
            WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.COMPLETE.getAction(), workFlowProcessMaster);
            workFlowAction.setAction(workFlowProcessMasterValue);
            workFlowAction.setWorkflowProcessEpeople(workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get());

        } else {
         //   workFlowAction.setWorkflowProcessEpeople(workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get());
            WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.CREATE.getAction(), workFlowProcessMaster);
            workFlowAction.setAction(workFlowProcessMasterValue);
        }
        workFlowAction.setActionDate(new Date());
        workFlowAction.setWorkflowProcess(workflowProcess);
        if (doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reference Document")) {
            workFlowAction.setComment("Attached " + doc.getDrafttype().getPrimaryvalue() + " In  " + (doc.getItemname() != null ? doc.getItemname() : " "));
        }
        if (doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reference Noting")) {
            workFlowAction.setComment("Attached " + doc.getDrafttype().getPrimaryvalue() + " In  " + (doc.getSubject() != null ? doc.getSubject() : " "));
        }
        if (doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Comment")) {
            workFlowAction.setComment("Attached " + doc.getDrafttype().getPrimaryvalue() + " In  " + (doc.getItemname() != null ? doc.getItemname() : " "));
        }
        if (doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Notesheet")) {
            workFlowAction.setComment("Create Version  " + doc.getWorkflowProcessReferenceDocVersion().size() + " for  " + (doc.getSubject() != null ? doc.getSubject() : " "));
        }
        if (doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Document") && !doc.getIssignature()) {
            workFlowAction.setComment("Create Version  " + doc.getWorkflowProcessReferenceDocVersion().size() + " for  " + (doc.getDrafttype().getPrimaryvalue() != null ? doc.getDrafttype().getPrimaryvalue() : " "));
        }
        if (doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Document") && doc.getIssignature()) {
            workFlowAction.setComment("Document Signature successfully  By " + doc.getDocumentsignator().getFullName() + " | " + doc.getDocumentsignator().getDesignation().getPrimaryvalue());
        }
        if (doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase(" Referral File") && doc.getIssignature()) {
            workFlowAction.setComment("Add Referral File " + doc.getItemname());
        }
        if (doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Outward") && !doc.getIssignature()) {
            workFlowAction.setComment("Create Version  " + doc.getWorkflowProcessReferenceDocVersion().size() + " for  " + (doc.getDrafttype().getPrimaryvalue() != null ? doc.getDrafttype().getPrimaryvalue() : " "));
        }
        workFlowProcessHistoryService.create(context, workFlowAction);
        System.out.println("::::::OUT :storeWorkFlowHistory::Document:::::::: ");
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getSignatureDocuments")
    public List<WorkflowProcessReferenceDocRest> getSignatureDocuments(HttpServletRequest request) {
        System.out.println("::::::::::::::::::getSignatureDocuments::::::::::::start");

        Context context = ContextUtil.obtainContext(request);
        List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRests = null;
        UUID draftid = null;
        try {
            WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Draft Type");
            if (workFlowProcessMaster != null) {
                WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, "Document", workFlowProcessMaster);
                if (workFlowProcessMasterValue != null) {
                    draftid = workFlowProcessMasterValue.getID();
                }
            }
            List<WorkflowProcessReferenceDoc> list = workflowProcessReferenceDocService.getDocumentBySignitore(context, context.getCurrentUser().getID(), draftid);
            workflowProcessReferenceDocRests = list.stream().map(d -> {
                return workflowProcessReferenceDocConverter.convert(d, utils.obtainProjection());
            }).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("::::::::::::::::::getSignatureDocuments::::::::::::stop");
        return workflowProcessReferenceDocRests;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getDocByDraftType")
    @PreAuthorize("hasPermission(#uuid, 'ITEM', 'WRITE')")
    public List<WorkflowProcessReferenceDocRest> getDocByDraftType(@Parameter(value = "drafttypeid", required = true) String drafttypeid, HttpServletRequest request) {
        System.out.println("::::::::::::::::::getDocByDraftType::::::::::::start");
        Context context = ContextUtil.obtainContext(request);
        List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRests = null;
        try {
            List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs = workflowProcessReferenceDocService.getDocumentBySignitore(context, context.getCurrentUser().getID(), UUID.fromString(drafttypeid));
            workflowProcessReferenceDocRests = workflowProcessReferenceDocs.stream().map(workflowProcessReferenceDoc -> {
                return workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
            }).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("::::::::::::::::::getDocByDraftType::::::::::::stop");
        return workflowProcessReferenceDocRests;
    }

    public static boolean isNullOrEmptyOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public Boolean checkSingData(Context context, WorkflowProcessReferenceDoc doc,InputStream P12file) {
        //CloseableHttpClient httpClient = HttpClients.createDefault();
        DigitalSignRequet digitalSignRequet = new DigitalSignRequet();
        InputStream pkcs12File = null;
        InputStream certFile = null;
        InputStream fileInputa = null;
        try {
            String certType = configurationService.getProperty("digital.sign.certtype");
            String password = doc.getDescription();
            String showSignature = configurationService.getProperty("digital.sign.showsignature");
            String reason = configurationService.getProperty("digital.sign.reason");
            String location = configurationService.getProperty("digital.sign.location");
            String name = context.getCurrentUser().getFullName();
            String pageNumber = configurationService.getProperty("digital.sign.pagenumber");
            File dummyfile = new File(configurationService.getProperty("digital.sign.dummy"));
            fileInputa = new FileInputStream(dummyfile);
            if (fileInputa != null) {
                digitalSignRequet.setFileInput(fileInputa);
            }
            if (doc.getBitstream() != null && doc.getBitstream().getName() != null) {
                System.out.println("in name");
                digitalSignRequet.setCertFileName(doc.getBitstream().getName());
                digitalSignRequet.setP12FileName(doc.getBitstream().getName());
            }
            pkcs12File = P12file;
            certFile = P12file;
            System.out.println("bitstreamid :" + doc.getBitstream().getID());
            System.out.println("certFile :" + certFile);
            System.out.println("pdf :" + fileInputa);
            System.out.println("p12File :" + pkcs12File);
            System.out.println("password :" + password);
            System.out.println("certType :" + certType);
            System.out.println("location :" + location);
            System.out.println("pageNumber :" + pageNumber);
            System.out.println("reason :" + reason);
            System.out.println("name :" + name);
            System.out.println("p12.getName() :" + doc.getBitstream().getName());
            System.out.println("cert.getName() :" + doc.getBitstream().getName());
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        Boolean isSign = false;
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        File tempsingpdf = new File(TEMP_DIRECTORY, "signdoc" + ".pdf");
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
            builder.addTextBody("certType", digitalSignRequet.getCertType(), ContentType.TEXT_PLAIN);
            builder.addTextBody("showSignature", digitalSignRequet.getShowSignature(), ContentType.TEXT_PLAIN);
            builder.addTextBody("location", digitalSignRequet.getLocation(), ContentType.TEXT_PLAIN);
            builder.addTextBody("reason", digitalSignRequet.getReason(), ContentType.TEXT_PLAIN);
            builder.addTextBody("pageNumber", digitalSignRequet.getPageNumber(), ContentType.TEXT_PLAIN);
            builder.addTextBody("name", digitalSignRequet.getName(), ContentType.TEXT_PLAIN);
            builder.addTextBody("password", digitalSignRequet.getPassword(), ContentType.TEXT_PLAIN);
            // Add a binary file
            builder.addBinaryBody("fileInput", digitalSignRequet.getFileInput(), ContentType.APPLICATION_OCTET_STREAM, digitalSignRequet.getFileInputName());
            builder.addBinaryBody("p12File", digitalSignRequet.getP12File(), ContentType.APPLICATION_OCTET_STREAM, digitalSignRequet.getP12FileName());
            builder.addBinaryBody("certFile", digitalSignRequet.getCertFile(), ContentType.APPLICATION_OCTET_STREAM, digitalSignRequet.getCertFileName());
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
//                    FileInputStream pdfFileInputStream = new FileInputStream(new File(tempsingpdf.getAbsolutePath()));
//                    Bitstream bitstreampdfsing = bundleRestRepository.processBitstreamCreationWithoutBundle1(context, pdfFileInputStream, "", tempsingpdf.getName(), bitstream);
//                    if (bitstreampdfsing != null) {
//                        Map<String, String> map = new HashMap<>();
//                        map.put("bitstreampid", bitstreampdfsing.getID().toString());
//                        System.out.println("Sing Doc Paths::" + tempsingpdf.getAbsolutePath());
//                        context.commit();
//                        isSign=true;
//                        return isSign;
//                    }
                    isSign = true;
                    return isSign;
                } else {
                    System.out.println("errot with " + statusCode);
                    HttpEntity entity = response.getEntity();
                    byte[] responseBody = EntityUtils.toByteArray(entity);
                    isSign = false;
                    return isSign;

                }
            } catch (IOException e) {
                System.out.println("error" + e.getMessage());
                isSign = false;
                e.printStackTrace();
                return isSign;

            }
        } catch (Exception e) {
            isSign = false;
            System.out.println("error" + e.getMessage());
            e.printStackTrace();
        }
        return isSign;
    }
}
