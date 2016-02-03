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

import com.jlgranda.fede.ejb.SettingService;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;
import org.jlgranda.fede.cdi.LoggedIn;
import org.jpapi.model.Setting;
import org.jpapi.model.profile.Subject;

/**
 *
 * @author jlgranda
 */
@ManagedBean
@SessionScoped
public class SettingHome implements Serializable {
    
    @Inject
    @LoggedIn
    private Subject subject;
    
    @EJB
    private SettingService settingService;
    
    public String getValue(String name, String defaultValue){
        Setting s = settingService.findByName(name, subject);
        if (s == null){ //No existe configuración de usuario, tomar la configuración global, sino el valor por defecto
            return getGlobalValue(name, defaultValue);
        }
        return s.getValue();
    }
    
    public String getGlobalValue(String name, String defaultValue){
        Setting s = settingService.findByName(name, null);
        if (s == null)
            return defaultValue;
        return s.getValue();
    }
}
