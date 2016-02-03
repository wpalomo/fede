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
package org.jlgranda.fede.ui.model;

import java.io.Serializable;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.jpapi.model.profile.Subject;
import org.primefaces.model.LazyDataModel;

/**
 * 
 * @author jlgranda
 */
public abstract class ListService<T extends Object> extends LazyDataModel<T> implements Serializable{
    private static final long serialVersionUID = -7643921510198997417L;
    
    protected static final int MAX_RESULTS = 5;
    
    
    
    private Subject subject;
    private Long subjectId;

    //Todo inicializar el objeto comun del modelo 
}