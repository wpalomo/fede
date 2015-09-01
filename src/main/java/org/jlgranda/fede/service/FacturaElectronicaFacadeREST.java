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

import com.jlgranda.fede.ejb.FacturaElectronicaService;
import com.jlgranda.fede.ejb.SubjectService;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import javax.ejb.Asynchronous;
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
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import org.jlgranda.fede.model.document.FacturaElectronica;

/**
 *
 * @author jlgranda
 */
@Stateless
@Path("factura")
public class FacturaElectronicaFacadeREST {

    @EJB
    private FacturaElectronicaService facturaElectronicaService;
    @EJB
    private SubjectService subjectService;

    public FacturaElectronicaFacadeREST() {
        super();
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public void create(FacturaElectronica entity) {
        facturaElectronicaService.save(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/xml", "application/json"})
    public void edit(@PathParam("id") Long id, FacturaElectronica entity) {
        facturaElectronicaService.save(id, entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Long id) {
        facturaElectronicaService.remove(id, null);
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    public FacturaElectronica find(@PathParam("id") Long id) {
        return facturaElectronicaService.find(id);
    }

    @GET
    @Path("lista/{subjectId}/{tag}/{start}/{end}")
    @Produces({"application/xml", "application/json"})
    public List<FacturaElectronica> listar(@PathParam(value = "subjectId") final Long subjectId, 
            @PathParam(value = "tag") final String tag, 
            @PathParam(value = "start") final long startInMilles, 
            @PathParam(value = "end") final long endInMilles) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startInMilles);
        Date start = c.getTime();
        
        c.setTimeInMillis(endInMilles);
        Date end = c.getTime();
        
        return facturaElectronicaService.listarFacturasElectronicas(tag, subjectService.find(subjectId), start, end);
    }

//    @GET
//    @Path("{from}/{to}")
//    @Produces({"application/xml", "application/json"})
//    public List<FacturaElectronica> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
//        return super.findRange(new int[]{from, to});
//    }
//
//    @GET
//    @Path("count")
//    @Produces("text/plain")
//    public String countREST() {
//        return String.valueOf(super.count());
//    }
//
}
