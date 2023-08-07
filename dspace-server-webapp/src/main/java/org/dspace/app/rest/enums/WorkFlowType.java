/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.enums;

import org.dspace.app.rest.converter.ItemConverter;
import org.dspace.app.rest.converter.WorkFlowProcessConverter;
import org.dspace.app.rest.converter.WorkFlowProcessMasterValueConverter;
import org.dspace.app.rest.converter.WorkflowProcessReferenceDocConverter;
import org.dspace.app.rest.model.WorkFlowProcessRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.*;
import org.dspace.content.service.WorkFlowProcessMasterService;
import org.dspace.content.service.WorkFlowProcessMasterValueService;
import org.dspace.content.service.WorkflowProcessSenderDiaryService;
import org.dspace.content.service.WorkflowProcessService;
import org.dspace.core.Context;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public enum WorkFlowType {
    MASTER("Workflow Type"),
    INWARD("Inward") {
        @Override
        public WorkFlowProcessRest storeWorkFlowProcess(Context context, WorkFlowProcessRest workFlowProcessRest) throws Exception {
            workFlowProcessRest.setWorkflowTypeStr("INWARD");
            WorkflowProcess workflowProcess = this.getWorkFlowProcessConverter().convert(workFlowProcessRest, context);
            Optional<WorkflowProcessSenderDiary> workflowProcessSenderDiaryOptional = Optional.ofNullable(this.getWorkflowProcessSenderDiaryService().findByEmailID(context, workflowProcess.getWorkflowProcessSenderDiary().getEmail()));
            if (workflowProcessSenderDiaryOptional.isPresent()) {
                workflowProcess.setWorkflowProcessSenderDiary(workflowProcessSenderDiaryOptional.get());
            }
            if (getWorkFlowStatus() != null) {
                Optional<WorkFlowProcessMasterValue> workFlowStatusOptional = getWorkFlowStatus().getUserTypeFromMasterValue(context);
                if (workFlowStatusOptional.isPresent()) {
                    workflowProcess.setWorkflowStatus(workFlowStatusOptional.get());
                }
            }
            workflowProcess = this.getWorkflowProcessService().create(context, workflowProcess);
            WorkflowProcess finalWorkflowProcess = workflowProcess;
            if(workFlowProcessRest.getWorkflowProcessReferenceDocRests()!=null) {
                workflowProcess.setWorkflowProcessReferenceDocs(workFlowProcessRest.getWorkflowProcessReferenceDocRests().stream().map(d -> {
                    try {
                        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = this.getWorkflowProcessReferenceDocConverter().convertByService(context, d);
                        workflowProcessReferenceDoc.setWorkflowProcess(finalWorkflowProcess);
                        return workflowProcessReferenceDoc;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList()));
            }
            this.getWorkflowProcessService().update(context, workflowProcess);
            workFlowProcessRest = getWorkFlowProcessConverter().convert(workflowProcess, this.getProjection());
            this.getWorkFlowAction().perfomeAction(context, workflowProcess, workFlowProcessRest);
            return workFlowProcessRest;
        }

        @Override
        public WorkFlowProcessRest storeWorkFlowProcessDraft(Context context, WorkFlowProcessRest workFlowProcessRest) throws Exception {
            workFlowProcessRest.setWorkflowTypeStr("INWARD");
            WorkflowProcess workflowProcess = this.getWorkFlowProcessConverter().convert(workFlowProcessRest, context);
            if (workflowProcess.getWorkflowProcessSenderDiary() != null) {
                Optional<WorkflowProcessSenderDiary> workflowProcessSenderDiaryOptional = Optional.ofNullable(this.getWorkflowProcessSenderDiaryService().findByEmailID(context, workflowProcess.getWorkflowProcessSenderDiary().getEmail()));
                if (workflowProcessSenderDiaryOptional.isPresent()) {
                    workflowProcess.setWorkflowProcessSenderDiary(workflowProcessSenderDiaryOptional.get());
                }
            }
            if (getWorkFlowStatus() != null) {
                Optional<WorkFlowProcessMasterValue> workFlowStatusOptional = getWorkFlowStatus().getUserTypeFromMasterValue(context);
                if (workFlowStatusOptional.isPresent()) {
                    workflowProcess.setWorkflowStatus(workFlowStatusOptional.get());
                }
            }
            workflowProcess = this.getWorkflowProcessService().create(context, workflowProcess);
            WorkflowProcess finalWorkflowProcess = workflowProcess;
            workflowProcess.setWorkflowProcessReferenceDocs(workFlowProcessRest.getWorkflowProcessReferenceDocRests().stream().map(d -> {
                try {
                    WorkflowProcessReferenceDoc workflowProcessReferenceDoc = this.getWorkflowProcessReferenceDocConverter().convertByService(context, d);
                    workflowProcessReferenceDoc.setWorkflowProcess(finalWorkflowProcess);
                    return workflowProcessReferenceDoc;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList()));
            this.getWorkflowProcessService().update(context, workflowProcess);
            workFlowProcessRest = getWorkFlowProcessConverter().convert(workflowProcess, this.getProjection());
            //this.getWorkFlowAction().perfomeAction(context, workflowProcess, workFlowProcessRest);
            return workFlowProcessRest;
        }
    },
    OUTWARED("Outward") {
        @Override
        public WorkFlowProcessRest storeWorkFlowProcess(Context context, WorkFlowProcessRest workFlowProcessRest) throws Exception {
            workFlowProcessRest.setWorkflowTypeStr("OUTWARED");
            WorkflowProcess workflowProcess = this.getWorkFlowProcessConverter().convert(workFlowProcessRest, context);
            Optional<WorkflowProcessSenderDiary> workflowProcessSenderDiaryOptional = Optional.ofNullable(this.getWorkflowProcessSenderDiaryService().findByEmailID(context, workflowProcess.getWorkflowProcessSenderDiary().getEmail()));
            if (workflowProcessSenderDiaryOptional.isPresent()) {
                workflowProcess.setWorkflowProcessSenderDiary(workflowProcessSenderDiaryOptional.get());
            }
            if (getWorkFlowStatus() != null) {
                Optional<WorkFlowProcessMasterValue> workFlowStatusOptional = getWorkFlowStatus().getUserTypeFromMasterValue(context);
                if (workFlowStatusOptional.isPresent()) {
                    workflowProcess.setWorkflowStatus(workFlowStatusOptional.get());
                }
            }
            workflowProcess = this.getWorkflowProcessService().create(context, workflowProcess);
            WorkflowProcess finalWorkflowProcess = workflowProcess;
            workflowProcess.setWorkflowProcessReferenceDocs(workFlowProcessRest.getWorkflowProcessReferenceDocRests().stream().map(d -> {
                try {
                    WorkflowProcessReferenceDoc workflowProcessReferenceDoc = this.getWorkflowProcessReferenceDocConverter().convertByService(context, d);
                    workflowProcessReferenceDoc.setWorkflowProcess(finalWorkflowProcess);
                    return workflowProcessReferenceDoc;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList()));
            this.getWorkflowProcessService().update(context, workflowProcess);
            workFlowProcessRest = getWorkFlowProcessConverter().convert(workflowProcess, this.getProjection());
            this.getWorkFlowAction().perfomeAction(context, workflowProcess, workFlowProcessRest);
            return workFlowProcessRest;
        }
        @Override
        public WorkFlowProcessRest storeWorkFlowProcessDraft(Context context, WorkFlowProcessRest workFlowProcessRest) throws Exception {
            workFlowProcessRest.setWorkflowTypeStr("OUTWARED");
            WorkflowProcess workflowProcess = this.getWorkFlowProcessConverter().convert(workFlowProcessRest, context);
            if (workflowProcess.getWorkflowProcessSenderDiary() != null) {
                Optional<WorkflowProcessSenderDiary> workflowProcessSenderDiaryOptional = Optional.ofNullable(this.getWorkflowProcessSenderDiaryService().findByEmailID(context, workflowProcess.getWorkflowProcessSenderDiary().getEmail()));
                if (workflowProcessSenderDiaryOptional.isPresent()) {
                    workflowProcess.setWorkflowProcessSenderDiary(workflowProcessSenderDiaryOptional.get());
                }
            }
            if (getWorkFlowStatus() != null) {
                Optional<WorkFlowProcessMasterValue> workFlowStatusOptional = getWorkFlowStatus().getUserTypeFromMasterValue(context);
                if (workFlowStatusOptional.isPresent()) {
                    workflowProcess.setWorkflowStatus(workFlowStatusOptional.get());
                }
            }
            workflowProcess = this.getWorkflowProcessService().create(context, workflowProcess);
            WorkflowProcess finalWorkflowProcess = workflowProcess;
            workflowProcess.setWorkflowProcessReferenceDocs(workFlowProcessRest.getWorkflowProcessReferenceDocRests().stream().map(d -> {
                try {
                    WorkflowProcessReferenceDoc workflowProcessReferenceDoc = this.getWorkflowProcessReferenceDocConverter().convertByService(context, d);
                    workflowProcessReferenceDoc.setWorkflowProcess(finalWorkflowProcess);
                    return workflowProcessReferenceDoc;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList()));
            this.getWorkflowProcessService().update(context, workflowProcess);
            workFlowProcessRest = getWorkFlowProcessConverter().convert(workflowProcess, this.getProjection());
            //this.getWorkFlowAction().perfomeAction(context, workflowProcess, workFlowProcessRest);
            return workFlowProcessRest;
        }
    },
    DRAFT("Draft") {
        @Override
        public WorkFlowProcessRest storeWorkFlowProcess(Context context, WorkFlowProcessRest workFlowProcessRest) throws Exception {
            workFlowProcessRest.setWorkflowTypeStr("DRAFT");
            WorkflowProcess workflowProcess = this.getWorkFlowProcessConverter().convert(workFlowProcessRest, context);
            if (getWorkFlowStatus() != null) {
                Optional<WorkFlowProcessMasterValue> workFlowStatusOptional = getWorkFlowStatus().getUserTypeFromMasterValue(context);
                if (workFlowStatusOptional.isPresent()) {
                    workflowProcess.setWorkflowStatus(workFlowStatusOptional.get());
                }
            }
            workflowProcess = this.getWorkflowProcessService().create(context, workflowProcess);
            WorkflowProcess finalWorkflowProcess = workflowProcess;
            workflowProcess.setWorkflowProcessReferenceDocs(workFlowProcessRest.getWorkflowProcessReferenceDocRests().stream().map(d -> {
                try {
                    WorkflowProcessReferenceDoc workflowProcessReferenceDoc = this.getWorkflowProcessReferenceDocConverter().convertByService(context, d);
                    workflowProcessReferenceDoc.setWorkflowProcess(finalWorkflowProcess);
                    return workflowProcessReferenceDoc;
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList()));
            this.getWorkflowProcessService().update(context, workflowProcess);
            workFlowProcessRest = getWorkFlowProcessConverter().convert(workflowProcess, this.getProjection());
            this.getWorkFlowAction().perfomeAction(context, workflowProcess, workFlowProcessRest);
            workFlowProcessRest = getWorkFlowProcessConverter().convert(workflowProcess, this.getProjection());
            return workFlowProcessRest;
        }
        @Override
        public WorkFlowProcessRest storeWorkFlowProcessDraft(Context context, WorkFlowProcessRest workFlowProcessRest) throws Exception {
            workFlowProcessRest.setWorkflowTypeStr("DRAFT");
            WorkflowProcess workflowProcess = this.getWorkFlowProcessConverter().convert(workFlowProcessRest, context);
            if (workflowProcess.getWorkflowProcessSenderDiary() != null) {
                Optional<WorkflowProcessSenderDiary> workflowProcessSenderDiaryOptional = Optional.ofNullable(this.getWorkflowProcessSenderDiaryService().findByEmailID(context, workflowProcess.getWorkflowProcessSenderDiary().getEmail()));
                if (workflowProcessSenderDiaryOptional.isPresent()) {
                    workflowProcess.setWorkflowProcessSenderDiary(workflowProcessSenderDiaryOptional.get());
                }
            }
            if (getWorkFlowStatus() != null) {
                Optional<WorkFlowProcessMasterValue> workFlowStatusOptional = getWorkFlowStatus().getUserTypeFromMasterValue(context);
                if (workFlowStatusOptional.isPresent()) {
                    workflowProcess.setWorkflowStatus(workFlowStatusOptional.get());
                }
            }
            workflowProcess = this.getWorkflowProcessService().create(context, workflowProcess);
            WorkflowProcess finalWorkflowProcess = workflowProcess;
            workflowProcess.setWorkflowProcessReferenceDocs(workFlowProcessRest.getWorkflowProcessReferenceDocRests().stream().map(d -> {
                try {
                    WorkflowProcessReferenceDoc workflowProcessReferenceDoc = this.getWorkflowProcessReferenceDocConverter().convertByService(context, d);
                    workflowProcessReferenceDoc.setWorkflowProcess(finalWorkflowProcess);
                    return workflowProcessReferenceDoc;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList()));
            this.getWorkflowProcessService().update(context, workflowProcess);
            workFlowProcessRest = getWorkFlowProcessConverter().convert(workflowProcess, this.getProjection());
            //this.getWorkFlowAction().perfomeAction(context, workflowProcess, workFlowProcessRest);
            return workFlowProcessRest;
        }
    };
    private String type;
    private WorkFlowStatus workFlowStatus;
    private WorkFlowAction workFlowAction;
    private WorkFlowProcessMasterValueService workFlowProcessMasterValueService;
    private WorkFlowProcessMasterService workFlowProcessMasterService;
    private WorkFlowProcessConverter workFlowProcessConverter;
    private WorkflowProcessSenderDiaryService workflowProcessSenderDiaryService;
    private WorkflowProcessService workflowProcessService;
    private WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;
    private ItemConverter itemConverter;

    private Projection projection;

    @Component
    public static class ServiceInjector {
        @Autowired
        private WorkFlowProcessMasterValueService workFlowProcessMasterValueService;
        @Autowired
        private WorkFlowProcessMasterService workFlowProcessMasterService;
        @Autowired
        private WorkFlowProcessConverter workFlowProcessConverter;
        @Autowired
        private WorkflowProcessSenderDiaryService workflowProcessSenderDiaryService;
        @Autowired
        private WorkflowProcessService workflowProcessService;
        @Autowired
        private WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;
        @Autowired
        private ItemConverter itemConverter;


        @PostConstruct
        public void postConstruct() {
            for (WorkFlowType rt : EnumSet.allOf(WorkFlowType.class)) {
                rt.setWorkFlowProcessMasterValueService(workFlowProcessMasterValueService);
                rt.setWorkFlowProcessMasterService(workFlowProcessMasterService);
                rt.setWorkFlowProcessConverter(workFlowProcessConverter);
                rt.setWorkflowProcessSenderDiaryService(workflowProcessSenderDiaryService);
                rt.setWorkflowProcessReferenceDocConverter(workflowProcessReferenceDocConverter);
                rt.setWorkflowProcessService(workflowProcessService);
                rt.setItemConverter(itemConverter);
            }
        }
    }

    WorkFlowType(String type) {
        this.type = type;
    }

    public String getAction() {
        return type;
    }

    public Optional<WorkFlowProcessMasterValue> getUserTypeFromMasterValue(Context context) throws SQLException {

        WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
        WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
        return Optional.ofNullable(workFlowProcessMasterValue);
    }

    public WorkFlowProcessRest storeWorkFlowProcess(Context context, WorkFlowProcessRest workFlowProcessRest) throws Exception {
        return null;
    }

    public WorkFlowProcessRest storeWorkFlowProcessDraft(Context context, WorkFlowProcessRest workFlowProcessRest) throws Exception {
        return null;
    }

    public WorkFlowProcessMaster getMaster(Context context) throws SQLException {
        return this.getWorkFlowProcessMasterService().findByName(context, this.getAction());
    }

    public WorkFlowProcessMasterValueService getWorkFlowProcessMasterValueService() {
        return workFlowProcessMasterValueService;
    }

    public void setWorkFlowProcessMasterValueService(WorkFlowProcessMasterValueService workFlowProcessMasterValueService) {
        this.workFlowProcessMasterValueService = workFlowProcessMasterValueService;
    }

    public WorkFlowProcessMasterService getWorkFlowProcessMasterService() {
        return workFlowProcessMasterService;
    }

    public void setWorkFlowProcessMasterService(WorkFlowProcessMasterService workFlowProcessMasterService) {
        this.workFlowProcessMasterService = workFlowProcessMasterService;
    }

    public WorkFlowProcessConverter getWorkFlowProcessConverter() {
        return workFlowProcessConverter;
    }

    public void setWorkFlowProcessConverter(WorkFlowProcessConverter workFlowProcessConverter) {
        this.workFlowProcessConverter = workFlowProcessConverter;
    }

    public WorkflowProcessSenderDiaryService getWorkflowProcessSenderDiaryService() {
        return workflowProcessSenderDiaryService;
    }

    public void setWorkflowProcessSenderDiaryService(WorkflowProcessSenderDiaryService workflowProcessSenderDiaryService) {
        this.workflowProcessSenderDiaryService = workflowProcessSenderDiaryService;
    }

    public ItemConverter getItemConverter() {
        return itemConverter;
    }

    public void setItemConverter(ItemConverter itemConverter) {
        this.itemConverter = itemConverter;
    }

    public WorkFlowStatus getWorkFlowStatus() {
        return workFlowStatus;
    }

    public void setWorkFlowStatus(WorkFlowStatus workFlowStatus) {
        this.workFlowStatus = workFlowStatus;
    }

    public WorkflowProcessService getWorkflowProcessService() {
        return workflowProcessService;
    }

    public void setWorkflowProcessService(WorkflowProcessService workflowProcessService) {
        this.workflowProcessService = workflowProcessService;
    }

    public WorkflowProcessReferenceDocConverter getWorkflowProcessReferenceDocConverter() {
        return workflowProcessReferenceDocConverter;
    }

    public void setWorkflowProcessReferenceDocConverter(WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter) {
        this.workflowProcessReferenceDocConverter = workflowProcessReferenceDocConverter;
    }

    public WorkFlowAction getWorkFlowAction() {
        return workFlowAction;
    }

    public void setWorkFlowAction(WorkFlowAction workFlowAction) {
        this.workFlowAction = workFlowAction;
    }

    public Projection getProjection() {
        return projection;
    }

    public void setProjection(Projection projection) {
        this.projection = projection;
    }
}
