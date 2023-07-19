/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;
import net.sf.saxon.Controller;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.projection.Projection;
import org.dspace.app.rest.utils.PdfUtils;
import org.dspace.content.*;
import org.dspace.content.enums.Priority;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.WorkflowProcessNoteService;
import org.dspace.content.service.WorkflowProcessReferenceDocService;
import org.dspace.core.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This is the converter from/to the EPerson in the DSpace API data model and the
 * REST data model
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@Component
public class WorkflowProcessReferenceDocConverter extends DSpaceObjectConverter<WorkflowProcessReferenceDoc, WorkflowProcessReferenceDocRest> {

    @Autowired
    BitstreamService bitstreamService;
    @Autowired
    BitstreamConverter bitstreamConverter;

    @Autowired
    EPersonConverter ePersonConverter;

    @Autowired
    WorkflowProcessNoteService workflowProcessNoteService;

    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;

    @Autowired
    ItemConverter itemConverter;
    @Autowired
    WorkflowProcessReferenceDocService workflowProcessReferenceDocService;
    public WorkflowProcessReferenceDocRest convert(WorkflowProcessReferenceDoc obj, Projection projection) {
        WorkflowProcessReferenceDocRest workflowProcessDefinitionRest = new WorkflowProcessReferenceDocRest();
        if(obj.getBitstream() != null) {
            workflowProcessDefinitionRest.setBitstreamRest(bitstreamConverter.convertFoWorkFLowRefDoc(obj.getBitstream(), projection));
        }
        if(obj.getWorkFlowProcessReferenceDocType() != null) {
            workflowProcessDefinitionRest.setWorkFlowProcessReferenceDocType(workFlowProcessMasterValueConverter.convert(obj.getWorkFlowProcessReferenceDocType(), projection));
        }
        if(obj.getDrafttype() != null) {
            workflowProcessDefinitionRest.setDrafttypeRest(workFlowProcessMasterValueConverter.convert(obj.getDrafttype(), projection));
        }
        if(obj.getLatterCategory() != null){
            workflowProcessDefinitionRest.setLatterCategoryRest(workFlowProcessMasterValueConverter.convert(obj.getLatterCategory(),projection));
        }
        if(obj.getSubject()!=null){
            workflowProcessDefinitionRest.setSubject(obj.getSubject());
        }
        if(obj.getReferenceNumber()!=null){
            workflowProcessDefinitionRest.setReferenceNumber(obj.getReferenceNumber());
        }
        if(obj.getEditortext()!=null){
            workflowProcessDefinitionRest.setEditortext(obj.getEditortext());
        }
        if(obj.getDescription()!=null){
            workflowProcessDefinitionRest.setDescription(obj.getDescription());
        }
        if(obj.getItemname()!=null){
            workflowProcessDefinitionRest.setItemname(obj.getItemname());
        }
        if(obj.getCreatedate()!=null){
            workflowProcessDefinitionRest.setCreatedate(obj.getCreatedate());
        }
        if (obj.getDocumentsignator() != null) {
            workflowProcessDefinitionRest.setDocumentsignatorRest(ePersonConverter.convert(obj.getDocumentsignator(), projection));
        }
        workflowProcessDefinitionRest.setIssignature(obj.getIssignature());
        workflowProcessDefinitionRest.setUuid(obj.getID().toString());
        return workflowProcessDefinitionRest;
    }
    public WorkflowProcessReferenceDocRest convertForWorkFlow(WorkflowProcessReferenceDoc obj, Projection projection) {
        WorkflowProcessReferenceDocRest workflowProcessDefinitionRest = super.convert(obj, projection);
        if(obj.getBitstream()!= null){
           BitstreamRest bitstreamRest= bitstreamConverter.convertFoWorkFLowRefDoc(obj.getBitstream(),projection);
            workflowProcessDefinitionRest.setBitstreamRest(bitstreamRest);
        }
        return workflowProcessDefinitionRest;
    }
    @Override
    protected WorkflowProcessReferenceDocRest newInstance() {
        return new WorkflowProcessReferenceDocRest();
    }
    @Override
    public Class<WorkflowProcessReferenceDoc> getModelClass() {
        return WorkflowProcessReferenceDoc.class;
    }
    public WorkflowProcessReferenceDoc convert(Context context, WorkflowProcessReferenceDocRest rest) throws SQLException {
        WorkflowProcessReferenceDoc workflowProcessReferenceDoc=new WorkflowProcessReferenceDoc();
        if(rest.getReferenceNumber()!=null){
        workflowProcessReferenceDoc.setReferenceNumber(rest.getReferenceNumber());}
        if(rest.getInitdate()!=null){
        workflowProcessReferenceDoc.setInitdate(rest.getInitdate());}
        if(rest.getSubject()!=null){
        workflowProcessReferenceDoc.setSubject(rest.getSubject());}
        if(rest.getEditortext()!=null){
            PdfUtils.htmlToText(rest.getEditortext());
        workflowProcessReferenceDoc.setEditortext(rest.getEditortext());}
        if(rest.getLatterCategoryRest()!=null) {
            workflowProcessReferenceDoc.setLatterCategory(workFlowProcessMasterValueConverter.convert(context, rest.getLatterCategoryRest()));
        }
        if(rest.getWorkFlowProcessReferenceDocType()!=null) {
            workflowProcessReferenceDoc.setWorkFlowProcessReferenceDocType(workFlowProcessMasterValueConverter.convert(context, rest.getWorkFlowProcessReferenceDocType()));
        }
        if(rest.getDrafttypeRest() != null) {
            workflowProcessReferenceDoc.setDrafttype(workFlowProcessMasterValueConverter.convert(context,rest.getDrafttypeRest()));
        }
        if(rest.getDescription() != null) {
            workflowProcessReferenceDoc.setDescription(rest.getDescription());
        }
        if(rest.getBitstreamRest() != null) {
            workflowProcessReferenceDoc.setBitstream(bitstreamService.find(context, UUID.fromString(rest.getBitstreamRest().getId())));
        }
        if(rest.getItemname() != null) {
            workflowProcessReferenceDoc.setItemname(rest.getItemname());
        }
        if(rest.getCreatedate()!=null){
            workflowProcessReferenceDoc.setCreatedate(rest.getCreatedate());
        }
        if (rest.getDocumentsignatorRest() != null && rest.getDocumentsignatorRest().getUuid()!=null && !rest.getDocumentsignatorRest().getUuid().toString().isEmpty()) {
            workflowProcessReferenceDoc.setDocumentsignator(ePersonConverter.convert(context, rest.getDocumentsignatorRest()));
        }
        workflowProcessReferenceDoc.setIssignature(rest.getIssignature());
        return  workflowProcessReferenceDoc;
    }
    public WorkflowProcessReferenceDoc convertByService(Context context, WorkflowProcessReferenceDocRest rest) throws SQLException {
        return  workflowProcessReferenceDocService.find(context,UUID.fromString(rest.getUuid()));
    }
    public WorkflowProcessReferenceDoc convert(WorkflowProcessReferenceDocRest obj, Context context) throws Exception {
        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = new WorkflowProcessReferenceDoc();
        if(obj.getSubject()!=null){
            workflowProcessReferenceDoc.setSubject(obj.getSubject());
        }
        if(obj.getReferenceNumber()!=null){
            workflowProcessReferenceDoc.setReferenceNumber(obj.getReferenceNumber());
        }
        if(obj.getInitdate()!=null) {
            workflowProcessReferenceDoc.setInitdate(obj.getInitdate());
        }
        if(obj.getEditortext()!=null){
            byte[] bytes = obj.getEditortext().getBytes("UTF-8");
            String string = new String(bytes, "UTF-8");
            workflowProcessReferenceDoc.setEditortext(string);
        }
        if(obj.getWorkFlowProcessReferenceDocType() != null) {
            workflowProcessReferenceDoc.setWorkFlowProcessReferenceDocType(workFlowProcessMasterValueConverter.convert(context, obj.getWorkFlowProcessReferenceDocType()));
        }
        if(obj.getLatterCategoryRest() != null) {
            workflowProcessReferenceDoc.setLatterCategory(workFlowProcessMasterValueConverter.convert(context, obj.getLatterCategoryRest()));
        }
        if(obj.getBitstreamRest()!= null){
            workflowProcessReferenceDoc.setBitstream(bitstreamService.find(context,UUID.fromString(obj.getBitstreamRest().getId())));
        }
        if(obj.getDrafttypeRest()!= null){
            workflowProcessReferenceDoc.setDrafttype(workFlowProcessMasterValueConverter.convert(context, obj.getDrafttypeRest()));
        }
        if(obj.getDescription()!=null){
            workflowProcessReferenceDoc.setDescription(obj.getDescription());
        }
        return  workflowProcessReferenceDoc;
    }
}
