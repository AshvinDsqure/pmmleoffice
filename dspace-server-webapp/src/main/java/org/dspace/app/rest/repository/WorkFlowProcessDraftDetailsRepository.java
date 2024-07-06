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
import org.dspace.app.rest.converter.WorkFlowProcessDraftDetailsConverter;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.WorkFlowProcessDraftDetailsRest;
import org.dspace.app.rest.model.WorkFlowProcessRest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkFlowProcessDraftDetails;
import org.dspace.content.WorkflowProcess;
import org.dspace.content.service.WorkFlowProcessDraftDetailsService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component(WorkFlowProcessDraftDetailsRest.CATEGORY + "." + WorkFlowProcessDraftDetailsRest.NAME)

public class WorkFlowProcessDraftDetailsRepository extends DSpaceObjectRestRepository<WorkFlowProcessDraftDetails, WorkFlowProcessDraftDetailsRest> {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkFlowProcessDraftDetailsRepository.class);
    @Autowired
    WorkFlowProcessDraftDetailsService workFlowProcessDraftDetailsService;
    @Autowired
    WorkFlowProcessDraftDetailsConverter workFlowProcessDraftDetailsConverter;

    public WorkFlowProcessDraftDetailsRepository(WorkFlowProcessDraftDetailsService dsoService) {
        super(dsoService);
    }

    @Override
    protected WorkFlowProcessDraftDetailsRest createAndReturn(Context context)
            throws AuthorizeException {
        log.info("::::::start::::createAndReturn::::::::::");
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        WorkFlowProcessDraftDetailsRest workFlowProcessDraftDetailsRest = null;
        WorkFlowProcessDraftDetails workFlowProcessDraftDetails = null;
        try {
            workFlowProcessDraftDetailsRest = mapper.readValue(req.getInputStream(), WorkFlowProcessDraftDetailsRest.class);
            workFlowProcessDraftDetails = createWorkFlowProcessDraftDetailsFromRestObject(context, workFlowProcessDraftDetailsRest);
        } catch (Exception e1) {
            log.info("::::::error::::createAndReturn::::::::::");
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        log.info("::::::complate::::createAndReturn::::::::::");
        return converter.toRest(workFlowProcessDraftDetails, utils.obtainProjection());
    }

    private WorkFlowProcessDraftDetails createWorkFlowProcessDraftDetailsFromRestObject(Context context, WorkFlowProcessDraftDetailsRest workFlowProcessDraftDetailsRest) throws AuthorizeException {
        log.info("::::::start::::createWorkFlowProcessDraftDetailsFromRestObject::::::::::");
        WorkFlowProcessDraftDetails workFlowProcessDraftDetails = new WorkFlowProcessDraftDetails();
        try {
            workFlowProcessDraftDetails = workFlowProcessDraftDetailsConverter.convert(context, workFlowProcessDraftDetailsRest);
            workFlowProcessDraftDetailsService.create(context, workFlowProcessDraftDetails);
        } catch (Exception e) {
            log.info("::::::error::::createWorkFlowProcessDraftDetailsFromRestObject::::::::::");
            throw new RuntimeException(e.getMessage(), e);
        }
        log.info("::::::complate::::createWorkFlowProcessDraftDetailsFromRestObject::::::::::");
        return workFlowProcessDraftDetails;
    }

    @Override
    protected WorkFlowProcessDraftDetailsRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                                  JsonNode jsonNode) throws SQLException, AuthorizeException {
        log.info("::::::start::::put::::::::::");
        ObjectMapper mapper = new ObjectMapper();
        WorkFlowProcessDraftDetailsRest workFlowProcessDraftDetailsRest = new Gson().fromJson(jsonNode.toString(), WorkFlowProcessDraftDetailsRest.class);
        WorkFlowProcessDraftDetails workFlowProcessDraftDetails = workFlowProcessDraftDetailsService.find(context, id);
        if (workFlowProcessDraftDetails == null) {
            System.out.println("workFlowProcessDraftDetails id ::: is Null  workFlowProcessDraftDetails tye null" + id);
            throw new ResourceNotFoundException("workFlowProcessDraftDetails  field with id: " + id + " not found");
        }
        workFlowProcessDraftDetails = workFlowProcessDraftDetailsConverter.convert(context, workFlowProcessDraftDetailsRest);
        workFlowProcessDraftDetailsService.update(context, workFlowProcessDraftDetails);
        context.commit();
        log.info("::::::End::::put::::::::::");
        return converter.toRest(workFlowProcessDraftDetails, utils.obtainProjection());
    }

    @Override
    public WorkFlowProcessDraftDetailsRest findOne(Context context, UUID uuid) {
      context.turnOffAuthorisationSystem();
        WorkFlowProcessDraftDetailsRest workFlowProcessDraftDetailsRest = null;
        log.info("::::::start::::findOne::::::::::");
        try {
            Optional<WorkFlowProcessDraftDetails> workFlowProcessDraftDetails = Optional.ofNullable(workFlowProcessDraftDetailsService.find(context, uuid));
            if (workFlowProcessDraftDetails.isPresent()) {
                workFlowProcessDraftDetailsRest = converter.toRest(workFlowProcessDraftDetails.get(), utils.obtainProjection());
            }
        } catch (Exception e) {
            log.info("::::::error::::findOne::::::::::");
            e.printStackTrace();
        }
        log.info("::::::End::::findOne::::::::::");
        return workFlowProcessDraftDetailsRest;
    }

    @Override
    public Page<WorkFlowProcessDraftDetailsRest> findAll(Context context, Pageable pageable) throws SQLException {
        int total = workFlowProcessDraftDetailsService.countRows(context);
        List<WorkFlowProcessDraftDetails> workFlowProcessDraftDetails = workFlowProcessDraftDetailsService.findAll(context,
                Math.toIntExact(pageable.getPageSize()), Math.toIntExact(pageable.getOffset()));
        return converter.toRestPage(workFlowProcessDraftDetails, pageable, total, utils.obtainProjection());
    }

    protected void delete(Context context, UUID id) throws AuthorizeException {
        log.info("::::::in::::delete::::::::::");
        context.turnOffAuthorisationSystem();
        WorkFlowProcessDraftDetails workFlowProcessDraftDetails = null;
        try {
            workFlowProcessDraftDetails = workFlowProcessDraftDetailsService.find(context, id);
            if (workFlowProcessDraftDetails == null) {
                log.info("::::::id not found::::delete::::::::::");
                throw new ResourceNotFoundException(WorkFlowProcessDraftDetailsRest.CATEGORY + "." + WorkFlowProcessDraftDetailsRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            workFlowProcessDraftDetailsService.delete(context, workFlowProcessDraftDetails);
            context.commit();
            log.info(":::::completed:::delete::::::::::");
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    @SearchRestMethod(name = "discardDraft")
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public WorkFlowProcessDraftDetailsRest discardDraft(@Parameter(value = "draftid", required = true) UUID draftid) {
        WorkFlowProcessDraftDetailsRest workFlowProcessDraftDetailsRest = null;
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            WorkFlowProcessDraftDetails workFlowProcessDraftDetails = workFlowProcessDraftDetailsService.find(context, draftid);
            if (workFlowProcessDraftDetails != null) {
                workFlowProcessDraftDetails.setIsdelete(true);
                workFlowProcessDraftDetailsService.update(context, workFlowProcessDraftDetails);
            }
            workFlowProcessDraftDetailsRest = workFlowProcessDraftDetailsConverter.convert(workFlowProcessDraftDetails, utils.obtainProjection());
            context.commit();
            return workFlowProcessDraftDetailsRest;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        } catch (AuthorizeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<WorkFlowProcessDraftDetailsRest> getDomainClass() {
        return null;
    }
}
