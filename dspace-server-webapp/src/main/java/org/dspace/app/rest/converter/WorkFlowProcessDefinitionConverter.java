/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.*;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.WorkflowProcessDefinition;
import org.dspace.content.WorkflowProcessEperson;
import org.dspace.content.WorkflowProcessReferenceDoc;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.WorkflowProcessEpersonService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is the converter from/to the EPerson in the DSpace API data model and the
 * REST data model
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@Component
public class WorkFlowProcessDefinitionConverter extends DSpaceObjectConverter<WorkflowProcessDefinition, WorkFlowProcessDefinitionRest> {
    @Autowired
    WorkFlowProcessEpersonConverter workFlowProcessDefinitionEpersonConverter;

    @Autowired
    WorkflowProcessEpersonService workflowProcessEpersonService;
    @Autowired
    BitstreamService bitstreamService;

    @Override
    public WorkFlowProcessDefinitionRest convert(WorkflowProcessDefinition obj, Projection projection) {
        WorkFlowProcessDefinitionRest workflowProcessDefinitionRest = super.convert(obj, projection);
        workflowProcessDefinitionRest.setWorkflowprocessdefinitionname(obj.getWorkflowprocessdefinitionname());
        obj.getWorkflowProcessDefinitionEpeople().forEach(workflowProcessDefinitionEperson -> {
            WorkflowProcessEpersonRest workflowProcessDefinitionEpersonRest = workFlowProcessDefinitionEpersonConverter.convert(workflowProcessDefinitionEperson, projection);
            workflowProcessDefinitionRest.getWorkflowProcessDefinitionEpersonRests().add(workflowProcessDefinitionEpersonRest);
        });
        return workflowProcessDefinitionRest;
    }

    public WorkflowProcessDefinition convert(Context context, WorkFlowProcessDefinitionRest workFlowProcessDefinitionRest) {
        WorkflowProcessDefinition workflowProcessDefinition = new WorkflowProcessDefinition();
        Set<WorkflowProcessEperson> workflowProcessDefinitionEpeople= workFlowProcessDefinitionRest.getWorkflowProcessDefinitionEpersonRests().stream().map(we -> {
            try {
                WorkflowProcessEperson workflowProcessEperson = workFlowProcessDefinitionEpersonConverter.convert(context, we);
                workflowProcessEperson.setWorkflowProcessDefinition(workflowProcessDefinition);
                return workflowProcessEperson;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toSet());
        workflowProcessDefinition.setWorkflowProcessDefinitionEpeople(workflowProcessDefinitionEpeople);
        workflowProcessDefinition.setWorkflowprocessdefinitionname(workFlowProcessDefinitionRest.getWorkflowprocessdefinitionname());
        return workflowProcessDefinition;
    }
    @Override
    protected WorkFlowProcessDefinitionRest newInstance() {
        return new WorkFlowProcessDefinitionRest();
    }

    @Override
    public Class<WorkflowProcessDefinition> getModelClass() {
        return WorkflowProcessDefinition.class;
    }

}
