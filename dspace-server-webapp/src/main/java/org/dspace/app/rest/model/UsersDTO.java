package org.dspace.app.rest.model;

public class UsersDTO {
   private String uuid;
   private String fullname;
   private String departmentname;
   private String officename;
   private String designation;
   private String epersontoepersonmapping;
    private Integer deskno;


    public String getDepartmentname() {
        return departmentname;
    }

    public void setDepartmentname(String departmentname) {
        this.departmentname = departmentname;
    }

    public String getOfficename() {
        return officename;
    }

    public void setOfficename(String officename) {
        this.officename = officename;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Integer getDeskno() {
        return deskno;
    }

    public void setDeskno(Integer deskno) {
        this.deskno = deskno;
    }

    public String getEpersontoepersonmapping() {
        return epersontoepersonmapping;
    }

    public void setEpersontoepersonmapping(String epersontoepersonmapping) {
        this.epersontoepersonmapping = epersontoepersonmapping;
    }
}
