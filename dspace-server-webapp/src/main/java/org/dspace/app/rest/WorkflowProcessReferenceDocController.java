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
import org.dspace.app.rest.exception.AlreadyDataExistException;
import org.dspace.app.rest.exception.InvalidDataException;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.ApprovedReplyDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
   ItemService itemService;
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


    @PostMapping("/uploadMultiple")
    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}, value = "/uploadMultiple")
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public List<WorkflowProcessReferenceDocRest> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files, String workflowProcessReferenceDocRestStr, HttpServletRequest request) throws Exception {
        Context context = ContextUtil.obtainContext(request);
        context.turnOffAuthorisationSystem();
        System.out.println(":::::::::::::uploadMultipleFiles:::::::::::::");
        ObjectMapper mapper = new ObjectMapper();
        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = null;
        WorkflowProcessReferenceDocRest workflowProcessReferenceDocRest = null;
        List<WorkflowProcessReferenceDocRest> restlist=new ArrayList<>();
        List<WorkflowProcessReferenceDoc> doclist=new ArrayList<>();
        try {
            workflowProcessReferenceDocRest = mapper.readValue(workflowProcessReferenceDocRestStr, WorkflowProcessReferenceDocRest.class);
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    InputStream fileInputStream1 = file.getInputStream();
                    Bitstream bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, fileInputStream1, "", file.getOriginalFilename());
                    workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDocRest, context);
                    workflowProcessReferenceDoc.setBitstream(bitstream);
                    if (workflowProcessReferenceDocRest.getWorkFlowProcessRest() != null && workflowProcessReferenceDocRest.getWorkFlowProcessRest().getUuid() != null) {
                        WorkflowProcess workflowProcess = workflowProcessService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkFlowProcessRest().getUuid()));
                        if (workflowProcess != null) {
                            workflowProcessReferenceDoc.setWorkflowProcess(workflowProcess);
                        }
                    }
                }
                WorkflowProcessReferenceDoc doc = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
                doclist.add(doc);
            }
            restlist = doclist.stream().map(d -> {
                return workflowProcessReferenceDocConverter.convert(d, utils.obtainProjection());
            }).collect(Collectors.toList());
            context.commit();

            System.out.println(":::::::::::::::::uploadMultipleFiles:::::::done::::::::");
            return restlist;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}, value = "/bitstream")
    //@PreAuthorize("hasPermission(#uuid, 'BUNDLE', 'ADD') && hasPermission(#uuid, 'BUNDLE', 'WRITE')")
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public WorkflowProcessReferenceDocRest uploadBitstream(
            HttpServletRequest request,
            HttpServletResponse res,
            MultipartFile file, String workflowProcessReferenceDocRestStr) throws SQLException, AuthorizeException, IOException, InvalidDataException {
        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = null;
        WorkflowProcessReferenceDocRest workflowProcessReferenceDocRest = null;
        Bitstream bitstream = null;
        File temppkcs12 = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            // System.out.println("ccuser   " + context.getCurrentUser().getEmail());
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
//                    if (fileExtension.isPresent() && fileExtension.get().equalsIgnoreCase("p12")) {
//                        P12file = file.getInputStream();
//                        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
//                        long currentTimeMillis = System.currentTimeMillis();
//                        // Create a date object with the current time
//                        Date now = new Date(currentTimeMillis);
//                        // Create a date format to include milliseconds
//                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
//                        // Format the date into a string
//                        String formattedDate = sdf.format(now);
//                        temppkcs12 = new File(TEMP_DIRECTORY, formattedDate + "_" + file.getOriginalFilename());
//                        if (!temppkcs12.exists()) {
//                            try {
//                                temppkcs12.createNewFile();
//                                System.out.println("File creted ::" + temppkcs12.getAbsolutePath());
//                            } catch (IOException e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
//                        } else {
//                            System.out.println("File name like  ::" + temppkcs12.getAbsolutePath());
//                        }
//
//                        String filename = file.getOriginalFilename();
//                        file.transferTo(new File(temppkcs12.getAbsolutePath()));
//                        FileInputStream p12fileinputstream = new FileInputStream(new File(temppkcs12.getAbsolutePath()));
//                        if (workflowProcessReferenceDocRest.getPassword() != null && !workflowProcessReferenceDocRest.getPassword().isEmpty() && p12fileinputstream != null) {
//                            if (DigitalSingPDF.checkDigital(temppkcs12.getAbsolutePath(), workflowProcessReferenceDocRest.getPassword())) {
//                                System.out.println("verifive Success fully Digital Credential");
//                                bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, P12file, "", filename);
//                                System.out.println(":::::bitstream:only p12 :::::" + bitstream.getName());
//                                workflowProcessReferenceDoc.setBitstream(bitstream);
//                            } else {
//                                res.sendError(406, "Mismatched Certificate and Key!");
//                                throw new InvalidDataException("Mismatched Certificate and Key!");
//                            }
//                        } else {
//                            System.out.println("not found p12 and password!");
//                        }
//                    }

                    else if (fileExtension.isPresent() && fileExtension.get().equalsIgnoreCase("pdf")) {
                        fileInputStream = file.getInputStream();
                        fileInputStream1 = file.getInputStream();
                        bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, fileInputStream, "", file.getOriginalFilename());
                        System.out.println("bitstream:only pdf:" + bitstream.getName());
                        workflowProcessReferenceDoc.setBitstream(bitstream);
                        workflowProcessReferenceDoc.setPage(FileUtils.getPageCountInPDF(fileInputStream1));
                    } else {
                        throw  new RuntimeException("Not Support  File Extension "+fileExtension.get());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                log.error("Something went wrong when trying to read the inputstream from the given file in the request",
                        e);
                throw new UnprocessableEntityException("The InputStream from the file couldn't be read", e);
            }

            if (workflowProcessReferenceDocRest.getWorkFlowProcessRest() != null && workflowProcessReferenceDocRest.getWorkFlowProcessRest().getUuid() != null) {
                System.out.println("save in workflow");
                WorkflowProcess workflowProcess = workflowProcessService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkFlowProcessRest().getUuid()));
                workflowProcessReferenceDoc.setWorkflowProcess(workflowProcess);
                workflowProcessService.update(context, workflowProcess);
                System.out.println("save in workflow done::");
            }

