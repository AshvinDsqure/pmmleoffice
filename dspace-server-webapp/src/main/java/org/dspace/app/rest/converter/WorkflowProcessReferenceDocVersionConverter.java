/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.WorkflowProcessReferenceDocVersionRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.WorkflowProcessReferenceDocVersion;
import org.dspace.content.service.WorkflowProcessReferenceDocVersionService;
import org.dspace.core.Context;
import org.dspace.eperson.service.EPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.UUID;

@Component
public class WorkflowProcessReferenceDocVersionConverter extends DSpaceObjectConverter<WorkflowProcessReferenceDocVersion, WorkflowProcessReferenceDocVersionRest> {
    @Autowired
    WorkflowProcessReferenceDocVersionService WorkflowProcessReferenceDocVersionService;
    @Autowired
    EPersonConverter ePersonConverter;


    @Autowired
    BitstreamConverter bitstreamConverter;
    @Autowired
    EPersonService ePersonService;
    @Autowired
    WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;
    @Override
    public Class<WorkflowProcessReferenceDocVersion> getModelClass() {
        return WorkflowProcessReferenceDocVersion.class;
    }
    @Override
    protected WorkflowProcessReferenceDocVersionRest newInstance() {
        return new WorkflowProcessReferenceDocVersionRest();
    }
    @Override
    public WorkflowProcessReferenceDocVersionRest convert(WorkflowProcessReferenceDocVersion obj, Projection projection) {
        WorkflowProcessReferenceDocVersionRest rest = new WorkflowProcessReferenceDocVersionRest();
       if(obj.getCreator()!=null) {
           rest.setCreator(ePersonConverter.convert(obj.getCreator(), projection));
       }
        if(obj.getBitstream()!=null) {
            rest.setBitstreamRest(bitstreamConverter.convert(obj.getBitstream(), projection));
        }
        if(obj.getRemark()!=null) {
            rest.setRemark(obj.getRemark());
        }
        if(obj.getIsactive()!=null){
            rest.setIsactive(obj.getIsactive());
        }
        if(obj.getEditortext()!=null){
            rest.setEditortext(obj.getEditortext());
        }
        if(obj.getCreationdatetime()!=null){
            rest.setCreationdatetime(obj.getCreationdatetime());
        }
        if(obj.getVersionnumber()!=null){
            rest.setVersionnumber(getVersionFormate(obj.getVersionnumber()));
        }

        rest.setUuid(obj.getID().toString());
        return rest;
    }
    public WorkflowProcessReferenceDocVersion convert(Context context, WorkflowProcessReferenceDocVersionRest rest) throws SQLException {
        WorkflowProcessReferenceDocVersion obj = new WorkflowProcessReferenceDocVersion();
        if(rest.getCreator()!=null) {
            obj.setCreator(ePersonConverter.convert(rest.getCreator()));
        }
        obj.setRemark(rest.getRemark());
        if(rest.getWorkflowProcessReferenceDocRest()!=null) {
            obj.setWorkflowProcessReferenceDoc(workflowProcessReferenceDocConverter.convertByService(context, rest.getWorkflowProcessReferenceDocRest()));
        }
        if(rest.getBitstreamRest()!=null) {
            obj.setBitstream(bitstreamConverter.convertByService(context, rest.getBitstreamRest()));
        }
        if(rest.getIsactive()!=null) {
            obj.setIsactive(rest.getIsactive());
        }
        if(rest.getEditortext()!=null){
            obj.setEditortext(rest.getEditortext());
        }
        obj.setVersionnumber(rest.getVersionnumber());
        return obj;
    }
    public WorkflowProcessReferenceDocVersion convertByService(Context context, WorkflowProcessReferenceDocVersionRest rest) throws SQLException {
        if (rest != null && rest.getId() != null) {
            return WorkflowProcessReferenceDocVersionService.find(context, UUID.fromString(rest.getId()));
        }
        return null;
    }

    public Double getVersionFormate(Double version){
        if (version == 1) {
            int jj=0;
            Double d=Double.valueOf("1."+jj);
            return d;
        } else {
            double d=version;
            int intValue = (int) d;
            int ss=intValue-1;
            Double value=Double.valueOf("1."+ss);
            return value;
        }
    }
}
