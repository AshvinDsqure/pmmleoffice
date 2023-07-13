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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.*;
import org.dspace.app.rest.enums.WorkFlowAction;
import org.dspace.app.rest.enums.WorkFlowType;
import org.dspace.app.rest.enums.WorkFlowUserType;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.model.hateoas.BitstreamResource;
import org.dspace.app.rest.repository.AbstractDSpaceRestRepository;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.PdfUtils;
import org.dspace.app.rest.utils.Utils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ControllerUtils;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.dspace.app.rest.utils.RegexUtils.REGEX_REQUESTMAPPING_IDENTIFIER_AS_UUID;

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
    private ItemConverter itemConverter;
    @Autowired
    WorkflowProcessNoteConverter workflowProcessNoteConverter;
    @Autowired
    WorkFlowProcessCommentService workFlowProcessCommentService;
    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterValueService;


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
                if (file != null && file.getInputStream() != null) {
                    fileInputStream = file.getInputStream();
                    bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, fileInputStream, "", file.getOriginalFilename());
                    System.out.println("bitstream::" + bitstream.getName());
                    workflowProcessReferenceDoc.setBitstream(bitstream);
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
            if (workflowProcessReferenceDocRest.getItemname() != null) {
                System.out.println("in item rest :");
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
                        workflowProcessReferenceDocVersionService.update(context, version);
                        storeWorkFlowHistoryVersionUpdate(context, workflowProcessReferenceDoc.getWorkflowProcess());
                        WorkflowProcessReferenceDocRest rest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
                        context.commit();
                        return rest;
                    }
                }
                if (workflowProcessReferenceDocRest.getUuid() != null) {
                    System.out.println("value version:" + workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest());
                    if (workflowProcessReferenceDocRest.getWorkflowProcessReferenceDocVersionRest() == null) {
                        workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, workflowProcessReferenceDocRest);
                        storeVersion(context, workflowProcessReferenceDoc, bitstream, workflowProcessReferenceDocRest);
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

    public void storeVersion(Context context, WorkflowProcessReferenceDoc workflowProcessReferenceDoc, Bitstream bitstream, WorkflowProcessReferenceDocRest rest) throws SQLException, AuthorizeException, UnsupportedEncodingException {
        WorkflowProcessReferenceDocVersion version = new WorkflowProcessReferenceDocVersion();
        version.setCreator(context.getCurrentUser());
        version.setIsactive(true);
        version.setCreationdatetime(new Date());
        if (bitstream != null) {
            version.setBitstream(bitstream);
        }
        if (rest.getEditortext() != null) {
            byte[] bytes = rest.getEditortext().getBytes("UTF-8");
            String string = new String(bytes, "UTF-8");
            System.out.println("getEditortext:::::::::" + string);
            version.setEditortext(string);
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
                if(wrd.getWorkFlowProcessRest()!=null && wrd.getDrafttypeRest()!=null){
                    storeWorkFlowHistoryforDocument(context,finalworkflowProcessReferenceDoc);
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

    public void storeWorkFlowHistoryVersionUpdate(Context context, WorkflowProcess workflowProcess) throws Exception {
        System.out.println("::::::IN :storeWorkFlowHistory::Update::version:::::: ");
        WorkFlowProcessHistory workFlowAction = null;
        workFlowAction = new WorkFlowProcessHistory();
        WorkFlowProcessMaster workFlowProcessMaster = WorkFlowAction.MASTER.getMaster(context);
        workFlowAction.setWorkflowProcessEpeople(workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get());
        WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.CREATE.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setAction(workFlowProcessMasterValue);
        workFlowAction.setWorkflowProcess(workflowProcess);
        workFlowAction.setComment("Update Version");
        workFlowProcessHistoryService.create(context, workFlowAction);
        System.out.println("::::::OUT :storeWorkFlowHistory::Update::version:::::: ");
    }
    public void storeWorkFlowHistoryforDocument(Context context, WorkflowProcessReferenceDoc doc) throws Exception {
        System.out.println("::::::IN :storeWorkFlowHistory::Update::Document:::::: ");
        WorkflowProcess workflowProcess=doc.getWorkflowProcess();
        WorkFlowProcessHistory workFlowAction = null;
        workFlowAction = new WorkFlowProcessHistory();
        WorkFlowProcessMaster workFlowProcessMaster = WorkFlowAction.MASTER.getMaster(context);
        workFlowAction.setWorkflowProcessEpeople(workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get());
        WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.CREATE.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setAction(workFlowProcessMasterValue);
        workFlowAction.setWorkflowProcess(workflowProcess);
        workFlowAction.setComment("Attached "+doc.getDrafttype().getPrimaryvalue());
        workFlowProcessHistoryService.create(context, workFlowAction);
        System.out.println("::::::OUT :storeWorkFlowHistory::Document:::::::: ");
    }

    @RequestMapping(method = RequestMethod.GET, value = "/test")
    public String test(HttpServletRequest request) {
        Context context = ContextUtil.obtainContext(request);
        try {
            System.out.println("Demo::");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Done";
    }
}
