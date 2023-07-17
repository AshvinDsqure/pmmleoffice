/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.enums;

import com.google.gson.Gson;
import org.dspace.app.rest.jbpm.JbpmServerImpl;
import org.dspace.app.rest.jbpm.models.JBPMResponse;
import org.dspace.app.rest.jbpm.models.JBPMResponse_;
import org.dspace.app.rest.model.WorkFlowProcessRest;
import org.dspace.app.rest.model.WorkflowProcessEpersonRest;
import org.dspace.app.rest.utils.PdfUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;

public enum WorkFlowAction {
    MASTER("Action"),
    CREATE("Create") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            List<String> usersUuid = this.removeInitiatorgetUserList(workFlowProcessRest);
            System.out.println("useruiid:::::::::::::::::" + usersUuid);
            if (usersUuid.size() != 0) {
                System.out.println("type:::::::::::::::::" + workFlowProcessRest.getWorkflowType().getPrimaryvalue());
                String jbpmResponce = this.getJbpmServer().startProcess(workFlowProcessRest, usersUuid);
                JBPMResponse_ jbpmResponse = new Gson().fromJson(jbpmResponce, JBPMResponse_.class);
                System.out.println("jbpm responce create" + new Gson().toJson(jbpmResponse));
                this.setComment(null);
                WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
                this.setComment(null);
                if (workflowProcess.getWorkflowType().getPrimaryvalue().equals("Draft")) {
                    WorkFlowProcessHistory workFlowActionInitv = this.storeWorkFlowHistoryforVersionCreate(context, workflowProcess, currentOwner);
                    this.getWorkFlowProcessHistoryService().create(context, workFlowActionInitv);
                    return this.storeWorkFlowHistoryforDocumentReference(context, workflowProcess, currentOwner);
                }
                WorkFlowProcessHistory workFlowActionInit = this.storeWorkFlowHistory(context, workflowProcess, currentOwner);
                this.setComment(null);
                WorkFlowProcessHistory workFlowActionForward = FORWARD.storeWorkFlowHistory(context, workflowProcess, currentOwner);
                workFlowActionInit.setComment(null);
                workFlowActionForward.setComment(null);
                this.getWorkFlowProcessHistoryService().create(context, workFlowActionInit);
                return this.getWorkFlowProcessHistoryService().create(context, workFlowActionForward);
            } else {
                throw new RuntimeException("initiator not  found.....");
            }
        }
    },
    FORWARD("Forward") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            List<String> usersUuid = null;
            if (this.getInitiator()) {
                System.out.println("in removeInitiatorgetUserList1");
                usersUuid = this.noteremoveInitiatorgetUserList(workFlowProcessRest);
            } else {
                System.out.println("in removeInitiatorgetUserList");
                usersUuid = this.removeInitiatorgetUserList(workFlowProcessRest);
            }
            System.out.println("user list" + usersUuid);
            String forwardResponce = this.getJbpmServer().forwardTask(workFlowProcessRest, usersUuid);
            System.out.println("forward jbpm responce create" + forwardResponce);
            JBPMResponse_ jbpmResponse = new Gson().fromJson(forwardResponce, JBPMResponse_.class);
            WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    },
    BACKWARD("Backward") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            String forwardResponce = this.getJbpmServer().backwardTask(workFlowProcessRest);
            JBPMResponse_ jbpmResponse = new Gson().fromJson(forwardResponce, JBPMResponse_.class);
            System.out.println("jbpmResponse:: Backward" + new Gson().toJson(jbpmResponse));
            if (workflowProcess.getWorkflowType().getPrimaryvalue().equals("Draft")) {
                this.setIsbackward(true);
            }
            WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
            this.setIsbackward(false);
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    },
    REFER("Refer") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            String usersUuid = this.getreferUserID(workFlowProcessRest);
            String forwardResponce = this.getJbpmServer().refer(workFlowProcessRest, usersUuid);
            System.out.println("Refer jbpm responce :" + forwardResponce);
            JBPMResponse_ jbpmResponse = new Gson().fromJson(forwardResponce, JBPMResponse_.class);
            WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    },
    HOLD("Hold") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            //List<String> usersUuid = this.removeInitiatorgetUserList(workFlowProcessRest);
            String forwardResponce = this.getJbpmServer().holdTask(workFlowProcessRest);
            System.out.println("suspend jbpm responce create" + forwardResponce);
            JBPMResponse_ jbpmResponse = new Gson().fromJson(forwardResponce, JBPMResponse_.class);
            WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }

    },
    UNHOLD("UnHold") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            //List<String> usersUuid = this.removeInitiatorgetUserList(workFlowProcessRest);
            String forwardResponce = this.getJbpmServer().resumeTask(workFlowProcessRest);
            System.out.println("suspend jbpm responce create" + forwardResponce);
            JBPMResponse_ jbpmResponse = new Gson().fromJson(forwardResponce, JBPMResponse_.class);
            WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    },
    REJECTED("Rejected") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            WorkflowProcessEperson currentOwner = this.changeOwnerByReject(context, workflowProcess);
            System.out.println("Reject action " + this.getComment());
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner);
            this.setComment(null);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    },
    DELETE("Delete"),
    UPDATE("Update"),
    COMPLETE("Complete") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            String forwardResponce = this.getJbpmServer().completeTask(workFlowProcessRest, new ArrayList<>());
            System.out.println("completed::::::::responce String:::::::::" + forwardResponce);
            JBPMResponse_ jbpmResponse = new Gson().fromJson(forwardResponce, JBPMResponse_.class);
            System.out.println("jbpmResponse:: Complete" + new Gson().toJson(jbpmResponse));
            WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
            System.out.println("this is Complete Comment" + this.getComment());
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner);
            this.setComment(null);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    },
    DISPATCH("Dispatch Ready") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            List<String> usersUuid = this.removeInitiatorgetUserList(workFlowProcessRest);
            System.out.println("normal user::" + usersUuid.toString());
            List<String> dispatchusersUuid = this.getDispatchUsers(workFlowProcessRest);
            System.out.println("dispatchusersUuid user::" + dispatchusersUuid.toString());
            String forwardResponce = this.getJbpmServer().dispatchReady(workFlowProcessRest, usersUuid, dispatchusersUuid);
            JBPMResponse_ jbpmResponse = new Gson().fromJson(forwardResponce, JBPMResponse_.class);
            System.out.println("Dispatch Ready" + new Gson().toJson(jbpmResponse));
            WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner);
            //workFlowAction.setComment(this.getComment());
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    };

    private String action;
    private String comment;

    private Boolean isInitiator = false;

    private Boolean isbackward = false;
    private Boolean isrefer = false;
    private List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs;
    private WorkFlowProcessHistoryService workFlowProcessHistoryService;
    private WorkFlowProcessCommentService workFlowProcessCommentService;
    private WorkFlowProcessMasterValueService workFlowProcessMasterValueService;
    private WorkFlowProcessMasterService workFlowProcessMasterService;
    private WorkflowProcessEpersonService workflowProcessEpersonService;
    private WorkflowProcessReferenceDocService workflowProcessReferenceDocService;

    private JbpmServerImpl jbpmServer;
    ModelMapper modelMapper;

    @Component
    public static class ServiceInjector {
        @Autowired
        private WorkFlowProcessHistoryService workFlowProcessHistoryService;
        @Autowired
        private WorkFlowProcessMasterValueService workFlowProcessMasterValueService;
        @Autowired
        private WorkFlowProcessMasterService workFlowProcessMasterService;

        @Autowired
        private WorkflowProcessReferenceDocService workflowProcessReferenceDocService;
        @Autowired
        JbpmServerImpl jbpmServer;
        @Autowired
        ModelMapper modelMapper;
        @Autowired
        private WorkflowProcessEpersonService workflowProcessEpersonService;

        @PostConstruct
        public void postConstruct() {
            for (WorkFlowAction rt : EnumSet.allOf(WorkFlowAction.class)) {
                rt.setWorkFlowProcessHistoryService(workFlowProcessHistoryService);
                rt.setWorkFlowProcessMasterValueService(workFlowProcessMasterValueService);
                rt.setWorkFlowProcessMasterService(workFlowProcessMasterService);
                rt.setJbpmServer(jbpmServer);
                rt.setModelMapper(modelMapper);
                rt.setWorkflowProcessEpersonService(workflowProcessEpersonService);
                rt.setWorkflowProcessReferenceDocService(workflowProcessReferenceDocService);
            }
        }
    }

    public Boolean getIsrefer() {
        return isrefer;
    }

    public void setIsrefer(Boolean isrefer) {
        this.isrefer = isrefer;
    }

    WorkFlowAction(String action) {
        this.action = action;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAction() {
        return action;
    }

    public Boolean getInitiator() {
        return isInitiator;
    }

    public void setInitiator(Boolean initiator) {
        isInitiator = initiator;
    }

    public List<WorkflowProcessReferenceDoc> getWorkflowProcessReferenceDocs() {
        return workflowProcessReferenceDocs;
    }

    public void setWorkflowProcessReferenceDocs(List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs) {
        this.workflowProcessReferenceDocs = workflowProcessReferenceDocs;
    }

    public Boolean getIsbackward() {
        return isbackward;
    }

    public void setIsbackward(Boolean isbackward) {
        this.isbackward = isbackward;
    }

    public String getreferUserID(WorkFlowProcessRest workFlowProcessRest) {
        System.out.println("in find referid");
        try {
            return workFlowProcessRest.getWorkflowProcessEpersonRests().stream()
                    .filter(s -> s.getIsrefer() != null)
                    .filter(s -> s.getIsrefer())
                    .map(d -> d.getUuid())
                    .findFirst().get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> removeInitiatorgetUserList(WorkFlowProcessRest workFlowProcessRest) {
        return workFlowProcessRest.getWorkflowProcessEpersonRests().stream()
                .filter(wei -> !wei.getUserType().getPrimaryvalue().equals(WorkFlowUserType.INITIATOR.getAction()))
                .filter(wei -> !wei.getUserType().getPrimaryvalue().equals(WorkFlowUserType.DISPATCH.getAction()))
                .sorted(Comparator.comparing(WorkflowProcessEpersonRest::getIndex)).map(d -> d.getUuid()).collect(Collectors.toList());
    }

    public List<String> noteremoveInitiatorgetUserList(WorkFlowProcessRest workFlowProcessRest) {
        return workFlowProcessRest.getWorkflowProcessEpersonRests().stream()
                .sorted(Comparator.comparing(WorkflowProcessEpersonRest::getIndex)).map(d -> d.getUuid()).collect(Collectors.toList());
    }

    public List<String> getDispatchUsers(WorkFlowProcessRest workFlowProcessRest) {
        return workFlowProcessRest.getWorkflowProcessEpersonRests().stream()
                .filter(wei -> wei.getUserType().getPrimaryvalue().equals(WorkFlowUserType.DISPATCH.getAction()))
                .sorted(Comparator.comparing(WorkflowProcessEpersonRest::getIndex)).map(d -> d.getUuid()).collect(Collectors.toList());
    }

    public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
        WorkFlowProcessHistory workFlowAction = new WorkFlowProcessHistory();
        System.out.println("Action::::" + this.getAction() + this.getWorkFlowProcessMasterService());
        WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
        System.out.println("workFlowProcessMaster Master Name::" + workFlowProcessMaster.getMastername());
        WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setAction(workFlowProcessMasterValue);
        workFlowAction.setComment(this.getComment());
        return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
    }

    public WorkFlowProcessHistory storeWorkFlowHistory(Context context, WorkflowProcess workflowProcess, WorkflowProcessEperson workflowProcessEperson) throws SQLException {
        System.out.println("::::::IN :storeWorkFlowHistory:::::::::: ");
        WorkFlowProcessHistory workFlowAction = null;
        workFlowAction = new WorkFlowProcessHistory();
        WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
        workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
        WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setAction(workFlowProcessMasterValue);
        workFlowAction.setWorkflowProcess(workflowProcess);
        if (this.getComment() != null && !this.getComment().isEmpty()) {
                String htmlcomment = "<div>" + this.getComment() + "</div>";
                System.out.println("::::::html::::::::::" + htmlcomment);
               System.out.println("::::::text:::::" + PdfUtils.htmlToText(htmlcomment));
            workFlowAction.setComment(PdfUtils.htmlToText(htmlcomment));
            if (workflowProcess.getWorkflowType().getPrimaryvalue().equals("Draft")) {

                WorkFlowProcessComment workFlowProcessComment = new WorkFlowProcessComment();
                workFlowProcessComment.setComment(PdfUtils.htmlToText(htmlcomment));
                workFlowProcessComment.setWorkFlowProcessHistory(workFlowAction);
                workFlowProcessComment.setSubmitter(context.getCurrentUser());
                workFlowProcessComment.setWorkFlowProcess(workflowProcess);
                if (this.getWorkflowProcessReferenceDocs() != null) {
                    System.out.println("in Comment Doc");
                    List<WorkflowProcessReferenceDoc> list = new ArrayList<>();
                    for (WorkflowProcessReferenceDoc doc1s : this.getWorkflowProcessReferenceDocs()) {
                        WorkflowProcessReferenceDoc doc1 = this.getWorkflowProcessReferenceDocService().find(context, doc1s.getID());
                        if (doc1 != null) {
                            doc1.setWorkflowProcess(workflowProcess);
                            doc1.setWorkflowprocesscomment(workFlowProcessComment);
                            list.add(doc1);
                        }
                    }
                    if (list != null) {
                        workFlowProcessComment.setWorkflowProcessReferenceDoc(list);
                    }
                }
                workFlowAction.setWorkFlowProcessComment(workFlowProcessComment);
            }
        }
        System.out.println("::::::OUT :storeWorkFlowHistory:::::::::: ");
        return workFlowAction;
    }

    public WorkFlowProcessHistory storeWorkFlowHistoryforVersionCreate(Context context, WorkflowProcess workflowProcess, WorkflowProcessEperson workflowProcessEperson) throws SQLException {
        System.out.println("::::::IN :storeWorkFlowHistory::::version:::::: ");
        WorkFlowProcessHistory workFlowAction = null;
        workFlowAction = new WorkFlowProcessHistory();
        WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
        workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
        WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setAction(workFlowProcessMasterValue);
        workFlowAction.setWorkflowProcess(workflowProcess);
        String htmlcomment = "<div>Create Version 1 for "+workflowProcess.getWorkflowProcessNote().getSubject()+" </div>";
        workFlowAction.setComment(PdfUtils.htmlToText(htmlcomment));
        System.out.println("::::::OUT :storeWorkFlowHistory::::create:::::: ");
        return workFlowAction;
    }
// this history call when draft note create time Attaged Reference Doc.
    public WorkFlowProcessHistory storeWorkFlowHistoryforDocumentReference(Context context, WorkflowProcess workflowProcess, WorkflowProcessEperson workflowProcessEperson) {
       WorkFlowProcessHistory workFlowAction = null;
        try {
            if (workflowProcess.getWorkflowProcessReferenceDocs() != null && workflowProcess.getWorkflowProcessReferenceDocs().size() != 0) {
                System.out.println("::::::IN :storeWorkFlowHistory::::DocumentReference:::::: ");
                for (WorkflowProcessReferenceDoc doc : workflowProcess.getWorkflowProcessReferenceDocs()) {
                    if (doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reference Document")) {
                        workFlowAction = new WorkFlowProcessHistory();
                        WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
                        workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
                        WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
                        workFlowAction.setActionDate(new Date());
                        workFlowAction.setAction(workFlowProcessMasterValue);
                        workFlowAction.setWorkflowProcess(workflowProcess);
                        workFlowAction.setComment("Attached "+doc.getDrafttype().getPrimaryvalue()+" In "+doc.getItemname());
                        this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
                    }
                    if (doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reference Noting")) {
                        workFlowAction = new WorkFlowProcessHistory();
                        WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
                        workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
                        WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
                        workFlowAction.setActionDate(new Date());
                        workFlowAction.setAction(workFlowProcessMasterValue);
                        workFlowAction.setWorkflowProcess(workflowProcess);
                        workFlowAction.setComment("Attached "+doc.getDrafttype().getPrimaryvalue() +" In "+doc.getSubject());
                        this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
                    }
               }
                System.out.println("::::::OUT :storeWorkFlowHistory::::DocumentReference:::::: ");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
       return workFlowAction;
    }

    public WorkflowProcessEperson changeOwnerByReject(Context context, WorkflowProcess workflowProcess) throws SQLException, AuthorizeException {
        WorkflowProcessEperson currentOwner = null;
        WorkflowProcessEperson initiator = null;
        currentOwner = workflowProcess.getWorkflowProcessEpeople()
                .stream()
                .filter(d -> d.getOwner() != null)
                .filter(s -> s.getOwner()).findFirst().get();
        if (currentOwner != null) {
            System.out.println("getPerformed_by:::::::::: " + currentOwner.getePerson().getEmail());
            currentOwner.setOwner(false);
            this.getWorkflowProcessEpersonService().update(context, currentOwner);
        }
        initiator = workflowProcess.getWorkflowProcessEpeople().stream()
                .filter(wei -> wei.getUsertype().getPrimaryvalue().equals(WorkFlowUserType.INITIATOR.getAction())).findFirst().get();

        if (initiator != null) {
            System.out.println("next User:::::::::: " + initiator.getePerson().getEmail());
            initiator.setOwner(true);
            this.getWorkflowProcessEpersonService().update(context, currentOwner);
        }
        return currentOwner;
    }

    public WorkflowProcessEperson changeOwnership(Context context, JBPMResponse_ jbpmResponse, WorkflowProcess workflowProcess) throws SQLException, AuthorizeException {
        WorkflowProcessEperson currentOwner = null;
        if (jbpmResponse.getPerformed_by_user() != null) {
            currentOwner = workflowProcess.getWorkflowProcessEpeople().stream().filter(we -> we.getID().equals(UUID.fromString(jbpmResponse.getPerformed_by_user()))).findFirst().get();
            if (this.isrefer) {
                System.out.println("getPerformed_by::::::::::::" + currentOwner.getePerson().getEmail());
                currentOwner.setOwner(false);
                currentOwner.setSender(true);
                this.getWorkflowProcessEpersonService().update(context, currentOwner);
                return currentOwner;
            }
            if (currentOwner.getePerson().getEmail() != null) {
                System.out.println("getPerformed_by::::::::::::" + currentOwner.getePerson().getEmail());
            }
            if (this.isbackward) {
                currentOwner.setIssequence(false);
            } else {
                currentOwner.setIssequence(true);
            }
            currentOwner.setOwner(false);
            currentOwner.setSender(true);
            this.getWorkflowProcessEpersonService().update(context, currentOwner);
        }
        if (jbpmResponse.getNext_user() != null && jbpmResponse.getNext_user().trim().length() != 0) {
            WorkflowProcessEperson workflowProcessEpersonOwner = workflowProcess.getWorkflowProcessEpeople().stream().filter(we -> we.getID().equals(UUID.fromString(jbpmResponse.getNext_user()))).findFirst().get();
            if (workflowProcessEpersonOwner.getePerson().getEmail() != null) {
                System.out.println("getNext_user::::::::::::" + workflowProcessEpersonOwner.getePerson().getEmail());
            }
            workflowProcessEpersonOwner.setOwner(true);
            workflowProcessEpersonOwner.setSender(false);
            workflowProcessEpersonOwner.setIssequence(true);
            this.getWorkflowProcessEpersonService().update(context, workflowProcessEpersonOwner);
        }
        if (jbpmResponse.getNext_group() != null && jbpmResponse.getNext_group().size() != 0) {
            List<WorkflowProcessEperson> workflowProcessEpersonOwners = workflowProcess.getWorkflowProcessEpeople().stream().filter(we -> jbpmResponse.getNext_group().stream().map(d -> d).anyMatch(d -> d.equals(we.getID().toString()))).collect(Collectors.toList());
            workflowProcessEpersonOwners.stream().forEach(d -> {
                System.out.println("next Group" + d.getID());
                d.setOwner(true);
                d.setSender(false);
                try {
                    this.getWorkflowProcessEpersonService().update(context, d);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (AuthorizeException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return currentOwner;
    }

    public WorkFlowProcessMaster getMaster(Context context) throws SQLException {
        System.out.println("Mastyer::::" + this.getAction());
        return this.getWorkFlowProcessMasterService().findByName(context, this.getAction());
    }

    public void setAction(String action) {
        this.action = action;
    }

    public WorkFlowProcessHistoryService getWorkFlowProcessHistoryService() {
        return workFlowProcessHistoryService;
    }

    public void setWorkFlowProcessHistoryService(WorkFlowProcessHistoryService workFlowProcessHistoryService) {
        this.workFlowProcessHistoryService = workFlowProcessHistoryService;
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

    public JbpmServerImpl getJbpmServer() {
        return jbpmServer;
    }

    public void setJbpmServer(JbpmServerImpl jbpmServer) {
        this.jbpmServer = jbpmServer;
    }

    public ModelMapper getModelMapper() {
        return modelMapper;
    }

    public void setModelMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public WorkflowProcessEpersonService getWorkflowProcessEpersonService() {
        return workflowProcessEpersonService;
    }

    public void setWorkflowProcessEpersonService(WorkflowProcessEpersonService workflowProcessEpersonService) {
        this.workflowProcessEpersonService = workflowProcessEpersonService;
    }

    public WorkFlowProcessCommentService getWorkFlowProcessCommentService() {
        return workFlowProcessCommentService;
    }

    public void setWorkFlowProcessCommentService(WorkFlowProcessCommentService workFlowProcessCommentService) {
        this.workFlowProcessCommentService = workFlowProcessCommentService;
    }

    public WorkflowProcessReferenceDocService getWorkflowProcessReferenceDocService() {
        return workflowProcessReferenceDocService;
    }

    public void setWorkflowProcessReferenceDocService(WorkflowProcessReferenceDocService workflowProcessReferenceDocService) {
        this.workflowProcessReferenceDocService = workflowProcessReferenceDocService;
    }
}
