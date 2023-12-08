/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.*;
import org.dspace.app.rest.enums.WorkFlowAction;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.WorkflowProcessReferenceDocRest;
import org.dspace.app.rest.repository.AbstractDSpaceRestRepository;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.FileUtils;
import org.dspace.app.rest.utils.MargedDocUtils;
import org.dspace.app.rest.utils.Utils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    @PreAuthorize("hasPermission(#uuid, 'BUNDLE', 'ADD') && hasPermission(#uuid, 'BUNDLE', 'WRITE')")
    public WorkflowProcessReferenceDocRest uploadBitstream(
            HttpServletRequest request,
            MultipartFile file, String workflowProcessReferenceDocRestStr) throws SQLException, AuthorizeException, IOException {
        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = null;
        WorkflowProcessReferenceDocRest workflowProcessReferenceDocRest = null;
        Bitstream bitstream = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            ObjectMapper mapper = new ObjectMapper();
            InputStream fileInputStream = null;
            workflowProcessReferenceDocRest = mapper.readValue(workflowProcessReferenceDocRestStr, WorkflowProcessReferenceDocRest.class);
            workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDocRest, context);
            try {
                if (file != null && file.getInputStream() != null && file.getOriginalFilename()!=null && !file.getOriginalFilename().isEmpty()) {
                    Optional<String> fileExtension = FileUtils.getExtensionByStringHandling(file.getOriginalFilename());
                    if (fileExtension.isPresent() && fileExtension.get().equalsIgnoreCase("docx") || fileExtension.get().equalsIgnoreCase("doc")) {
                        String pdfstorepathe = MargedDocUtils.ConvertDocInputstremToPDF(file.getInputStream(), FileUtils.getNameWithoutExtension(file.getOriginalFilename()));
                        FileInputStream pdfFileInputStream = new FileInputStream(new File(pdfstorepathe));
                        bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, pdfFileInputStream, "", FileUtils.getNameWithoutExtension(file.getOriginalFilename()));
                        System.out.println("bitstream:doc to :pdf" + bitstream.getName());
                        workflowProcessReferenceDoc.setBitstream(bitstream);
                    } else {
                        fileInputStream = file.getInputStream();
                        bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, fileInputStream, "", file.getOriginalFilename());
                        System.out.println("bitstream:only pdf:" + bitstream.getName());
                        workflowProcessReferenceDoc.setBitstream(bitstream);
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
                    if (workflowProcess.getItem() != null) {
                        workflowProcessService.storeWorkFlowMataDataTOBitsream(context, finaldoc, workflowProcess.getItem());
                    }
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
            } else {
                workflowProcessReferenceDoc = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
                workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc);
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

    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}, value = "/outward")
    @PreAuthorize("hasPermission(#uuid, 'BUNDLE', 'ADD') && hasPermission(#uuid, 'BUNDLE', 'WRITE')")
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
                if (wrd.getWorkFlowProcessRest() != null && wrd.getDrafttypeRest() != null) {
                    storeWorkFlowHistoryforDocument(context, finalworkflowProcessReferenceDoc);
                }
                workflowProcessService.storeWorkFlowMataDataTOBitsream(context, finalworkflowProcessReferenceDoc);
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
            eperson.setIndex(workflowProcess.getWorkflowProcessEpeople().stream().map(d->d.getSequence()).max(Integer::compareTo).get());
            workflowProcess.setnewUser(eperson);
            workflowProcessService.create(context, workflowProcess);
            WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.COMPLETE.getAction(), workFlowProcessMaster);
            workFlowAction.setAction(workFlowProcessMasterValue);
            workFlowAction.setWorkflowProcessEpeople(workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get());

        } else {
            workFlowAction.setWorkflowProcessEpeople(workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get());
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
        UUID draftid=null;
        try {
            WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Draft Type");
            if (workFlowProcessMaster != null) {
                WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, "Document", workFlowProcessMaster);
                if (workFlowProcessMasterValue != null) {
                    draftid=workFlowProcessMasterValue.getID();
                }
            }
            List<WorkflowProcessReferenceDoc> list = workflowProcessReferenceDocService.getDocumentBySignitore(context, context.getCurrentUser().getID(),draftid);
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
    public List<WorkflowProcessReferenceDocRest> getDocByDraftType(@Parameter(value = "workflowtypeid",required = true)UUID drafttypeid, HttpServletRequest request) {
        System.out.println("::::::::::::::::::getDocByDraftType::::::::::::start");
        Context context = ContextUtil.obtainContext(request);
        List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRests = null;
        try {
            List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs = workflowProcessReferenceDocService.getDocumentBySignitore(context, context.getCurrentUser().getID(),drafttypeid);
            workflowProcessReferenceDocRests = workflowProcessReferenceDocs.stream().map(workflowProcessReferenceDoc -> {
                return workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
            }).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("::::::::::::::::::getDocByDraftType::::::::::::stop");
        return workflowProcessReferenceDocRests;
    }
}
