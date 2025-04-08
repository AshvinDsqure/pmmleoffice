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
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.converter.ConverterService;
import org.dspace.app.rest.converter.WorkFlowProcessMasterValueConverter;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.enums.WorkFlowUserType;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.EPersonRest;
import org.dspace.app.rest.model.WorkFlowProcessMasterRest;
import org.dspace.app.rest.model.WorkFlowProcessMasterValueRest;
import org.dspace.app.rest.utils.DateUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkFlowProcessMaster;
import org.dspace.content.WorkFlowProcessMasterValue;
import org.dspace.content.service.WorkFlowProcessMasterService;
import org.dspace.content.service.WorkFlowProcessMasterValueService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.state.Workflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
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
@Component(WorkFlowProcessMasterValueRest.CATEGORY + "." + WorkFlowProcessMasterValueRest.NAME)
public class WorkFlowProcessMasterValueRepository extends DSpaceObjectRestRepository<WorkFlowProcessMasterValue, WorkFlowProcessMasterValueRest> {

    @Autowired
    private WorkFlowProcessMasterValueService workFlowProcessMasterValueService;

    @Autowired
    private WorkFlowProcessMasterService masterService;

    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;


    public WorkFlowProcessMasterValueRepository(WorkFlowProcessMasterValueService dso) {
        super(dso);
    }

