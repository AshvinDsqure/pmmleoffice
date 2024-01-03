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
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.*;
import org.dspace.app.rest.enums.WorkFlowAction;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.enums.WorkFlowType;
import org.dspace.app.rest.enums.WorkFlowUserType;
import org.dspace.app.rest.exception.MissingParameterException;
import org.dspace.app.rest.jbpm.JbpmServerImpl;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.repository.AbstractDSpaceRestRepository;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.app.rest.repository.LinkRestRepository;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.DateUtils;
import org.dspace.app.rest.utils.Utils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.InputStream;
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
    WorkflowProcessReferenceDocService workflowProcessReferenceDocService;

    EPersonConverter ePersonConverter;

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = RequestMethod.POST,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity create(MultipartFile file,String workFlowProcessReststr) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        WorkFlowProcessRest workFlowProcessRest1=null;
        InputStream fileInputStream = null;
        Bitstream bitstream = null;
        workFlowProcessRest1 = mapper.readValue(workFlowProcessReststr, WorkFlowProcessRest.class);
        WorkFlowProcessRest workFlowProcessRest = workFlowProcessRest1;
        WorkFlowProcessRest workFlowProcessRestTemp = null;
        List<WorkflowProcessReferenceDoc>listdoc=new ArrayList<>();
        HttpServletRequest request = getRequestService().getCurrentRequest().getHttpServletRequest();
        Context context = ContextUtil.obtainContext(request);
        try {
            System.out.println(":::::::::::::::::::::::::::::::::IN INWARD FLOW:::::::::::::::::::::::::::::");
            Optional<WorkflowProcessEpersonRest> workflowProcessEpersonRest_ = Optional.ofNullable((getSubmitor(context)));
            if (!workflowProcessEpersonRest_.isPresent()) {
                return ResponseEntity.badRequest().body("no user found");
            }

            if(file!=null){
                System.out.println("in file doc save");
                WorkflowProcessReferenceDoc doc=new WorkflowProcessReferenceDoc();
                fileInputStream = file.getInputStream();
                bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, fileInputStream, "", file.getOriginalFilename());
                System.out.println("bitstream:only pdf:" + bitstream.getName());
                doc.setBitstream(bitstream);
              WorkFlowProcessInwardDetails workFlowProcessInwardDetails=  workFlowProcessInwardDetailsConverter.convert(context,workFlowProcessRest.getWorkFlowProcessInwardDetailsRest());
              if(workFlowProcessInwardDetails.getLatterDate()!=null){
                  doc.setInitdate(workFlowProcessInwardDetails.getLatterDate());
              }
              if(workFlowProcessRest.getSubject()!=null){
                    doc.setSubject(workFlowProcessRest.getSubject());
                }
              if(workFlowProcessRest.getDocumenttypeRest()!=null){
                  doc.setWorkFlowProcessReferenceDocType(workFlowProcessMasterValueConverter.convert(context,workFlowProcessRest.getDocumenttypeRest()));
              }
                WorkflowProcessReferenceDoc workflowProcessReferenceDoc= workflowProcessReferenceDocService.create(context,doc);
                listdoc.add(workflowProcessReferenceDoc);
            }
            if(workFlowProcessRest.getActionRest()!=null){
                workFlowProcessRest.setComment(workFlowProcessMasterValueConverter.convert(context,workFlowProcessRest.getActionRest()).getPrimaryvalue());
            }
            WorkFlowType workFlowType = WorkFlowType.INWARD;
            workFlowType.setWorkFlowStatus(WorkFlowStatus.INPROGRESS);
            WorkFlowAction create = WorkFlowAction.CREATE;
            workFlowType.setWorkFlowAction(create);
            workFlowType.setProjection(utils.obtainProjection());
            List<WorkflowProcessEpersonRest> templist = workFlowProcessRest.getWorkflowProcessEpersonRests().stream().filter(d -> d.getIndex() != 0).collect(Collectors.toList());
            List<WorkflowProcessReferenceDocRest> tempdoclist = listdoc.stream().map(d -> {
                try {
                    return workflowProcessReferenceDocConverter.convert(d, utils.obtainProjection());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

            System.out.println("doc size"+tempdoclist.size());
            int i = 0;
            int cccount = 1;
            for (WorkflowProcessEpersonRest nextEpersonrest : templist) {
                Context context1 = ContextUtil.obtainContext(request);
                workFlowProcessRest.setWorkflowProcessEpersonRests(null);
                workFlowProcessRest.setWorkflowProcessReferenceDocRests(null);
                List<WorkflowProcessEpersonRest> initeatorandnextuserlist = new ArrayList<>();
                initeatorandnextuserlist.add(0, workflowProcessEpersonRest_.get());
                nextEpersonrest.setIndex(1);
                initeatorandnextuserlist.add(1, nextEpersonrest);
                workFlowProcessRest.setWorkflowProcessEpersonRests(initeatorandnextuserlist);
                if (i > 0) {
                    WorkFlowProcessInwardDetailsRest workFlowProcessInwardDetailsRest = workFlowProcessRest.getWorkFlowProcessInwardDetailsRest();
                    WorkFlowProcessMasterValue usertype = workFlowProcessMasterValueConverter.convert(context1, nextEpersonrest.getUserType());
                    if (usertype != null && usertype.getPrimaryvalue() != null && usertype.getPrimaryvalue().equalsIgnoreCase("cc")) {
                        EPerson e = ePersonService.find(context1,UUID.fromString(nextEpersonrest.getePersonRest().getUuid()));
                        if (e != null) {
                            Map<String, String> d = getCCUserTapalnumber(cccount);
                            String inwardnumber = d.get("inwardnumber");
                            if (inwardnumber != null) {
                                System.out.println("cc user inward" + inwardnumber);
                                workFlowProcessInwardDetailsRest.setInwardNumber(inwardnumber);
                                cccount++;
                            }
                        }
                    } else {
                        workFlowProcessInwardDetailsRest.setInwardNumber(getInwardNumber().get("inwardnumber"));
                    }
                    workFlowProcessRest.setWorkFlowProcessInwardDetailsRest(workFlowProcessInwardDetailsRest);
                    workFlowProcessRest.setWorkflowProcessReferenceDocRests(tempdoclist);
                    workFlowProcessRestTemp = workFlowType.storeWorkFlowProcess(context, workFlowProcessRest);
                    context1.commit();
                } else {
                    workFlowProcessRest.setWorkflowProcessReferenceDocRests(tempdoclist);
                    workFlowProcessRestTemp = workFlowType.storeWorkFlowProcess(context, workFlowProcessRest);
                    context1.commit();
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(workFlowProcessRestTemp);
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "/draft")
    public ResponseEntity draft(@RequestBody WorkFlowProcessRest workFlowProcessRest) throws Exception {
        try {
            System.out.println("workFlowProcessRest::" + new Gson().toJson(workFlowProcessRest));
            HttpServletRequest request = getRequestService().getCurrentRequest().getHttpServletRequest();
            Context context = ContextUtil.obtainContext(request);
            workFlowProcessRest.getWorkflowProcessEpersonRests().clear();
            Optional<WorkflowProcessEpersonRest> WorkflowProcessEpersonRest = Optional.ofNullable((getSubmitor(context)));
            if (!WorkflowProcessEpersonRest.isPresent()) {
                return ResponseEntity.badRequest().body("no user found");
            }
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
        return ResponseEntity.ok(workFlowProcessRest);
    }

    public Map<String, String> getCCUserTapalnumber(int cccount) {
        String inwardnumber = null;
        HttpServletRequest request = getRequestService().getCurrentRequest().getHttpServletRequest();
        Context context = ContextUtil.obtainContext(request);
        EPerson cEPerson=context.getCurrentUser();
        try {

            StringBuffer sb = new StringBuffer();
            WorkFlowProcessMasterValue department;
            if (cEPerson != null) {
                department = workFlowProcessMasterValueService.find(context, context.getCurrentUser().getDepartment().getID());
                if (department.getPrimaryvalue() != null) {
                    sb.append(department.getSecondaryvalue());
                }
            }
            if (cEPerson.getTablenumber() != null) {
                sb.append("/" + cEPerson.getTablenumber());
            }
            int count = workflowProcessService.getCountByType(context, getMastervalueData(context, WorkFlowType.MASTER.getAction(), WorkFlowType.INWARD.getAction()).getID());
            count = count + 1;
            sb.append("/0000" + count);
            sb.append("/" + DateUtils.getFinancialYear());
            sb.append("/C_"+cccount);
            inwardnumber = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in getInwardNumber ");
        }
        Map<String, String> map = new HashMap<>();
        map.put("inwardnumber", inwardnumber);
        return map;
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEAM', 'READ')")
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/getInwardNumber")
    public Map<String, String> getInwardNumber() throws Exception {
        String inwardnumber = null;
        try {
            HttpServletRequest request = getRequestService().getCurrentRequest().getHttpServletRequest();
            Context context = ContextUtil.obtainContext(request);
            EPerson currentuser = context.getCurrentUser();
            StringBuffer sb = new StringBuffer();
            WorkFlowProcessMasterValue department;
            if (currentuser != null) {
                department = workFlowProcessMasterValueService.find(context, context.getCurrentUser().getDepartment().getID());
                if (department.getPrimaryvalue() != null) {
                    sb.append(department.getSecondaryvalue());
                }
            }
            if (currentuser.getTablenumber() != null) {
                sb.append("/" + currentuser.getTablenumber());
            }
            int count = workflowProcessService.getCountByType(context, getMastervalueData(context, WorkFlowType.MASTER.getAction(), WorkFlowType.INWARD.getAction()).getID());
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
}
