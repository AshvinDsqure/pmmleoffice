/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.ConverterService;
import org.dspace.app.rest.converter.EPersonConverter;
import org.dspace.app.rest.converter.WorkFlowProcessConverter;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.enums.WorkFlowType;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.model.hateoas.BitstreamResource;
import org.dspace.app.rest.repository.AbstractDSpaceRestRepository;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.ExcelHelper;
import org.dspace.app.rest.utils.Utils;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
@RequestMapping("/api/" + WorkFlowProcessRest.CATEGORY + "/" + WorkFlowProcessRest.PLURAL_NAME + "/filter")
public class WorkflowProcessFilterController {

    @Autowired
    WorkflowProcessService workflowProcessService;
    @Autowired
    WorkFlowProcessMasterService workFlowProcessMasterService;
    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterValueService;
    @Autowired
    WorkFlowProcessOutwardDetailsService workFlowProcessOutwardDetailsService;

    @Autowired
    WorkFlowProcessInwardDetailsService workFlowProcessInwardDetailsService;

    @Autowired
    EPersonConverter ePersonConverter;
    @Autowired
    EPersonService ePersonService;
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
    public List<WorkFlowProcessRest> filter(HttpServletRequest request, @RequestBody WorkFlowProcessFilterRest rest) {
        try {
            Context context = ContextUtil.obtainContext(request);
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
              WorkFlowProcessInwardDetails inward=workFlowProcessInwardDetailsService.getByInwardNumber(context,rest.getInward());
              if(inward!=null) {
                  map.put("inward",inward.getID().toString());
              }
            }
            if (rest.getOutward() != null) {
                WorkFlowProcessOutwardDetails outward=workFlowProcessOutwardDetailsService.getByOutwardNumber(context,rest.getOutward());
                if(outward!=null) {
                    map.put("outward", outward.getID().toString());
                }
            }
            System.out.println(map);
            List<WorkflowProcess> list = workflowProcessService.Filter(context, map, 0, 199);
            List<WorkFlowProcessRest> rests = list.stream().map(d -> {
                return workFlowProcessConverter.convert(d, utils.obtainProjection());
            }).collect(Collectors.toList());
            return rests;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getFilterPerameter")
    public HashMap<String, String> getFilterdate(HttpServletRequest request) {
        try {
            System.out.println("in getFilterPerameter ");
            Context context = ContextUtil.obtainContext(request);
            HashMap<String, String> map = new HashMap<>();
            map.put("Priority", "dropdown");
            map.put("Status", "dropdown");
            map.put("Department", "dropdown");
            map.put("Subject", "text");
            map.put(WorkFlowType.INWARD.getAction()+" Number", "text");
            map.put(WorkFlowType.OUTWARED.getAction()+" Number", "text");
            System.out.println("out getFilterPerameter ");
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getCountsDashboard")
    public HashMap<String, HashMap<String, Integer>> getCountsDashboard(HttpServletRequest request) {
        try {
            System.out.println("in getCountsDashboard ");
            Context context = ContextUtil.obtainContext(request);
            UUID Lowid = null;
            UUID Mediumid = null;
            UUID Highid = null;
            UUID inwardid = null;
            UUID outwardid = null;
            UUID draftid = null;
            WorkFlowProcessMaster PriorityMaster = workFlowProcessMasterService.findByName(context, "Priority");
            if (PriorityMaster != null) {
                Lowid = workFlowProcessMasterValueService.findByName(context, "Low", PriorityMaster).getID();
                Mediumid = workFlowProcessMasterValueService.findByName(context, "Medium", PriorityMaster).getID();
                Highid = workFlowProcessMasterValueService.findByName(context, "High", PriorityMaster).getID();
            }
            inwardid = WorkFlowType.INWARD.getUserTypeFromMasterValue(context).get().getID();
            draftid = WorkFlowType.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            outwardid = WorkFlowType.OUTWARED.getUserTypeFromMasterValue(context).get().getID();

            UUID tSuspendid = WorkFlowStatus.SUSPEND.getUserTypeFromMasterValue(context).get().getID();
            UUID tCloseid = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            UUID tInProgressid = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context).get().getID();
            UUID tReferid = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context).get().getID();
            //inward Map
            HashMap<String, Integer> mapInward = new HashMap<>();
            mapInward.put("Medium", workflowProcessService.countByTypeAndPriority(context,inwardid,Mediumid));
            mapInward.put("Low", workflowProcessService.countByTypeAndPriority(context,inwardid,Lowid));
            mapInward.put("High", workflowProcessService.countByTypeAndPriority(context,inwardid,Highid));
            mapInward.put("Suspend", workflowProcessService.countByTypeAndPriority(context,inwardid,tSuspendid));
            mapInward.put("Close", workflowProcessService.countByTypeAndPriority(context,inwardid,tCloseid));
            mapInward.put("In Progress", workflowProcessService.countByTypeAndPriority(context,inwardid,tInProgressid));
            mapInward.put("Refer", workflowProcessService.countByTypeAndPriority(context,inwardid,tReferid));

            //Outward Map
            HashMap<String, Integer> mapOutward = new HashMap<>();
            mapOutward.put("Medium", workflowProcessService.countByTypeAndPriority(context,outwardid,Mediumid));
            mapOutward.put("Low", workflowProcessService.countByTypeAndPriority(context,outwardid,Lowid));
            mapOutward.put("High", workflowProcessService.countByTypeAndPriority(context,outwardid,Highid));
            mapOutward.put("Suspend", workflowProcessService.countByTypeAndPriority(context,outwardid,tSuspendid));
            mapOutward.put("Close", workflowProcessService.countByTypeAndPriority(context,outwardid,tCloseid));
            mapOutward.put("In Progress", workflowProcessService.countByTypeAndPriority(context,outwardid,tInProgressid));
            mapOutward.put("Refer", workflowProcessService.countByTypeAndPriority(context,outwardid,tReferid));

            //Draft Map
            HashMap<String, Integer> mapDraft = new HashMap<>();
            mapDraft.put("Medium", workflowProcessService.countByTypeAndPriority(context,draftid,Mediumid));
            mapDraft.put("Low", workflowProcessService.countByTypeAndPriority(context,draftid,Lowid));
            mapDraft.put("High", workflowProcessService.countByTypeAndPriority(context,draftid,Highid));
            mapDraft.put("Suspend", workflowProcessService.countByTypeAndPriority(context,draftid,tSuspendid));
            mapDraft.put("Close", workflowProcessService.countByTypeAndPriority(context,draftid,tCloseid));
            mapDraft.put("In Progress", workflowProcessService.countByTypeAndPriority(context,draftid,tInProgressid));
            mapDraft.put("Refer", workflowProcessService.countByTypeAndPriority(context,draftid,tReferid));

            HashMap<String, HashMap<String, Integer>> maps = new HashMap<>();
            maps.put(WorkFlowType.INWARD.getAction(), mapInward);
            maps.put(WorkFlowType.OUTWARED.getAction(), mapOutward);
            maps.put(WorkFlowType.DRAFT.getAction(), mapDraft);

            System.out.println("out getCountsDashbord ");
            return maps;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
