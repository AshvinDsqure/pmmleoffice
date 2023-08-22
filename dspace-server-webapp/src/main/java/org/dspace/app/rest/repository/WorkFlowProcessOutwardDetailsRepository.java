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
import org.dspace.app.rest.converter.WorkFlowProcessOutwardDetailsConverter;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.WorkFlowProcessOutwardDetailsRest;
import org.dspace.app.rest.model.WorkflowProcessSenderDiaryRest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkFlowProcessOutwardDetails;
import org.dspace.content.WorkflowProcessSenderDiary;
import org.dspace.content.service.WorkFlowProcessOutwardDetailsService;
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

@Component(WorkFlowProcessOutwardDetailsRest.CATEGORY + "." + WorkFlowProcessOutwardDetailsRest.NAME)

public class WorkFlowProcessOutwardDetailsRepository extends DSpaceObjectRestRepository<WorkFlowProcessOutwardDetails, WorkFlowProcessOutwardDetailsRest> {


    private static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(WorkFlowProcessOutwardDetailsRepository.class);
    @Autowired
    WorkFlowProcessOutwardDetailsService workFlowProcessOutwardDetailsService;

    @Autowired
    WorkFlowProcessOutwardDetailsConverter workFlowProcessOutwardDetailsConverter;

    public WorkFlowProcessOutwardDetailsRepository(WorkFlowProcessOutwardDetailsService dsoService) {
        super(dsoService);
    }

    @Override
    protected WorkFlowProcessOutwardDetailsRest createAndReturn(Context context)
            throws AuthorizeException {
        log.info("::::::start::::createAndReturn::::::::::");
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        WorkFlowProcessOutwardDetailsRest workFlowProcessOutwardDetailsRest = null;
        WorkFlowProcessOutwardDetails workFlowProcessOutwardDetails = null;
        try {
            workFlowProcessOutwardDetailsRest = mapper.readValue(req.getInputStream(), WorkFlowProcessOutwardDetailsRest.class);
            workFlowProcessOutwardDetails = createWorkFlowProcessOutwardDetailsFromRestObject(context, workFlowProcessOutwardDetailsRest);

        } catch (Exception e1) {
            log.info("::::::error::::createAndReturn::::::::::");
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        log.info("::::::complate::::createAndReturn::::::::::");
        return converter.toRest(workFlowProcessOutwardDetails, utils.obtainProjection());
    }


    private WorkFlowProcessOutwardDetails createWorkFlowProcessOutwardDetailsFromRestObject(Context context, WorkFlowProcessOutwardDetailsRest workFlowProcessOutwardDetailsRest) throws AuthorizeException {
        log.info("::::::start::::createWorkFlowProcessOutwardDetailsFromRestObject::::::::::");
        WorkFlowProcessOutwardDetails workFlowProcessOutwardDetails = new WorkFlowProcessOutwardDetails();
        try {
            workFlowProcessOutwardDetails=workFlowProcessOutwardDetailsConverter.convert(context,workFlowProcessOutwardDetailsRest );
            workFlowProcessOutwardDetailsService.create(context, workFlowProcessOutwardDetails);
        } catch (Exception e) {
            log.info("::::::error::::createWorkFlowProcessOutwardDetailsFromRestObject::::::::::");
            throw new RuntimeException(e.getMessage(), e);
        }
        log.info("::::::complate::::createWorkFlowProcessOutwardDetailsFromRestObject::::::::::");
        return workFlowProcessOutwardDetails;
    }

    @Override
    protected WorkFlowProcessOutwardDetailsRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                                   JsonNode jsonNode) throws SQLException, AuthorizeException {
        log.info("::::::start::::put::::::::::");
        WorkFlowProcessOutwardDetailsRest workFlowProcessOutwardDetailsRest  = new Gson().fromJson(jsonNode.toString(), WorkFlowProcessOutwardDetailsRest.class);

        WorkFlowProcessOutwardDetails workFlowProcessOutwardDetails = workFlowProcessOutwardDetailsService.find(context, id);
        if (workFlowProcessOutwardDetails == null) {
            System.out.println("workFlowProcessOutwardDetails id ::: is Null  workFlowProcessOutwardDetails tye null"+id);
            throw new ResourceNotFoundException("workFlowProcessOutwardDetails  field with id: " + id + " not found");
        }
        //workFlowProcessOutwardDetails=workFlowProcessOutwardDetailsConverter.convert(workFlowProcessOutwardDetails,context);
        workFlowProcessOutwardDetailsService.update(context, workFlowProcessOutwardDetails);
        context.commit();
        log.info("::::::End::::put::::::::::");

        return converter.toRest(workFlowProcessOutwardDetails, utils.obtainProjection());
    }


