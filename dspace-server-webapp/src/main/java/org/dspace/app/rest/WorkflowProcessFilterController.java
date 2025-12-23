/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.EPersonConverter;
import org.dspace.app.rest.converter.EpersonToEpersonMappingConverter;
import org.dspace.app.rest.converter.WorkFlowProcessConverter;
import org.dspace.app.rest.converter.WorkFlowProcessMasterValueConverter;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.enums.WorkFlowType;
import org.dspace.app.rest.enums.WorkFlowUserType;
import org.dspace.app.rest.jbpm.JbpmServerImpl;
import org.dspace.app.rest.jbpm.models.JBPMProcess;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.ExcelHelper;
import org.dspace.app.rest.utils.Utils;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
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
@RequestMapping("/api/" + WorkFlowProcessRest.CATEGORY + "/" + WorkFlowProcessRest.PLURAL_NAME + "/filter")
public class WorkflowProcessFilterController {

    @Autowired
    WorkflowProcessService workflowProcessService;

    @Autowired
    CountryService countryService;
    @Autowired
    JbpmServerImpl jbpmServer;



    @Autowired
    EPersonService ePersonService;

    @Autowired
    GroupService groupService;



    @Autowired
    VipService vipService;
    @Autowired
    CategoryService categoryService;


    @Autowired
    SubCategoryService subcategoryService;

    @Autowired
    VipNameService vipNameService;
    @Autowired
    StateService stateService;
    @Autowired
    CityService cityService;



    @Autowired
    EpersonMappingService epersonMappingService;

    @Autowired
    EpersonToEpersonMappingService epersonToEpersonMappingService;

    @Autowired
    EpersonToEpersonMappingConverter epersonToEpersonMappingConverter;



    @Autowired
    WorkFlowProcessMasterService workFlowProcessMasterService;
    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterValueService;
    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;

    @Autowired
    WorkFlowProcessOutwardDetailsService workFlowProcessOutwardDetailsService;

    @Autowired
    WorkFlowProcessInwardDetailsService workFlowProcessInwardDetailsService;

    @Autowired
    EPersonConverter ePersonConverter;
    @Autowired
    private DiscoverableEndpointsService discoverableEndpointsService;


    @Autowired
    WorkFlowProcessConverter workFlowProcessConverter;

    private static final Logger log = LogManager.getLogger();

    @Autowired
    protected Utils utils;
    @Autowired
    protected ItemService itemService;

    public void afterPropertiesSet() throws Exception {
        this.discoverableEndpointsService.register(this,
                Arrays.asList(new Link[]{Link.of("/api/workflowprocesse", "workflowprocesses")}));
    }

