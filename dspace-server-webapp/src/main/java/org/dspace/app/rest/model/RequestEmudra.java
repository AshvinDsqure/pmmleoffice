package org.dspace.app.rest.model;

public class RequestEmudra {

    public  String encryptedRequest;

    public  String encryptionKeyId;

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
}
