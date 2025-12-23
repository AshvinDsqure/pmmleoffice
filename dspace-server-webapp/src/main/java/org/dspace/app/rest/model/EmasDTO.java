package org.dspace.app.rest.model;

public class EmasDTO {

    private String key;
    private String epersonid;
    private String uuid;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEpersonid() {
        return epersonid;
    }

    public void setEpersonid(String epersonid) {
        this.epersonid = epersonid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
