/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.dspace.content.WorkFlowProcessMasterValue;

import java.util.Date;

/**
 * The rest resource used for workflow definitions
 *
 * @author Maria Verdonck (Atmire) on 11/12/2019
 */
@LinksRest(links = {

})
public class WorkflowProcessTemplateRest extends DSpaceObjectRest {

    public static final String CATEGORY = "workflowprocesstemplate";
    public static final String NAME = "workflowprocesstemplate";
    public static final String NAME_PLURAL = "workflowprocesstemplates";

    public static final String COLLECTIONS_MAPPED_TO = "collections";
    public static final String STEPS = "steps";

    @JsonProperty
    private Date initDate = null;
    @JsonProperty
    private EPersonRest ePersonRest=null;
    @JsonProperty
    private Integer index;
    @JsonProperty
    private WorkFlowProcessMasterValueRest templateRest = null;
    @JsonProperty
    private WorkFlowProcessMasterValueRest templatetypeRest = null;
    @JsonProperty
    private String editortext;

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getType() {
        return NAME;
    }


    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public WorkFlowProcessMasterValueRest getTemplateRest() {
        return templateRest;
    }

    public void setTemplateRest(WorkFlowProcessMasterValueRest templateRest) {
        this.templateRest = templateRest;
    }

    public EPersonRest getePersonRest() {
        return ePersonRest;
    }

    public void setePersonRest(EPersonRest ePersonRest) {
        this.ePersonRest = ePersonRest;
    }

    public WorkFlowProcessMasterValueRest getTemplatetypeRest() {
        return templatetypeRest;
    }

    public void setTemplatetypeRest(WorkFlowProcessMasterValueRest templatetypeRest) {
        this.templatetypeRest = templatetypeRest;
    }

    public String getEditortext() {
        return editortext;
    }

    public void setEditortext(String editortext) {
        this.editortext = editortext;
    }
}