//            if (workflowProcessReferenceDocRest.getWorkFlowProcessRest() != null && workflowProcessReferenceDocRest.getWorkFlowProcessRest().getUuid() != null) {
//                WorkflowProcess workflowProcess = workflowProcessService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkFlowProcessRest().getUuid()));
//                Item item = workflowProcess.getItem();
//                if (item != null) {
//                    workflowProcess.getWorkflowProcessReferenceDocs().forEach(wd -> {
//                        try {
//                            workflowProcessService.storeWorkFlowMataDataTOBitsream(context, wd, item);
//                        } catch (SQLException e) {
//                            throw new RuntimeException(e);
//                        } catch (AuthorizeException e) {
//                            throw new RuntimeException(e);
//                        }
//                    });
//                    workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc, item);
//                }
//                workflowProcessReferenceDoc.setWorkflowProcess(workflowProcess);
//                workflowProcessService.update(context, workflowProcess);
//            }

//            if (workflowProcessReferenceDocRest.getDocumentsignatorRest() != null && workflowProcessReferenceDocRest.getWorkFlowProcessRest() != null && workflowProcessReferenceDocRest.getWorkFlowProcessRest().getId() != null) {
//                System.out.println("::::::::::::Document Signator::Update::::::::::::::::");
//                WorkFlowProcessDraftDetails workFlowProcessDraftDetails = workFlowProcessDraftDetailsService.getbyDocumentsignator(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkFlowProcessRest().getId()));
//                workFlowProcessDraftDetails.setDocumentsignator(ePersonConverter.convert(context, workflowProcessReferenceDocRest.getDocumentsignatorRest()));
//                workFlowProcessDraftDetailsService.update(context, workFlowProcessDraftDetails);
//                System.out.println("::::::::::::Document Signator::Update:::Done!:::::::::::::");
//            }
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
                                //workflowProcessService.storeWorkFlowMataDataTOBitsream(context, d, item);
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
                                //workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDocf, item);
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
                            System.out.println("::::::::::::::::::::create Notesheet :::pdf:::::::::::::::::");
                            version.setEditortext(workflowProcessReferenceDocRest.getEditortext());
                            workflowProcessReferenceDoc.setEditortext(workflowProcessReferenceDocRest.getEditortext());
                            InputStream inputStream = createFinalNoteComment(context, workflowProcessReferenceDoc, getTempDirPath());
                            Bitstream pdfbitstream1 = getByInputStream(context, inputStream, getTempDirPath());
                            if (pdfbitstream1 != null) {
                                workflowProcessReferenceDoc.setBitstream(pdfbitstream1);
                                version.setBitstream(pdfbitstream1);
                            }
                            System.out.println("::::::::::::::::::::create Notesheet :::pdf::::::::::done::::::");
                        }
                        if (workflowProcessReferenceDocRest.getBitstreamRest() != null) {
                            version.setBitstream(bitstream);
                        }
                        version.setCreationdatetime(new Date());
                       // storeWorkFlowHistoryVersionUpdate(context, workflowProcessReferenceDoc, version.getVersionnumber());
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
                        System.out.println("::::::::::::::::::::create Notesheet :::pdf:::::::::::::::::");
                        if(workflowProcessReferenceDocRest.getEditortext()!=null) {
                            workflowProcessReferenceDoc.setEditortext(workflowProcessReferenceDocRest.getEditortext());
                            InputStream inputStream = createFinalNoteComment(context, workflowProcessReferenceDoc, getTempDirPath());
                            Bitstream bitstream1 = getByInputStream(context, inputStream, getTempDirPath());
                            workflowProcessReferenceDoc.setBitstream(bitstream1);
                            System.out.println("::::::::::::::::::::create Notesheet :::pdf::::::::::done::::::");
                            storeVersion(context, workflowProcessReferenceDoc, bitstream1, workflowProcessReferenceDocRest);
                        }else {
                            storeVersion(context, workflowProcessReferenceDoc, bitstream, workflowProcessReferenceDocRest);
                        }

