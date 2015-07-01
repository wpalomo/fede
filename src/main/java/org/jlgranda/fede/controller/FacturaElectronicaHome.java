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
import com.jlgranda.fede.ejb.OrganizacionService;
import com.jlgranda.fede.ejb.SettingService;
import com.jlgranda.fede.ejb.SubjectService;
import com.jlgranda.fede.ejb.mail.reader.FacturaElectronicaMailReader;
import com.jlgranda.fede.ejb.mail.reader.FacturaReader;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.enterprise.context.ConversationScoped;
import javax.faces.bean.ManagedBean;
import javax.mail.MessagingException;
import org.jlgranda.fede.model.document.FacturaElectronica;
import org.jlgranda.fede.sri.jaxb.exception.FacturaXMLReadException;
import org.jlgranda.fede.util.FacturaUtil;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jlgranda.fede.sri.jaxb.factura.v110.Factura;
import org.jpapi.model.CodeType;
import org.jpapi.model.Group;
import org.jpapi.model.management.Organization;
import org.jpapi.model.profile.Subject;
import org.jpapi.util.Dates;
import org.picketlink.idm.credential.Password;
import org.primefaces.event.FileUploadEvent;
import com.jlgranda.fede.SettingNames;
import com.jlgranda.fede.ejb.GroupService;
import org.jpapi.util.Strings;

/**
 *
 * @author jlgranda
 */
@ManagedBean
@ConversationScoped
public class FacturaElectronicaHome extends FedeController implements Serializable {
    
    Logger  logger = LoggerFactory.getLogger(FacturaElectronicaHome.class);
    
    private static final long serialVersionUID = -8639341517802129909L;
    
    @EJB
    private SettingService settingService;
    
    @EJB
    private GroupService groupService;
    
    @EJB
    private FacturaElectronicaService facturaElectronicaService;
    
    @EJB
    private FacturaElectronicaMailReader facturaElectronicaMailReader;
    
    @EJB
    private OrganizacionService organizacionService;
    
    @EJB
    private SubjectService subjectService;
    
    private Group defaultGroup = null;
    
    private String tags;
    
    /**
     * Obtener todas las facturas disponibles en el sistema
     * @return 
     */
    public List<FacturaElectronica> listarFacturasElectronicasParaLineaTiempo(){
        return facturaElectronicaService.listarFacturasElectronicas(Strings.toInt(settingService.findByName("fede.dashboard.timeline.length").getValue()));
    }
    
    /**
     * Obtener todas las facturas disponibles en el sistema
     * @return 
     */
    public List<FacturaElectronica> listarFacturasElectronicas(){
        //Todo procesar tags, cuando vengan separadas por ,
        return listarFacturasElectronicas(getTags());
    }
    
    /**
     * Obtener todas las facturas disponibles en el sistema
     * @param tag agrupación de facturas
     * @return lista de facturas electrónicas
     */
    public List<FacturaElectronica> listarFacturasElectronicas(String tag){
        return facturaElectronicaService.listarFacturasElectronicas(tag);
    }
    
    /**
     * TODO !IMPLEMENTACION TEMPORAL
     * @return 
     */
    public BigDecimal getImporteTotal(){
        BigDecimal total = new BigDecimal(0);
        for (FacturaElectronica fe : facturaElectronicaService.listarFacturasElectronicas()){
            total = total.add(fe.getImporteTotal());
        }
        return total;
    }
    
