/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.dspace.app.rest.RestResourceController;
import org.dspace.app.rest.model.helper.MyDateConverter;
import org.dspace.content.WorkFlowProcessMasterValue;
import org.dspace.content.WorkflowProcess;
import org.dspace.content.WorkflowProcessReferenceDoc;
import org.dspace.eperson.EPerson;

import javax.persistence.*;
import java.util.*;

/**
 * The rest resource used for workflow definitions
 *
 * @author Maria Verdonck (Atmire) on 11/12/2019
 */
@LinksRest(links = {

})
public class EpersonToEpersonMappingRest extends DSpaceObjectRest{
    public static final String CATEGORY = "epersontoepersonmapping";

    public static final String NAME = "epersontoepersonmapping";

    public static final String NAME_PLURAL = "epersontoepersonmappings";
    @JsonProperty
    private EPersonRest epersonRest = null;

    @JsonProperty
    private EpersonMappingRest epersonMappingRest = null;

    private Boolean isactive = false;
    public Boolean isdelete = false;


    @Override
    public String getCategory() {
        return CATEGORY;
    }
    @Override
    public String getType() {
        return NAME;
    }

    public EPersonRest getEpersonRest() {
        return epersonRest;
    }

    public void setEpersonRest(EPersonRest epersonRest) {
        this.epersonRest = epersonRest;
    }

    public EpersonMappingRest getEpersonMappingRest() {
        return epersonMappingRest;
    }

    public void setEpersonMappingRest(EpersonMappingRest epersonMappingRest) {
        this.epersonMappingRest = epersonMappingRest;
    }

    public Boolean getIsactive() {
        return isactive;
    }

    public void setIsactive(Boolean isactive) {
        this.isactive = isactive;
    }

    public Boolean getIsdelete() {
        return isdelete;
    }

    public void setIsdelete(Boolean isdelete) {
        this.isdelete = isdelete;
    }
}
