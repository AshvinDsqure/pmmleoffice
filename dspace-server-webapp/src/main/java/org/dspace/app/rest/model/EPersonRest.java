/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import org.dspace.app.rest.RestResourceController;


/**
 * The EPerson REST Resource
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@LinksRest(links = {
        @LinkRest(
                name = EPersonRest.GROUPS,
                method = "getGroups"
        )
})
public class EPersonRest extends DSpaceObjectRest {
    public static final String NAME = "eperson";
    public static final String CATEGORY = RestAddressableModel.EPERSON;

    public static final String GROUPS = "groups";

    private String netid;

    private Date lastActive;

    private boolean canLogIn;

    private String email;

    private boolean requireCertificate = false;

    private Boolean selfRegistered;
    @JsonProperty
    private  String  employeeid;

    @JsonProperty
    private  String  epersonmapping;

    @JsonProperty
    private  Integer  tablenumber;
    @JsonProperty
    private WorkFlowProcessMasterValueRest officeRest=null;
    @JsonProperty
    private WorkFlowProcessMasterValueRest departmentRest=null;
    @JsonProperty
    private WorkFlowProcessMasterValueRest designationRest=null;

    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;
    @JsonProperty
    private String fullname;

    @Override
    @JsonProperty(access = Access.READ_ONLY)
    public String getType() {
        return NAME;
    }

    public String getNetid() {
        return netid;
    }

    public void setNetid(String netid) {
        this.netid = netid;
    }

    public Date getLastActive() {
        return lastActive;
    }

    public void setLastActive(Date lastActive) {
        this.lastActive = lastActive;
    }

    public boolean isCanLogIn() {
        return canLogIn;
    }

    public void setCanLogIn(boolean canLogIn) {
        this.canLogIn = canLogIn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isRequireCertificate() {
        return requireCertificate;
    }

    public void setRequireCertificate(boolean requireCertificate) {
        this.requireCertificate = requireCertificate;
    }

    public Boolean isSelfRegistered() {
        return selfRegistered;
    }

    public void setSelfRegistered(Boolean selfRegistered) {
        this.selfRegistered = selfRegistered;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public Class getController() {
        return RestResourceController.class;
    }

    public String getEmployeeid() {
        return employeeid;
    }

    public void setEmployeeid(String employeeid) {
        this.employeeid = employeeid;
    }

    public Integer getTablenumber() {
        return tablenumber;
    }

    public void setTablenumber(Integer tablenumber) {
        this.tablenumber = tablenumber;
    }

    public WorkFlowProcessMasterValueRest getOfficeRest() {
        return officeRest;
    }

    public void setOfficeRest(WorkFlowProcessMasterValueRest officeRest) {
        this.officeRest = officeRest;
    }

    public WorkFlowProcessMasterValueRest getDepartmentRest() {
        return departmentRest;
    }

    public void setDepartmentRest(WorkFlowProcessMasterValueRest departmentRest) {
        this.departmentRest = departmentRest;
    }

    public WorkFlowProcessMasterValueRest getDesignationRest() {
        return designationRest;
    }

    public void setDesignationRest(WorkFlowProcessMasterValueRest designationRest) {
        this.designationRest = designationRest;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEpersonmapping() {
        return epersonmapping;
    }

    public void setEpersonmapping(String epersonmapping) {
        this.epersonmapping = epersonmapping;
    }
}
