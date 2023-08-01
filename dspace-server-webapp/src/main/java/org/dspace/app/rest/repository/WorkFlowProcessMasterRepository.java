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
import org.dspace.app.rest.converter.WorkFlowProcessMasterConverter;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.WorkFlowProcessMasterRest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkFlowProcessMaster;
import org.dspace.content.service.WorkFlowProcessMasterService;
import org.dspace.content.service.WorkflowProcessDefinitionService;
import org.dspace.content.service.WorkflowProcessService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is the repository responsible to manage Item Rest object
 *
 * @author ashvinmajethiya
 */
@Component(WorkFlowProcessMasterRest.CATEGORY + "." + WorkFlowProcessMasterRest.NAME)
public class WorkFlowProcessMasterRepository extends DSpaceObjectRestRepository<WorkFlowProcessMaster, WorkFlowProcessMasterRest> {

    @Autowired
    private WorkFlowProcessMasterService workFlowProcessMasterService;

    @Autowired
    WorkFlowProcessMasterConverter workFlowProcessMasterConverter;


    public WorkFlowProcessMasterRepository(WorkFlowProcessMasterService dso) {
        super(dso);
    }

    @Override
    protected WorkFlowProcessMasterRest createAndReturn(Context context)
            throws AuthorizeException {
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        WorkFlowProcessMasterRest WorkFlowProcessMasterRest = null;
        WorkFlowProcessMaster workFlowProcessMaster = null;
        try {
            WorkFlowProcessMasterRest = mapper.readValue(req.getInputStream(), WorkFlowProcessMasterRest.class);
            workFlowProcessMaster = createWorkFlowProcessMasterFromRestObject(context, WorkFlowProcessMasterRest);

        } catch (Exception e1) {
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        return converter.toRest(workFlowProcessMaster, utils.obtainProjection());
    }


    private WorkFlowProcessMaster createWorkFlowProcessMasterFromRestObject(Context context, WorkFlowProcessMasterRest workFlowProcessMasterRest) throws AuthorizeException {
        WorkFlowProcessMaster workFlowProcessMaster = new WorkFlowProcessMaster();
        try {
            workFlowProcessMaster.setMastername(workFlowProcessMasterRest.getMastername());
            workFlowProcessMasterService.create(context, workFlowProcessMaster);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return workFlowProcessMaster;
    }

    @Override
    protected WorkFlowProcessMasterRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                            JsonNode jsonNode) throws SQLException, AuthorizeException {
        WorkFlowProcessMasterRest workFlowProcessMasterRest = new Gson().fromJson(jsonNode.toString(), WorkFlowProcessMasterRest.class);

        WorkFlowProcessMaster wflowProcessMaster = workFlowProcessMasterService.find(context, id);
        if (wflowProcessMaster == null) {
            System.out.println("WorkFlowProcessMasterRest id ::: is Null  WorkFlowProcessMasterRest tye null" + id);
            throw new ResourceNotFoundException("WorkFlowProcessMasterRest  field with id: " + id + " not found");
        }
        wflowProcessMaster.setMastername(workFlowProcessMasterRest.getMastername());
        workFlowProcessMasterService.update(context, wflowProcessMaster);
        context.commit();
        return converter.toRest(wflowProcessMaster, utils.obtainProjection());
    }


    @Override
    public WorkFlowProcessMasterRest findOne(Context context, UUID uuid) {
        WorkFlowProcessMasterRest workFlowProcessMasterRest = null;
        try {
            Optional<WorkFlowProcessMaster> workFlowProcessMaster = Optional.ofNullable(workFlowProcessMasterService.find(context, uuid));
            if (workFlowProcessMaster.isPresent()) {
                workFlowProcessMasterRest = converter.toRest(workFlowProcessMaster.get(), utils.obtainProjection());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workFlowProcessMasterRest;
    }

    @Override
    public Page<WorkFlowProcessMasterRest> findAll(Context context, Pageable pageable) throws SQLException {
        int total = workFlowProcessMasterService.countRows(context);
        List<WorkFlowProcessMaster> list = new ArrayList<>();
        List<String> dontshowmasterlist = Arrays.asList("Workflow Status", "Workflow Type", "Action", "Workflow User Type","Draft Type");
        List<WorkFlowProcessMaster> workFlowProcessMasters = workFlowProcessMasterService.findAll(context);
        list = workFlowProcessMasters.stream().filter(d -> !dontshowmasterlist.contains(d.getMastername())).collect(Collectors.toList());
        return converter.toRestPage(list, pageable, total, utils.obtainProjection());
    }

    protected void delete(Context context, UUID id) throws AuthorizeException {

        WorkFlowProcessMaster wflowProcessMaster = null;
        try {
            wflowProcessMaster = workFlowProcessMasterService.find(context, id);
            if (wflowProcessMaster == null) {
                throw new ResourceNotFoundException(WorkFlowProcessMasterRest.CATEGORY + "." + WorkFlowProcessMasterRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            workFlowProcessMasterService.delete(context, wflowProcessMaster);
            context.commit();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Class<WorkFlowProcessMasterRest> getDomainClass() {
        return null;
    }
}
