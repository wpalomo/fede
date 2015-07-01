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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlgranda
 */
public abstract class FedeController {
    
    Logger  logger = LoggerFactory.getLogger(FedeController.class);

    /**
     * Gets the http servlet request.
     *
     * @return the http servlet request
     */
    public HttpServletRequest getHttpServletRequest() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        return request;
    }

    /**
     * Gets the http session.
     *
     * @return the http session
     */
    public HttpSession getHttpSession() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
        return session;
    }

    //Seccion Mensages de Informacion
    /**
     * Adds the info message.
     *
     * @param msg the msg
     * @param submensaje the submensaje
     */
    public void addInfoMessage(String msg, String submensaje) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, submensaje);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    //Seccion Mensajes de Exito
    /**
     * Adds the success message.
     *
     * @param msg the msg
     * @param submensaje the submensaje
     */
    public void addSuccessMessage(String msg, String submensaje) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, submensaje);
        FacesContext.getCurrentInstance().addMessage("successInfo", facesMsg);
    }

    //Seccion Mensajes de Advertencia
    /**
     * Adds the warning message.
     *
     * @param msg the msg
     * @param submensaje the submensaje
     */
    public void addWarningMessage(String msg, String submensaje) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, msg, submensaje);
        FacesContext.getCurrentInstance().addMessage("successInfo", facesMsg);
    }

    //Seccion Mensajes Error
    /**
     * Adds the error message.
     *
     * @param ex the ex
     * @param defaultMsg the default msg
     */
    public void addErrorMessage(Exception ex, String defaultMsg) {
        String msg = ex.getLocalizedMessage();

        if ((msg != null) && (msg.length() > 0)) {
            addErrorMessage(msg, "");
        } else {
            addErrorMessage(defaultMsg, "");
        }
    }

    /**
     * Adds the error messages.
     *
     * @param messages the messages
     */
    public void addErrorMessages(List<String> messages) {

        for (String message : messages) {
            addErrorMessage(message, "");
        }
    }

    /**
     * Adds the error message.
     *
     * @param msg the msg
     * @param submensaje the submensaje
     */
    public void addErrorMessage(String msg, String submensaje) {

        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, submensaje);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    /**
     * Adds the error message.
     *
     * @param context the context
     * @param msg the msg
     * @param submensaje the submensaje
     */
    public void addErrorMessage(FacesContext context, String msg, String submensaje) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, submensaje);
        context.addMessage(null, facesMsg);
    }

    /**
     * Gets the request parameter.
     *
     * @param key the key
     * @return the request parameter
     */
    public String getRequestParameter(String key) {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(key);
    }

    /**
     * Gets the object from request parameter.
     *
     * @param requestParameterName the request parameter name
     * @param converter the converter
     * @param component the component
     * @return the object from request parameter
     */
    public Object getObjectFromRequestParameter(String requestParameterName, Converter converter, UIComponent component) {
        String theId = this.getRequestParameter(requestParameterName);

        return converter.getAsObject(FacesContext.getCurrentInstance(), component, theId);
    }
    
    ////////////////////////////////////////////////////////////////////////
    // Popups general management
    ////////////////////////////////////////////////////////////////////////
    /**
     * Abre la ventana emergente indicada por popupName con el ancho y alto especificado
     * @param name nombre de la ventana emergente
     * @param width ancho de la ventana emergente
     * @param height alto de la ventana emergente
     * @param modal indica si la ventana emergente debe ser modal o no
     */
    protected void openDialog(String name, int width, int height, boolean modal) {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", modal);
        options.put("draggable", false);
        options.put("resizable", true);
        options.put("contentWidth", width);
        options.put("contentHeight", height);
        options.put("closable", true);
        //options.put("includeViewParams", false);

//        Map<String, List<String>> params = new HashMap<String, List<String>>();
//        List<String> values = new ArrayList<String>();
//        values.add(bookName);
//        params.put("bookName", values);
        RequestContext.getCurrentInstance().openDialog(name, options, null);
        logger.info("Popup '{}' abierto, con opciones {}. Context: {}", name, options, RequestContext.getCurrentInstance());
    }
    
    public void closeDialog(Object data){
        RequestContext.getCurrentInstance().closeDialog(data);
        logger.info("Popup '{}' cerrado, con data {}. Context: {}", "activo", data, RequestContext.getCurrentInstance());
    }
            
    public abstract void handleReturn(SelectEvent event);
}
