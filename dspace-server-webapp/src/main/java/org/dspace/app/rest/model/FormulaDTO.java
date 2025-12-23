package org.dspace.app.rest.model;

import java.util.ArrayList;
import java.util.List;

public class FormulaDTO {

    private Long totalFilecreate;
    private Long totalFileclose;

    private Integer totleuser;

    private Integer activeuser;


    private double fileDisposalRate;


    private Long totaltapalcreate;
    private Long totaltapaleclose;

    private List<Long> dayWiseDuration=new ArrayList<>();
    private List<Long> stagewiseDuration=new ArrayList<>();





    public Long getTotalFilecreate() {
        return totalFilecreate;
    }

    public void setTotalFilecreate(Long totalFilecreate) {
        this.totalFilecreate = totalFilecreate;
    }

    public Long getTotalFileclose() {
        return totalFileclose;
    }

    public void setTotalFileclose(Long totalFileclose) {
        this.totalFileclose = totalFileclose;
    }

    public Long getTotaltapalcreate() {
        return totaltapalcreate;
    }

    public void setTotaltapalcreate(Long totaltapalcreate) {
        this.totaltapalcreate = totaltapalcreate;
    }

    public Long getTotaltapaleclose() {
        return totaltapaleclose;
    }

    public void setTotaltapaleclose(Long totaltapaleclose) {
        this.totaltapaleclose = totaltapaleclose;
    }

    public List<Long> getDayWiseDuration() {
        return dayWiseDuration;
    }

    public void setDayWiseDuration(List<Long> dayWiseDuration) {
        this.dayWiseDuration = dayWiseDuration;
    }

    public List<Long> getStagewiseDuration() {
        return stagewiseDuration;
    }

    public void setStagewiseDuration(List<Long> stagewiseDuration) {
        this.stagewiseDuration = stagewiseDuration;
    }

    public double getFileDisposalRate() {
        return fileDisposalRate;
    }

    public void setFileDisposalRate(double fileDisposalRate) {
        this.fileDisposalRate = fileDisposalRate;
    }

    public Integer getTotleuser() {
        return totleuser;
    }

    public void setTotleuser(Integer totleuser) {
        this.totleuser = totleuser;
    }

    public Integer getActiveuser() {
        return activeuser;
    }

    public void setActiveuser(Integer activeuser) {
        this.activeuser = activeuser;
    }
}