//                        if (workflowProcessReferenceDoc.getWorkflowProcess() != null) {
//                            System.out.println("in store histitory create new version ");
//                            storeWorkFlowHistoryforDocument(context, workflowProcessReferenceDoc);
//                        }


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
                    if(workflowProcessReferenceDoc.getEditortext()!=null) {
                        System.out.println("::::::::::::::::::::create Notesheet :::pdf:::::::::::::::::");
                        InputStream inputStream = createFinalNoteComment(context, workflowProcessReferenceDoc, getTempDirPath());
                        Bitstream bitstream1 = getByInputStream(context, inputStream, getTempDirPath());
                        workflowProcessReferenceDoc.setBitstream(bitstream1);
                        System.out.println("::::::::::::::::::::create Notesheet :::pdf::::::::::done::::::");
                        storeVersion(context, workflowProcessReferenceDoc, bitstream1, workflowProcessReferenceDocRest);
                    }else{
                        storeVersion(context, workflowProcessReferenceDoc, bitstream, workflowProcessReferenceDocRest);
                    }
                   // workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc);
                    WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                    if (workflowProcessReferenceDoc.getWorkflowprocessnote() != null && workflowProcessReferenceDoc.getWorkflowprocessnote().getID() != null) {
                        rest.setWorkflowProcessNoteRest(workflowProcessNoteConverter.convert(workflowProcessNoteService.find(context, workflowProcessReferenceDoc.getWorkflowprocessnote().getID()), utils.obtainProjection()));
                    }
                    context.commit();
                    return rest;
                }
            }
            //Reply Tapal