    /**
     * Method to upload a Bitstream to a Bundle with the given UUID in the URL. This will create a Bitstream with the
     * file provided in the request and attach this to the Item that matches the UUID in the URL.
     * This will only work for uploading one file, any extra files will silently be ignored
     *
     * @return The created BitstreamResource
     */
    @RequestMapping(method = RequestMethod.POST, value = "/filterbyData")
    public List<WorkFlowProcessRest> filter(HttpServletRequest request, @RequestBody WorkFlowProcessFilterRest rest, Pageable pageable
    ) {
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            HashMap<String, String> map = new HashMap<>();
            if (rest.getPriorityRest() != null && rest.getPriorityRest().getUuid() != null) {
                map.put("priority", rest.getPriorityRest().getUuid());
            }
            if (rest.getWorkflowStatusRest() != null && rest.getWorkflowStatusRest().getUuid() != null) {
                map.put("status", rest.getWorkflowStatusRest().getUuid());
            }
            if (rest.getWorkflowTypeRest() != null && rest.getWorkflowTypeRest().getUuid() != null) {
                map.put("type", rest.getWorkflowTypeRest().getUuid());
            }
            if (rest.getDepartmentRest() != null && rest.getDepartmentRest().getUuid() != null) {
                map.put("department", rest.getWorkflowTypeRest().getUuid());
            }
            if (rest.getePersonRest() != null && rest.getePersonRest().getUuid() != null) {
                map.put("user", rest.getePersonRest().getUuid());
            }
            if (rest.getSubject() != null) {
                map.put("subject", rest.getSubject());
            }
            if (rest.getInward() != null) {
             /* WorkFlowProcessInwardDetails inward=workFlowProcessInwardDetailsService.getByInwardNumber(context,rest.getInward());
              if(inward!=null) {*/
                map.put("inward", rest.getInward());
                /*}*/
            }
            if (rest.getYear() != null) {
             /* WorkFlowProcessInwardDetails inward=workFlowProcessInwardDetailsService.getByInwardNumber(context,rest.getInward());
              if(inward!=null) {*/
                map.put("inward", rest.getYear());
                /*}*/
            }
            if (rest.getOutward() != null) {
               /* WorkFlowProcessOutwardDetails outward=workFlowProcessOutwardDetailsService.getByOutwardNumber(context,rest.getOutward());
                if(outward!=null) {*/
                map.put("outward", rest.getOutward());
                /*}*/
            }
            //System.out.println(map);
            List<WorkflowProcess> list = workflowProcessService.Filter(context, map, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            List<WorkFlowProcessRest> rests = list.stream().map(d -> {
                return workFlowProcessConverter.convertFilter(d, utils.obtainProjection());
            }).collect(Collectors.toList());
            return rests;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/filterbyInwardAndOutWard")
    public Page<WorkFlowProcessRest> filterbyInwardAndOutWard(HttpServletRequest request, @RequestBody WorkFlowProcessFilterRest rest, Pageable pageable) {
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            HashMap<String, String> map = new HashMap<>();
            if (rest.getPriorityRest() != null && rest.getPriorityRest().getUuid() != null) {
                map.put("priority", rest.getPriorityRest().getUuid());
            }
            if (rest.getWorkflowStatusRest() != null && rest.getWorkflowStatusRest().getUuid() != null) {
                map.put("status", rest.getWorkflowStatusRest().getUuid());
            }
            if (rest.getWorkflowTypeRest() != null && rest.getWorkflowTypeRest().getUuid() != null) {
                map.put("type", rest.getWorkflowTypeRest().getUuid());
            }
            if (rest.getDepartmentRest() != null && rest.getDepartmentRest().getUuid() != null) {
                map.put("department", rest.getWorkflowTypeRest().getUuid());
            }
            if (rest.getCategoryRest() != null && rest.getCategoryRest().getUuid() != null) {
                map.put("categoryRest", rest.getCategoryRest().getUuid());
            }
            if (rest.getSubcategoryRest() != null && rest.getSubcategoryRest().getUuid() != null) {
                map.put("subcategoryRest", rest.getSubcategoryRest().getUuid());
            }
            if (rest.getOfficeRest() != null && rest.getOfficeRest().getUuid() != null) {
                map.put("officeRest", rest.getOfficeRest().getUuid());
            }
            if (rest.getInwardmodeRest() != null && rest.getInwardmodeRest().getUuid() != null) {
                map.put("inwardmodeRest", rest.getInwardmodeRest().getUuid());
            }
            if (rest.getOutwardmodeRest() != null && rest.getOutwardmodeRest().getUuid() != null) {
                map.put("outwardmodeRest", rest.getOutwardmodeRest().getUuid());
            }
            if (rest.getOutwardmediumRest() != null && rest.getOutwardmediumRest().getUuid() != null) {
                map.put("outwardmedium", rest.getOutwardmediumRest().getUuid());
            }
            if (rest.getDesignationRest() != null && rest.getDesignationRest().getUuid() != null) {
                map.put("designation", rest.getDesignationRest().getUuid());
            }
            if (rest.getePersonRest() != null && rest.getePersonRest().getUuid() != null) {
                map.put("user", rest.getePersonRest().getUuid());
            }
            //text
            if (rest.getSubject() != null) {
                map.put("subject", rest.getSubject());
            }
            if (rest.getInwarddate() != null) {
                map.put("inwarddate", rest.getInwarddate());
            }
            if (rest.getOutwarddate() != null) {
                map.put("outwarddate", rest.getOutwarddate());
            }
            if (rest.getReceiveddate() != null) {
                map.put("receiveddate", rest.getReceiveddate());
            }
            if (rest.getUsername() != null) {
                map.put("username", rest.getUsername());
            }
            if (rest.getSendername() != null) {
                map.put("sendername", rest.getSendername());
            }
            if (rest.getSenderphonenumber() != null) {
                map.put("senderphonenumber", rest.getSenderphonenumber());
            }
            if (rest.getSenderaddress() != null) {
                map.put("senderaddress", rest.getSenderaddress());
            }
            if (rest.getSendercity() != null) {
                map.put("sendercity", rest.getSendercity());
            }
            if (rest.getSendercountry() != null) {
                map.put("sendercountry", rest.getSendercountry());
            }
            if (rest.getSenderpincode() != null) {
                map.put("senderpincode", rest.getSenderpincode());
            }
            if (rest.getInward() != null) {
                map.put("inward", rest.getInward());
            }
            if (rest.getOutward() != null) {
                map.put("outward", rest.getOutward());
            }
            //System.out.println(map);
            int count = workflowProcessService.countfilterInwarAndOutWard(context, map, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            List<WorkflowProcess> list = workflowProcessService.filterInwarAndOutWard(context, map, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            List<WorkFlowProcessRest> rests = list.stream().map(d -> {
                return workFlowProcessConverter.convertFilter(d, utils.obtainProjection());
            }).collect(Collectors.toList());
            return new PageImpl(rests, pageable, count);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/filterDepartmentWiseCount")
    public Page<DepartmentDTO> filterDepartmentWiseCount(HttpServletRequest request, @RequestBody WorkFlowProcessFilterRest rest, Pageable pageable) {
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            HashMap<String, String> map = new HashMap<>();
            if (rest.getPriorityRest() != null && rest.getPriorityRest().getUuid() != null) {
                map.put("priority", rest.getPriorityRest().getUuid());
            }
            if (rest.getWorkflowStatusRest() != null && rest.getWorkflowStatusRest().getUuid() != null) {
                map.put("status", rest.getWorkflowStatusRest().getUuid());
            }
            if (rest.getWorkflowTypeRest() != null && rest.getWorkflowTypeRest().getUuid() != null) {
                map.put("type", rest.getWorkflowTypeRest().getUuid());
                WorkFlowProcessMasterValue wm = workFlowProcessMasterValueService.find(context, UUID.fromString(rest.getWorkflowTypeRest().getUuid()));
                if (wm != null) {
                    if (wm.getPrimaryvalue().equalsIgnoreCase("Draft")) {
                        map.put("draft", "draft");
                    } else {
                        map.put("tapal", "tapal");
                    }
                }
            }
            if (rest.getePersonRest() != null && rest.getePersonRest().getUuid() != null) {
                map.put("user", rest.getePersonRest().getUuid());
            }
            //text
            if (rest.getSubject() != null) {
                map.put("subject", rest.getSubject());
            }
            //System.out.println(map);
            int count = 100;
            List<Object[]> list = workflowProcessService.filterDepartmentWiseCount(context, map, rest.getStartdate(), rest.getEnddate(), Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            // System.out.println("List----" + list.size());
            List<DepartmentDTO> rests = list.stream().filter(d -> d[0] != null && d[1] != null).map(d -> {
                DepartmentDTO dd = new DepartmentDTO();
                dd.setName(d[0].toString());
                dd.setCount(Long.parseLong(d[1].toString()));
                return dd;
            }).collect(Collectors.toList());
            return new PageImpl(rests, pageable, count);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/downloadfilterDepartmentWiseCount")
    public ResponseEntity<Resource> downloadItem(HttpServletRequest request, @RequestBody WorkFlowProcessFilterRest rest) {
        try {
            Context context = ContextUtil.obtainContext(request);
            HashMap<String, String> map = new HashMap<>();
            String name = "_";
            if (rest.getPriorityRest() != null && rest.getPriorityRest().getUuid() != null) {
                map.put("priority", rest.getPriorityRest().getUuid());
            }
            if (rest.getWorkflowStatusRest() != null && rest.getWorkflowStatusRest().getUuid() != null) {
                map.put("status", rest.getWorkflowStatusRest().getUuid());
            }
            if (rest.getWorkflowTypeRest() != null && rest.getWorkflowTypeRest().getUuid() != null) {
                map.put("type", rest.getWorkflowTypeRest().getUuid());
                WorkFlowProcessMasterValue wm = workFlowProcessMasterValueService.find(context, UUID.fromString(rest.getWorkflowTypeRest().getUuid()));
                if (wm != null) {
                    if (wm.getPrimaryvalue().equalsIgnoreCase("Draft")) {
                        map.put("draft", "draft");
                        name = "EFile";
                    } else {
                        map.put("tapal", "tapal");
                        name = "Tapal";
                    }
                }
            }
            if (rest.getePersonRest() != null && rest.getePersonRest().getUuid() != null) {
                map.put("user", rest.getePersonRest().getUuid());
            }
            //text
            if (rest.getSubject() != null) {
                map.put("subject", rest.getSubject());
            }
            //System.out.println(map);

            String filename = "DepartmentWiseTotal_" + name + "count.xlsx";
            List<Object[]> list = workflowProcessService.filterDepartmentWiseCountDownload(context, map, rest.getStartdate(), rest.getEnddate());
            // System.out.println("List----" + list.size());
            List<DepartmentDTO> rests = list.stream().filter(d -> d[0] != null && d[1] != null).map(d -> {
                DepartmentDTO dd = new DepartmentDTO();
                dd.setName(d[0].toString());
                dd.setCount(Long.parseLong(d[1].toString()));
                return dd;
            }).collect(Collectors.toList());
            ByteArrayInputStream in = ExcelHelper.tutorialsToExceldEPARTMENT(rests);
            InputStreamResource file = new InputStreamResource(in);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (SQLException e) {
            System.out.println("error :" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getFilterPerameter")
    public HashMap<String, String> getFilterdate(HttpServletRequest request) {
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            HashMap<String, String> map = new HashMap<>();
            map.put("Priority", "dropdown");
            map.put("Status", "dropdown");
            map.put("Department", "dropdown");
            map.put("Subject", "text");
            map.put(WorkFlowType.INWARD.getAction() + " Number", "text");
            map.put(WorkFlowType.OUTWARED.getAction() + " Number", "text");
            map.put("Year", "text");
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getFilterPerameterByInwardAndOutWard")
    public HashMap<String, String> getFilterPerameterByInwardAndOutWard(HttpServletRequest request) {
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put("Priority", "dropdown");
            map.put("Status", "dropdown");
            map.put("Department", "dropdown");
            map.put("Subject", "text");
            map.put(WorkFlowType.INWARD.getAction() + " Number", "text");
            map.put(WorkFlowType.OUTWARED.getAction() + " Number", "text");
            map.put("Inward Date", "text");
            map.put("Outward Date", "text");
            map.put("Received Date", "text");
            map.put("Category", "dropdown");
            map.put("Sub Category", "dropdown");
            map.put("Letter Category", "dropdown");
            map.put("Office", "dropdown");
            map.put("Inward Mode", "dropdown");
            map.put("Outward Mode", "dropdown");
            map.put("Outward Medium", "dropdown");
            map.put("Designation", "dropdown");
            map.put("User Name", "text");
            map.put("Sender/Recipient Name", "text");
            map.put("Sender/Recipient Email", "text");
            map.put("Sender/Recipient Phone Number", "text");
            map.put("Sender/Recipient Organization", "text");
            map.put("Sender/Recipient Address", "text");
            map.put("Sender/Recipient City", "text");
            map.put("Sender/Recipient Country", "text");
            map.put("Sender/Recipient Pin code", "text");
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getFilterPerameterSearch")
    public HashMap<String, String> getFilterPerameterSearch(HttpServletRequest request) {
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            HashMap<String, String> map = new HashMap<>();
            map.put("Priority", "dropdown");
            map.put("Status", "dropdown");
            map.put("Department", "dropdown");
            map.put("Subject", "text");
            map.put(WorkFlowType.INWARD.getAction() + " Number", "text");
            map.put(WorkFlowType.OUTWARED.getAction() + " Number", "text");
            map.put(WorkFlowType.DRAFT.getAction(), "text");
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }





    @RequestMapping(method = RequestMethod.GET, value = "/getCountsDashboard")
    public HashMap<String, HashMap<String, Integer>> getCountsDashboard( @Parameter(value = "type", required = true) String type,
                                                                         @Parameter(value = "tab", required = true) String tab,
                                                                         HttpServletRequest request) {
        try {
            Context context = ContextUtil.obtainContext(request);
            HashMap<String, String> perameter = new HashMap<>();
            context.turnOffAuthorisationSystem();
            UUID userid = context.getCurrentUser().getID();
            UUID Lowid = null;
            UUID Mediumid = null;
            UUID Highid = null;
            UUID mostimedeatlyid = null;
            UUID inwardid = null;
            UUID draftid = null;
            UUID epersonToEpersonMappingid = null;
            HashMap<String, HashMap<String, Integer>> maps = new HashMap<>();
            Optional<EpersonToEpersonMapping> map = context.getCurrentUser().getEpersonToEpersonMappings().stream().filter(d -> d.getIsactive() == true).findFirst();
            if (map.isPresent()) {
                epersonToEpersonMappingid = map.get().getID();
            }
            WorkFlowProcessMaster PriorityMaster = workFlowProcessMasterService.findByName(context, "Priority");
            if (PriorityMaster != null) {
                Lowid = workFlowProcessMasterValueService.findByName(context, "Low", PriorityMaster).getID();
                Mediumid = workFlowProcessMasterValueService.findByName(context, "Medium", PriorityMaster).getID();
                Highid = workFlowProcessMasterValueService.findByName(context, "High", PriorityMaster).getID();
                mostimedeatlyid = workFlowProcessMasterValueService.findByName(context, "Most Immediate", PriorityMaster).getID();
            }
            inwardid = WorkFlowType.INWARD.getUserTypeFromMasterValue(context).get().getID();
            draftid = WorkFlowType.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            //:::::::::::::::::::status:::::::::::::::::::::::::::
            UUID statusdraft = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            //:::::::::::::::::::inward Map:::::::::::::::::::::::::
            HashMap<String, Integer> mapInward = new HashMap<>();
            if(type!=null&&type.equalsIgnoreCase("Tapal")) {
                //inbox
                if(tab!=null&&tab.equalsIgnoreCase("Inbox")) {
                    UUID tInProgressid = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context).get().getID();
                    UUID tSuspendid = WorkFlowStatus.SUSPEND.getUserTypeFromMasterValue(context).get().getID();
                    mapInward.put("inbox_Medium", workflowProcessService.countByTypeAndPriority(context, inwardid, Mediumid, userid, tInProgressid, epersonToEpersonMappingid));
                    mapInward.put("inbox_Low", workflowProcessService.countByTypeAndPriority(context, inwardid, Lowid, userid, tInProgressid, epersonToEpersonMappingid));
                    mapInward.put("inbox_High", workflowProcessService.countByTypeAndPriority(context, inwardid, Highid, userid, tInProgressid, epersonToEpersonMappingid));
                    mapInward.put("inbox_MostImmediate", workflowProcessService.countByTypeAndPriority(context, inwardid, mostimedeatlyid, userid, tInProgressid, epersonToEpersonMappingid));
                    mapInward.put("inbox_Suspend", workflowProcessService.countByTypeAndStatus(context, inwardid, tSuspendid, userid, epersonToEpersonMappingid));
                    UUID statusid = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context).get().getID();
                    int inbox_InProgresst = workflowProcessService.countfindNotCompletedByUser(context, context.getCurrentUser().getID(), statusid, inwardid, epersonToEpersonMappingid);
                    mapInward.put("inbox_InProgress", inbox_InProgresst);
                } else  if(tab!=null&&tab.equalsIgnoreCase("Draft")) {
                    UUID createdid = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
                    //draft
                    mapInward.put("draft_Medium", workflowProcessService.countByTypeAndPriorityDraftTapal(context, inwardid, Mediumid, userid, createdid, epersonToEpersonMappingid));
                    mapInward.put("draft_Low", workflowProcessService.countByTypeAndPriorityDraftTapal(context, inwardid, Lowid, userid, createdid, epersonToEpersonMappingid));
                    mapInward.put("draft_High", workflowProcessService.countByTypeAndPriorityDraftTapal(context, inwardid, Highid, userid, createdid, epersonToEpersonMappingid));
                    mapInward.put("draft_MostImmediate", workflowProcessService.countByTypeAndPriorityDraftTapal(context, inwardid, mostimedeatlyid, userid, createdid, epersonToEpersonMappingid));
                }else  if(tab!=null&&tab.equalsIgnoreCase("Closed")) {
                    UUID tCloseid = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
                    UUID statusdispathcclose = WorkFlowStatus.DISPATCHCLOSE.getUserTypeFromMasterValue(context).get().getID();
                    UUID INITIATORid = WorkFlowUserType.INITIATOR.getUserTypeFromMasterValue(context).get().getID();
                    //close
                    mapInward.put("close_Medium", workflowProcessService.countByTypeAndPriorityCloseTapal(context, inwardid, Mediumid, userid, tCloseid, epersonToEpersonMappingid, statusdraft, statusdispathcclose, INITIATORid));
                    mapInward.put("close_Low", workflowProcessService.countByTypeAndPriorityCloseTapal(context, inwardid, Lowid, userid, tCloseid, epersonToEpersonMappingid, statusdraft, statusdispathcclose, INITIATORid));
                    mapInward.put("close_High", workflowProcessService.countByTypeAndPriorityCloseTapal(context, inwardid, Highid, userid, tCloseid, epersonToEpersonMappingid, statusdraft, statusdispathcclose, INITIATORid));
                    mapInward.put("close_MostImmediate", workflowProcessService.countByTypeAndPriorityCloseTapal(context, inwardid, mostimedeatlyid, userid, tCloseid, epersonToEpersonMappingid, statusdraft, statusdispathcclose, INITIATORid));
                }else  if(tab!=null&&tab.equalsIgnoreCase("SentTo")) {
                    UUID tparkedid = WorkFlowStatus.PARKED.getUserTypeFromMasterValue(context).get().getID();
                    UUID createdid = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
                    //sentto
                    mapInward.put("sent_Medium", workflowProcessService.countByTypeAndPriorityNotDraft(context, inwardid, Mediumid, userid, createdid, epersonToEpersonMappingid));
                    mapInward.put("sent_Low", workflowProcessService.countByTypeAndPriorityNotDraft(context, inwardid, Lowid, userid, createdid, epersonToEpersonMappingid));
                    mapInward.put("sent_High", workflowProcessService.countByTypeAndPriorityNotDraft(context, inwardid, Highid, userid, createdid, epersonToEpersonMappingid));
                    mapInward.put("sent_MostImmediate", workflowProcessService.countByTypeAndPriorityNotDraft(context, inwardid, mostimedeatlyid, userid, createdid, epersonToEpersonMappingid));
                    mapInward.put("sent_InProgress", sentInProgressTapal(context, epersonToEpersonMappingid, perameter));
                    mapInward.put("sent_Parked", workflowProcessService.countByTypeAndStatusandNotDraft(context, inwardid, tparkedid, userid, createdid, epersonToEpersonMappingid));

                }else  if(tab!=null&&tab.equalsIgnoreCase("Parked")) {
                    UUID tparkedid = WorkFlowStatus.PARKED.getUserTypeFromMasterValue(context).get().getID();
                    //park
                    mapInward.put("park_Medium", workflowProcessService.countByTypeAndPriorityPark(context, inwardid, Mediumid, userid, tparkedid, epersonToEpersonMappingid, statusdraft));
                    mapInward.put("park_Low", workflowProcessService.countByTypeAndPriorityPark(context, inwardid, Lowid, userid, tparkedid, epersonToEpersonMappingid, statusdraft));
                    mapInward.put("park_High", workflowProcessService.countByTypeAndPriorityPark(context, inwardid, Highid, userid, tparkedid, epersonToEpersonMappingid, statusdraft));
                    mapInward.put("park_MostImmediate", workflowProcessService.countByTypeAndPriorityPark(context, inwardid, mostimedeatlyid, userid, tparkedid, epersonToEpersonMappingid, statusdraft));
                }
                maps.put(WorkFlowType.INWARD.getAction(), mapInward);
            }else  if (type!=null&&type.equalsIgnoreCase("File")) {
                //::::::::::::::::::Draft Map::::FILE::::::::::::::::::::::::::::::::::
                HashMap<String, Integer> mapDraft = new HashMap<>();
                if(tab!=null&&tab.equalsIgnoreCase("Inbox")) {
                    UUID tInProgressid = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context).get().getID();
                    UUID tparkedid = WorkFlowStatus.PARKED.getUserTypeFromMasterValue(context).get().getID();
                    //inbox
                    mapDraft.put("inbox_Medium", workflowProcessService.countByTypeAndPriority(context, draftid, Mediumid, userid, tInProgressid, epersonToEpersonMappingid));
                    mapDraft.put("inbox_Low", workflowProcessService.countByTypeAndPriority(context, draftid, Lowid, userid, tInProgressid, epersonToEpersonMappingid));
                    mapDraft.put("inbox_High", workflowProcessService.countByTypeAndPriority(context, draftid, Highid, userid, tInProgressid, epersonToEpersonMappingid));
                    mapDraft.put("inbox_MostImmediate", workflowProcessService.countByTypeAndPriority(context, draftid, mostimedeatlyid, userid, tInProgressid, epersonToEpersonMappingid));
                    mapDraft.put("inbox_InProgress", inboxInProgressFile(context, epersonToEpersonMappingid));
                    mapDraft.put("inbox_Parked", workflowProcessService.countByTypeAndStatus(context, draftid, tparkedid, userid, epersonToEpersonMappingid));
                    mapDraft.put("duedate_count", duedatecount(context, epersonToEpersonMappingid));

                }else if(tab!=null&&tab.equalsIgnoreCase("Draft")) {
                    UUID draftnote = WorkFlowStatus.DRAFTNOTE.getUserTypeFromMasterValue(context).get().getID();
                    //Draft
                    mapDraft.put("draft_Medium", workflowProcessService.countByTypeAndPriority(context, draftid, Mediumid, userid, draftnote, epersonToEpersonMappingid));
                    mapDraft.put("draft_Low", workflowProcessService.countByTypeAndPriority(context, draftid, Lowid, userid, draftnote, epersonToEpersonMappingid));
                    mapDraft.put("draft_High", workflowProcessService.countByTypeAndPriority(context, draftid, Highid, userid, draftnote, epersonToEpersonMappingid));
                    mapDraft.put("draft_MostImmediate", workflowProcessService.countByTypeAndPriority(context, draftid, mostimedeatlyid, userid, draftnote, epersonToEpersonMappingid));
                }else if(tab!=null&&tab.equalsIgnoreCase("Created")) {
                    UUID createdid = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
                    //created
                    mapDraft.put("created_Medium", workflowProcessService.countByTypeAndPriorityCreted(context, draftid, Mediumid, userid, createdid, epersonToEpersonMappingid));
                    mapDraft.put("created_Low", workflowProcessService.countByTypeAndPriorityCreted(context, draftid, Lowid, userid, createdid, epersonToEpersonMappingid));
                    mapDraft.put("created_High", workflowProcessService.countByTypeAndPriorityCreted(context, draftid, Highid, userid, createdid, epersonToEpersonMappingid));
                    mapDraft.put("created_MostImmediate", workflowProcessService.countByTypeAndPriorityCreted(context, draftid, mostimedeatlyid, userid, createdid, epersonToEpersonMappingid));
                }else if(tab!=null&&tab.equalsIgnoreCase("Parked")) {
                    UUID tparkedid = WorkFlowStatus.PARKED.getUserTypeFromMasterValue(context).get().getID();
                    //park
                    mapDraft.put("park_Medium", workflowProcessService.countByTypeAndPriorityPark(context, draftid, Mediumid, userid, tparkedid, epersonToEpersonMappingid, statusdraft));
                    mapDraft.put("park_Low", workflowProcessService.countByTypeAndPriorityPark(context, draftid, Lowid, userid, tparkedid, epersonToEpersonMappingid, statusdraft));
                    mapDraft.put("park_High", workflowProcessService.countByTypeAndPriorityPark(context, draftid, Highid, userid, tparkedid, epersonToEpersonMappingid, statusdraft));
                    mapDraft.put("park_MostImmediate", workflowProcessService.countByTypeAndPriorityPark(context, draftid, mostimedeatlyid, userid, tparkedid, epersonToEpersonMappingid, statusdraft));
                    mapDraft.put("pending_Sign", getPendingsign(context, epersonToEpersonMappingid));
                }else if(tab!=null&&tab.equalsIgnoreCase("Closed")) {
                    UUID tCloseid = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
                    UUID statusdispathcclose = WorkFlowStatus.DISPATCHCLOSE.getUserTypeFromMasterValue(context).get().getID();
                    UUID INITIATORid = WorkFlowUserType.INITIATOR.getUserTypeFromMasterValue(context).get().getID();
                    //close
                    mapDraft.put("close_Medium", workflowProcessService.countByTypeAndPriorityClose(context, draftid, Mediumid, userid, tCloseid, epersonToEpersonMappingid, statusdraft, statusdispathcclose, INITIATORid));
                    mapDraft.put("close_Low", workflowProcessService.countByTypeAndPriorityClose(context, draftid, Lowid, userid, tCloseid, epersonToEpersonMappingid, statusdraft, statusdispathcclose, INITIATORid));
                    mapDraft.put("close_High", workflowProcessService.countByTypeAndPriorityClose(context, draftid, Highid, userid, tCloseid, epersonToEpersonMappingid, statusdraft, statusdispathcclose, INITIATORid));
                    mapDraft.put("close_MostImmediate", workflowProcessService.countByTypeAndPriorityClose(context, draftid, mostimedeatlyid, userid, tCloseid, epersonToEpersonMappingid, statusdraft, statusdispathcclose, INITIATORid));
                }else if(tab!=null&&tab.equalsIgnoreCase("SentTo")) {
                    UUID tparkedid = WorkFlowStatus.PARKED.getUserTypeFromMasterValue(context).get().getID();
                    UUID createdid = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
                    //sent
                    mapDraft.put("sent_Medium", workflowProcessService.countByTypeAndPriorityNotDraft(context, draftid, Mediumid, userid, createdid, epersonToEpersonMappingid));
                    mapDraft.put("sent_Low", workflowProcessService.countByTypeAndPriorityNotDraft(context, draftid, Lowid, userid, createdid, epersonToEpersonMappingid));
                    mapDraft.put("sent_High", workflowProcessService.countByTypeAndPriorityNotDraft(context, draftid, Highid, userid, createdid, epersonToEpersonMappingid));
                    mapDraft.put("sent_MostImmediate", workflowProcessService.countByTypeAndPriorityNotDraft(context, draftid, mostimedeatlyid, userid, createdid, epersonToEpersonMappingid));
                    mapDraft.put("sent_InProgress", sentInProgressFile(context, epersonToEpersonMappingid, perameter));
                    mapDraft.put("sent_Parked", workflowProcessService.countByTypeAndStatusandNotDraft(context, draftid, tparkedid, userid, createdid, epersonToEpersonMappingid));
                }else if(tab!=null&&tab.equalsIgnoreCase("SignLetter")){
                    UUID tCloseid = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
                    UUID statusdispathcclose = WorkFlowStatus.DISPATCHCLOSE.getUserTypeFromMasterValue(context).get().getID();
                    UUID INITIATORid = WorkFlowUserType.INITIATOR.getUserTypeFromMasterValue(context).get().getID();
                    //sign Latter
                    mapDraft.put("sign_Medium", workflowProcessService.countByTypeAndPriorityCloseSignLatter(context, draftid, Mediumid, userid, tCloseid, epersonToEpersonMappingid, statusdraft, statusdispathcclose, INITIATORid));
                    mapDraft.put("sign_Low", workflowProcessService.countByTypeAndPriorityCloseSignLatter(context, draftid, Lowid, userid, tCloseid, epersonToEpersonMappingid, statusdraft, statusdispathcclose, INITIATORid));
                    mapDraft.put("sign_High", workflowProcessService.countByTypeAndPriorityCloseSignLatter(context, draftid, Highid, userid, tCloseid, epersonToEpersonMappingid, statusdraft, statusdispathcclose, INITIATORid));
                    mapDraft.put("sign_MostImmediate", workflowProcessService.countByTypeAndPriorityCloseSignLatter(context, draftid, mostimedeatlyid, userid, tCloseid, epersonToEpersonMappingid, statusdraft, statusdispathcclose, INITIATORid));
                }
                maps.put(WorkFlowType.DRAFT.getAction(), mapDraft);
            }
            return maps;
        } catch (Exception e) {
            // System.out.println("ERROr in Dsbrd count"+e.getMessage());
        }
        return null;
    }


    public int inboxInProgressFile(Context context, UUID epersonToEpersonMappingid) {
        try {
            UUID statuscloseid = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context).get().getID();
            UUID statusRejected = WorkFlowStatus.REJECTED.getUserTypeFromMasterValue(context).get().getID();
            UUID statusdraft = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Workflow Type");
            UUID statusdraftid = null;
            if (workFlowProcessMaster != null) {
                statusdraftid = workFlowProcessMasterValueService.findByName(context, "Draft", workFlowProcessMaster).getID();
            }
            int count = workflowProcessService.countfindDraftPending(context, context.getCurrentUser().getID(), statuscloseid, statusdraftid, statusdraft, epersonToEpersonMappingid,statusRejected);
            return count;
        } catch (Exception e) {
            System.out.println("error inboxInProgressFile" +e.getMessage());
            return 0;
        }
    }
    public int sentInProgressTapal(Context context, UUID epersonToEpersonMappingid,HashMap<String, String> perameter) {
        try {
            UUID statusidclose = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            UUID statusid1 = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            UUID workflowtypeid = WorkFlowType.INWARD.getUserTypeFromMasterValue(context).get().getID();
            int counts = workflowProcessService.countTapal(context, context.getCurrentUser().getID(), statusid1, workflowtypeid, statusidclose, epersonToEpersonMappingid,perameter);
            return counts;
        } catch (Exception e) {
            System.out.println("error inboxInProgressFile" +e.getMessage());
            return 0;
        }
    }
    public int sentInProgressFile(Context context, UUID epersonToEpersonMappingid,HashMap<String, String> perameter) {
        try {
            UUID statusidclose1 = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            UUID statusid1a = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            UUID workflowtypeid1 = WorkFlowType.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            int sent_InProgress = workflowProcessService.countTapal(context, context.getCurrentUser().getID(), statusid1a, workflowtypeid1, statusidclose1, epersonToEpersonMappingid,perameter);
            return sent_InProgress;
        } catch (Exception e) {
            System.out.println("error inboxInProgressFile" +e.getMessage());
            return 0;
        }
    }

    public int duedatecount(Context context, UUID epersonToEpersonMappingid) {
        try {
            UUID statusinpogress = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context).get().getID();
            UUID statusdraft = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Workflow Type");
            UUID statusdraftid = null;
            if (workFlowProcessMaster != null) {
                statusdraftid = workFlowProcessMasterValueService.findByName(context, "Draft", workFlowProcessMaster).getID();
            }
            int count = workflowProcessService.countfindFilePendingDueDate(context, context.getCurrentUser().getID(), statusinpogress, statusdraftid, statusdraft,epersonToEpersonMappingid);

            return count;
        } catch (Exception e) {
            System.out.println("error inboxInProgressFile" +e.getMessage());
            return 0;
        }
    }


    @RequestMapping(method = RequestMethod.POST, value = "/addContry")
    public ContryDTO addContry(HttpServletRequest request, @RequestBody ContryDTO rest) {
        try {
            ContryDTO obj = new ContryDTO();
            Context context = ContextUtil.obtainContext(request);
            if (rest.getContryname() != null) {
                Country c = new Country();
                c.setCountryname(rest.getContryname());
                Country cc = countryService.create(context, c);
                obj.setContryuuid(cc.getID().toString());
                obj.setContryname(cc.getCountryname());
                context.commit();
                return obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/addNoteGroup")
    public ContryDTO addNoteGroup(HttpServletRequest request, @Parameter(value = "eperson", required = true) String eperson) {
        try {
            ContryDTO obj = new ContryDTO();
            Context context = ContextUtil.obtainContext(request);
            if (eperson!=null&&!eperson.isEmpty()) {
                EPerson epersons= ePersonService.find(context,UUID.fromString(eperson));
                if(eperson!=null){
                    try {
                        Group note = groupService.findByName(context, "NOTE");
                        if(note!=null){
                            System.out.println("::::: add in note Group ::::");
                            groupService.addMember(context, note, epersons);
                            groupService.update(context, note);
                            System.out.println("::::: add in note Group :done:::");
                        }
                    }catch (Exception e){
                        System.out.println("error in NOTE group "+e.getMessage());
                    }
                }
                context.commit();
                return obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getPendingsign(Context context, UUID epersonToEpersonMappingid) throws SQLException {
        try {
            UUID statuscloseid = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Workflow Type");
            UUID statusdraftid = null;
            if (workFlowProcessMaster != null) {
                statusdraftid = workFlowProcessMasterValueService.findByName(context, "Draft", workFlowProcessMaster).getID();
            }
            int count = workflowProcessService.getCountWorkflowAfterNoteApproved(context, context.getCurrentUser().getID(), statuscloseid, statusdraftid, statusdraftid, epersonToEpersonMappingid);
            return count;
        } catch (Exception e) {
            System.out.println("error getPendingsign count" + e.getMessage());
            return 0;
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/addVip")
    public VipDTO addVip(HttpServletRequest request, @RequestBody VipDTO rest) {
        try {
            VipDTO obj = new VipDTO();
            Context context = ContextUtil.obtainContext(request);
            if (rest.getVip() != null) {
                Vip c = new Vip();
                c.setVip(rest.getVip());
                Vip cc = vipService.create(context, c);
                obj.setVipuuid(cc.getID().toString());
                obj.setVip(cc.getVip());
                context.commit();
                return obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/addVipName")
    public VipDTO addVipName(HttpServletRequest request, @RequestBody VipDTO rest) {
        try {
            VipDTO obj = new VipDTO();
            Context context = ContextUtil.obtainContext(request);
            if (rest.getVipname() != null && rest.getVipuuid() != null) {
                Vip v = vipService.find(context, UUID.fromString(rest.getVipuuid()));
                VipName c = new VipName();
                c.setVipname(rest.getVipname());
                c.setVip(v);
                VipName cc = vipNameService.create(context, c);
                obj.setVipnameuuid(cc.getID().toString());
                obj.setVipname(cc.getVipname());
                context.commit();
                return obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/addCategory")
    public CategoryDTO addCategory(HttpServletRequest request, @RequestBody CategoryDTO rest) {
        try {
            CategoryDTO obj = new CategoryDTO();
            Context context = ContextUtil.obtainContext(request);
            if (rest.getCategoryname() != null) {
                Category c = new Category();
                c.setCategoryname(rest.getCategoryname());
                Category cc = categoryService.create(context, c);
                obj.setCategoryuuid(cc.getID().toString());
                obj.setCategoryname(cc.getCategoryname());
                context.commit();
                return obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/addSubCategory")
    public CategoryDTO addSubCategory(HttpServletRequest request, @RequestBody CategoryDTO rest) {
        try {
            CategoryDTO obj = new CategoryDTO();
            Context context = ContextUtil.obtainContext(request);
            if (rest.getCategoryuuid() != null && rest.getSubcategoryname() != null) {
                Category v = categoryService.find(context, UUID.fromString(rest.getCategoryuuid()));
                SubCategory c = new SubCategory();
                c.setSubcategoryname(rest.getSubcategoryname());
                c.setCategory(v);
                SubCategory cc = subcategoryService.create(context, c);
                obj.setSubcategoryuuid(cc.getID().toString());
                obj.setSubcategoryname(cc.getSubcategoryname());
                context.commit();
                return obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getAllCategory")
    public List<CategoryDTO> getAllCategory(HttpServletRequest request) {
        try {
            Context context = ContextUtil.obtainContext(request);
            List<Category> list = categoryService.getAll(context);
            List<CategoryDTO> rest = list.stream().map(d -> {
                CategoryDTO contryDTO = new CategoryDTO();
                contryDTO.setCategoryname(d.getCategoryname());
                contryDTO.setCategoryuuid(d.getID().toString());
                return contryDTO;
            }).collect(Collectors.toList());
            return rest;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getDepartmentByOffice")
    public List<UserDepartmentDTO> getDepartmentByOffice(HttpServletRequest request, @Parameter(value = "officeid", required = true) String officeid) {
        try {
            //System.out.println("in getDepartmentByOffice ");
            Context context = ContextUtil.obtainContext(request);
            List<UserDepartmentDTO> rest = new ArrayList<>();

            UUID office = UUID.fromString(officeid);

            List<EpersonMapping> witems = epersonMappingService.findByOffice(context, office);
            if (witems != null) {
                rest = witems.stream()
                        .filter(row -> row.getDepartment() != null)
                        .map(d -> {
                            UserDepartmentDTO contryDTO = new UserDepartmentDTO();
                            contryDTO.setUuid(d.getDepartment().getID().toString());
                            contryDTO.setDepartmentname(d.getDepartment().getPrimaryvalue());
                            return contryDTO;
                        }).collect(Collectors.toList());
            }
            List<UserDepartmentDTO> uniqueList = rest.stream()
                    .filter(distinctByKey(UserDepartmentDTO::getUuid))
                    .sorted(Comparator.comparing(UserDepartmentDTO::getDepartmentname))
                    .collect(Collectors.toList());

            return uniqueList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/isEpersionToEpersionMappingInWorkflow")
    public boolean isEpersionToEpersionMappingInWorkflow(HttpServletRequest request,
                                                         @Parameter(value = "epersiontoepersionmapping", required = true) String epersiontoepersionmapping) {
        try {
            //System.out.println("in getDepartmentByOffice ");
            Context context = ContextUtil.obtainContext(request);
            UUID epersiontoepersionmappings = UUID.fromString(epersiontoepersionmapping);
            return epersonToEpersonMappingService.existsByEpersonToEpersonMappingId(context, epersiontoepersionmappings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getfindDesignationAndDepartmentAndOffice")
    public List<UserDesignationDTO> getfindDesignationAndDepartmentAndOffice(HttpServletRequest request, @Parameter(value = "officeid", required = true) String officeid,
                                                                             @Parameter(value = "department", required = true) String department) {
        try {
            //System.out.println("in getDepartmentByOffice ");
            Context context = ContextUtil.obtainContext(request);
            List<UserDesignationDTO> rest = new ArrayList<>();

            UUID office = UUID.fromString(officeid);
            UUID departments = UUID.fromString(department);
            List<EpersonMapping> witems = epersonMappingService.findOfficeAndDepartment(context, office, departments);
            if (witems != null) {
                rest = witems.stream()
                        .filter(row -> row.getDesignation() != null)
                        .map(d -> {
                            UserDesignationDTO contryDTO = new UserDesignationDTO();
                            contryDTO.setUuid(d.getDesignation().getID().toString());
                            contryDTO.setDesignation(d.getDesignation().getPrimaryvalue());
                            return contryDTO;
                        }).collect(Collectors.toList());
            }

            List<UserDesignationDTO> uniqueList = rest.stream()
                    .filter(distinctByKey(UserDesignationDTO::getUuid))
                    .sorted(Comparator.comparing(UserDesignationDTO::getDesignation))
                    .collect(Collectors.toList());

            return uniqueList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
    @RequestMapping(method = RequestMethod.GET, value = "/getEpersonByDepartmentAndOfficeAndDesignation")
    public List<UsersDTO> getEpersonByDepartmentAndOfficeAndDesignation(HttpServletRequest request
            , @Parameter(value = "officeid", required = false) String officeid
            , @Parameter(value = "departmentid", required = false) String departmentid
            , @Parameter(value = "designation", required = false) String designation
            , @Parameter(value = "isdisable", required = false) Boolean isdisable) {
        List<UsersDTO> usersList = new ArrayList<>();
        List<UsersDTO> uniqueList = null;
        try {
            // Obtain context
            Context context = ContextUtil.obtainContext(request);
            UUID office = UUID.fromString(officeid);
            UUID departments = UUID.fromString(departmentid);
            UUID designationids = UUID.fromString(designation);
            List<EpersonToEpersonMapping> witems = epersonToEpersonMappingService.findByofficeandDepartmentanddesignation(context, office, departments, designationids);
            if (isdisable) {
                if (witems != null) {
                    usersList = witems.stream()
                            .filter(row -> row.getEperson() != null)
                            .filter(row -> !row.getEperson().getID().toString().equalsIgnoreCase(context.getCurrentUser().getID().toString()))
                            .filter(d -> d.getID() != null)
                            .map(d -> {
                                UsersDTO userdto = new UsersDTO();
                                userdto.setUuid(d.getEperson().getID().toString());
                                userdto.setDesignation(d.getEpersonmapping().getDesignation().getPrimaryvalue());
                                userdto.setOfficename(d.getEpersonmapping().getOffice().getPrimaryvalue());
                                userdto.setDepartmentname(d.getEpersonmapping().getDepartment().getPrimaryvalue());
                                userdto.setFullname(d.getEperson().getFullName());
                                userdto.setEpersontoepersonmapping(d.getID().toString());
                                if (d.getEpersonmapping().getTablenumber() != null) {
                                    userdto.setDeskno(d.getEpersonmapping().getTablenumber());
                                }
                                return userdto;
                            }).collect(Collectors.toList());

                }
            } else {
                if (witems != null) {
                    usersList = witems.stream()
                            .filter(row -> row.getEperson() != null)
                            .filter(d -> d.getID() != null)
                            .map(d -> {
                                UsersDTO userdto = new UsersDTO();
                                userdto.setUuid(d.getEperson().getID().toString());
                                userdto.setDesignation(d.getEpersonmapping().getDesignation().getPrimaryvalue());
                                userdto.setOfficename(d.getEpersonmapping().getOffice().getPrimaryvalue());
                                userdto.setDepartmentname(d.getEpersonmapping().getDepartment().getPrimaryvalue());
                                userdto.setFullname(d.getEperson().getFullName());
                                userdto.setEpersontoepersonmapping(d.getID().toString());
                                if (d.getEpersonmapping().getTablenumber() != null) {
                                    userdto.setDeskno(d.getEpersonmapping().getTablenumber());
                                }
                                return userdto;
                            }).collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error processing request: " + e.getMessage());
        }

        if (usersList != null) {
            uniqueList = usersList.stream()
                    .filter(distinctByKey(UsersDTO::getUuid))
                    .sorted(Comparator.comparing(UsersDTO::getFullname))
                    .collect(Collectors.toList());
        }
        return uniqueList;
    }

    private String generateFullName(Context context, EPerson eperson) {
        String fullName = eperson.getFullName();

        if (eperson.getDesignation() != null) {
            try {
                WorkFlowProcessMasterValue designation = workFlowProcessMasterValueService.find(
                        context, eperson.getDesignation().getID()
                );
                if (designation != null && designation.getPrimaryvalue() != null) {
                    return fullName + " / " + designation.getPrimaryvalue();
                }
            } catch (Exception e) {
                // System.err.println("Error fetching designation with"+eperson.getID()+" and name is  " + eperson.getFullName());
                System.err.println("Error fetching designation: " + e.getMessage());
            }
        }
        return fullName + ".";
    }


    @RequestMapping(method = RequestMethod.GET, value = "/getSubCategoryByCategoryID")
    public List<CategoryDTO> getSubCategoryByCategoryID(HttpServletRequest request, @Parameter(value = "categoryid", required = true) String categoryid) {
        try {
            Context context = ContextUtil.obtainContext(request);
            List<SubCategory> list = subcategoryService.getByCountryId(context, UUID.fromString(categoryid));
            List<CategoryDTO> rest = list.stream().map(d -> {
                CategoryDTO contryDTO = new CategoryDTO();
                contryDTO.setCategoryname(d.getCategory().getCategoryname());
                contryDTO.setSubcategoryname(d.getSubcategoryname());
                contryDTO.setCategoryuuid(d.getCategory().getID().toString());
                contryDTO.setSubcategoryuuid(d.getID().toString());
                return contryDTO;
            }).collect(Collectors.toList());
            return rest;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getAllVip")
    public List<VipDTO> getAllVip(HttpServletRequest request) {
        try {
            Context context = ContextUtil.obtainContext(request);
            List<Vip> list = vipService.getAll(context);
            List<VipDTO> rest = list.stream().map(d -> {
                VipDTO contryDTO = new VipDTO();
                contryDTO.setVip(d.getVip());
                contryDTO.setVipuuid(d.getID().toString());
                return contryDTO;
            }).collect(Collectors.toList());
            return rest;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getVipNameByVipid")
    public List<VipDTO> getVipNameByVipid(HttpServletRequest request, @Parameter(value = "vipid", required = true) String vipid) {
        try {
            Context context = ContextUtil.obtainContext(request);
            List<VipName> list = vipNameService.getByCountryId(context, UUID.fromString(vipid));
            List<VipDTO> rest = list.stream().map(d -> {
                VipDTO contryDTO = new VipDTO();
                contryDTO.setVipname(d.getVipname());
                contryDTO.setVipnameuuid(d.getID().toString());
                contryDTO.setVip(d.getVip().getVip());
                contryDTO.setVipuuid(d.getVip().getID().toString());
                return contryDTO;
            }).collect(Collectors.toList());
            return rest;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/addState")
    public ContryDTO addState(HttpServletRequest request, @RequestBody ContryDTO rest) {
        try {
            ContryDTO obj = new ContryDTO();
            Context context = ContextUtil.obtainContext(request);
            if (rest.getStatename() != null && rest.getContryuuid() != null) {
                UUID uuid = UUID.fromString(rest.getContryuuid());
                Country country = countryService.find(context, uuid);
                if (country != null) {
                    State state = new State();
                    state.setStatename(rest.getStatename());
                    state.setCountry(country);
                    State state1 = stateService.create(context, state);
                    obj.setStateuuid(state1.getID().toString());
                    obj.setStatename(state1.getStatename());
                    context.commit();
                }
                context.commit();
                return obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/addCity")
    public ContryDTO addCity(HttpServletRequest request, @RequestBody ContryDTO rest) {
        try {
            ContryDTO obj = new ContryDTO();
            Context context = ContextUtil.obtainContext(request);
            if (rest.getCityname() != null && rest.getStateuuid() != null) {
                UUID uuid = UUID.fromString(rest.getStateuuid());
                State state = stateService.find(context, uuid);
                if (state != null) {
                    City city = new City();
                    city.setCityname(rest.getCityname());
                    city.setState(state);
                    City city1 = cityService.create(context, city);
                    obj.setCityuuid(city1.getID().toString());
                    obj.setCityname(city1.getCityname());
                    context.commit();
                    return obj;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/startJBPM")
    public ResponseEntity<String> startJBPM(@RequestBody JBPMProcess request) {
        try {
            String response = jbpmServer.startProcess1(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error occurred while starting JBPM process: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @RequestMapping(method = RequestMethod.POST, value = "/forwardJBPM")
    public ResponseEntity<String> forwardJBPM(@RequestBody JBPMProcess request) {
        try {
            String response = jbpmServer.forwardTask1(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error occurred while starting JBPM process: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getAllCountries")
    public List<ContryDTO> getContryAll(HttpServletRequest request) {
        try {
            Context context = ContextUtil.obtainContext(request);
            List<Country> list = countryService.getAll(context);
            List<ContryDTO> rest = list.stream().map(d -> {
                ContryDTO contryDTO = new ContryDTO();
                contryDTO.setContryname(d.getCountryname());
                contryDTO.setContryuuid(d.getID().toString());
                return contryDTO;
            }).collect(Collectors.toList());
            return rest;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getStateByContryid")
    public List<ContryDTO> getStateByContryid(HttpServletRequest request, @Parameter(value = "countyid", required = true) String countyid) {
        try {
            Context context = ContextUtil.obtainContext(request);
            List<State> list = stateService.getByCountryId(context, UUID.fromString(countyid));
            List<ContryDTO> rest = list.stream().map(d -> {
                ContryDTO contryDTO = new ContryDTO();
                contryDTO.setStatename(d.getStatename());
                contryDTO.setStateuuid(d.getID().toString());
                contryDTO.setContryname(d.getCountry().getCountryname());
                contryDTO.setContryuuid(d.getCountry().getID().toString());
                return contryDTO;
            }).collect(Collectors.toList());
            return rest;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getCityByStateid")
    public List<ContryDTO> getCityByStateid(HttpServletRequest request, @Parameter(value = "stateid", required = true) String stateid) {
        try {
            Context context = ContextUtil.obtainContext(request);
            List<City> list = cityService.getCityByStateid(context, UUID.fromString(stateid));
            List<ContryDTO> rest = list.stream().map(d -> {
                ContryDTO contryDTO = new ContryDTO();
                contryDTO.setCityname(d.getCityname());
                contryDTO.setCityuuid(d.getID().toString());
                contryDTO.setStatename(d.getState().getStatename());
                contryDTO.setStateuuid(d.getState().getID().toString());
                contryDTO.setContryname(d.getState().getCountry().getCountryname());
                contryDTO.setContryuuid(d.getState().getCountry().getID().toString());
                return contryDTO;
            }).collect(Collectors.toList());
            return rest;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/citySearchByState")
    public List<ContryDTO> citySearchByState(HttpServletRequest request, @Parameter(value = "stateid", required = true) String stateid, @Parameter(value = "searchcity", required = true) String searchcity) {
        try {
            Context context = ContextUtil.obtainContext(request);
            List<City> list = cityService.getCityByStateid(context, UUID.fromString(stateid), searchcity);
            List<ContryDTO> rest = list.stream().map(d -> {
                ContryDTO contryDTO = new ContryDTO();
                contryDTO.setCityname(d.getCityname());
                contryDTO.setCityuuid(d.getID().toString());
                contryDTO.setStatename(d.getState().getStatename());
                contryDTO.setStateuuid(d.getState().getID().toString());
                contryDTO.setContryname(d.getState().getCountry().getCountryname());
                contryDTO.setContryuuid(d.getState().getCountry().getID().toString());
                return contryDTO;
            }).collect(Collectors.toList());
            return rest;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
