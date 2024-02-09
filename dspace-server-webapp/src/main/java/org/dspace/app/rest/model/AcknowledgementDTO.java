package org.dspace.app.rest.model;

public class AcknowledgementDTO {
   private  String tapalnumber;
   private  String receiveddate;
   private  String office ;
   private  String department ;
   private  String subject ;
   private  String RecipientName ;
   private  String RecipientDesignation ;
   private  String RecipientOrganization ;
   private  String RecipientAddress ;
    private  String Recipientemail ;
   private  String body ;



    public String getTapalnumber() {
        return tapalnumber;
    }

    public void setTapalnumber(String tapalnumber) {
        this.tapalnumber = tapalnumber;
    }

    public String getReceiveddate() {
        return receiveddate;
    }

    public void setReceiveddate(String receiveddate) {
        this.receiveddate = receiveddate;
    }

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getRecipientName() {
        return RecipientName;
    }

    public void setRecipientName(String recipientName) {
        RecipientName = recipientName;
    }

    public String getRecipientDesignation() {
        return RecipientDesignation;
    }

    public void setRecipientDesignation(String recipientDesignation) {
        RecipientDesignation = recipientDesignation;
    }

    public String getRecipientOrganization() {
        return RecipientOrganization;
    }

    public void setRecipientOrganization(String recipientOrganization) {
        RecipientOrganization = recipientOrganization;
    }

    public String getRecipientAddress() {
        return RecipientAddress;
    }

    public void setRecipientAddress(String recipientAddress) {
        RecipientAddress = recipientAddress;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getRecipientemail() {
        return Recipientemail;
    }

    public void setRecipientemail(String recipientemail) {
        Recipientemail = recipientemail;
    }
}
