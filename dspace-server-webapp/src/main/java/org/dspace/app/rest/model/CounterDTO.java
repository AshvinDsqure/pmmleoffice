package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CounterDTO {

    @JsonProperty
    private String month;
    @JsonProperty
    private String year;
    @JsonProperty
    private String count;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
