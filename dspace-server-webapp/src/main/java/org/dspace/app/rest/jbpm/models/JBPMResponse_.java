/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.jbpm.models;

import java.util.ArrayList;
import java.util.List;

public class JBPMResponse_ {

    private Integer count;
    private String message ;

    private String next_user;
    private List<String> next_group=new ArrayList<>();
    private String performed_by_user;
    private List<String> performed_by_group=new ArrayList<>();
    private String type;
    public Integer getCount() {
        return count;
    }
    public void setCount(Integer count) {
        this.count = count;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNext_user() {
        return next_user;
    }

    public void setNext_user(String next_user) {
        this.next_user = next_user;
    }

    public String getPerformed_by_user() {
        return performed_by_user;
    }

    public void setPerformed_by_user(String performed_by_user) {
        this.performed_by_user = performed_by_user;
    }

    public List<String> getNext_group() {
        return next_group;
    }

    public void setNext_group(List<String> next_group) {
        this.next_group = next_group;
    }

    public List<String> getPerformed_by_group() {
        return performed_by_group;
    }

    public void setPerformed_by_group(List<String> performed_by_group) {
        this.performed_by_group = performed_by_group;
    }
}
