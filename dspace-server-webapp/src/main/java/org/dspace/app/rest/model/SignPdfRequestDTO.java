/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

public class SignPdfRequestDTO {
    private String pdfFilePath;
    private String signedPdfFilePath;
    private String keystorePassword;
    private String reason;
    private String location;
    private String pageNumber;
    private String name;
    private String certType;
    private String showSignature;
    private String documentuuid;
    private String pksc12orpemdocuuid;

    public String getPdfFilePath() {
        return pdfFilePath;
    }

    public void setPdfFilePath(String pdfFilePath) {
        this.pdfFilePath = pdfFilePath;
    }

    public String getSignedPdfFilePath() {
        return signedPdfFilePath;
    }

    public void setSignedPdfFilePath(String signedPdfFilePath) {
        this.signedPdfFilePath = signedPdfFilePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCertType() {
        return certType;
    }

    public void setCertType(String certType) {
        this.certType = certType;
    }

    public String getShowSignature() {
        return showSignature;
    }

    public void setShowSignature(String showSignature) {
        this.showSignature = showSignature;
    }

    public String getDocumentuuid() {
        return documentuuid;
    }
    public void setDocumentuuid(String documentuuid) {
        this.documentuuid = documentuuid;
    }
    public String getPksc12orpemdocuuid() {
        return pksc12orpemdocuuid;
    }
    public void setPksc12orpemdocuuid(String pksc12orpemdocuuid) {
        this.pksc12orpemdocuuid = pksc12orpemdocuuid;
    }
}
