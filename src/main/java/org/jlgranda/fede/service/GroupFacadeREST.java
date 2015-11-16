/*
 * Copyright (C) 2015 jlgranda
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jlgranda.fede.service;

import com.jlgranda.fede.ejb.GroupService;
import com.jlgranda.fede.ejb.SubjectService;
import org.jpapi.model.Group;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 *
 * @author jlgranda
 */
@Stateless
@Path("grupos")
public class GroupFacadeREST {
    @EJB
    private GroupService groupService;
    @EJB
    private SubjectService subjectService;

    public GroupFacadeREST() {
        super();
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public void create(Group entity) {
        groupService.save(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/xml", "application/json"})
    public void edit(@PathParam("id") Long id, Group entity) {
        groupService.save(id, entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Long id) {
        groupService.remove(id, null);
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    public Group find(@PathParam("id") Long id) {
        return groupService.find(id);
    }

    @GET
    @Path("{subjectId}")
    @Produces({"application/xml", "application/json"})
    public List<Group> listar(@PathParam(value = "subjectId") final Long subjectId) {
        
        return groupService.findAllByOwner(subjectService.find(subjectId));
    }
}
