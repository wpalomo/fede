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

import com.jlgranda.fede.ejb.GroupService;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import org.jpapi.model.CodeType;
import org.jpapi.model.Group;
import org.jpapi.model.StatusType;
import org.jpapi.model.profile.Subject;
import org.jpapi.util.Dates;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador de entidades GGroup
 * @author jlgranda
 */
@Named
@RequestScoped
public class GroupHome extends FedeController implements Serializable {
    
    private static final long serialVersionUID = -1007161141552849702L;
    
    Logger logger = LoggerFactory.getLogger(GroupHome.class);
    
    @EJB
    GroupService groupService;
    

    @Override
    public void handleReturn(SelectEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    public void createDefaultGroups(Subject subject) {
        
        Map<String, String> props = new HashMap<String, String>();
        
        //email settings
        props.put("fede", "fede");
        props.put("salud", "Salud");
        props.put("alimentos", "Alimentos");
        props.put("ropa", "Ropa");
        props.put("educacion", "Educación");
        props.put("vivienda", "Vivienda");

        Group group = null;
        String value = null;
        for (String key : props.keySet()){
            value = props.get(key);
            group = groupService.createInstance();
            group.setCode(key);
            group.setName(value);
            group.setOwner(subject);
            
            groupService.save(group);
            
            logger.info("Added group id: {}, code: {}, name: [{}]", group.getId(), group.getCode(), group.getName());
        }
    }
}
