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
import org.dspace.app.rest.converter.EpersonToEpersonMappingConverter;
import org.dspace.app.rest.converter.WorkflowProcessReferenceDocConverter;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.EpersonMappingRest;
import org.dspace.app.rest.model.EpersonToEpersonMappingRest;
import org.dspace.app.rest.model.ErrorRest;
import org.dspace.app.rest.model.WorkFlowProcessDefinitionRest;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.EpersonMapping;
import org.dspace.content.EpersonToEpersonMapping;
import org.dspace.content.WorkflowProcessEperson;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.eperson.service.EPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
 * This is the rest repository responsible for managing WorkflowDefinition Rest objects
 *
 * @author Maria Verdonck (Atmire) on 11/12/2019
 */
@Component(EpersonToEpersonMappingRest.CATEGORY + "." + EpersonToEpersonMappingRest.NAME)
public class EpersonToEpersonMappingRepository extends DSpaceObjectRestRepository<EpersonToEpersonMapping, EpersonToEpersonMappingRest> {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkFlowProcessDraftDetailsRepository.class);


    @Autowired
    private EpersonToEpersonMappingService EpersonToEpersonMappingService;
    @Autowired
    private EPersonService ePersonService;

    @Autowired
    private WorkflowProcessEpersonService workflowProcessEpersonService;
    @Autowired
    private WorkFlowProcessDraftDetailsService workFlowProcessDraftDetailsService;

    @Autowired
    private WorkFlowProcessCommentService workFlowProcessCommentService;
    @Autowired
    private WorkflowProcessSenderDiaryEpersonService workflowProcessSenderDiaryEpersonService;


    @Autowired
    private EpersonToEpersonMappingConverter EpersonToEpersonMappingConverter;
    @Autowired
    private WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;

    public EpersonToEpersonMappingRepository(EpersonToEpersonMappingService dso) {
        super(dso);
    }

    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public EpersonToEpersonMappingRest findOne(Context context, UUID id) {
        EpersonToEpersonMappingRest EpersonToEpersonMappingRest = null;
        try {
            Optional<EpersonToEpersonMapping> workflowProcessDefinitionOption = Optional.ofNullable(EpersonToEpersonMappingService.find(context, id));
            if (workflowProcessDefinitionOption.isPresent()) {
                EpersonToEpersonMappingRest = converter.toRest(workflowProcessDefinitionOption.get(), utils.obtainProjection());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return EpersonToEpersonMappingRest;
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public Page<EpersonToEpersonMappingRest> findAll(Context context, Pageable pageable) {
        try {
            List<EpersonToEpersonMapping> workflowProcessDefinitions = EpersonToEpersonMappingService.findAll(context, pageable.getPageSize(), Math.toIntExact(pageable.getOffset()));
            return converter.toRestPage(workflowProcessDefinitions, pageable, 10, utils.obtainProjection());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Class<EpersonToEpersonMappingRest> getDomainClass() {
        return null;
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @Override
    protected EpersonToEpersonMappingRest createAndReturn(Context context)
            throws AuthorizeException {
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        EpersonToEpersonMappingRest wroEpersonToEpersonMappingRest = null;
        EpersonToEpersonMapping EpersonToEpersonMapping = null;
        try {
            wroEpersonToEpersonMappingRest = mapper.readValue(req.getInputStream(), EpersonToEpersonMappingRest.class);
            EpersonToEpersonMapping = createworkflowProcessDefinitionFromRestObject(context, wroEpersonToEpersonMappingRest);
        } catch (Exception e1) {
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        return converter.toRest(EpersonToEpersonMapping, utils.obtainProjection());
    }

    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    protected EpersonToEpersonMappingRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                              JsonNode jsonNode) throws SQLException, AuthorizeException {
        EpersonToEpersonMappingRest EpersonToEpersonMappingRest = new Gson().fromJson(jsonNode.toString(), EpersonToEpersonMappingRest.class);
        EpersonToEpersonMapping EpersonToEpersonMapping = EpersonToEpersonMappingService.find(context, id);
        if (EpersonToEpersonMapping == null) {
            System.out.println("documentTypeRest id ::: is Null  document tye null");
            throw new ResourceNotFoundException("metadata field with id: " + id + " not found");
        }

        EpersonToEpersonMappingService.update(context, EpersonToEpersonMapping);
        return converter.toRest(EpersonToEpersonMappingRest, utils.obtainProjection());
    }

    private EpersonToEpersonMapping createworkflowProcessDefinitionFromRestObject(Context context, EpersonToEpersonMappingRest EpersonToEpersonMappingRest) throws AuthorizeException {
        EpersonToEpersonMapping EpersonToEpersonMapping = new EpersonToEpersonMapping();
        try {
            EpersonToEpersonMapping = EpersonToEpersonMappingConverter.convertbycreatenew(context, EpersonToEpersonMappingRest);
            EpersonToEpersonMapping = EpersonToEpersonMappingService.create(context, EpersonToEpersonMapping);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return EpersonToEpersonMapping;
    }

    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    protected void patch(Context context, HttpServletRequest request, String apiCategory, String model, UUID uuid,
                         Patch patch) throws AuthorizeException, SQLException {
        patchDSpaceObject(apiCategory, model, uuid, patch);
    }

    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    protected void delete(Context context, UUID id) throws AuthorizeException {
        EpersonToEpersonMapping epersonToEpersonMapping = null;
        try {
            epersonToEpersonMapping = EpersonToEpersonMappingService.find(context, id);
            if (epersonToEpersonMapping == null) {
                throw new ResourceNotFoundException(WorkFlowProcessDefinitionRest.CATEGORY + "." + WorkFlowProcessDefinitionRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        try {
            epersonToEpersonMapping.setIsdelete(true);
            epersonToEpersonMapping.setIsactive(false);
            EpersonToEpersonMappingService.update(context, epersonToEpersonMapping);
            System.out.println("epersonToEpersonMapping ::::DELETED !");
            context.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "getMappingByEperson")
    public Page<EpersonToEpersonMapping> getMappingByEperson(
            @Parameter(value = "eperson", required = true) UUID eperson,
            Pageable pageable) {
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            long total = EpersonToEpersonMappingService.countfindByEperson(context, eperson);
            List<EpersonToEpersonMapping> witems = EpersonToEpersonMappingService.findByEperson(context, eperson, Math.toIntExact(pageable.getOffset()),
                    Math.toIntExact(pageable.getPageSize()));
            return converter.toRestPage(witems, pageable, total, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "UpdateMapping")
    public void getMappingByEperson(
            @Parameter(value = "epersontoepersonmappingid", required = true) UUID epersontoepersonmappingid) {

        try (Context context = obtainContext()) {
            context.turnOffAuthorisationSystem();

            // Fetch all active mappings for the current user
            List<EpersonToEpersonMapping> activeMappings =
                    EpersonToEpersonMappingService.findByEperson(context, context.getCurrentUser().getID(), 0, 100);
            System.out.println("size:::" + activeMappings.size());
            for (EpersonToEpersonMapping mapping : activeMappings) {

                try {
                    mapping.setIsactive(false);
                    EpersonToEpersonMappingService.update(context, mapping);
                } catch (SQLException | AuthorizeException e) {
                    throw new RuntimeException("Error updating mapping: " + mapping.getID(), e);
                }
            }
            // Activate the requested mapping
            EpersonToEpersonMapping epersonMapping = EpersonToEpersonMappingService.find(context, epersontoepersonmappingid);
            if (epersonMapping != null) {
                epersonMapping.setIsactive(true);
                EpersonToEpersonMappingService.update(context, epersonMapping);
                context.commit(); // Commit changes
                System.out.println("update done::::::");
            } else {
                throw new RuntimeException("Mapping not found for ID: " + epersontoepersonmappingid);
            }
        } catch (SQLException | AuthorizeException e) {
            throw new RuntimeException("Error processing eperson mapping", e);
        }
    }


    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "isEpersonMapping")
    public EpersonToEpersonMappingRest isEpersonMapping(@Parameter(value = "epersonmapping", required = true) UUID epersonmapping,
                                                        @Parameter(value = "epersonid", required = true) UUID epersonid) {

        EpersonToEpersonMappingRest epersonToEpersonMappingRest = null;
        Context context = obtainContext();
        try {
            EpersonToEpersonMapping epersonMapping = EpersonToEpersonMappingService.findByEpersonAndEpersonMapping(context, epersonmapping, epersonid);
            if (epersonMapping != null) {
                epersonToEpersonMappingRest = EpersonToEpersonMappingConverter.convert(epersonMapping, utils.obtainProjection());
            } else {
                System.out.println("already exist:");
            }
        } catch (Exception e) {
            System.out.println("Eror::::::" + e.getMessage());
        }
        return epersonToEpersonMappingRest;
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "isEpersonAndEpersonMappingINWorkflow")
    public EpersonToEpersonMappingRest isEpersonAndEpersonMappingINWorkflow(@Parameter(value = "epersonmapping", required = true) UUID epersonmapping,
                                                                            @Parameter(value = "epersonid", required = true) UUID epersonid) {

        EpersonToEpersonMappingRest epersonToEpersonMappingRest = new EpersonToEpersonMappingRest();
        epersonToEpersonMappingRest.setIsactive(false);
        Context context = obtainContext();
        try {
            int ep = workflowProcessEpersonService.getCountByEpersontoepersonmapping(context, epersonid, epersonmapping);
            if (ep != 0) {
                System.out.println("::::::::DATA FOUND IN ::workflowProcessEperson::::::::" + ep);
                epersonToEpersonMappingRest = new EpersonToEpersonMappingRest();
                epersonToEpersonMappingRest.setIsactive(true);
            }
            int ep1 = workflowProcessSenderDiaryEpersonService.getCountByEpersontoepersonmapping(context, epersonid, epersonmapping);
            if (ep1 != 0) {
                System.out.println("::::::::DATA FOUND IN ::workflowProcessSenderDiaryEperson::::::::" + ep1);
                epersonToEpersonMappingRest = new EpersonToEpersonMappingRest();
                epersonToEpersonMappingRest.setIsactive(true);
            }
            int ep2 = workFlowProcessDraftDetailsService.getCountByEpersontoepersonmapping(context, epersonid, epersonmapping);
            if (ep2 != 0) {
                System.out.println("::::::::DATA FOUND IN ::workFlowProcessDraftDetails::::::::" + ep2);
                epersonToEpersonMappingRest = new EpersonToEpersonMappingRest();
                epersonToEpersonMappingRest.setIsactive(true);
            }
        } catch (Exception e) {
            System.out.println(":::::::::::::Error:::::::::" + e.getMessage());
        }
        return epersonToEpersonMappingRest;
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "isEpersonINWorkflow")
    public EpersonToEpersonMappingRest isEpersonINWorkflow(@Parameter(value = "epersonid", required = true) UUID epersonid) {

        EpersonToEpersonMappingRest epersonToEpersonMappingRest = new EpersonToEpersonMappingRest();
        epersonToEpersonMappingRest.setIsactive(false);
        Context context = obtainContext();
        try {
            int epcount = workflowProcessEpersonService.getCountByEperson(context, epersonid);
            if (epcount != 0) {
                System.out.println("::::::::DATA FOUND IN ::workflowProcessEperson::::::::" + epcount);
                epersonToEpersonMappingRest = new EpersonToEpersonMappingRest();
                epersonToEpersonMappingRest.setIsactive(true);
            }
            int SenderDiarycount = workflowProcessSenderDiaryEpersonService.getCountByEperson(context, epersonid);
            if (SenderDiarycount != 0) {
                System.out.println("::::::::DATA FOUND IN ::workflowProcessSenderDiaryEperson::::::::" + SenderDiarycount);
                epersonToEpersonMappingRest = new EpersonToEpersonMappingRest();
                epersonToEpersonMappingRest.setIsactive(true);
            }
            int DraftDetailscount = workFlowProcessDraftDetailsService.getCountByEperson(context, epersonid);
            if (DraftDetailscount != 0) {
                System.out.println("::::::::DATA FOUND IN ::workFlowProcessDraftDetails::::::::" + DraftDetailscount);
                epersonToEpersonMappingRest = new EpersonToEpersonMappingRest();
                epersonToEpersonMappingRest.setIsactive(true);
            }
            int CommentCount=workFlowProcessCommentService.getCountBysubmiter(context,epersonid);
            if (CommentCount != 0) {
                System.out.println("::::::::DATA FOUND IN ::workFlowProcessComment::::::::" + CommentCount);
                epersonToEpersonMappingRest = new EpersonToEpersonMappingRest();
                epersonToEpersonMappingRest.setIsactive(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(":::::::::::::Error:::::::::" + e.getMessage());
        }
        return epersonToEpersonMappingRest;
    }


    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "transferWorkflow")
    public EpersonToEpersonMappingRest transferWorkflow(@Parameter(value = "epersonmappingfrom", required = true) UUID epersonmappingfrom,
                                                        @Parameter(value = "epersonidfrom", required = true) UUID epersonidfrom,
                                                        @Parameter(value = "epersonmappingto", required = true) UUID epersonmappingto,
                                                        @Parameter(value = "epersonidto", required = true) UUID epersonidto
    ) {

        EpersonToEpersonMappingRest response = new EpersonToEpersonMappingRest();
        response.setIsactive(false);
        Context context = obtainContext();
        try {
            int r = workflowProcessEpersonService.getCountByEpersontoepersonmapping(context, epersonidfrom, epersonmappingfrom);
            if (r > 0) {
                if (workflowProcessEpersonService.updateWorkflowProcessEperson(context, epersonidfrom, epersonmappingfrom, epersonidto, epersonmappingto) == 1) {
                    response.setIsactive(true);
                    System.out.println("workflowProcessEperson updated!");
                    log.info("::::::workflowProcessEperson count:::::::" + r);

                }
            }
            int r2 = workflowProcessSenderDiaryEpersonService.getCountByEpersontoepersonmapping(context, epersonidfrom, epersonmappingfrom);
            if (r2 > 0) {
                if (workflowProcessSenderDiaryEpersonService.updateWorkflowProcessSenderDiaryEperson(context, epersonidfrom, epersonmappingfrom, epersonidto, epersonmappingto) == 1) {
                    response.setIsactive(true);
                    System.out.println("workflowProcessSenderDiaryEperson updated!");
                    log.info("::::::workflowProcessSenderDiaryEperson count:::::::" + r2);
                }
            }
            int r3=workFlowProcessDraftDetailsService.getCountByEpersontoepersonmapping(context, epersonidfrom, epersonmappingfrom);
            if (r3 > 0) {
                if (workFlowProcessDraftDetailsService.updateWorkFlowProcessDraftDetails(context, epersonidfrom, epersonmappingfrom, epersonidto, epersonmappingto) == 1) {
                    response.setIsactive(true);
                    System.out.println("workFlowProcessDraftDetails updated!");
                    log.info("::::::workFlowProcessDraftDetails count:::::::" + r3);

                }
            }

            System.out.println("from Person"+epersonidfrom);
            System.out.println("from mapping"+epersonmappingfrom);
            System.out.println("to Person"+epersonidto);
            System.out.println("to mapping"+epersonmappingto);
            log.info("::::::epersonmappingfrom:::::::::" + epersonmappingfrom);
            log.info(":::::epersonidfrom::::::::::::::::" + epersonidfrom);
            log.info("::::epersonmappingto::::::::::::::" + epersonmappingto);
            log.info("::::epersonidto:::::::::::::::::::" + epersonidto);

            context.commit();
        } catch (Exception e) {
            e.printStackTrace();
            response.setName(e.getMessage());
        }
        return response;
    }
}
