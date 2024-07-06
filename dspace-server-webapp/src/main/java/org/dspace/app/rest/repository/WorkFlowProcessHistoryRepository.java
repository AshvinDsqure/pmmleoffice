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
import org.dspace.app.rest.converter.WorkFlowProcessHistoryConverter;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.WorkFlowProcessHistoryRest;
import org.dspace.app.rest.model.WorkFlowProcessRest;
import org.dspace.app.rest.model.WorkspaceItemRest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.WorkFlowProcessComment;
import org.dspace.content.WorkFlowProcessHistory;
import org.dspace.content.service.WorkFlowProcessHistoryService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Component(WorkFlowProcessHistoryRest.CATEGORY + "." + WorkFlowProcessHistoryRest.NAME)

public class WorkFlowProcessHistoryRepository extends DSpaceObjectRestRepository<WorkFlowProcessHistory, WorkFlowProcessHistoryRest> {


    private static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(WorkFlowProcessHistoryRepository.class);
    @Autowired
    WorkFlowProcessHistoryService workFlowProcessHistoryService;

    @Autowired
    WorkFlowProcessHistoryConverter workFlowProcessHistoryConverter;

    public WorkFlowProcessHistoryRepository(WorkFlowProcessHistoryService dsoService) {
        super(dsoService);
    }

    @Override
    protected WorkFlowProcessHistoryRest createAndReturn(Context context)
            throws AuthorizeException {
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        WorkFlowProcessHistoryRest workFlowProcessHistoryRest = null;
        WorkFlowProcessHistory workFlowProcessHistory = null;
        try {
            workFlowProcessHistoryRest = mapper.readValue(req.getInputStream(), WorkFlowProcessHistoryRest.class);
            workFlowProcessHistory = createWorkFlowProcessHistoryFromRestObject(context, workFlowProcessHistoryRest);

        } catch (Exception e1) {
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }

        return converter.toRest(workFlowProcessHistory, utils.obtainProjection());
    }

    private WorkFlowProcessHistory createWorkFlowProcessHistoryFromRestObject(Context context, WorkFlowProcessHistoryRest workFlowProcessHistoryRest) throws AuthorizeException {
        WorkFlowProcessHistory workFlowProcessHistory = new WorkFlowProcessHistory();
        try {
            workFlowProcessHistory = workFlowProcessHistoryConverter.convert(context, workFlowProcessHistoryRest);
            workFlowProcessHistoryService.create(context, workFlowProcessHistory);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return workFlowProcessHistory;
    }

    @Override
    protected WorkFlowProcessHistoryRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                             JsonNode jsonNode) throws SQLException, AuthorizeException {
        log.info("::::::start::::put::::::::::");
        WorkFlowProcessHistoryRest workFlowProcessHistoryRest = new Gson().fromJson(jsonNode.toString(), WorkFlowProcessHistoryRest.class);
        WorkFlowProcessHistory workFlowProcessHistory = workFlowProcessHistoryService.find(context, id);
        if (workFlowProcessHistory == null) {
            System.out.println("workFlowProcessHistoryrest id ::: is Null  workFlowProcessHistoryrest tye null" + id);
            throw new ResourceNotFoundException("workFlowProcessHistoryrest  field with id: " + id + " not found");
        }
        workFlowProcessHistory = workFlowProcessHistoryConverter.convert(context, workFlowProcessHistoryRest);
        workFlowProcessHistoryService.update(context, workFlowProcessHistory);
        context.commit();
        return converter.toRest(workFlowProcessHistory, utils.obtainProjection());
    }
    @Override
    public WorkFlowProcessHistoryRest findOne(Context context, UUID uuid) {
        WorkFlowProcessHistoryRest workFlowProcessHistoryRest = null;
        try {
            Optional<WorkFlowProcessHistory> workFlowProcessHistory = Optional.ofNullable(workFlowProcessHistoryService.find(context, uuid));
            if (workFlowProcessHistory.isPresent()) {
                workFlowProcessHistoryRest = converter.toRest(workFlowProcessHistory.get(), utils.obtainProjection());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workFlowProcessHistoryRest;
    }
    @Override
    public Page<WorkFlowProcessHistoryRest> findAll(Context context, Pageable pageable) throws SQLException {
        int total = workFlowProcessHistoryService.countRows(context);
        List<WorkFlowProcessHistory> workFlowProcessHistories = workFlowProcessHistoryService.findAll(context,
                Math.toIntExact(pageable.getPageSize()), Math.toIntExact(pageable.getOffset()));
        return converter.toRestPage(workFlowProcessHistories, pageable, total, utils.obtainProjection());

    }
    protected void delete(Context context, UUID id) throws AuthorizeException {
        WorkFlowProcessHistory workFlowProcessHistory = null;
        try {
            workFlowProcessHistory = workFlowProcessHistoryService.find(context, id);
            if (workFlowProcessHistory == null) {
                throw new ResourceNotFoundException(WorkFlowProcessHistoryRest.CATEGORY + "." + WorkFlowProcessHistoryRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            workFlowProcessHistoryService.delete(context, workFlowProcessHistory);
            context.commit();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    @SearchRestMethod(name = "getHistory")
    public Page<WorkFlowProcessHistoryRest> getHistory(@Parameter(value = "workflowprocessid", required = true) UUID workflowprocessid, Pageable pageable) {
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            List<WorkFlowProcessHistoryRest> workflowsRes = new ArrayList<WorkFlowProcessHistoryRest>();
            long total = workFlowProcessHistoryService.countHistory(context, workflowprocessid);
            List<WorkFlowProcessHistory> witems = workFlowProcessHistoryService.getHistory(context, workflowprocessid);
            workflowsRes = witems.stream().map(d -> {
                return workFlowProcessHistoryConverter.convert(d, utils.obtainProjection());
            }).collect(toList());
            log.info("in getDraftNotePendingWorkflow stop!");
            return new PageImpl(workflowsRes, pageable, total);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Class<WorkFlowProcessHistoryRest> getDomainClass() {
        return null;
    }
}
