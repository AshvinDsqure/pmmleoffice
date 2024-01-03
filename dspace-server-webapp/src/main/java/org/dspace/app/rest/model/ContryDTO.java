package org.dspace.app.rest.model;

public class ContryDTO {

    private String contryname;
    private String statename;
    private String cityname;
    private String contryuuid;
    private String stateuuid;
    private String cityuuid;


    public String getContryname() {
        return contryname;
    }

    public void setContryname(String contryname) {
        this.contryname = contryname;
    }

    public String getStatename() {
        return statename;
    }

    public void setStatename(String statename) {
        this.statename = statename;
    }

    public String getCityname() {
        return cityname;
    }

    public void setCityname(String cityname) {
        this.cityname = cityname;
    }

    public String getContryuuid() {
        return contryuuid;
    }

    public void setContryuuid(String contryuuid) {
        this.contryuuid = contryuuid;
    }

    public String getStateuuid() {
        return stateuuid;
    }

    public void setStateuuid(String stateuuid) {
        this.stateuuid = stateuuid;
    }

    public String getCityuuid() {
        return cityuuid;
    }

    public void setCityuuid(String cityuuid) {
        this.cityuuid = cityuuid;
    }
}
