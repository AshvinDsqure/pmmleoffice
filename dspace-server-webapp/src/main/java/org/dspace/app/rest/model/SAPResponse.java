package org.dspace.app.rest.model;

public class SAPResponse {

    private  String MESSAGE;
    private  String MSGTYP;

    public String getMESSAGE() {
        return MESSAGE;
    }

    public void setMESSAGE(String MESSAGE) {
        this.MESSAGE = MESSAGE;
    }

    public String getMSGTYP() {
        return MSGTYP;
    }

    public void setMSGTYP(String MSGTYP) {
        this.MSGTYP = MSGTYP;
    }
}
