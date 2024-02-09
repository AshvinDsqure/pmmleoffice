package org.dspace.app.rest.model;

import java.util.ArrayList;
import java.util.List;

public class RecipientDataDTO {
    private  List<WorkflowProcessSenderDiaryRest> workflowProcessSenderDiaryRestList=new ArrayList<>();
    private  String subject;

    public List<WorkflowProcessSenderDiaryRest> getWorkflowProcessSenderDiaryRestList() {
        return workflowProcessSenderDiaryRestList;
    }

    public void setWorkflowProcessSenderDiaryRestList(List<WorkflowProcessSenderDiaryRest> workflowProcessSenderDiaryRestList) {
        this.workflowProcessSenderDiaryRestList = workflowProcessSenderDiaryRestList;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
