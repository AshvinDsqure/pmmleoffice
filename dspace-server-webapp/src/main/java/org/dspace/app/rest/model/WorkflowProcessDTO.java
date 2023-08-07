/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.dspace.app.rest.model.helper.MyDateConverter;

import java.util.Date;

public class WorkflowProcessDTO {
    private String uuid;
    private String Subject;
    private String sendername;
    private String workflowstatus;
    private String workflowtype;
    private String priority;
    @JsonProperty
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date InitDate = new Date();

    @JsonProperty
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date dateRecived = new Date();

    @JsonProperty
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date dueDate = new Date();

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public String getSendername() {
        return sendername;
    }

    public void setSendername(String sendername) {
        this.sendername = sendername;
    }

    public String getWorkflowstatus() {
        return workflowstatus;
    }

    public void setWorkflowstatus(String workflowstatus) {
        this.workflowstatus = workflowstatus;
    }

    public String getWorkflowtype() {
        return workflowtype;
    }

    public void setWorkflowtype(String workflowtype) {
        this.workflowtype = workflowtype;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Date getInitDate() {
        return InitDate;
    }

    public void setInitDate(Date initDate) {
        InitDate = initDate;
    }

    public Date getDateRecived() {
        return dateRecived;
    }

    public void setDateRecived(Date dateRecived) {
        this.dateRecived = dateRecived;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
}
