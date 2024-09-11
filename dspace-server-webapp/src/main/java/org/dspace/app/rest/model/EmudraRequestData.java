package org.dspace.app.rest.model;

public class EmudraRequestData {

    public String requestMode;
    public String requestedDataType;
    public String version;
    public RequestData requstedData;
    public String getRequestMode() {
        return requestMode;
    }
    public void setRequestMode(String requestMode) {
        this.requestMode = requestMode;
    }
    public String getRequestedDataType() {
        return requestedDataType;
    }
    public void setRequestedDataType(String requestedDataType) {
        this.requestedDataType = requestedDataType;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public RequestData getRequstedData() {
        return requstedData;
    }
    public void setRequstedData(RequestData requstedData) {
        this.requstedData = requstedData;
    }
}
