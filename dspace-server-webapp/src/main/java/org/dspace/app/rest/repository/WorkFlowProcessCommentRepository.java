/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
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
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.jbpm.JbpmServerImpl;
import org.dspace.app.rest.model.WorkFlowProcessCommentRest;
import org.dspace.app.rest.model.WorkFlowProcessHistoryRest;
import org.dspace.app.rest.model.WorkFlowProcessMasterValueRest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
            workFlowProcessComment = createWorkFlowProcessCommentFromRestObject(context, workFlowProcessCommentRest);
            workFlowProcessCommentRest= converter.toRest(workFlowProcessComment, utils.obtainProjection());
            context.commit();
        } catch (Exception e1) {
            log.info("::::::error::::createAndReturn::::::::::");
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        log.info("::::::complate::::createAndReturn::::::::::");
        return workFlowProcessCommentRest;
    }
    private WorkFlowProcessComment createWorkFlowProcessCommentFromRestObject(Context context, WorkFlowProcessCommentRest workFlowProcessCommentRest) throws AuthorizeException {
        log.info("::::::start::::createWorkFlowProcessCommentFromRestObject::::::::::");
        WorkFlowProcessComment workFlowProcessComment = new WorkFlowProcessComment();
        try {
            workFlowProcessComment=workFlowProcessCommentConverter.convert(context,workFlowProcessCommentRest);
            WorkFlowProcessComment workFlowProcessComment1=  workFlowProcessCommentService.create(context, workFlowProcessComment);

            if(workFlowProcessCommentRest.getWorkflowProcessRest()!=null&&workFlowProcessCommentRest.getWorkflowProcessRest().getId()!=null) {
                WorkflowProcess workflowProcess = workflowProcessService.find(context, UUID.fromString(workFlowProcessCommentRest.getWorkflowProcessRest().getId()));
                workFlowProcessComment1.setWorkFlowProcess(workflowProcess);
            }
            if (workFlowProcessCommentRest.getWorkflowProcessReferenceDocRest() != null) {
                List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs = workFlowProcessCommentRest.getWorkflowProcessReferenceDocRest().stream().filter(d -> d.getUuid() != null).filter(d -> d != null).map(d -> {
                    try {
                        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, d);
                        workflowProcessReferenceDoc.setWorkflowprocesscomment(workFlowProcessComment1);
                        return workflowProcessReferenceDoc;
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
                List<Bitstream>bitstreams=workflowProcessReferenceDocs.stream().filter(d->d.getBitstream()!=null)
                        .map(d->d.getBitstream()).collect(Collectors.toList());

                final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
                long notecount = 0;
                if (workFlowProcessCommentRest.getItemRest() != null) {
                    UUID statusid = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
                    notecount = workflowProcessNoteService.getNoteCountNumber(context, UUID.fromString(workFlowProcessCommentRest.getItemRest().getUuid()), statusid);
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
                WorkflowProcessReferenceDoc  notedoc = createFinalNoteComment(context, workFlowProcessComment1, tempFile1html,bitstreams,workFlowProcessCommentRest);
               workFlowProcessCommentRest.setMargeddocuuid(notedoc.getID().toString());
                notedoc.setWorkflowprocesscomment(workFlowProcessComment1);
                workflowProcessReferenceDocs.add(notedoc);
                if (workflowProcessReferenceDocs != null && workflowProcessReferenceDocs.size() != 0) {
                    workFlowProcessComment1.setWorkflowProcessReferenceDoc(workflowProcessReferenceDocs);
                }
                workFlowProcessCommentService.update(context, workFlowProcessComment1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("::::::error::::createWorkFlowProcessCommentFromRestObject::::::::::");
            throw new RuntimeException(e.getMessage(), e);
        }
        log.info("::::::complate::::createWorkFlowProcessCommentFromRestObject::::::::::");
        return workFlowProcessComment;
    }
    @Override
    protected WorkFlowProcessCommentRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                                  JsonNode jsonNode) throws Exception {
        log.info("::::::start::::put::::::::::");
        WorkFlowProcessCommentRest workFlowProcessCommentRest  = new Gson().fromJson(jsonNode.toString(), WorkFlowProcessCommentRest.class);

        WorkFlowProcessComment workFlowProcessComment = workFlowProcessCommentService.find(context, id);
        if (workFlowProcessComment == null) {
            System.out.println("workFlowProcessComment id ::: is Null  workFlowProcessComment tye null"+id);
            throw new ResourceNotFoundException("workFlowProcessComment  field with id: " + id + " not found");
        }
        workFlowProcessComment=workFlowProcessCommentConverter.convert(context,workFlowProcessCommentRest);
        workFlowProcessCommentService.update(context, workFlowProcessComment);
        context.commit();
        log.info("::::::End::::put::::::::::");
        return converter.toRest(workFlowProcessComment, utils.obtainProjection());
    }
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @Override
    public WorkFlowProcessCommentRest findOne(Context context, UUID uuid) {
        context.turnOffAuthorisationSystem();
        WorkFlowProcessCommentRest workFlowProcessCommentRest =null;
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
        List<WorkFlowProcessComment>  workFlowProcessComment= workFlowProcessCommentService.findAll(context,
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
            long total = workFlowProcessCommentService.countComment(context, workflowprocessid);
            List<WorkFlowProcessComment> witems = workFlowProcessCommentService.getComments(context, workflowprocessid);

            return converter.toRestPage(witems, pageable, total, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    public WorkflowProcessReferenceDoc  createFinalNoteComment(Context context, WorkFlowProcessComment comment, File tempFile1html, List<Bitstream> bitstreams,WorkFlowProcessCommentRest workFlowProcessCommentRest) throws

            Exception {

        boolean isTextEditorFlow = false;
        int notenumbe=0;
        if(workFlowProcessCommentRest.getWorkflowProcessRest()!=null&&workFlowProcessCommentRest.getWorkflowProcessRest().getUuid()!=null) {
            List<WorkFlowProcessComment> comments = workFlowProcessCommentService.getComments(context, UUID.fromString(workFlowProcessCommentRest.getWorkflowProcessRest().getId()));
            if(comments!=null&&comments.size()!=0) {
                notenumbe = comments.size();
            }
        }
        if(notenumbe==0){
            notenumbe=1;
        }
        System.out.println("note number---- "+notenumbe);
       // System.out.println("start.......createFinalNote");
        StringBuffer sb = new StringBuffer("<!DOCTYPE html>\n" + "<html>\n" + "<head><style>@page{size:A4;margin: 0;}.footer {\n" +
                "            width: 100%;\n" +
                "            text-align: left;\n" +
                "            font-size: 12pt;\n" +
                "            font-weight: bold;\n" +
                "            position: fixed;\n" +
                "            bottom: 10px;\n" +
                "            left: 0;\n" +
                "            right: 0;\n" +
                "            padding: 10px;\n" +
                "            background: #c5e6c1; /* Match body background color */\n" +
                "        }</style>\n" + "<title>Note</title>\n" + "</head>\n" + "<body style=\"padding-right: 20px;padding-left: 20px;background-color:#c5e6c1;\">");
       // System.out.println("start.......createFinalNote" + tempFile1html.getAbsolutePath());
        //Items

        if(notenumbe==1&&workFlowProcessCommentRest.getSubject()!=null) {
            sb.append("<p> <b>Subject : " + workFlowProcessCommentRest.getSubject() + "</b></p>");
        }
            if(workFlowProcessCommentRest.getSapdocumentno()!=null&&workFlowProcessCommentRest.getSapdocumenttypeRest()!=null) {
                WorkFlowProcessMasterValue saptype=workFlowProcessMasterValueService.find(context,UUID.fromString(workFlowProcessCommentRest.getSapdocumenttypeRest().getUuid()));
                sb.append("<p> <b>SAP Document Type :  " + saptype.getPrimaryvalue() + "</b></p>");
                sb.append("<p> <b>SAP Document Number : " + workFlowProcessCommentRest.getSapdocumentno() + "</b></p>");
            }
            isTextEditorFlow = true;
            sb.append("<div style=\"width:100% ;text-align: left; float:left;\">");
            //coment count
            sb.append("<p><u>Note# " + notenumbe + "</u></p>");
            //comment text
            if (comment.getComment() != null) {
                sb.append("<p>" + comment.getComment() + "</p>");
            }
            sb.append("<br><div style=\"width:100%;\"> ");
            sb.append("<div style=\"width:70%;  float:left;\"> <p><b>Attachment :</b></p> ");
            System.out.println("omment.getWorkflowProcessReferenceDoc().size():::"+comment.getWorkflowProcessReferenceDoc().size());
            if (bitstreams.size()!= 0) {
                for (Bitstream bitstream : bitstreams) {
                    if (bitstream != null) {
                        System.out.println("in Attachment");
                        String baseurl = configurationService.getProperty("dspace.server.url");
                        sb.append("<span> <a href=" + baseurl + "/api/core/bitstreams/" + bitstream.getID() + "/content>");
                        sb.append(bitstream.getName() + "</a></span>");
                        // stroremetadate(bitstream, sb);
                    }
                }
            }
            sb.append("</div>");
            sb.append("<div style=\"    float: right;  width:30%\"><p> <B>Signature_1_Name:</B> </p><B><span>");
            sb.append("</div>" +
                    "</br>\n" +
                    "</br>\n" +
                    "</br>\n" +
                    "<center><p style=\"float:left;\">------------------------------------------- This Note ends here. / ही नोंद इथे संपते. ---------------------------------</p></center>\n</div>");

        sb.append("<div class=footer>");
        Item i=null;

        if(workFlowProcessCommentRest.getItemRest()!=null){
           i =itemConverter.convert(workFlowProcessCommentRest.getItemRest(),context);
            sb.append("["+i.getName()+"], ["+workFlowProcessCommentRest.getSubject()+"], [Note #"+1+"]");

        }
        sb.append("</div>");
        sb.append("  </body></html>");
        if (isTextEditorFlow) {
            System.out.println("::::::::::IN isTextEditorFlow :::::::::");
            FileOutputStream files = new FileOutputStream(new File(tempFile1html.getAbsolutePath()));
            //System.out.println("HTML:::" + sb.toString());
            int ii= jbpmServer.htmltopdf(sb.toString(),files);
            //int result = PdfUtils.HtmlconvertToPdf(sb.toString(), files);
            System.out.println("HTML CONVERT DONE::::::::::::::: :" + tempFile1html.getAbsolutePath());
            InputStream outputfile = new FileInputStream(new File(tempFile1html.getAbsolutePath()));
            Bitstream bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, outputfile, "", tempFile1html.getName());
            WorkflowProcessReferenceDoc margedoc = new WorkflowProcessReferenceDoc();
            if(bitstream!=null){
                margedoc.setBitstream(bitstream);
            }


            if(workFlowProcessCommentRest.getSubject()!=null) {
                margedoc.setSubject(workFlowProcessCommentRest.getSubject());
            }
            margedoc.setInitdate(new Date());
            if(i!=null&&i.getName()!=null) {
                margedoc.setItemname(i.getName());
            }
            WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Draft Type");
            if (workFlowProcessMaster != null) {
                WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, "Note", workFlowProcessMaster);
                if (workFlowProcessMasterValue != null) {
                    margedoc.setDrafttype(workFlowProcessMasterValue);
                }
            }
            int index=1;
            if(comment.getWorkFlowProcess()!=null) {
                margedoc.setWorkflowProcess(comment.getWorkFlowProcess());
                List<WorkflowProcessReferenceDoc> dd = comment.getWorkFlowProcess().getWorkflowProcessReferenceDocs().stream()
                        .filter(d -> d.getDrafttype() != null)
                        .filter(d -> d.getDrafttype() != null && d.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Note")).collect(Collectors.toList());
                if (dd != null && dd.size() != 0) {
                    System.out.println("dd.size()::::::::::" + dd.size());
                    index = dd.size() + 1;
                }
            }


            System.out.println("doc index::::::::::::::::::"+index);
            margedoc.setIndex(index);
            //margedoc.setWorkflowProcess(workflowProcess);
            WorkflowProcessReferenceDoc margedoc1 = workflowProcessReferenceDocService.create(context, margedoc);
            //context.commit();
            return margedoc1;
        }
        return null;
    }


    @Override
    public Class<WorkFlowProcessCommentRest> getDomainClass() {
        return null;
    }
}
