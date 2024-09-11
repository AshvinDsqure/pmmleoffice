package org.dspace.app.rest.jbpm.models;

public class JBPMCallbackRequest {
    public String queueid;
    public String procstatus;

    public String getQueueid() {
        return queueid;
    }

    public void setQueueid(String queueid) {
        this.queueid = queueid;
    }

    public String getProcstatus() {
        return procstatus;
    }

    public void setProcstatus(String procstatus) {
        this.procstatus = procstatus;
    }
}
