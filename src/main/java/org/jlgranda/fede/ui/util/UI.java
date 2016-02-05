/*
 * Copyright (C) 2016 jlgranda
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
package org.jlgranda.fede.ui.util;

import com.jlgranda.fede.ejb.BussinesEntityService;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.jlgranda.fede.cdi.Web;
import org.jlgranda.fede.model.management.Organization;
import org.jpapi.util.I18nUtil;

/**
 *
 * @author jlgranda
 */
@ManagedBean(name = "ui")
@RequestScoped
public class UI {
    
    @Inject
    protected BussinesEntityService bussinesEntityService;

    @PostConstruct
    public void init() {
    }
    
    public Organization.Type[] getOrganizationTypes() {
        return Organization.Type.values();
    }
    
    public List<SelectItem> getOrganizationTypesAsSelectItem() {
        List<SelectItem> items = new ArrayList<SelectItem>();
        SelectItem item = null;
        for (Organization.Type t : getOrganizationTypes()) {
            item = new SelectItem(t, I18nUtil.getMessages(t.name()));
            items.add(item);
        }
        return items;
    }
    
     public static SelectItem[] getSelectItems(List<?> entities, boolean selectOne) {
        int size = selectOne ? entities.size() + 1 : entities.size();
        SelectItem[] items = new SelectItem[size];
        int i = 0;
        if (selectOne) {
            items[0] = new SelectItem("", "---");
            i++;
        }
        for (Object x : entities) {
            items[i++] = new SelectItem(x, x.toString());
        }
        return items;
    }
    

    public List<SelectItem> getValuesAsSelectItem(List<Object> values) {
        List<SelectItem> items = new ArrayList<SelectItem>();
        SelectItem item = null;
        item = new SelectItem(null, I18nUtil.getMessages("common.choice"));
        items.add(item);
        for (Object o : values) {
            item = new SelectItem(cleanValue(o), cleanValue(o).toString());
            items.add(item);
        }

        return items;
    }

    private Object cleanValue(Object value) {
        
        if (value == null) {
            return null;
        }
        if (!(value instanceof String)){
            return value;
        }
        
        String cleaned = value.toString();

        if (cleaned.contains("*")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        return cleaned;
    }
}
