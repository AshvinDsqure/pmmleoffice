/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.converter.*;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.exception.FieldBlankOrNullException;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.jbpm.JbpmServerImpl;
import org.dspace.app.rest.model.WorkFlowProcessCommentRest;
import org.dspace.app.rest.model.WorkFlowProcessHistoryRest;
import org.dspace.app.rest.model.WorkFlowProcessMasterValueRest;
import org.dspace.app.rest.model.WorkFlowProcessRest;
import org.dspace.app.rest.utils.DateUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component(WorkFlowProcessCommentRest.CATEGORY + "." + WorkFlowProcessCommentRest.NAME)

public class WorkFlowProcessCommentRepository extends DSpaceObjectRestRepository<WorkFlowProcessComment, WorkFlowProcessCommentRest> {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkFlowProcessCommentRepository.class);
    @Autowired
    WorkFlowProcessCommentService workFlowProcessCommentService;
    @Autowired
    WorkFlowProcessCommentConverter workFlowProcessCommentConverter;

    @Autowired
    WorkflowProcessNoteService workflowProcessNoteService;

    @Autowired
    WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;


    @Autowired
    WorkflowProcessService workflowProcessService;
    @Autowired
    WorkFlowProcessConverter workFlowProcessConverter;
    @Autowired
    WorkFlowProcessDraftDetailsConverter workFlowProcessDraftDetailsConverter;
    @Autowired
    WorkFlowProcessDraftDetailsService workFlowProcessDraftDetailsService;
    @Autowired
    WorkflowProcessSenderDiaryConverter workflowProcessSenderDiaryConverter;

    @Autowired
    ItemConverter itemConverter;

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
    BundleRestRepository bundleRestRepository;

    @Autowired
    WorkFlowProcessMasterService workFlowProcessMasterService;
    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterValueService;

    @Autowired
    WorkflowProcessReferenceDocVersionService workflowProcessReferenceDocVersionService;

    @Autowired

    ConfigurationService configurationService;

    public WorkFlowProcessCommentRepository(WorkFlowProcessCommentService dsoService) {
        super(dsoService);
    }

    @Override
    protected WorkFlowProcessCommentRest createAndReturn(Context context)
            throws AuthorizeException {
        log.info("::::::start::::createAndReturn::::::::::");
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        WorkFlowProcessCommentRest workFlowProcessCommentRest = null;
        WorkFlowProcessComment workFlowProcessComment = null;
        try {
            workFlowProcessCommentRest = mapper.readValue(req.getInputStream(), WorkFlowProcessCommentRest.class);
            // Validate comment field
            if (workFlowProcessCommentRest.getComment() == null || workFlowProcessCommentRest.getComment().trim().isEmpty()) {
                throw new FieldBlankOrNullException("Please Enter note.");
            }
            workFlowProcessComment = createWorkFlowProcessCommentFromRestObject(context, workFlowProcessCommentRest);
            workFlowProcessCommentRest = workFlowProcessCommentConverter.convert(workFlowProcessComment, utils.obtainProjection());
            context.commit();
        } catch (Exception e1) {
            log.info("::::::error::::createAndReturn::::::::::");
            e1.printStackTrace();
            throw new UnprocessableEntityException("Error Create Comment Note " + e1.getMessage());
        }
        log.info("::::::complate::::createAndReturn::::::::::");
        return workFlowProcessCommentRest;
    }

    private WorkFlowProcessComment createWorkFlowProcessCommentFromRestObject(Context context, WorkFlowProcessCommentRest workFlowProcessCommentRest) throws AuthorizeException, FieldBlankOrNullException {
        log.info("::::::start::::createWorkFlowProcessCommentFromRestObject::::::::::");
        WorkFlowProcessComment workFlowProcessComment = null;
        WorkFlowProcessComment workFlowProcessComment1 = null;
        WorkflowProcessReferenceDoc workflowProcessReferenceDoc1 = null;
        final String TEMP_DIRECTORY = getFolderTmp("NOTES");
        Integer notecount = 0;
        try {
            if (workFlowProcessCommentRest.getId() != null) {
                System.out.println("update::");
                workFlowProcessComment = workFlowProcessCommentConverter.convertByService(context, workFlowProcessCommentRest);
                if (workFlowProcessComment.getNoteindex() != null) {
                    notecount = workFlowProcessComment.getNoteindex();
                    if (notecount == 0) {
                        notecount = 1;
                    }
                } else {
                    if (workFlowProcessCommentRest.getItemRest() != null && workFlowProcessCommentRest.getItemRest().getId() != null) {
                        WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Department Counts");
                        if (workFlowProcessMaster != null) {
                            WorkFlowProcessMasterValue itemnotecountvalue = workFlowProcessMasterValueService.findByName(context, workFlowProcessCommentRest.getItemRest().getId(), workFlowProcessMaster);
                            if (itemnotecountvalue != null) {
                                Integer t = Integer.valueOf(itemnotecountvalue.getSecondaryvalue());
                                notecount = t + 1;
                                itemnotecountvalue.setSecondaryvalue(String.valueOf(notecount));
                                workFlowProcessMasterValueService.update(context, itemnotecountvalue);
                                System.out.println("new number update------.......");
                            } else {
                                System.out.println("new number create update........" + workFlowProcessCommentRest.getItemRest().getId());
                                WorkFlowProcessMasterValue workFlowProcessMasterValue = new WorkFlowProcessMasterValue();
                                workFlowProcessMasterValue.setWorkflowprocessmaster(workFlowProcessMaster);
                                workFlowProcessMasterValue.setSecondaryvalue("1");
                                workFlowProcessMasterValue.setPrimaryvalue(workFlowProcessCommentRest.getItemRest().getId());
                                workFlowProcessMasterValueService.create(context, workFlowProcessMasterValue);
                                notecount = 1;
                            }

                        } else {
                            System.out.println("master not avalable");
                        }
                    } else {
                        System.out.println("item not found!");
                    }
                }
                Optional<WorkflowProcessReferenceDoc> workflowProcessReferenceDoc = workFlowProcessComment.getWorkflowProcessReferenceDoc().stream().filter(d -> d.getDrafttype() != null)
                        .filter(d -> d.getDrafttype().getPrimaryvalue() != null)
                        .filter(d -> d.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Note"))
                        .findFirst();
                if (workflowProcessReferenceDoc.isPresent()) {
                    System.out.println("IN UPDATE COMMENT DOC!:::>>>>>>>>>>>>>>:old present");
                    workflowProcessReferenceDoc1 = workflowProcessReferenceDocService.find(context, workflowProcessReferenceDoc.get().getID());
                }
                workFlowProcessComment = workFlowProcessCommentConverter.convertupdate(context, workFlowProcessComment, workFlowProcessCommentRest);
                workFlowProcessComment1 = workFlowProcessComment;


            } else {
                //System.out.println("create:::");
                UUID epersonToEpersonMappingid = null;
                Optional<EpersonToEpersonMapping> map = context.getCurrentUser().getEpersonToEpersonMappings().stream().filter(d -> d.getIsactive() == true).findFirst();
                if (map.isPresent()) {
                    workFlowProcessCommentRest.setDesignation(map.get().getEpersonmapping().getDesignation().getPrimaryvalue());
                }
                workFlowProcessComment = workFlowProcessCommentConverter.convert(context, workFlowProcessCommentRest);
                workFlowProcessComment.setIsdraftsave(true);
                workFlowProcessComment1 = workFlowProcessCommentService.create(context, workFlowProcessComment);


                if (workFlowProcessCommentRest.getItemRest() != null && workFlowProcessCommentRest.getItemRest().getId() != null) {
                    WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Department Counts");
                    if (workFlowProcessMaster != null) {
                        WorkFlowProcessMasterValue itemnotecountvalue = workFlowProcessMasterValueService.findByName(context, workFlowProcessCommentRest.getItemRest().getId(), workFlowProcessMaster);
                        if (itemnotecountvalue != null) {
                            Integer t = Integer.valueOf(itemnotecountvalue.getSecondaryvalue());
                            notecount = t + 1;
                            itemnotecountvalue.setSecondaryvalue(String.valueOf(notecount));
                            workFlowProcessMasterValueService.update(context, itemnotecountvalue);
                            System.out.println("new number update");
                        } else {
                            System.out.println("new number create " + workFlowProcessCommentRest.getItemRest().getId());
                            WorkFlowProcessMasterValue workFlowProcessMasterValue = new WorkFlowProcessMasterValue();
                            workFlowProcessMasterValue.setWorkflowprocessmaster(workFlowProcessMaster);
                            workFlowProcessMasterValue.setSecondaryvalue("1");
                            workFlowProcessMasterValue.setPrimaryvalue(workFlowProcessCommentRest.getItemRest().getId());
                            workFlowProcessMasterValueService.create(context, workFlowProcessMasterValue);
                            notecount = 1;
                        }

                    } else {
                        System.out.println("master not avalable");
                    }
                } else {
                    System.out.println("item not found!");
                }
            }

            if (notecount == 0) {
                notecount = 1;
            }

            System.out.println("count::::::::::::::" + notecount);
            if (workFlowProcessCommentRest.getWorkflowProcessRest() != null && workFlowProcessCommentRest.getWorkflowProcessRest().getId() != null) {
                WorkflowProcess workflowProcess = workflowProcessService.find(context, UUID.fromString(workFlowProcessCommentRest.getWorkflowProcessRest().getId()));
                workFlowProcessComment1.setWorkFlowProcess(workflowProcess);
            }
            if (workFlowProcessCommentRest.getWorkflowProcessReferenceDocRest() != null) {
                WorkFlowProcessComment finalWorkFlowProcessComment = workFlowProcessComment1;
                List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs = workFlowProcessCommentRest.getWorkflowProcessReferenceDocRest().stream().filter(d -> d.getUuid() != null).filter(d -> d != null).map(d -> {
                    try {
                        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, d);
                        workflowProcessReferenceDoc.setWorkflowprocesscomment(finalWorkFlowProcessComment);
                        return workflowProcessReferenceDoc;
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
                List<Bitstream> bitstreams = workflowProcessReferenceDocs.stream().filter(d -> d.getDrafttype() != null && !d.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reference Document")).filter(d -> d.getBitstream() != null)
                        .map(d -> d.getBitstream()).collect(Collectors.toList());


                File tempFile1html = new File(TEMP_DIRECTORY, "Note#" + notecount + ".pdf");
                if (!tempFile1html.exists()) {
                    try {
                        tempFile1html.createNewFile();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                workFlowProcessComment1 = finalWorkFlowProcessComment;
                WorkflowProcessReferenceDoc notedoc = createFinalNoteComment(context, workFlowProcessComment1, tempFile1html, bitstreams, workFlowProcessCommentRest, workflowProcessReferenceDoc1, notecount);
                if (notedoc == null) {
                    throw new UnprocessableEntityException("Note document creation failed.");
                }
                workFlowProcessCommentRest.setMargeddocuuid(notedoc.getID().toString());
                workFlowProcessComment1.setMargeddocuuid(notedoc.getID().toString());
                notedoc.setWorkflowprocesscomment(workFlowProcessComment1);

                if (notedoc.getBitstream() != null) {
                    Bitstream b = bitstreamService.find(context, notedoc.getBitstream().getID());
                    b.setNoteindex(notecount);
                    bitstreamService.update(context, b);
                    System.out.println("Bitstrem update index:>>>>>>>>>>>>>>>:");
                }
                workflowProcessReferenceDocs.add(notedoc);
                if (workflowProcessReferenceDocs != null && workflowProcessReferenceDocs.size() != 0) {
                    workFlowProcessComment1.setWorkflowProcessReferenceDoc(workflowProcessReferenceDocs);
                }
                workFlowProcessComment1.setNoteindex(notecount);
                workFlowProcessComment.setNoteindex(notecount);
                workFlowProcessCommentService.update(context, workFlowProcessComment1);
            }
        } catch (UnprocessableEntityException e) {
            log.info("::::::error::::createWorkFlowProcessCommentFromRestObject::::::::::");
            e.printStackTrace();
            workFlowProcessComment = null;
            throw new UnprocessableEntityException(e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("::::::error::::createWorkFlowProcessCommentFromRestObject::::::::::");
            workFlowProcessComment = null;
            throw new RuntimeException(e.getMessage(), e);
        }

        log.info("::::::complate::::createWorkFlowProcessCommentFromRestObject::::::::::");
        return workFlowProcessComment;
    }
//    private WorkFlowProcessComment createWorkFlowProcessCommentFromRestObject(Context context, WorkFlowProcessCommentRest workFlowProcessCommentRest) throws AuthorizeException {
//        log.info("::::::start::::createWorkFlowProcessCommentFromRestObject::::::::::");
//        WorkFlowProcessComment workFlowProcessComment = null;
//        WorkFlowProcessComment workFlowProcessComment1 = null;
//        WorkflowProcessReferenceDoc workflowProcessReferenceDoc1 = null;
//        try {
//            if (workFlowProcessCommentRest.getId() != null) {
//                System.out.println("update::");
//                workFlowProcessComment = workFlowProcessCommentConverter.convertByService(context, workFlowProcessCommentRest);
//
//                Optional<WorkflowProcessReferenceDoc> workflowProcessReferenceDoc = workFlowProcessComment.getWorkflowProcessReferenceDoc().stream().filter(d -> d.getDrafttype() != null)
//                        .filter(d -> d.getDrafttype().getPrimaryvalue() != null)
//                        .filter(d -> d.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Note"))
//                        .findFirst();
//                if (workflowProcessReferenceDoc.isPresent()) {
//                    System.out.println("IN UPDATE COMMENT DOC!:::>>>>>>>>>>>>>>:old present");
//                    workflowProcessReferenceDoc1 = workflowProcessReferenceDocService.find(context, workflowProcessReferenceDoc.get().getID());
//                }
//                workFlowProcessComment = workFlowProcessCommentConverter.convertupdate(context, workFlowProcessComment, workFlowProcessCommentRest);
//                workFlowProcessComment1 = workFlowProcessComment;
//            } else {
//                System.out.println("create:::");
//                workFlowProcessComment = workFlowProcessCommentConverter.convert(context, workFlowProcessCommentRest);
//                workFlowProcessComment.setIsdraftsave(true);
//                workFlowProcessComment1 = workFlowProcessCommentService.create(context, workFlowProcessComment);
//            }
//            if (workFlowProcessCommentRest.getWorkflowProcessRest() != null && workFlowProcessCommentRest.getWorkflowProcessRest().getId() != null) {
//                WorkflowProcess workflowProcess = workflowProcessService.find(context, UUID.fromString(workFlowProcessCommentRest.getWorkflowProcessRest().getId()));
//                workFlowProcessComment1.setWorkFlowProcess(workflowProcess);
//            }
//            if (workFlowProcessCommentRest.getWorkflowProcessReferenceDocRest() != null) {
//                WorkFlowProcessComment finalWorkFlowProcessComment = workFlowProcessComment1;
//                List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs = workFlowProcessCommentRest.getWorkflowProcessReferenceDocRest().stream().filter(d -> d.getUuid() != null).filter(d -> d != null).map(d -> {
//                    try {
//                        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, d);
//                        workflowProcessReferenceDoc.setWorkflowprocesscomment(finalWorkFlowProcessComment);
//                        return workflowProcessReferenceDoc;
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                        throw new RuntimeException(e);
//                    }
//                }).collect(Collectors.toList());
//
//                List<Bitstream> bitstreams = workflowProcessReferenceDocs.stream().filter(d -> d.getDrafttype() != null && !d.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reference Document")).filter(d -> d.getBitstream() != null)
//                        .map(d -> d.getBitstream()).collect(Collectors.toList());
//
//                final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
//                long notecount = 0;
//                if (workFlowProcessCommentRest.getItemRest() != null) {
//                    UUID statusid = WorkFlowStatus.COMPLETE.getUserTypeFromMasterValue(context).get().getID();
//                    notecount = workflowProcessNoteService.getNoteCountNumber(context, UUID.fromString(workFlowProcessCommentRest.getItemRest().getUuid()), statusid);
//                }
//                notecount = notecount + 1;
//                File tempFile1html = new File(TEMP_DIRECTORY, "Note#" + notecount + ".pdf");
//                if (!tempFile1html.exists()) {
//                    try {
//                        tempFile1html.createNewFile();
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//                workFlowProcessComment1 = finalWorkFlowProcessComment;
//
//                WorkflowProcessReferenceDoc notedoc = createFinalNoteComment(context, workFlowProcessComment1, tempFile1html, bitstreams, workFlowProcessCommentRest, workflowProcessReferenceDoc1);
//                if(notedoc==null){
//                    throw new UnprocessableEntityException("Note document creation failed.");
//                }
//                if(notedoc==null){
//                    return null;
//                }
//                workFlowProcessCommentRest.setMargeddocuuid(notedoc.getID().toString());
//                workFlowProcessComment1.setMargeddocuuid(notedoc.getID().toString());
//                notedoc.setWorkflowprocesscomment(workFlowProcessComment1);
//                workflowProcessReferenceDocs.add(notedoc);
//                if (workflowProcessReferenceDocs != null && workflowProcessReferenceDocs.size() != 0) {
//                    System.out.println("doc added to note:::");
//                    workFlowProcessComment1.setWorkflowProcessReferenceDoc(workflowProcessReferenceDocs);
//                }
//                workFlowProcessCommentService.update(context, workFlowProcessComment1);
//            }
//        }catch (UnprocessableEntityException e){
//            throw new UnprocessableEntityException(e.getMessage(), e);
//
//        }catch (Exception e) {
//            e.printStackTrace();
//            log.info("::::::error::::createWorkFlowProcessCommentFromRestObject::::::::::");
//            throw new RuntimeException(e.getMessage(), e);
//
//        }
//        log.info("::::::complate::::createWorkFlowProcessCommentFromRestObject::::::::::");
//        return workFlowProcessComment;
//    }

    @Override
    protected WorkFlowProcessCommentRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                             JsonNode jsonNode) throws Exception {
        log.info("::::::start::::put::::::::::");
        WorkFlowProcessCommentRest workFlowProcessCommentRest = new Gson().fromJson(jsonNode.toString(), WorkFlowProcessCommentRest.class);

        WorkFlowProcessComment workFlowProcessComment = workFlowProcessCommentService.find(context, id);
        if (workFlowProcessComment == null) {
            System.out.println("workFlowProcessComment id ::: is Null  workFlowProcessComment tye null" + id);
            throw new ResourceNotFoundException("workFlowProcessComment  field with id: " + id + " not found");
        }
        workFlowProcessComment = workFlowProcessCommentConverter.convert(context, workFlowProcessCommentRest);
        workFlowProcessCommentService.update(context, workFlowProcessComment);
        context.commit();
        log.info("::::::End::::put::::::::::");
        return converter.toRest(workFlowProcessComment, utils.obtainProjection());
    }

    public static String getFolderTmp(String folderName) {
        final String tempDirectory = System.getProperty("java.io.tmpdir");
        String timestamp = java.time.format.DateTimeFormatter.ofPattern("ddMMMyyyyHHmmss")
                .format(java.time.LocalDateTime.now().plusMinutes(3));
        String uniqueId = java.util.UUID.randomUUID().toString(); // ✅ IMPORTANT
        File directory = new File(tempDirectory + File.separator + folderName + "_" + timestamp + "_" + uniqueId);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Failed to create temporary directory: " + directory.getAbsolutePath());
        }
        return directory.getAbsolutePath();
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @Override
    public WorkFlowProcessCommentRest findOne(Context context, UUID uuid) {
        context.turnOffAuthorisationSystem();
        WorkFlowProcessCommentRest workFlowProcessCommentRest = null;
        log.info("::::::start::::findOne::::::::::");
        try {
            Optional<WorkFlowProcessComment> workFlowProcessComment = Optional.ofNullable(workFlowProcessCommentService.find(context, uuid));
            if (workFlowProcessComment.isPresent()) {
                workFlowProcessCommentRest = converter.toRest(workFlowProcessComment.get(), utils.obtainProjection());
            }
        } catch (Exception e) {
            log.info("::::::error::::findOne::::::::::");
            e.printStackTrace();
        }
        log.info("::::::End::::findOne::::::::::");
        return workFlowProcessCommentRest;
    }

    @Override
    public Page<WorkFlowProcessCommentRest> findAll(Context context, Pageable pageable) throws SQLException {
        int total = workFlowProcessCommentService.countRows(context);
        List<WorkFlowProcessComment> workFlowProcessComment = workFlowProcessCommentService.findAll(context,
                Math.toIntExact(pageable.getPageSize()), Math.toIntExact(pageable.getOffset()));
        return converter.toRestPage(workFlowProcessComment, pageable, total, utils.obtainProjection());
    }

    protected void delete(Context context, UUID id) throws AuthorizeException {
        log.info("::::::in::::delete::::::::::");
        WorkFlowProcessComment workFlowProcessComment = null;
        try {
            workFlowProcessComment = workFlowProcessCommentService.find(context, id);
            if (workFlowProcessComment == null) {
                log.info("::::::id not found::::delete::::::::::");
                throw new ResourceNotFoundException(WorkFlowProcessCommentRest.CATEGORY + "." + WorkFlowProcessCommentRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            workFlowProcessCommentService.delete(context, workFlowProcessComment);
            context.commit();
            log.info(":::::completed:::delete::::::::::");
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "getComments")
    public Page<WorkFlowProcessCommentRest> getComments(@Parameter(value = "workflowprocessid", required = true) UUID workflowprocessid, Pageable pageable) {
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            WorkflowProcess wp = workflowProcessService.find(context, workflowprocessid);
            List<WorkFlowProcessComment> witems = workFlowProcessCommentService.getComments(context, workflowprocessid);
            
            // Apply filtering based on user role
            List<WorkFlowProcessComment> filteredItems = filterCommentsForCurrentUser(context, wp, witems);
            
            // Convert to REST objects
            List<WorkFlowProcessCommentRest> workflowsRes = filteredItems.stream()
                .map(comment -> workFlowProcessCommentConverter.convert(comment, utils.obtainProjection()))
                .collect(toList());

            long total = workFlowProcessCommentService.countComment(context, workflowprocessid);
            return new PageImpl(workflowsRes, pageable, total);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Filters workflow comments based on the current user's role and requirements.
     * 
     * Logic:
     * 1. Sort comments by noteIndex in ascending order
     * 2. For initiator: return all comments
     * 3. For sender/owner: return comments up to and including the last occurrence
     *    of the current user as sender (submitter)
     * 
     * @param context the DSpace context
     * @param wp the workflow process
     * @param comments the list of comments to filter
     * @return filtered list of comments
     */
    private List<WorkFlowProcessComment> filterCommentsForCurrentUser(Context context, WorkflowProcess wp, 
            List<WorkFlowProcessComment> comments) {
        
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }
        
        // If current user is initiator, return all comments (no filtering)


        // For sender or owner, apply filtering logic
        if (comments!=null) {
            System.out.println("=============total note============="+comments.size());
            EPerson currentUser = context.getCurrentUser();
            if (currentUser == null) {
                return new ArrayList<>();
            }
            // Sort by noteIndex first
            List<WorkFlowProcessComment> sortedComments = comments.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                    WorkFlowProcessComment::getNoteindex,
                    Comparator.nullsLast(Integer::compareTo)))
                .collect(toList());

            if(iscurrentuserinisiator(context,wp)&&iscurrentuserWoner(context,wp)){
                return sortedComments;
            }else if(iscurrentuserWoner(context,wp)){
                return sortedComments;
            }else  if(wp.getIsconfidential()!=null&&!wp.getIsconfidential()){
                return sortedComments;
            }else if(wp.getWorkflowStatus()!=null&&wp.getWorkflowStatus().getPrimaryvalue()!=null&&wp.getWorkflowStatus().getPrimaryvalue().equalsIgnoreCase("Complete"))
            {
                return sortedComments;
            }
            // Find the last index where current user is the submitter
            int lastUserIndex = -1;
            for (int i = 0; i < sortedComments.size(); i++) {
                WorkFlowProcessComment comment = sortedComments.get(i);
                EPerson submitter = comment.getSubmitter();
                if (submitter != null && submitter.equals(currentUser)) {
                    lastUserIndex = i;
                }
            }
            
            // If current user is found in the comments, return up to that point
            if (lastUserIndex != -1) {
                return sortedComments.subList(0, lastUserIndex + 1);
            }
            
            // If current user not found, return all sorted comments (fallback)
            return sortedComments;
        }
        
        // Default case: return all comments sorted
        return comments.stream()
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(
                WorkFlowProcessComment::getNoteindex,
                Comparator.nullsLast(Integer::compareTo)))
            .collect(toList());
    }

    boolean iscurrentuserinisiator(Context context, WorkflowProcess wp) {
        Optional<EPerson> epp = wp.getWorkflowProcessEpeople().stream().filter(d -> d.getSequence() == 0).map(f -> f.getePerson()).findFirst();

        if (epp.isPresent()) {
            if (epp.get().getID().toString().equalsIgnoreCase(context.getCurrentUser().getID().toString())) {
                System.out.println("--> initiator");
                return true;
            } else {
                System.out.println("-->not initiator");
                return false;
            }
        }
        return false;

    }
    boolean iscurrentuserWoner(Context context, WorkflowProcess wp) {
        Optional<EPerson> epp = wp.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() == true).map(f -> f.getePerson()).findFirst();
        if (epp.isPresent()) {
            if (epp.get().getID().toString().equalsIgnoreCase(context.getCurrentUser().getID().toString())) {
                System.out.println("--> getOwner");
                return true;
            } else {
                System.out.println("-->not getOwner");
                return false;
            }
        }
        return false;

    }

    boolean iscurrentusersender(Context context, WorkflowProcess wp) {
        Optional<EPerson> epp = wp.getWorkflowProcessEpeople().stream().filter(d -> d.getSender() == true).map(f -> f.getePerson()).findFirst();
        if (epp.isPresent()) {
            if (epp.get().getID().toString().equalsIgnoreCase(context.getCurrentUser().getID().toString())) {
                System.out.println("--> sender");
                return true;
            } else {
                System.out.println("-->not sender");
                return false;
            }
        }
        return false;

    }

//    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
//    @SearchRestMethod(name = "getComments")
//    public Page<WorkFlowProcessCommentRest> getComments(@Parameter(value = "workflowprocessid", required = true) UUID workflowprocessid, Pageable pageable) {
//        try {
//            Context context = obtainContext();
//            context.turnOffAuthorisationSystem();
//            List<WorkFlowProcessCommentRest> workflowsRes = new ArrayList<WorkFlowProcessCommentRest>();
//            long total = workFlowProcessCommentService.countComment(context, workflowprocessid);
//
//            List<WorkFlowProcessComment> witems = workFlowProcessCommentService.getComments(context, workflowprocessid);
//            workflowsRes = witems.stream().map(d -> {
//                return workFlowProcessCommentConverter.convert(d, utils.obtainProjection());
//            }).collect(toList());
//
//
//            return new PageImpl(workflowsRes, pageable, total);
//            // return converter.toRestPage(witems, pageable, total, utils.obtainProjection());
//        } catch (SQLException e) {
//            throw new RuntimeException(e.getMessage(), e);
//        }
//    }


    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "updateComment")
    public WorkFlowProcessCommentRest updateComment(@Parameter(value = "commentid", required = true) UUID commentid,
                                                    @Parameter(value = "issign", required = true) Boolean issign) {
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            WorkFlowProcessComment workFlowProcessComment = workFlowProcessCommentService.find(context, commentid);
            WorkFlowProcessComment workFlowProcessComment1 = workFlowProcessComment;
            workFlowProcessComment.setIsdraftsave(issign);
            workFlowProcessCommentService.update(context, workFlowProcessComment);
            workFlowProcessComment1.setIsdraftsave(issign);
            WorkFlowProcessCommentRest rest = workFlowProcessCommentConverter.convert(workFlowProcessComment1, utils.obtainProjection());
            context.commit();
            return rest;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (AuthorizeException e) {
            throw new RuntimeException(e);
        }
    }


    public WorkflowProcessReferenceDoc createFinalNoteComment(Context context, WorkFlowProcessComment comment, File tempFile1html, List<Bitstream> bitstreams, WorkFlowProcessCommentRest workFlowProcessCommentRest, WorkflowProcessReferenceDoc margedoc, Integer notecount) throws

            Exception {

        boolean isTextEditorFlow = false;
        boolean isdocupdate = false;
//        if (workFlowProcessCommentRest.getWorkflowProcessRest() != null && workFlowProcessCommentRest.getWorkflowProcessRest().getUuid() != null) {
//            List<WorkFlowProcessComment> comments = workFlowProcessCommentService.getComments(context, UUID.fromString(workFlowProcessCommentRest.getWorkflowProcessRest().getId()));
//            if (comments != null && comments.size() != 0) {
//                notenumbe = comments.size();
//            }
//        }
//        if (notenumbe == 0) {
//            notenumbe = 1;
//        }
        System.out.println("note number---- " + notecount);
        // System.out.println("start.......createFinalNote");
        StringBuffer sb = new StringBuffer("<!DOCTYPE html>\n" + "<html>\n" + "<head><style>.footer {\n" +
                "                margin-top: 10px;width: 100%;\n" +
                "            text-align: left;\n" +
                "            font-size: 12pt;\n" +
                "            font-weight: bold;\n" +
                "            position: fixed;\n" +
                "            bottom: 0px;\n" +
                "            left: 0;\n" +
                "            right: 0;\n" +
                "            padding: 0px;\n" +
                "            /* background: #c5e6c1; Match body background color */\n" +
                "        }   body {\n" +
                "      font-family: Georgia, serif;\n" +
                "      line-height: 1.8;\n" +
                "     \n" +
                "    }\n" +
                "\n" +
                "    h1, h2 {\n" +
                "      text-align: center;\n" +
                "    }\n" +
                "\n" +
                "    p {\n" +
                "      margin-bottom: 15px;\n" +
                "      font-size: 14pt;\n" +
                "    }\n" +
                "\n" +
                "    body {\n" +
                "      background-color:#c5e6c1;" +
                "font-family: Georgia, serif;\n" +
                "      line-height: 1.8;padding: 20px;min-height: 100%;\n" +
                "      margin: 0;\n" +
                "\t  text-align: justify; word-break: break-word;\n" +
                "    }\n" +
                "\n" +
                "    h1, h2 {\n" +
                "      text-align: center;\n" +
                "    }\n" +
                "\n" +
                "    p {\n" +
                "      margin-bottom: 15px;\n" +
                "      font-size: 14pt;\n" +
                "    }\n" +
                "\n" +
                ".sign {\n" +
                "        text-align: left; margin: 1px; margin-top: -31px;\n" +
                "    }\n" +
                "    .sign i {\n" +
                "        font-size: 24px; /* Adjust size as needed */\n" +
                "        margin-bottom: 10px; /* Space between icon and text */\n" +
                "        color: #000; /* Icon color */\n" +
                "    }\n" +
                "\t.img{height: 75px;\n" +
                "    width: 128px;\n" +
                "    margin-bottom: -75px;" +
                "}" +
                "@page { size: A4; margin: 0; }" +
                "html { background-color: #c5e6c1; }" +
                "  </style>\n" + "<title>Note</title>\n" + "</head>\n" + "<body >");
        // System.out.println("start.......createFinalNote" + tempFile1html.getAbsolutePath());
        //Items

        Item items = null;
        if (workFlowProcessCommentRest.getItemRest() != null) {
            items = itemConverter.convert(workFlowProcessCommentRest.getItemRest(), context);
        }
        if (items != null && items.getName() != null) {
            sb.append("<div style=\"float: right; width:65%\">");
            sb.append("<span><b>FileNo : </b> " + items.getName() + "</span><br>");
            sb.append("<span><b>Date : </b> " + DateUtils.getCurrentDDMMYY() + "</span>");
            sb.append("</div>");
        }

        sb.append("<br/><br/><br/><p><b>Subject : </b>" + workFlowProcessCommentRest.getSubject() + "</p>");
        if (workFlowProcessCommentRest.getSapdocumentno() != null && workFlowProcessCommentRest.getSapdocumenttypeRest() != null) {
            WorkFlowProcessMasterValue saptype = workFlowProcessMasterValueService.find(context, UUID.fromString(workFlowProcessCommentRest.getSapdocumenttypeRest().getUuid()));
            sb.append("<p> <b>SAP Document Type :  " + saptype.getPrimaryvalue() + "</b></p>");
            sb.append("<p> <b>SAP Document Number : " + workFlowProcessCommentRest.getSapdocumentno() + "</b></p>");
        }

        isTextEditorFlow = true;
        sb.append("<div style=\"text-align: justify; word-break: break-word;width:100% ;text-align: left; float:left;\">");
        //coment count
        sb.append("<p><u>Note# " + notecount + "</u></p>");
        //comment text
        if (comment.getComment() != null) {
            sb.append("<div style=\"text-align: justify; word-break: break-word;\">" + comment.getComment() + "</div>");
        }
        sb.append("<br><div style=\"width:100%;\"> ");

        sb.append("<div style=\"width:50%;  float:left;\"> <p><b></b></p> ");
        System.out.println("omment.getWorkflowProcessReferenceDoc().size():::" + comment.getWorkflowProcessReferenceDoc().size());
//        if (bitstreams.size() != 0) {
//            for (Bitstream bitstream : bitstreams) {
//                if (bitstream != null) {
//                    System.out.println("in Attachment");
//                    String baseurl = configurationService.getProperty("dspace.server.url");
//                    sb.append("<span> <a href=" + baseurl + "/api/core/bitstreams/" + bitstream.getID() + "/content>");
//                    sb.append(bitstream.getName() + "</a></span>");
//                    // stroremetadate(bitstream, sb);
//                }
//            }
//        }
        sb.append("</div>");

        sb.append("<div style=\"    float: right;  width:30%;line-height: 1.1;\"><p> <B>Signature_1_Name:</B> </p><B>");
        if (!workFlowProcessCommentRest.getIsdosign()) {
            //this is normal
            String icon = configurationService.getProperty("digital.sign.icon");

            // String base64Image = java.util.Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(icon)));

            String base64Image = "";
            try {
                File file = new File(icon);
                if (file.exists() && file.isFile()) {
                    base64Image = java.util.Base64.getEncoder()
                            .encodeToString(Files.readAllBytes(file.toPath()));
                } else {
                    System.out.println("Signature file not found: " + icon);
                }
            } catch (Exception e) {
                System.out.println("Error loading signature: " + e.getMessage());
            }

            //sb.append("<div class=\"sign\">  <img class=\"img\" src=\"data:image/png;base64," + base64Image + "\">");
            sb.append("<div class=\"sign\">");
            sb.append("<B>");
            if (comment.getIsdosign() && comment.getCommonname() != null) {
                sb.append("<br>Signed By : " + comment.getCommonname());

            } else {
                if (comment.getSubmitter() != null) {
                    if (comment.getSubmitter().getFullName() != null) {
                        sb.append("<br>Signed By : " + comment.getSubmitter().getFullName());
                    }
                }
            }
            if (comment.getDesignation() != null) {
                sb.append("<br>Designation : " + comment.getDesignation());
            }
            if (comment.getActionDate() != null) {
                sb.append("<br>Date : " + DateFormate(comment.getActionDate()));
            }

//            sb.append("<br>Reason :Digital Copy.");
//            sb.append("<br>Location :Location.");
            sb.append("</B>");
            //end  normal sign
            sb.append("</div>");
        }

        sb.append("</div>" +
                "</br>\n" +
                "</br>\n" +
                "</br>\n" +
                "</br>\n" +
                "<p style=\"float:left;\">--------------------- This Note ends here. -------------------------- </p>\n</div>");

        sb.append("<div class=footer>");
        if (items != null) {
            sb.append("[" + items.getName() + "], [" + workFlowProcessCommentRest.getSubject() + "], [Note #" + notecount + "]");
        }
        sb.append("</div>");
        sb.append("  </body></html>");
        if (isTextEditorFlow) {
            System.out.println("::::::::::IN isTextEditorFlow :::::::::");
            FileOutputStream files = new FileOutputStream(new File(tempFile1html.getAbsolutePath()));
            System.out.println("HTML:::" + sb.toString());
            int ii = jbpmServer.htmltopdf(sb.toString(), files);
            if (ii == 0) {
                throw new UnprocessableEntityException("PDF conversion failed. This might be due to unsupported content or a system error. Please check your input and try again.");
            }
            //int result = PdfUtils.HtmlconvertToPdf(sb.toString(), files);
            System.out.println("HTML CONVERT DONE::::::::::::::: :" + tempFile1html.getAbsolutePath());
            InputStream outputfile = new FileInputStream(new File(tempFile1html.getAbsolutePath()));

            Bitstream bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, outputfile, "", tempFile1html.getName());
            if (margedoc != null) {
                isdocupdate = true;
                try {
                    bitstreamService.delete(context, margedoc.getBitstream());
                    System.out.println("bitstreamService>>>>>>>>>>>>:old deleted");

                } catch (Exception e) {
                    System.out.println("bitstreamService>>>>>>>>>>>>:old not deleted" + e.getMessage());
                }
            } else {
                System.out.println("IN NEW DOC!:created ");
                margedoc = new WorkflowProcessReferenceDoc();
            }
            if (bitstream != null) {
                margedoc.setBitstream(bitstream);
            }
            if (workFlowProcessCommentRest.getSubject() != null) {
                margedoc.setSubject(workFlowProcessCommentRest.getSubject());
            }
            margedoc.setInitdate(new Date());
            if (items != null && items.getName() != null) {
                margedoc.setItemname(items.getName());
            }
            WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Draft Type");
            if (workFlowProcessMaster != null) {
                WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, "Note", workFlowProcessMaster);
                if (workFlowProcessMasterValue != null) {
                    margedoc.setDrafttype(workFlowProcessMasterValue);
                }
            }
            int index = 1;
            if (comment.getWorkFlowProcess() != null) {
                margedoc.setWorkflowProcess(comment.getWorkFlowProcess());
                List<WorkflowProcessReferenceDoc> dd = comment.getWorkFlowProcess().getWorkflowProcessReferenceDocs().stream()
                        .filter(d -> d.getDrafttype() != null)
                        .filter(d -> d.getDrafttype() != null && d.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Note")).collect(Collectors.toList());
                if (dd != null && dd.size() != 0) {
                    System.out.println("dd.size()::::::::::" + dd.size());
                    index = dd.size() + 1;
                }
            }


            System.out.println("doc index::::::::::::::::::" + index);

            margedoc.setIndex(index);
            //margedoc.setWorkflowProcess(workflowProcess);
            if (isdocupdate) {
                workflowProcessReferenceDocService.update(context, margedoc);
                margedoc.setBitstream(bitstream);
                return margedoc;
            } else {
                WorkflowProcessReferenceDoc margedoc1 = workflowProcessReferenceDocService.create(context, margedoc);
                margedoc.setBitstream(bitstream);
                //context.commit();
                return margedoc1;
            }

        }
        return null;
    }


    private static String DateFormate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        return formatter.format(date);
    }

    @Override
    public Class<WorkFlowProcessCommentRest> getDomainClass() {
        return null;
    }
}
