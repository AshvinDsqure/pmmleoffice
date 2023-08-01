/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.GroupRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.UUID;

/**
 * This is the converter from/to the Group in the DSpace API data model
 * and the REST data model
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@Component
public class GroupConverter extends DSpaceObjectConverter<Group, GroupRest> {

    @Autowired
    GroupService groupService;
    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;

    public GroupRest convert(Group obj, Projection projection) {
        GroupRest epersongroup = new GroupRest();
        epersongroup.setPermanent(obj.isPermanent());
        epersongroup.setIsdspace(obj.getIsdspace());
        if(obj.getGrouptype()!=null) {
            epersongroup.setGrouptypeRest(workFlowProcessMasterValueConverter.convert(obj.getGrouptype(), projection));
        } return epersongroup;
    }
    public Group convert(Context context,GroupRest rest) throws SQLException {
       if(rest!=null && rest.getId()!=null){
           return groupService.find(context, UUID.fromString(rest.getId()));
       }
        return null;
    }


    @Override
    protected GroupRest newInstance() {
        return new GroupRest();
    }

    @Override
    public Class<Group> getModelClass() {
        return Group.class;
    }

}
