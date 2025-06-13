/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.dspace.app.rest.converter.EPersonConverter;
import org.dspace.app.rest.model.EPersonRest;
import org.dspace.app.rest.model.GroupRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toList;

/**
 * Link repository for "epersons" subresource of an individual group.
 */
@Component(GroupRest.CATEGORY + "." + GroupRest.NAME + "." + GroupRest.EPERSONS)
public class GroupEPersonLinkRepository extends AbstractDSpaceRestRepository
        implements LinkRestRepository {

    @Autowired
    GroupService groupService;

    @Autowired
    EPersonService ePersonService;


    @Autowired
    EPersonConverter ePersonConverter;

    @PreAuthorize("hasPermission(#groupId, 'GROUP', 'READ')")
    public Page<GroupRest> getMembers(@Nullable HttpServletRequest request,
                                     UUID groupId,
                                     @Nullable Pageable optionalPageable,
                                     Projection projection) {
        try {
            Context context = obtainContext();
            System.out.println(":::getMembers:::::::");
            Group group = groupService.find(context, groupId);
            if (group == null) {
                throw new ResourceNotFoundException("No such group: " + groupId);
            }
            int total=group.getMembers().size();
            List<EPersonRest> rests=new ArrayList<>();
            List<EPerson> list=ePersonService.findGroupMembers(context,groupId,Math.toIntExact(optionalPageable.getOffset()), Math.toIntExact(optionalPageable.getPageSize()));
            System.out.println("size:::"+list.size());
            System.out.println("total:::"+total);
            rests = list.stream().map(d -> {
                return ePersonConverter.convert(d, utils.obtainProjection());
            }).collect(toList());
            return new PageImpl(rests, optionalPageable, total);
           // return converter.toRestPage(list, optionalPageable, projection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
