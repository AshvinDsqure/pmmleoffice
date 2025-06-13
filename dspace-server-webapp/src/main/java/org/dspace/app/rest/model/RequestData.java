package org.dspace.app.rest.model;

import emh.Model.RequestData.CertificateFilter;
import emh.Model.RequestData.PKCSSignRequest;

public class RequestData {
    public String encryptedRequest;
    public String encryptionKeyId;
    public String appID;
    public String keyStoreDisplayName;
    public String keyStorePassphrase;
    public String keyId;
    public String dataToSign;
    public String dataType;
    public String timeStamp;
    public String tempfilepath;
    public String tempfolder;
    public String documentuuid;
    public String bitstreampid;
    public String reason;
    public String location;
    public String errorMsg;
    public String status;
    public String errorCode;
    public String commonName;
    public CertificateFilter certFilter;
    public PKCSSignRequest pKCSSignRequest;



    public String getEncryptedRequest() {
        return encryptedRequest;
    }
    public void setEncryptedRequest(String encryptedRequest) {
        this.encryptedRequest = encryptedRequest;
    }
    public String getEncryptionKeyId() {
        return encryptionKeyId;
    }
    public void setEncryptionKeyId(String encryptionKeyId) {
        this.encryptionKeyId = encryptionKeyId;
    }
    public String getAppID() {
        return appID;
    }
    public void setAppID(String appID) {
        this.appID = appID;
    }
    public String getKeyStoreDisplayName() {
        return keyStoreDisplayName;
    }
    public void setKeyStoreDisplayName(String keyStoreDisplayName) {
        this.keyStoreDisplayName = keyStoreDisplayName;
    }
    public CertificateFilter getCertFilter() {
        return certFilter;
    }
    public void setCertFilter(CertificateFilter certFilter) {
        this.certFilter = certFilter;
    }
    public PKCSSignRequest getpKCSSignRequest() {
        return pKCSSignRequest;
    }
    public void setpKCSSignRequest(PKCSSignRequest pKCSSignRequest) {
        this.pKCSSignRequest = pKCSSignRequest;
    }
    public String getKeyStorePassphrase() {
        return keyStorePassphrase;
    }
    public void setKeyStorePassphrase(String keyStorePassphrase) {
        this.keyStorePassphrase = keyStorePassphrase;
    }
    public String getKeyId() {
        return keyId;
    }
    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }
    public String getDataToSign() {
        return dataToSign;
    }
    public void setDataToSign(String dataToSign) {
        this.dataToSign = dataToSign;
    }
    public String getDataType() {
        return dataType;
    }
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    public String getTimeStamp() {
        return timeStamp;
    }
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTempfilepath() {
        return tempfilepath;
    }

    public void setTempfilepath(String tempfilepath) {
        this.tempfilepath = tempfilepath;
    }

    public String getDocumentuuid() {
        return documentuuid;
    }

    public void setDocumentuuid(String documentuuid) {
        this.documentuuid = documentuuid;
    }

    public String getTempfolder() {
        return tempfolder;
    }

    public void setTempfolder(String tempfolder) {
        this.tempfolder = tempfolder;
    }

    public String getBitstreampid() {
        return bitstreampid;
    }

    public void setBitstreampid(String bitstreampid) {
        this.bitstreampid = bitstreampid;
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

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }
}
