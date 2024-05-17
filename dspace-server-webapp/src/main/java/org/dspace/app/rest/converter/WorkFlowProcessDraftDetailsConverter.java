/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.WorkFlowProcessDraftDetailsRest;
import org.dspace.app.rest.model.WorkflowProcessSenderDiaryRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.app.rest.utils.DateUtils;
import org.dspace.content.WorkFlowProcessDraftDetails;
import org.dspace.content.service.WorkFlowProcessDraftDetailsService;
import org.dspace.core.Context;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class WorkFlowProcessDraftDetailsConverter extends DSpaceObjectConverter<WorkFlowProcessDraftDetails, WorkFlowProcessDraftDetailsRest> {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    EPersonConverter ePersonConverter;
    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;
    @Autowired
    ItemConverter itemConverter;

    @Autowired
    WorkFlowProcessDraftDetailsService workFlowProcessDraftDetailsService;


    @Autowired
    WorkFlowProcessInwardDetailsConverter workFlowProcessInwardDetailsConverter;

    @Override
    public Class<WorkFlowProcessDraftDetails> getModelClass() {
        return WorkFlowProcessDraftDetails.class;
    }

    @Override
    protected WorkFlowProcessDraftDetailsRest newInstance() {
        return new WorkFlowProcessDraftDetailsRest();
    }

    @Override
    public WorkFlowProcessDraftDetailsRest convert(WorkFlowProcessDraftDetails obj, Projection projection) {
        WorkFlowProcessDraftDetailsRest rest = new WorkFlowProcessDraftDetailsRest();
        List<WorkflowProcessSenderDiaryRest> workflowProcessSenderDiaryRests = new ArrayList<>();
        if (obj.getDrafttype() != null) {
            rest.setDrafttypeRest(workFlowProcessMasterValueConverter.convert(obj.getDrafttype(), projection));
        }
        if (obj.getDocumentsignator() != null) {
            rest.setDocumentsignatorRest(ePersonConverter.convert(obj.getDocumentsignator(), projection));
        }
        //for reply tapal
        if (obj.getConfidential() != null) {
            rest.setConfidentialRest(workFlowProcessMasterValueConverter.convert(obj.getConfidential(), projection));
        }
        if (obj.getReplytype() != null) {
            rest.setReplytypeRest(workFlowProcessMasterValueConverter.convert(obj.getReplytype(), projection));
        }

        if (obj.getConfidential() != null) {
            rest.setConfidentialRest(workFlowProcessMasterValueConverter.convert(obj.getConfidential(), projection));
        }

        if (obj.getDraftnature() != null) {
            rest.setDraftnatureRest(workFlowProcessMasterValueConverter.convert(obj.getDraftnature(), projection));
        }
        if (!DateUtils.isNullOrEmptyOrBlank(obj.getReferencefilenumber())) {
            rest.setReferencefilenumberRest(obj.getReferencefilenumber());
        }
        if (obj.getReferencetapalnumber() != null) {
            rest.setReferencetapalnumberRest(workFlowProcessInwardDetailsConverter.convert(obj.getReferencetapalnumber(), projection));
        }
        if (!DateUtils.isNullOrEmptyOrBlank(obj.getSubject())) {
            rest.setSubject(obj.getSubject());
        }
        if (obj.getDraftdate() != null) {
            rest.setDraftdate(obj.getDraftdate());
        }
        if (!DateUtils.isNullOrEmptyOrBlank(obj.getID().toString())) {
            rest.setUuid(obj.getID().toString());
        }
        if (obj.getIssinglatter() != null) {
            rest.setIssinglatter(obj.getIssinglatter());
        }
        if (obj.getIsdispatchbyself() != null) {
            rest.setIsdispatchbyself(obj.getIsdispatchbyself());
        }
        if (obj.getIsdispatchbycru() != null) {
            rest.setIsdispatchbycru(obj.getIsdispatchbycru());
        }
        if (obj.getIsdelete() != null) {
            rest.setIsdelete(obj.getIsdelete());
        }
        //sap
        if(obj.getIssapdoc()!=null){
            rest.setIssapdoc(obj.getIssapdoc());
        }
        if(!DateUtils.isNullOrEmptyOrBlank(obj.getSapdocumentno())){
            rest.setSapdocumentno(obj.getSapdocumentno());
        }
        if(obj.getSapdocumenttype()!=null){
            rest.setSapdocumenttypeRest(workFlowProcessMasterValueConverter.convert(obj.getSapdocumenttype(), projection));
        }
        return rest;
    }

    public WorkFlowProcessDraftDetails convert(Context context, WorkFlowProcessDraftDetailsRest rest) throws SQLException {
        WorkFlowProcessDraftDetails obj = new WorkFlowProcessDraftDetails();
        if (rest.getDrafttypeRest() != null) {
            obj.setDrafttype(workFlowProcessMasterValueConverter.convert(context, rest.getDrafttypeRest()));
        }
        if (rest.getDocumentsignatorRest() != null && rest.getDocumentsignatorRest().getUuid() != null && !rest.getDocumentsignatorRest().getUuid().toString().isEmpty()) {
            obj.setDocumentsignator(ePersonConverter.convert(context, rest.getDocumentsignatorRest()));
        }
        if (rest.getConfidentialRest() != null) {
            obj.setConfidential(workFlowProcessMasterValueConverter.convert(context, rest.getConfidentialRest()));
        }
        if (rest.getReplytypeRest() != null) {
            obj.setReplytype(workFlowProcessMasterValueConverter.convert(context, rest.getReplytypeRest()));
        }
        if (rest.getConfidentialRest() != null) {
            obj.setConfidential(workFlowProcessMasterValueConverter.convert(context, rest.getConfidentialRest()));
        }
        if (rest.getDraftnatureRest() != null) {
            obj.setDraftnature(workFlowProcessMasterValueConverter.convert(context, rest.getDraftnatureRest()));
        }
        if (!DateUtils.isNullOrEmptyOrBlank(rest.getReferencefilenumberRest())) {
            obj.setReferencefilenumber(rest.getReferencefilenumberRest());
        }
        try {
            if (rest.getReferencetapalnumberRest() != null) {
                obj.setReferencetapalnumber(workFlowProcessInwardDetailsConverter.convert(context, rest.getReferencetapalnumberRest()));
            }
        } catch (Exception e) {

        }
        if (rest.getSubject() != null) {
            obj.setSubject(rest.getSubject());
        }
        if (rest.getIssinglatter() != null) {
            obj.setIssinglatter(rest.getIssinglatter());
        }
        if (rest.getIsdispatchbyself() != null) {
            obj.setIsdispatchbyself(rest.getIsdispatchbyself());
        }
        if (rest.getIsdispatchbycru() != null) {
            obj.setIsdispatchbycru(rest.getIsdispatchbycru());
        }
        if (rest.getIsdelete() != null) {
            obj.setIsdelete(rest.getIsdelete());
        }
        //sap
        if(rest.getIssapdoc()!=null){
            obj.setIssapdoc(rest.getIssapdoc());
        }
        if(!DateUtils.isNullOrEmptyOrBlank(rest.getSapdocumentno())){
            obj.setSapdocumentno(rest.getSapdocumentno());
        }
        if(rest.getSapdocumenttypeRest()!=null){
            obj.setSapdocumenttype(workFlowProcessMasterValueConverter.convert(context, rest.getSapdocumenttypeRest()));
        }
        obj.setDraftdate(new Date());
        return obj;
    }

    public WorkFlowProcessDraftDetails convertbyService(Context context, WorkFlowProcessDraftDetailsRest rest) {

        if (rest != null && rest.getUuid() != null && !rest.getUuid().isEmpty() && rest.getUuid().length() != 0) {
            try {
                return workFlowProcessDraftDetailsService.find(context, UUID.fromString(rest.getUuid()));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
