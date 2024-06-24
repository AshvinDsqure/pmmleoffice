package org.dspace.app.rest.model;

public class ApprovedReplyDTO {
    private String approvedlatterbitstreamid;
    private String subject;
    private String notebitstreamid;

    public String getApprovedlatterbitstreamid() {
        return approvedlatterbitstreamid;
    }

    public void setApprovedlatterbitstreamid(String approvedlatterbitstreamid) {
        this.approvedlatterbitstreamid = approvedlatterbitstreamid;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getNotebitstreamid() {
        return notebitstreamid;
    }

    public void setNotebitstreamid(String notebitstreamid) {
        this.notebitstreamid = notebitstreamid;
    }
}
