/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.dspace.app.rest.model.BitstreamRest;
import org.dspace.app.rest.model.CheckSumRest;
import org.dspace.app.rest.model.WorkflowProcessReferenceDocRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.app.rest.utils.FileUtils;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.WorkflowProcessReferenceDoc;
import org.dspace.content.service.BitstreamService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * This is the converter from/to the Bitstream in the DSpace API data model and the REST data model
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@Component
public class BitstreamConverter extends DSpaceObjectConverter<Bitstream, BitstreamRest> {

    @Autowired
    BitstreamService bitstreamService;

    @Override
    public BitstreamRest convert(org.dspace.content.Bitstream obj, Projection projection) {
        BitstreamRest b = super.convert(obj, projection);
        b.setSequenceId(obj.getSequenceID());
        List<Bundle> bundles = null;
        try {
            bundles = obj.getBundles();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        if (bundles != null && bundles.size() > 0) {
            b.setBundleName(bundles.get(0).getName());
        }
        CheckSumRest checksum = new CheckSumRest();
        checksum.setCheckSumAlgorithm(obj.getChecksumAlgorithm());
        checksum.setValue(obj.getChecksum());
        b.setCheckSum(checksum);
        b.setSizeBytes(obj.getSizeBytes());
        b.setName(FileUtils.getNameWithoutExtension(obj.getName()));
        return b;
    }
    public BitstreamRest convert1(org.dspace.content.Bitstream obj, Projection projection) {
        BitstreamRest b = new BitstreamRest();
        b.setSequenceId(obj.getSequenceID());
        List<Bundle> bundles = null;
        try {
            bundles = obj.getBundles();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        if (bundles != null && bundles.size() > 0) {
            b.setBundleName(bundles.get(0).getName());
        }
        CheckSumRest checksum = new CheckSumRest();
        checksum.setCheckSumAlgorithm(obj.getChecksumAlgorithm());
        checksum.setValue(obj.getChecksum());
        b.setCheckSum(checksum);
        b.setSizeBytes(obj.getSizeBytes());
        b.setName(FileUtils.getNameWithoutExtension(obj.getName()));
        return b;
    }
    public Bitstream convertByService(Context context, BitstreamRest rest) throws SQLException {
        return  bitstreamService.find(context, UUID.fromString(rest.getUuid()));
    }
    public BitstreamRest convertFoWorkFLowRefDoc(org.dspace.content.Bitstream obj, Projection projection) {
        BitstreamRest b = super.convert(obj, projection);
        b.setName(FileUtils.getNameWithoutExtension(obj.getName()));
        return b;
    }
    @Override
    protected BitstreamRest newInstance() {
        return new BitstreamRest();
    }

    @Override
    public Class<Bitstream> getModelClass() {
        return Bitstream.class;
    }
}
