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

import com.jlgranda.fede.ejb.BussinesEntityService;
import com.jlgranda.fede.ejb.FacturaElectronicaService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.jlgranda.fede.model.document.FacturaElectronica;
import org.jlgranda.fede.model.document.FacturaElectronica_;
import org.jpapi.model.BussinesEntity;
import org.jpapi.model.BussinesEntityType;
import org.jpapi.model.BussinesEntity_;
import org.jpapi.model.profile.Subject;
import org.jpapi.util.I18nUtil;
import org.jpapi.util.QueryData;
import org.jpapi.util.QuerySortOrder;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlgranda
 */
public class LazyFacturaElectronicaDataModel extends LazyDataModel<FacturaElectronica> implements Serializable {

    private static final int MAX_RESULTS = 5;
    Logger  logger = LoggerFactory.getLogger(LazyFacturaElectronicaDataModel.class);

    private FacturaElectronicaService bussinesEntityService;
    
    private List<FacturaElectronica> resultList;
    private int firstResult = 0;
    
    private BussinesEntityType type;
    
    private Subject owner;
    /**
     * Lista de etiquetas para filtrar facturas
     */
    private String tags;
    
    /**
     * Inicio del rango de fecha
     */
    private Date start;
    
    /**
     * Fin del rango de fecha
     */
    private Date end;
    
    private String typeName;
    private BussinesEntity[] selectedBussinesEntities;
    private BussinesEntity selectedBussinesEntity; //Filtro de cuenta schema
    
    private String filterValue;

    public LazyFacturaElectronicaDataModel(FacturaElectronicaService bussinesEntityService) {
        setPageSize(MAX_RESULTS);
        resultList = new ArrayList<>();
        this.bussinesEntityService = bussinesEntityService;
    }

    @PostConstruct
    public void init() {
    }

    public List<FacturaElectronica> getResultList() {
        logger.info("load BussinesEntitys");

        if (resultList.isEmpty()/* && getSelectedBussinesEntity() != null*/) {
            resultList = bussinesEntityService.find(this.getPageSize(), this.getFirstResult());
        }
        return resultList;
    }

    public int getNextFirstResult() {
        return firstResult + this.getPageSize();
    }

    public int getPreviousFirstResult() {
        return this.getPageSize() >= firstResult ? 0 : firstResult - this.getPageSize();
    }

    public Integer getFirstResult() {
        return firstResult;
    }

    public Subject getOwner() {
        return owner;
    }

    public void setOwner(Subject owner) {
        this.owner = owner;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public BussinesEntityType getType() {
        //if (type == null){
        //   setType(bussinesEntityService.findBussinesEntityTypeByName(getTypeName()));
        //}
        return type;
    }

    public void setType(BussinesEntityType type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setFirstResult(Integer firstResult) {
        logger.info("set first result + firstResult");
        this.firstResult = firstResult;
        this.resultList = null;
    }

    public boolean isPreviousExists() {
        return firstResult > 0;
    }

    public boolean isNextExists() {
        return bussinesEntityService.count() > this.getPageSize() + firstResult;
    }

    @Override
    public FacturaElectronica getRowData(String rowKey) {
        return bussinesEntityService.find(Long.valueOf(rowKey));
    }

    @Override
    public Object getRowKey(FacturaElectronica entity) {
        System.err.println("//--> getRowKey:entity" + entity);
        return entity.getName();
    }

    @Override
    public List<FacturaElectronica> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {

        int end = first + pageSize;

        QuerySortOrder order = QuerySortOrder.DESC;
        if (sortOrder == SortOrder.ASCENDING) {
            order = QuerySortOrder.ASC;
        }
        Map<String, Object> _filters = new HashMap<>();
        Map<String, Date> range = new HashMap<>();
        range.put("start", getStart());
        range.put("end", getEnd());
        //_filters.put(BussinesEntity_.type.getName(), getType()); //Filtro por defecto
        _filters.put(FacturaElectronica_.owner.getName(), getOwner()); //Filtro por defecto
        _filters.put(FacturaElectronica_.fechaEmision.getName(), range); //Filtro de fecha inicial
        _filters.put("tag", getTags()); //Filtro de etiquetas
        if (getFilterValue() != null && !getFilterValue().isEmpty()){
            _filters.put("keyword", getFilterValue()); //Filtro general
        }
        
        _filters.putAll(filters);
        
        if (sortField == null){
            sortField = FacturaElectronica_.fechaEmision.getName();
        }

        QueryData<FacturaElectronica> qData = bussinesEntityService.find(first, end, sortField, order, _filters);
        this.setRowCount(qData.getTotalResultCount().intValue());
        return qData.getResult();
    }

    public BussinesEntity[] getSelectedBussinesEntities() {
        return selectedBussinesEntities;
    }

    public void setSelectedBussinesEntities(BussinesEntity[] selectedBussinesEntities) {
        this.selectedBussinesEntities = selectedBussinesEntities;
    }

    public BussinesEntity getSelectedBussinesEntity() {
        return selectedBussinesEntity;
    }

    public void setSelectedBussinesEntity(BussinesEntity selectedBussinesEntity) {
        this.selectedBussinesEntity = selectedBussinesEntity;
    }
}
