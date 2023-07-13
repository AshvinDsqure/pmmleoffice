/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import com.google.gson.Gson;
import org.dspace.app.rest.model.WorkFlowProcessDefinitionRest;
import org.dspace.app.rest.model.WorkflowItemRest;
import org.dspace.app.rest.model.LoginCounterRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.WorkflowProcessDefinition;
import org.dspace.content.LoginCounter;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class LoginCounterConverter extends DSpaceObjectConverter<LoginCounter, LoginCounterRest> {

    @Override
    public Class<LoginCounter> getModelClass() {

        return LoginCounter.class;
    }

    @Autowired
    ModelMapper modelMapper;


    @Override
    protected LoginCounterRest newInstance() {
        return new LoginCounterRest();
    }

    @Override
    public LoginCounterRest convert(LoginCounter obj, Projection projection) {
        LoginCounterRest rest = new LoginCounterRest();
        rest = modelMapper.map(obj, LoginCounterRest.class);
        rest.setUuid(obj.getID().toString());
        return rest;
    }

    public LoginCounter convert(LoginCounter obj, LoginCounterRest rest) {
        obj = modelMapper.map(rest, LoginCounter.class);
        return obj;
    }

    public LoginCounter convert(LoginCounterRest rest) {
        LoginCounter obj = null;
        if (rest != null) {
            obj = new LoginCounter();
            obj = modelMapper.map(rest, LoginCounter.class);
        }
        return obj;
    }

}
