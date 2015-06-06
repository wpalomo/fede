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
package org.jlgranda.fede.controller;

import javax.ejb.EJB;
import com.jlgranda.fede.ejb.FacturaElectronicaService;
import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.ConversationScoped;
import javax.faces.bean.ManagedBean;
import org.jlgranda.fede.model.document.FacturaElectronica;

/**
 *
 * @author jlgranda
 */
@ManagedBean
@ConversationScoped
public class FacturaElectronicaHome implements Serializable {
    private static final long serialVersionUID = -8639341517802129909L;
    
    @EJB
    private FacturaElectronicaService service;
    
    /**
     * Obtener todas las facturas disponibles en el sistema
     * @return 
     */
    public List<FacturaElectronica> findAll(){
        return service.getFacturasElectronicas();
    }
}