    @Override
    public WorkFlowProcessOutwardDetailsRest findOne(Context context, UUID uuid) {
        WorkFlowProcessOutwardDetailsRest workFlowProcessOutwardDetailsRest =null;
        log.info("::::::start::::findOne::::::::::");

        try {
            Optional<WorkFlowProcessOutwardDetails> workFlowProcessOutwardDetails = Optional.ofNullable(workFlowProcessOutwardDetailsService.find(context, uuid));
            if (workFlowProcessOutwardDetails.isPresent()) {
                workFlowProcessOutwardDetailsRest = converter.toRest(workFlowProcessOutwardDetails.get(), utils.obtainProjection());
            }
        } catch (Exception e) {
            log.info("::::::error::::findOne::::::::::");
            e.printStackTrace();
        }
        log.info("::::::End::::findOne::::::::::");
        return workFlowProcessOutwardDetailsRest;
    }

    @Override
    public Page<WorkFlowProcessOutwardDetailsRest> findAll(Context context, Pageable pageable) throws SQLException {
        int total = workFlowProcessOutwardDetailsService.countRows(context);
        List<WorkFlowProcessOutwardDetails>  workFlowProcessOutwardDetails= workFlowProcessOutwardDetailsService.findAll(context,
                Math.toIntExact(pageable.getPageSize()), Math.toIntExact(pageable.getOffset()));
        return converter.toRestPage(workFlowProcessOutwardDetails, pageable, total, utils.obtainProjection());
    }
    protected void delete(Context context, UUID id) throws AuthorizeException {
        log.info("::::::in::::delete::::::::::");
        WorkFlowProcessOutwardDetails workFlowProcessOutwardDetails = null;
        try {
            workFlowProcessOutwardDetails = workFlowProcessOutwardDetailsService.find(context, id);
            if (workFlowProcessOutwardDetails == null) {
                log.info("::::::id not found::::delete::::::::::");
                throw new ResourceNotFoundException(WorkFlowProcessOutwardDetailsRest.CATEGORY + "." + WorkFlowProcessOutwardDetailsRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            workFlowProcessOutwardDetailsService.delete(context, workFlowProcessOutwardDetails);
            context.commit();
            log.info(":::::completed:::delete::::::::::");
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    @PreAuthorize("hasPermission(#uuid, 'ITEM', 'WRITE')")
    @SearchRestMethod(name = "searchByOutwardNumber")
    public Page<WorkFlowProcessOutwardDetailsRest> searchByOutwardNumber(@Parameter(value = "outwardnumber", required = true) String name, Pageable pageable) {
        try {
            System.out.println("sear>>>>>>>>>>>" + name);
            Context context = obtainContext();
            Optional<List<WorkFlowProcessOutwardDetails>> workFlowProcessOutwardDetails = Optional.ofNullable(workFlowProcessOutwardDetailsService.searchOutwardNumber(context, name));
            if (workFlowProcessOutwardDetails.isPresent()) {
                return converter.toRestPage(workFlowProcessOutwardDetails.get(), pageable, 999, utils.obtainProjection());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Class<WorkFlowProcessOutwardDetailsRest> getDomainClass() {
        return null;
    }
}
