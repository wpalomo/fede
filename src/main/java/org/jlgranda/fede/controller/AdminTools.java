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

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.jlgranda.fede.cdi.BussinesEntityRepository;
import com.google.common.base.Throwables;

/**
 *
 * @author jlgranda
 */
@ManagedBean
public class AdminTools implements Serializable {
    private static final long serialVersionUID = 8204586602687291604L;

    
    @Inject @BussinesEntityRepository EntityManager em;
    
    private String query;
    
    private String result;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
    
    
    public void executeQuery(){
        try{
        Query query = em.createQuery(getQuery());
        result = query.getResultList().isEmpty() ? "" : query.getResultList().toString();
        } catch (Exception e){
            result = getExceptionMessage(e, 404);
        }
    }
    
    private String getExceptionMessage(Throwable throwable, Integer statusCode) {
        if (throwable != null) {
            throwable.printStackTrace();
            return Throwables.getRootCause(throwable).getMessage();
        }
        return "Error!";
    }
    
}