    public void mostrarFormularioCargaFacturaElectronica() {
        super.openDialog(SettingNames.POPUP_SUBIR_FACTURA_ELECTRONICA, 800, 600, true);
    }

    
    public List<FacturaElectronica> importarDesdeInbox(){
        List<FacturaElectronica> result = new ArrayList<>();
        try {
            for (FacturaReader fr : facturaElectronicaMailReader.getFacturasElectronicas(null)){ //Por defecto para pruebas
                result.add(procesarFactura(fr));
            }
            this.addSuccessMessage("Excelente!", "Se leyeron " + result.size() + " mensajes de correo!");
        } catch (MessagingException ex) {
            java.util.logging.Logger.getLogger(FacturaElectronicaHome.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(FacturaElectronicaHome.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FacturaXMLReadException ex) {
            java.util.logging.Logger.getLogger(FacturaElectronicaHome.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public void handleFileUpload(FileUploadEvent event) {
        procesarUploadFile(event.getFile());
    }
    
    private FacturaElectronica procesarUploadFile(UploadedFile file){
        FacturaElectronica instancia = null;
        try {
                String xml = new String(file.getContents());
                instancia = procesarFactura(FacturaUtil.read(xml), xml, file.getFileName());
                this.addSuccessMessage("Excelente!", "la factura electrónica del archivo " + file.getFileName() + " ahora esta segura en fede!");
            } catch (Exception e) {
                e.printStackTrace();
                this.addErrorMessage("Ups!", "No fue posible cargar el archivo " + file.getFileName() + ". Intente nuevamente!");
            }
        
        return instancia;
    }

    private FacturaElectronica procesarFactura(Factura factura, String xml, String filename) throws FacturaXMLReadException {
         FacturaElectronica instancia = null;
        if (factura == null) {
            addErrorMessage("Ups! Algo salió mal", "No fue posible leer el contenido XML");
            throw new FacturaXMLReadException("No fue posible leer el contenido XML!");
        }

        StringBuilder codigo = new StringBuilder(factura.getInfoTributaria().getEstab());
        codigo.append("-");
        codigo.append(factura.getInfoTributaria().getPtoEmi());
        codigo.append("-");
        codigo.append(factura.getInfoTributaria().getSecuencial());
        
        if ((instancia = facturaElectronicaService.findUniqueByNamedQuery("BussinesEntity.findByCode", codigo.toString())) == null) {

            instancia = facturaElectronicaService.createInstance();
            instancia.setCode(codigo.toString());
            instancia.setCodeType(CodeType.NUMERO_FACTURA);
            instancia.setFilename(filename);
            instancia.setContenido(xml);
            instancia.setFechaEmision(Dates.toDate(factura.getInfoFactura().getFechaEmision()));
            instancia.setTotalSinImpuestos(factura.getInfoFactura().getTotalSinImpuestos());
            instancia.setTotalDescuento(factura.getInfoFactura().getTotalDescuento());
            instancia.setImporteTotal(factura.getInfoFactura().getImporteTotal());
            instancia.setMoneda(factura.getInfoFactura().getMoneda());
            
            instancia.setClaveAcceso(factura.getInfoTributaria().getClaveAcceso());
            instancia.setFechaAutorizacion(Dates.toDate(FacturaUtil.read(xml, settingService.findByName(SettingNames.TAG_FECHA_AUTORIZACION).getValue())));
            instancia.setNumeroAutorizacion(FacturaUtil.read(xml, settingService.findByName(SettingNames.TAG_FECHA_AUTORIZACION).getValue()));
            
            Organization organizacion = null;
            if ((organizacion = organizacionService.findUniqueByNamedQuery("BussinesEntity.findByCodeAndCodeType", factura.getInfoTributaria().getRuc(), CodeType.RUC)) == null) {
                organizacion = organizacionService.createInstance();
                organizacion.setCode(factura.getInfoTributaria().getRuc());
                organizacion.setName(factura.getInfoTributaria().getRazonSocial());
                organizacion.setInitials((factura.getInfoTributaria().getNombreComercial() != null && !factura.getInfoTributaria().getNombreComercial().isEmpty()) ? factura.getInfoTributaria().getNombreComercial() : factura.getInfoTributaria().getRazonSocial());
                //Todo guardar la dirección como html o xml para uso posterior
                organizacion.setDescription(factura.getInfoTributaria().getDirMatriz());
                organizacion.setOrganizationType(Organization.Type.PRIVATE);
                organizacion.setCodeType(CodeType.RUC);
                organizacion.setRuc(factura.getInfoTributaria().getRuc());
                organizacion.setNumeroContribuyenteEspecial(factura.getInfoFactura().getContribuyenteEspecial());

                organizacionService.save(organizacion);
            }

            Subject subject = null;
            CodeType codeType = CodeType.encode(factura.getInfoFactura().getTipoIdentificacionComprador());
            logger.info("IdentificacionComprador {}, CodeType {}", codeType, factura.getInfoFactura().getIdentificacionComprador());
            if ((subject = subjectService.findUniqueByNamedQuery("BussinesEntity.findByCodeAndCodeType", factura.getInfoFactura().getIdentificacionComprador(), codeType)) == null) {
                subject = subjectService.createInstance();
                subject.setCode(factura.getInfoFactura().getIdentificacionComprador());
                subject.setCodeType(CodeType.encode(factura.getInfoFactura().getTipoIdentificacionComprador()));
                subject.setName(factura.getInfoFactura().getRazonSocialComprador());
                subject.setDescription(factura.getInfoFactura().getDireccionComprador());
                subject.setEmail(subject.getCode() + "@" + settingService.findByName("mail.host").getValue());

                if (factura.getInfoAdicional() != null){
                    for (Factura.InfoAdicional.CampoAdicional campoAdicional : factura.getInfoAdicional().getCampoAdicional()) {
                        if ("email".equalsIgnoreCase(campoAdicional.getNombre())) {
                            subject.setEmail(campoAdicional.getValue());
                        } else if ("dirección".equalsIgnoreCase(campoAdicional.getNombre())) {
                            subject.setBio(campoAdicional.getValue());
                        } else if ("teléfono".equalsIgnoreCase(campoAdicional.getNombre())) {
                            subject.setMobileNumber(campoAdicional.getValue());
                        } else {
                            //TODO activar estructure para almacenar todos los campos adicionales
                            subject.setDescription(subject.getDescription() + "\n" + campoAdicional.getValue());
                        }

                    }
                }

                //login info for persistence
                subject.setUsername(subject.getCode());
                subject.setUsernameConfirmed(false);
                subject.setPassword(new Password(subject.getCode()).toString());

                subjectService.save(subject);
            }

            instancia.setOrganization(organizacion);
            instancia.setOwner(subject);
            
            instancia = facturaElectronicaService.save(instancia);
            
            //Establecer grupo por defecto
            if (instancia.isPersistent() && getDefaultGroup().isPersistent()){
                instancia.add(getDefaultGroup());
                facturaElectronicaService.save(instancia.getId(), instancia);
            }
            
        } else {
            this.addWarningMessage("Ups!", "El archivo " + filename + " contiene una factura que ya existe en fede. ID: " + factura.getInfoTributaria().getClaveAcceso() + ".");
        }
        
        return instancia;
    }
    
    
    private FacturaElectronica procesarFactura(FacturaReader fr) throws FacturaXMLReadException {
        return procesarFactura(fr.getFactura(), fr.getXml(), fr.getFileName());
    }

    @Override
    public void handleReturn(SelectEvent event) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Group getDefaultGroup() {
        if (defaultGroup == null){
            return groupService.findByCode(settingService.findByName(SettingNames.DEFAULT_INVOICES_GROUP_NAME).getValue());
        }
        return defaultGroup;
    }

    public void setDefaultGroup(Group defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

}
