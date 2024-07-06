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
import org.dspace.app.rest.converter.WorkFlowProcessInwardDetailsConverter;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.WorkFlowProcessInwardDetailsRest;
import org.dspace.app.rest.model.WorkFlowProcessOutwardDetailsRest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkFlowProcessInwardDetails;
import org.dspace.content.WorkFlowProcessOutwardDetails;
import org.dspace.content.service.WorkFlowProcessInwardDetailsService;
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

@Component(WorkFlowProcessInwardDetailsRest.CATEGORY + "." + WorkFlowProcessInwardDetailsRest.NAME)

public class WorkFlowProcessInwardDetailsRepository extends DSpaceObjectRestRepository<WorkFlowProcessInwardDetails, WorkFlowProcessInwardDetailsRest> {


    private static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(WorkFlowProcessInwardDetailsRepository.class);
    @Autowired
    WorkFlowProcessInwardDetailsService workFlowProcessInwardDetailsService;

    @Autowired
    WorkFlowProcessInwardDetailsConverter workFlowProcessInwardDetailsConverter;

    public WorkFlowProcessInwardDetailsRepository(WorkFlowProcessInwardDetailsService dsoService) {
        super(dsoService);
    }

    @Override
    protected WorkFlowProcessInwardDetailsRest createAndReturn(Context context)
            throws AuthorizeException {
        log.info("::::::start::::createAndReturn::::::::::");
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        WorkFlowProcessInwardDetailsRest workFlowProcessInwardDetailsRest = null;
        WorkFlowProcessInwardDetails workFlowProcessInwardDetails = null;
        try {
            workFlowProcessInwardDetailsRest = mapper.readValue(req.getInputStream(), WorkFlowProcessInwardDetailsRest.class);
            workFlowProcessInwardDetails = createWorkFlowProcessInwardDetailsFromRestObject(context, workFlowProcessInwardDetailsRest);

        } catch (Exception e1) {
            log.info("::::::error::::createAndReturn::::::::::");
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        log.info("::::::complate::::createAndReturn::::::::::");
        return converter.toRest(workFlowProcessInwardDetails, utils.obtainProjection());
    }


    private WorkFlowProcessInwardDetails createWorkFlowProcessInwardDetailsFromRestObject(Context context, WorkFlowProcessInwardDetailsRest workFlowProcessInwardDetailsRest) throws AuthorizeException {
        log.info("::::::start::::createWorkFlowProcessInwardDetailsFromRestObject::::::::::");
        WorkFlowProcessInwardDetails workFlowProcessInwardDetails = new WorkFlowProcessInwardDetails();
        try {
            workFlowProcessInwardDetails=workFlowProcessInwardDetailsConverter.convert(context,workFlowProcessInwardDetailsRest);
            workFlowProcessInwardDetailsService.create(context, workFlowProcessInwardDetails);
        } catch (Exception e) {
            log.info("::::::error::::createWorkFlowProcessInwardDetailsFromRestObject::::::::::");
            throw new RuntimeException(e.getMessage(), e);
        }
        log.info("::::::complate::::createWorkFlowProcessInwardDetailsFromRestObject::::::::::");
        return workFlowProcessInwardDetails;
    }

    @Override
    protected WorkFlowProcessInwardDetailsRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                             JsonNode jsonNode) throws SQLException, AuthorizeException {
        log.info("::::::start::::put::::::::::");
        WorkFlowProcessInwardDetailsRest workFlowProcessInwardDetailsRest  = new Gson().fromJson(jsonNode.toString(), WorkFlowProcessInwardDetailsRest.class);
        WorkFlowProcessInwardDetails workFlowProcessInwardDetails = workFlowProcessInwardDetailsService.find(context, id);
        if (workFlowProcessInwardDetails == null) {
            System.out.println("workFlowProcessInwardDetails id ::: is Null  workFlowProcessInwardDetails tye null"+id);
            throw new ResourceNotFoundException("workFlowProcessInwardDetails  field with id: " + id + " not found");
        }
        workFlowProcessInwardDetails=workFlowProcessInwardDetailsConverter.convert(context,workFlowProcessInwardDetails,workFlowProcessInwardDetailsRest);
        workFlowProcessInwardDetailsService.update(context, workFlowProcessInwardDetails);
        context.commit();
        log.info("::::::End::::put::::::::::");
        return converter.toRest(workFlowProcessInwardDetails, utils.obtainProjection());
    }
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "searchByInwardNumber")
    public Page<WorkFlowProcessOutwardDetailsRest> searchByInwardNumber(@Parameter(value = "inwardnumber", required = true) String name, Pageable pageable) {
        try {
            System.out.println("sear>>>>>>>>>>>" + name);
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            Optional<List<WorkFlowProcessInwardDetails>> workFlowProcessInwardDetails = Optional.ofNullable(workFlowProcessInwardDetailsService.searchInwardNumber(context, name));
            if (workFlowProcessInwardDetails.isPresent()) {
                return converter.toRestPage(workFlowProcessInwardDetails.get(), pageable, 999, utils.obtainProjection());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public WorkFlowProcessInwardDetailsRest findOne(Context context, UUID uuid) {
        WorkFlowProcessInwardDetailsRest workFlowProcessInwardDetailsRest =null;
        log.info("::::::start::::findOne::::::::::");
        try {
            Optional<WorkFlowProcessInwardDetails> workFlowProcessInwardDetails = Optional.ofNullable(workFlowProcessInwardDetailsService.find(context, uuid));
            if (workFlowProcessInwardDetails.isPresent()) {
                workFlowProcessInwardDetailsRest = converter.toRest(workFlowProcessInwardDetails.get(), utils.obtainProjection());
            }
        } catch (Exception e) {
            log.info("::::::error::::findOne::::::::::");
            e.printStackTrace();
        }
        log.info("::::::End::::findOne::::::::::");
        return workFlowProcessInwardDetailsRest;
    }

    @Override
    public Page<WorkFlowProcessInwardDetailsRest> findAll(Context context, Pageable pageable) throws SQLException {
        int total = workFlowProcessInwardDetailsService.countRows(context);
        List<WorkFlowProcessInwardDetails>  workFlowProcessInwardDetails= workFlowProcessInwardDetailsService.findAll(context,
                Math.toIntExact(pageable.getPageSize()), Math.toIntExact(pageable.getOffset()));
        return converter.toRestPage(workFlowProcessInwardDetails, pageable, total, utils.obtainProjection());

    }

    protected void delete(Context context, UUID id) throws AuthorizeException {
        log.info("::::::in::::delete::::::::::");
        WorkFlowProcessInwardDetails workFlowProcessInwardDetails = null;
        try {
            workFlowProcessInwardDetails = workFlowProcessInwardDetailsService.find(context, id);
            if (workFlowProcessInwardDetails == null) {
                log.info("::::::id not found::::delete::::::::::");
                throw new ResourceNotFoundException(WorkFlowProcessInwardDetailsRest.CATEGORY + "." + WorkFlowProcessInwardDetailsRest.NAME +
                        " with id: " + id + " not found");

            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            workFlowProcessInwardDetailsService.delete(context, workFlowProcessInwardDetails);
            context.commit();
            log.info(":::::completed:::delete::::::::::");
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Class<WorkFlowProcessInwardDetailsRest> getDomainClass() {
        return null;
    }
}
