/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class workflowDTO {
    @JsonProperty
    private List<String> workflowlist = new ArrayList<>();

    public List<String> getWorkflowlist() {
        return workflowlist;
    }

    public void setWorkflowlist(List<String> workflowlist) {
        this.workflowlist = workflowlist;
    }
}
