package org.dspace.app.rest.model;

import java.util.UUID;

public class PDfObject {
    private String htmlContent;
    private  String pdfPath;
    private  String sequance;
    private  String itemid;
    private  String base64Pdf;
    private  String particulars;
    private  String coram;


    private  UUID uuid = UUID.randomUUID();



    public PDfObject(String htmlContent){
        this.htmlContent=htmlContent;
    }
    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public String getSequance() {
        return sequance;
    }

    public void setSequance(String sequance) {
        this.sequance = sequance;
    }

    public String getItemid() {
        return itemid;
    }

    public void setItemid(String itemid) {
        this.itemid = itemid;
    }



    public String getBase64Pdf() {
        return base64Pdf;
    }

    public void setBase64Pdf(String base64Pdf) {
        this.base64Pdf = base64Pdf;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getParticulars() {
        return particulars;
    }

    public void setParticulars(String particulars) {
        this.particulars = particulars;
    }

    public String getCoram() {
        return coram;
    }

    public void setCoram(String coram) {
        this.coram = coram;
    }
}