    @Override
    protected WorkFlowProcessMasterValueRest createAndReturn(Context context)
            throws AuthorizeException {
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        WorkFlowProcessMasterValueRest workFlowProcessMasterValueRest = null;
        WorkFlowProcessMasterValue workFlowProcessMasterValue = null;
        try {
            workFlowProcessMasterValueRest = mapper.readValue(req.getInputStream(), WorkFlowProcessMasterValueRest.class);
            if (workFlowProcessMasterValueRest.getWorkFlowProcessMaster().getID() == null && workFlowProcessMasterValueRest.getWorkFlowProcessMaster().getMastername() != null&&DateUtils.isNullOrEmptyOrBlank(workFlowProcessMasterValueRest.getWorkFlowProcessMaster().getMastername())) {
               throw new RuntimeException("MasterName Can't black.");
            }
                if (workFlowProcessMasterValueRest.getWorkFlowProcessMaster().getID() == null && workFlowProcessMasterValueRest.getWorkFlowProcessMaster().getMastername() != null&&!DateUtils.isNullOrEmptyOrBlank(workFlowProcessMasterValueRest.getWorkFlowProcessMaster().getMastername())) {
                WorkFlowProcessMaster workFlowProcessMaster = new WorkFlowProcessMaster();
                workFlowProcessMaster.setMastername(workFlowProcessMasterValueRest.getWorkFlowProcessMaster().getMastername());
                WorkFlowProcessMaster ms = masterService.create(context, workFlowProcessMaster);
                workFlowProcessMasterValueRest.setWorkFlowProcessMaster(ms);
            }
            workFlowProcessMasterValue = createWorkFlowProcessMasterFromRestObject(context, workFlowProcessMasterValueRest);
        } catch (Exception e1) {
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        return converter.toRest(workFlowProcessMasterValue, utils.obtainProjection());
    }


    private WorkFlowProcessMasterValue createWorkFlowProcessMasterFromRestObject(Context context, WorkFlowProcessMasterValueRest workFlowProcessMasterValueRest) throws AuthorizeException {
        WorkFlowProcessMasterValue workFlowProcessMasterValue = new WorkFlowProcessMasterValue();

        try {
            workFlowProcessMasterValue = workFlowProcessMasterValueConverter.convert(workFlowProcessMasterValue, workFlowProcessMasterValueRest);
            workFlowProcessMasterValue.setIsdelete(false);
            workFlowProcessMasterValueService.create(context, workFlowProcessMasterValue);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return workFlowProcessMasterValue;
    }

    @Override
    protected WorkFlowProcessMasterValueRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                                 JsonNode jsonNode) throws SQLException, AuthorizeException {
        WorkFlowProcessMasterValueRest workFlowProcessMasterValueRest = new Gson().fromJson(jsonNode.toString(), WorkFlowProcessMasterValueRest.class);

        WorkFlowProcessMasterValueRest workFlowProcessMasterValueRestre =null;

        WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.find(context, id);
        if (workFlowProcessMasterValue == null) {
            System.out.println("WorkFlowProcessMasterRest id ::: is Null  WorkFlowProcessMasterRest tye null" + id);
            throw new ResourceNotFoundException("WorkFlowProcessMasterRest  field with id: " + id + " not found");
        }
        workFlowProcessMasterValue = workFlowProcessMasterValueConverter.convert(workFlowProcessMasterValue, workFlowProcessMasterValueRest);
        workFlowProcessMasterValueService.update(context, workFlowProcessMasterValue);
        workFlowProcessMasterValueRestre =converter.toRest(workFlowProcessMasterValue, utils.obtainProjection());
        context.commit();
        return workFlowProcessMasterValueRestre;
    }


    @Override
    public WorkFlowProcessMasterValueRest findOne(Context context, UUID uuid) {

        WorkFlowProcessMasterValueRest workFlowProcessMasterRest = null;
        try {
            Optional<WorkFlowProcessMasterValue> workFlowProcessMasterValue = Optional.ofNullable(workFlowProcessMasterValueService.find(context, uuid));
            if (workFlowProcessMasterValue.isPresent()) {
                workFlowProcessMasterRest = converter.toRest(workFlowProcessMasterValue.get(), utils.obtainProjection());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workFlowProcessMasterRest;
    }
    @Override
    public Page<WorkFlowProcessMasterValueRest> findAll(Context context, Pageable pageable) throws SQLException {
        int total = workFlowProcessMasterValueService.countRows(context);
        List<WorkFlowProcessMasterValue> workFlowProcessMasters = workFlowProcessMasterValueService.findAll(context,
                Math.toIntExact(pageable.getPageSize()), Math.toIntExact(pageable.getOffset()));
        return converter.toRestPage(workFlowProcessMasters, pageable, total, utils.obtainProjection());

    }

    protected void delete(Context context, UUID id) throws AuthorizeException {

        WorkFlowProcessMasterValue workFlowProcessMasterValue = null;
        try {
            workFlowProcessMasterValue = workFlowProcessMasterValueService.find(context, id);
            if (workFlowProcessMasterValue == null) {
                throw new ResourceNotFoundException(WorkFlowProcessMasterRest.CATEGORY + "." + WorkFlowProcessMasterRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            workFlowProcessMasterValue.setIsdelete(true);
            workFlowProcessMasterValueService.update(context, workFlowProcessMasterValue);
            context.commit();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
        @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
        @SearchRestMethod(name = "findByType")
        public Page<WorkFlowProcessMasterRest> findByStartDateAndEndDate(
                @Parameter(value = "type", required = true) String type,
                Pageable pageable) {
            try {
                String workflowstatus=type;
                Context context = obtainContext();
                List<WorkFlowProcessMasterValue> workFlowProcessMasterValueRests=null;
                int total = workFlowProcessMasterValueService.countfindByType(context, type);
                WorkFlowProcessMaster master = masterService.findByName(context, type);
                if (master != null) {
                    type = master.getID().toString();
                }
               workFlowProcessMasterValueRests = workFlowProcessMasterValueService.findByType(context, type, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
                if(workflowstatus.equalsIgnoreCase(WorkFlowStatus.MASTER.getAction())){
                    List<WorkFlowProcessMasterValueRest> transformedList = workFlowProcessMasterValueRests.stream()
                            .filter(wei -> !wei.getPrimaryvalue().equals(WorkFlowStatus.REFER.getAction()))
                            .map(f -> {
                        return workFlowProcessMasterValueConverter.convert(f, utils.obtainProjection());
                    }).collect(Collectors.toList());
                    return new PageImpl(transformedList, pageable, total);
                }else if(workflowstatus.equalsIgnoreCase("Priority")){
                    List<String> customOrder = Arrays.asList("Most Immediate", "High", "Medium", "Low");
                    List<WorkFlowProcessMasterValueRest> transformedList = null;
                    transformedList = workFlowProcessMasterValueRests.stream()
                            .sorted(Comparator.comparingInt(obj -> customOrder.indexOf(obj.getPrimaryvalue())))
                            .map(f -> {
                        return workFlowProcessMasterValueConverter.convert(f, utils.obtainProjection());
                    }).collect(Collectors.toList());
                    return new PageImpl(transformedList, pageable, total);
                }else {
                    List<WorkFlowProcessMasterValueRest> transformedList = null;
                    transformedList = workFlowProcessMasterValueRests.stream().map(f -> {
                        return workFlowProcessMasterValueConverter.convert(f, utils.obtainProjection());
                    }).collect(Collectors.toList());
                    return new PageImpl(transformedList, pageable, total);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "searchByDepartment")
    public Page<WorkFlowProcessMasterValueRest> searchByDepartment(
            @Parameter(value = "mastername", required = true) String mastername,
            @Parameter(value = "search", required = true) String search,
            Pageable pageable) {
        try {
            Context context = obtainContext();
            UUID masterid = null;
            WorkFlowProcessMaster master = masterService.findByName(context, mastername);
            if (master != null) {
                masterid = master.getID();
            }

            List<WorkFlowProcessMasterValue> witems = workFlowProcessMasterValueService.searchByDepartment(context, masterid, search);
            return converter.toRestPage(witems, pageable, 1000, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<WorkFlowProcessMasterValueRest> getDomainClass() {
        return null;
    }
}
