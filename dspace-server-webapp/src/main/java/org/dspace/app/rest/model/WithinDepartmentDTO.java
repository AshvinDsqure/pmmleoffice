package org.dspace.app.rest.model;

public class WithinDepartmentDTO {

    private String name;
    private Long days;
    private Long filecount;
    private String  type;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDays() {
        return days;
    }

    public void setDays(Long days) {
        this.days = days;
    }

    public Long getFilecount() {
        return filecount;
    }

    public void setFilecount(Long filecount) {
        this.filecount = filecount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
