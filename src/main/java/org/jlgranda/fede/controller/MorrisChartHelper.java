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

import com.jlgranda.fede.ejb.FacturaElectronicaService;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.jlgranda.fede.model.document.FacturaElectronica;
import org.jpapi.model.profile.Subject;
/**
 * Helper para impresión de objetos de gráficas Morris
 * @see http://morrisjs.github.io/morris.js/bars.html
 * @author jlgranda
 */
@ManagedBean
@SessionScoped
public class MorrisChartHelper {
    
    @EJB
    private FacturaElectronicaService facturaElectronicaService;

    public String printLine(String tag, Subject owner, Date start, Date end){
        List<FacturaElectronica> datas = new ArrayList<>();
        datas = facturaElectronicaService.listarFacturasElectronicas(tag, owner, start, end);
        StringBuilder morrisChart = new StringBuilder();
        morrisChart.append("new Morris.Line({\n")
                .append("element: 'dashboard-chart-" + tag + "',\n")
                .append("data: [");
        //Agregar datos aquí
        for (FacturaElectronica s : datas){
             morrisChart.append("{date: '" + s.getFechaEmision() + "', value: " + s.getImporteTotal() + "},\n");
        }
        morrisChart.append("],\n")
                .append("xkey: 'date',\n")
                .append("ykeys: ['value'],\n")
                .append("labels: ['Importe total']\n")
                .append("});");
        
        //Sample print
//        return "new Morris.Line({\n" +
//"                        // ID of the element in which to draw the chart.\n" +
//"                        element: 'dashboard-chart-#{tag.code}',\n" +
//"                        // Chart data records -- each entry in this array corresponds to a point on\n" +
//"                        // the chart.\n" +
//"                        data: [\n" +
//"                            {year: '2008', value: 20},\n" +
//"                            {year: '2009', value: 10},\n" +
//"                            {year: '2010', value: 5},\n" +
//"                            {year: '2011', value: 5},\n" +
//"                            {year: '2012', value: 20}\n" +
//"                        ],\n" +
//"                        // The name of the data record attribute that contains x-values.\n" +
//"                        xkey: 'year',\n" +
//"                        // A list of names of data record attributes that contain y-values.\n" +
//"                        ykeys: ['value'],\n" +
//"                        // Labels for the ykeys -- will be displayed when you hover over the\n" +
//"                        // chart.\n" +
//"                        labels: ['Value']\n" +
//"                    });";
        return morrisChart.toString();
    }
}