//            if (workflowProcessReferenceDocRest.getDrafttypeRest() != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()) != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()).getPrimaryvalue().equalsIgnoreCase("Reply Tapal")) {
//                System.out.println("Reply Tapal in");
//                if (workflowProcessReferenceDocRest.getUuid() != null && workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest() != null && workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest().getId() != null) {
//                    workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
//                    WorkflowProcessReferenceDocVersion version = workflowProcessReferenceDocVersionService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest().getUuid()));
//                    if (version != null) {
//                        System.out.println("Reply Tapal in version update!");
//                        if (workflowProcessReferenceDocRest.getEditortext() != null) {
//                            version.setEditortext(workflowProcessReferenceDocRest.getEditortext());
//                        }
//                        if (workflowProcessReferenceDocRest.getBitstreamRest() != null) {
//                            version.setBitstream(bitstream);
//                        }
//                        version.setCreationdatetime(new Date());
//                        storeWorkFlowHistoryVersionUpdate(context, workflowProcessReferenceDoc, version.getVersionnumber());
//                        workflowProcessReferenceDocVersionService.update(context, version);
//                        WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
//                        context.commit();
//                        return rest;
//                    }
//                }
//                if (workflowProcessReferenceDocRest.getUuid() != null) {
//                    if (workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest() == null) {
//                        System.out.println("in create new version of Reply Tapal ");
//                        workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
//                        storeVersion(context, workflowProcessReferenceDoc, bitstream, workflowProcessReferenceDocRest);
//                        if (workflowProcessReferenceDoc.getWorkflowProcess() != null) {
//                            System.out.println("in store histitory create new version ");
//                            //storeWorkFlowHistoryforDocument(context, workflowProcessReferenceDoc);
//                        }
//                        WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
//                        context.commit();
//                        return rest;
//                    }
//                } else {
//                    System.out.println("Reply Tapal in else ");
//                    workflowProcessReferenceDoc = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
//                    storeVersion(context, workflowProcessReferenceDoc, bitstream, workflowProcessReferenceDocRest);
//                    workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc);
//                    WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
//                    if (workflowProcessReferenceDoc.getWorkflowprocessnote() != null && workflowProcessReferenceDoc.getWorkflowprocessnote().getID() != null) {
//                        rest.setWorkflowProcessNoteRest(workflowProcessNoteConverter.convert(workflowProcessNoteService.find(context, workflowProcessReferenceDoc.getWorkflowprocessnote().getID()), utils.obtainProjection()));
//                    }
//                    context.commit();
//                    return rest;
//                }
//            }


            //Reply Tapal 1
            if (workflowProcessReferenceDocRest.getDrafttypeRest() != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()) != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()).getPrimaryvalue().equalsIgnoreCase("Reply Tapal")) {
                System.out.println("Reply Tapal in");
                if (workflowProcessReferenceDocRest.getUuid() != null && workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest() != null && workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest().getId() != null) {
                    workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
                    WorkflowProcessReferenceDocVersion version = workflowProcessReferenceDocVersionService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest().getUuid()));
                    if (version != null) {
                        System.out.println(":::::::Reply Tapal in version update!::::::::::");
                        if (workflowProcessReferenceDocRest.getEditortext() != null) {
                            version.setEditortext(workflowProcessReferenceDocRest.getEditortext());
                            workflowProcessReferenceDoc.setEditortext(workflowProcessReferenceDocRest.getEditortext());
                        }
                        if (workflowProcessReferenceDocRest.getBitstreamRest() != null) {
                            version.setBitstream(bitstream);
                        }
                        version.setCreationdatetime(new Date());
                        // storeWorkFlowHistoryVersionUpdate(context, workflowProcessReferenceDoc, version.getVersionnumber());
                        workflowProcessReferenceDocVersionService.update(context, version);
                        WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                        if (workflowProcessReferenceDocRest.getEditortext() != null) {
                            System.out.println("doc update>>>>>");
                            workflowProcessReferenceDoc.setEditortext(workflowProcessReferenceDocRest.getEditortext());
                        }
                        workflowProcessReferenceDocService.update(context, workflowProcessReferenceDoc);
                        context.commit();
                        return rest;
                    }
                }
                if (workflowProcessReferenceDocRest.getUuid() != null) {
                    if (workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest() == null) {
                        System.out.println(":::::in create new version of Reply Tapal.:::::  ");
                        workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
                        storeVersion(context, workflowProcessReferenceDoc, bitstream, workflowProcessReferenceDocRest);
                        if (workflowProcessReferenceDoc.getWorkflowProcess() != null) {
                         //  System.out.println("in store histitory create new version ");
                            // storeWorkFlowHistoryforDocument(context, workflowProcessReferenceDoc);
                        }
                        WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                        context.commit();
                        return rest;
                    }
                } else {
                    System.out.println("Reply Tapal Create Fress! ");
                    // System.out.println("::::::::::::::::::::create Note :::pdf:::::::::::::::::");
//                    InputStream inputStream = createFinalNoteComment(context, workflowProcessReferenceDoc, getTempDirPath());
//                    Bitstream bitstream1 = getByInputStream(context, inputStream, getTempDirPath());
//                    workflowProcessReferenceDoc.setBitstream(bitstream1);
                    //   System.out.println("::::::::::::::::::::create Note :::pdf::::::::::done::::::");
                    workflowProcessReferenceDoc = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
                    storeVersion(context, workflowProcessReferenceDoc, bitstream, workflowProcessReferenceDocRest);
                   // workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc);
                    WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                    if (workflowProcessReferenceDoc.getWorkflowprocessnote() != null && workflowProcessReferenceDoc.getWorkflowprocessnote().getID() != null) {
                        rest.setWorkflowProcessNoteRest(workflowProcessNoteConverter.convert(workflowProcessNoteService.find(context, workflowProcessReferenceDoc.getWorkflowprocessnote().getID()), utils.obtainProjection()));
                    }
                    context.commit();
                    return rest;
                }
            }




            //Note
            if (workflowProcessReferenceDocRest.getDrafttypeRest() != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()) != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()).getPrimaryvalue().equalsIgnoreCase("Note")) {
                System.out.println("Note Doc");
                if (workflowProcessReferenceDocRest.getUuid() != null && workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest() != null && workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest().getId() != null) {
                    workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
                    WorkflowProcessReferenceDocVersion version = workflowProcessReferenceDocVersionService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest().getUuid()));
                    if (version != null) {
                        System.out.println("Note Doc in version update!");
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
                        System.out.println("in create new version of Note Doc ");
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
                    System.out.println("Note Docin create ");

                    if(workflowProcessReferenceDocRest.getEditortext()!=null){

                    }
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
            //Reply Note
            if (workflowProcessReferenceDocRest.getDrafttypeRest() != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()) != null && workFlowProcessMasterValueConverter.convert(context, workflowProcessReferenceDocRest.getDrafttypeRest()).getPrimaryvalue().equalsIgnoreCase("Reply Note")) {
                System.out.println("Reply Note in");
                if (workflowProcessReferenceDocRest.getUuid() != null && workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest() != null && workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest().getId() != null) {
                    workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
                    WorkflowProcessReferenceDocVersion version = workflowProcessReferenceDocVersionService.find(context, UUID.fromString(workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest().getUuid()));
                    if (version != null) {
                        System.out.println("Reply Note in version update!");
                        if (workflowProcessReferenceDocRest.getEditortext() != null) {
                            version.setEditortext(workflowProcessReferenceDocRest.getEditortext());
                            workflowProcessReferenceDoc.setEditortext(workflowProcessReferenceDocRest.getEditortext());
                        }
                        if (workflowProcessReferenceDocRest.getBitstreamRest() != null) {
                            version.setBitstream(bitstream);
                        }
                        version.setCreationdatetime(new Date());
                        // storeWorkFlowHistoryVersionUpdate(context, workflowProcessReferenceDoc, version.getVersionnumber());
                        workflowProcessReferenceDocVersionService.update(context, version);
                        WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                        if (workflowProcessReferenceDocRest.getEditortext() != null) {
                            System.out.println("doc update>>>>>");
                            workflowProcessReferenceDoc.setEditortext(workflowProcessReferenceDocRest.getEditortext());
                        }
                        workflowProcessReferenceDocService.update(context, workflowProcessReferenceDoc);
                        context.commit();
                        return rest;
                    }
                }
                if (workflowProcessReferenceDocRest.getUuid() != null) {
                    if (workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest() == null) {
                        System.out.println("in create new version of Reply Note  ");
                        workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
                        storeVersion(context, workflowProcessReferenceDoc, bitstream, workflowProcessReferenceDocRest);
                        if (workflowProcessReferenceDoc.getWorkflowProcess() != null) {
                            System.out.println("in store histitory create new version ");
                            // storeWorkFlowHistoryforDocument(context, workflowProcessReferenceDoc);
                        }
                        WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                        context.commit();
                        return rest;
                    }
                } else {
                    System.out.println("Reply Note in else ");
                   // System.out.println("::::::::::::::::::::create Note :::pdf:::::::::::::::::");
//                    InputStream inputStream = createFinalNoteComment(context, workflowProcessReferenceDoc, getTempDirPath());
//                    Bitstream bitstream1 = getByInputStream(context, inputStream, getTempDirPath());
//                    workflowProcessReferenceDoc.setBitstream(bitstream1);
                 //   System.out.println("::::::::::::::::::::create Note :::pdf::::::::::done::::::");
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
                System.out.println("PKCS12 ");
                WorkFlowProcessMasterValue drafttypep12 = getMastervalueData(context, "Draft Type", "PKCS12");
                if (drafttypep12 != null) {
                    WorkflowProcessReferenceDoc p12doc = workflowProcessReferenceDocService.getDocumentBySignitoreAndDraftType(context, context.getCurrentUser().getID(), drafttypep12.getID());
                    if (p12doc != null) {
                        System.out.println("already Existing.");
                        WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(p12doc, utils.obtainProjection());
                        return rest;
                    }
                }
                workflowProcessReferenceDoc.setDocumentsignator(context.getCurrentUser());
                workflowProcessReferenceDoc = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
                //workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc);
                WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                context.commit();
                return rest;
            } else {
                if (workflowProcessReferenceDoc.getDrafttype() != null && workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue() != null) {
                    System.out.println("IN Other document " + workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue());
                }
                System.out.println("IN Other document Other");
                workflowProcessReferenceDoc = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
              //  workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc);
                context.commit();
            }
        } catch (InvalidDataException e) {
            res.sendError(406, "Mismatched Certificate and Key");
            return null;
        } catch (AlreadyDataExistException e) {
            res.sendError(202, "Already Certificate and Key Exist!");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
    }


    @RequestMapping(method = RequestMethod.GET, value = "/corspondenseAdd")
    public WorkflowProcessReferenceDocRest docAdd(HttpServletRequest request,
                                                  @Parameter(value = "docid", required = true) String docid,
                                                  @Parameter(value = "item", required = true) String item) {
        return store(request,docid,item);

    }

    public synchronized WorkflowProcessReferenceDocRest  store(HttpServletRequest request,String docid,String item){
        WorkflowProcessReferenceDocRest rest = null;
        System.out.println("in doc add corspondenseAdd");
        try {
            Context context = ContextUtil.obtainContext(request);
            WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.find(context, UUID.fromString(docid));
            if (workflowProcessReferenceDoc != null) {
                rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                Item item1 = itemService.find(context, UUID.fromString(item));
                if (item1 != null) {
                    workflowProcessService.storeWorkFlowMataDataTOBitsream(context, workflowProcessReferenceDoc, item1);
                    context.commit();
                }
            }
            System.out.println("in doc add corspondenseAdd done ");
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (AuthorizeException e) {
            throw new RuntimeException(e);
        }
        return rest;
    }


    public WorkFlowProcessMasterValue getMastervalueData(Context context, String mastername, String mastervaluename) throws
            SQLException {
        WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, mastername);
        if (workFlowProcessMaster != null) {
            WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, mastervaluename, workFlowProcessMaster);
            if (workFlowProcessMasterValue != null) {
                return workFlowProcessMasterValue;
            }
        }
        return null;
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
    //@PreAuthorize("hasPermission(#uuid, 'BUNDLE', 'ADD') && hasPermission(#uuid, 'BUNDLE', 'WRITE')")
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public List<WorkflowProcessReferenceDocRest> uploadBitstreamoutward(HttpServletRequest request,
                                                                        @RequestBody List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRestListstr,
                                                                        @Nullable Pageable optionalPageable) throws SQLException, AuthorizeException, IOException {
        List<WorkflowProcessReferenceDocRest> rsponce = new ArrayList<>();
        Context context = ContextUtil.obtainContext(request);
        context.turnOffAuthorisationSystem();
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
        WorkFlowProcessHistory workFlowAction = null;
        WorkflowProcess workflowProcess = doc.getWorkflowProcess();
        workFlowAction = new WorkFlowProcessHistory();
        WorkFlowProcessMaster workFlowProcessMaster = WorkFlowAction.MASTER.getMaster(context);
        workFlowAction.setWorkflowProcessEpeople(workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get());
        WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.UPDATE.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setAction(workFlowProcessMasterValue);
        workFlowAction.setWorkflowProcess(workflowProcess);
        Optional<WorkflowProcessEperson> sento = workflowProcess.getWorkflowProcessEpeople().stream().filter(we -> we.getOwner()).findFirst();
        if (sento != null) {
            workFlowAction.setSenttoname(sento.get().getePerson().getFullName());
        }
        if (sento != null) {
            workFlowAction.setSentbyname(sento.get().getePerson().getFullName());
        }
        //workflowProcess.getWorkflowProcessNote().getSubject();
        workFlowAction.setComment("Update Version " + versionnumber + " for " + doc.getDrafttype().getPrimaryvalue() + ".");
        workFlowProcessHistoryService.create(context, workFlowAction);
    }

    //this hisory call when Attaged Reference Doc in view Draft note.
    public void storeWorkFlowHistoryforDocument(Context context, WorkflowProcessReferenceDoc doc) throws Exception {
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
        if (doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reply Note")) {
            workFlowAction.setComment("Create Version  " + doc.getWorkflowProcessReferenceDocVersion().size() + " for  Reply Note.");
        }
        WorkflowProcessEperson current = workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get();
        if (current != null) {
            workFlowAction.setWorkflowProcessEpeople(current);
            workFlowAction.setSentto(current);
            workFlowAction.setSenttoname(current.getePerson().getFullName());
            workFlowAction.setSentbyname(current.getePerson().getFullName());
        }
        workFlowProcessHistoryService.create(context, workFlowAction);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getSignatureDocuments")
    public List<WorkflowProcessReferenceDocRest> getSignatureDocuments(HttpServletRequest request) {

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
        return workflowProcessReferenceDocRests;
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = RequestMethod.GET, value = "/getNoteAndApprovedReply")
    public List<ApprovedReplyDTO> getNoteAndApprovedReply(HttpServletRequest request,
                                                          @Parameter(value = "itemid", required = true) String itemid) {
        List<ApprovedReplyDTO> approvedReplyDTOS = new ArrayList<>();
        Context context = ContextUtil.obtainContext(request);
        context.turnOffAuthorisationSystem();
        List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRests = null;
        UUID ReplyNoteID = null;
        UUID NoteUUID = null;
        ApprovedReplyDTO approvedReplyDTO = null;
        try {
            WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Draft Type");
            if (workFlowProcessMaster != null) {
                WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, "Reply Note", workFlowProcessMaster);
                if (workFlowProcessMasterValue != null) {
                    ReplyNoteID = workFlowProcessMasterValue.getID();
                }
                WorkFlowProcessMasterValue notedraft = workFlowProcessMasterValueService.findByName(context, "Note", workFlowProcessMaster);
                if (notedraft != null) {
                    NoteUUID = notedraft.getID();
                }
            }
            List<WorkflowProcessReferenceDoc> witems = workflowProcessReferenceDocService.getDocumentByItemid(context, NoteUUID, UUID.fromString(itemid));

            if (witems != null && witems.size() != 0) {
                for (WorkflowProcessReferenceDoc note : witems) {
                    WorkflowProcessReferenceDoc approveddoc = workflowProcessReferenceDocService.findbydrafttypeandworkflowprocessAndItem(context, UUID.fromString(itemid), note.getWorkflowProcess().getID(), ReplyNoteID);
                    if (approveddoc != null) {
                        if (note.getBitstream() != null && note.getBitstream().getID() != null && approveddoc.getBitstream() != null && approveddoc.getBitstream().getID() != null) {
                            approvedReplyDTO = new ApprovedReplyDTO();
                            approvedReplyDTO.setNotebitstreamid(note.getBitstream().getID().toString());
                            approvedReplyDTO.setApprovedlatterbitstreamid(approveddoc.getBitstream().getID().toString());
                            if (note.getSubject() != null) {
                                approvedReplyDTO.setSubject(note.getSubject());
                            }
                            approvedReplyDTOS.add(approvedReplyDTO);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("error" + e.getMessage());
        }

        return approvedReplyDTOS;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getDocByDraftType")
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public List<WorkflowProcessReferenceDocRest> getDocByDraftType(@Parameter(value = "drafttypeid", required = true) String drafttypeid, HttpServletRequest request) {

        Context context = ContextUtil.obtainContext(request);
        context.turnOffAuthorisationSystem();
        List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRests = null;
        try {
            List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs = workflowProcessReferenceDocService.getDocumentBySignitore(context, context.getCurrentUser().getID(), UUID.fromString(drafttypeid));
            workflowProcessReferenceDocRests = workflowProcessReferenceDocs.stream().map(workflowProcessReferenceDoc -> {
                return workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
            }).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workflowProcessReferenceDocRests;
    }

    public static boolean isNullOrEmptyOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public File getTempDirPath() {
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        Random random = new Random();
        // Generate a random 4-digit number
        int randomNumber = random.nextInt(9000) + 1000;
        File tempFile1html = new File(TEMP_DIRECTORY, "sing_" + randomNumber + ".pdf");
        if (!tempFile1html.exists()) {
            try {
                tempFile1html.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return tempFile1html;
    }

    public Bitstream getByInputStream(Context context, InputStream inputStream, File tempFile1html) {
        try {
            Bitstream bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, inputStream, "", tempFile1html.getName());
            return bitstream;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public InputStream createFinalNoteComment(Context context, WorkflowProcessReferenceDoc doc, File tempFile1html) throws
            Exception {
        boolean isTextEditorFlow = false;
        System.out.println("start.......createFinalNote");
        StringBuffer sb = new StringBuffer("<!DOCTYPE html>\n" + "<html>\n" + "<head><style>@page{size:A4;margin: 0;}</style>\n" + "<title>Note</title>\n" + "</head>\n" + "<body style=\"padding-right: 20px;padding-left: 20px;background-color:#c5e6c1;\">");
        System.out.println("start.......createFinalNote" + tempFile1html.getAbsolutePath());
        //Items
        if (doc.getSubject() != null) {
            sb.append("<p> <b>Subject : " + doc.getSubject() + "</b></p>");
        }
        //need to create filed sap
//        if(doc.getWorkFlowProcessDraftDetails()!=null) {
//            if(workflowProcess.getWorkFlowProcessDraftDetails().getIssapdoc() && workflowProcess.getWorkFlowProcessDraftDetails().getSapdocumentno()!=null && workflowProcess.getWorkFlowProcessDraftDetails().getSapdocumenttype()!=null&& workflowProcess.getWorkFlowProcessDraftDetails().getSapdocumenttype().getPrimaryvalue()!=null) {
//                sb.append("<p> <b>SAP Document Type :  " + workflowProcess.getWorkFlowProcessDraftDetails().getSapdocumenttype().getPrimaryvalue() + "</b></p>");
//                sb.append("<p> <b>SAP Document Number : " + workflowProcess.getWorkFlowProcessDraftDetails().getSapdocumentno() + "</b></p>");
//            }
//        }
        isTextEditorFlow = true;
        sb.append("<div style=\"width:100% ;text-align: left; float:left;\">");
        //coment count
        sb.append("<p><u>Note# " + 1 + "</u></p>");
        //comment text
        if (doc.getEditortext() != null) {
            sb.append("<p>" + doc.getEditortext() + "</p>");
        }
        sb.append("<br><div style=\"width:100%;\"> ");
        sb.append("<div style=\"width:70%;  float:left;\"> <p><b>Attachment :</b></p> ");
//            if (comment.getWorkflowProcessReferenceDoc() != null && comment.getWorkflowProcessReferenceDoc().size() != 0) {
//                for (WorkflowProcessReferenceDoc workflowProcessReferenceDoc : comment.getWorkflowProcessReferenceDoc()) {
//                    if (workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue() != null && !workflowProcessReferenceDoc.getDrafttype().getPrimaryvalue().equals("PKCS12")) {
//                        if (workflowProcessReferenceDoc.getBitstream() != null) {
//                            String baseurl = configurationService.getProperty("dspace.server.url");
//                            sb.append("<span> <a href=" + baseurl + "/api/core/bitstreams/" + workflowProcessReferenceDoc.getBitstream().getID() + "/content>");
//                           // stroremetadate(workflowProcessReferenceDoc.getBitstream(), sb);
//                        }
//                    }
//                }
//            }


        sb.append("</div>");
        sb.append("<div style=\"    float: right;  width:30%\"><p> <B>Signature :</B> </p><B><span>");
        sb.append("</div></div>");

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


}
