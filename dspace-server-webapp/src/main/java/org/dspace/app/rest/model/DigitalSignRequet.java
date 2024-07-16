package org.dspace.app.rest.model;

import java.io.InputStream;

public class DigitalSignRequet {
    private String password;
    private String reason;
    private String location;
    private String pageNumber;
    private String name;
    private String certType;
    private String showSignature;
    InputStream p12File = null;
    InputStream certFile = null;
    InputStream fileInput = null;
    String p12FileName = null;
    String certFileName = null;
    String fileInputName = null;

    float xCordinate;
    float yCordinate;
    String storepath = null;

    int index=0;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public InputStream getP12File() {
        return p12File;
    }

    public void setP12File(InputStream p12File) {
        this.p12File = p12File;
    }

    public InputStream getCertFile() {
        return certFile;
    }

    public void setCertFile(InputStream certFile) {
        this.certFile = certFile;
    }

    public InputStream getFileInput() {
        return fileInput;
    }

    public void setFileInput(InputStream fileInput) {
        this.fileInput = fileInput;
    }

    public String getP12FileName() {
        return p12FileName;
    }

    public void setP12FileName(String p12FileName) {
        this.p12FileName = p12FileName;
    }

    public String getCertFileName() {
        return certFileName;
    }

    public void setCertFileName(String certFileName) {
        this.certFileName = certFileName;
    }

    public String getFileInputName() {
        return fileInputName;
    }

    public void setFileInputName(String fileInputName) {
        this.fileInputName = fileInputName;
    }

    public float getxCordinate() {
        return xCordinate;
    }

    public void setxCordinate(float xCordinate) {
        this.xCordinate = xCordinate;
    }

    public float getyCordinate() {
        return yCordinate;
    }

    public void setyCordinate(float yCordinate) {
        this.yCordinate = yCordinate;
    }

    public String getStorepath() {
        return storepath;
    }

    public void setStorepath(String storepath) {
        this.storepath = storepath;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
