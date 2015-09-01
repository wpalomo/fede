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
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.logging.Level;
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
import org.primefaces.event.FileUploadEvent;
import com.jlgranda.fede.SettingNames;
import com.jlgranda.fede.ejb.GroupService;
import com.jlgranda.fede.ejb.url.reader.FacturaElectronicaURLReader;
import java.util.Collections;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.faces.validator.Validator;
import javax.inject.Inject;
import org.jlgranda.fede.cdi.LoggedIn;
import org.jpapi.model.SourceType;
import org.jpapi.util.I18nUtil;
import org.jpapi.util.Lists;
import org.jpapi.util.Strings;

/**
 *
 * @author jlgranda
 */
@ManagedBean
@ViewScoped
public class FacturaElectronicaHome extends FedeController implements Serializable {

    Logger logger = LoggerFactory.getLogger(FacturaElectronicaHome.class);

    private static final long serialVersionUID = -8639341517802129909L;

    @Inject
    @LoggedIn
    private Subject subject;

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
    
    private String keys;
    
    /**
     * Inicio del rango de fecha
     */
    private Date start;
    
    /**
     * Fin del rango de fecha
     */
    private Date end;
    
    private String url;
    
    private List<String> urls = new ArrayList<>();

    private List<UploadedFile> uploadedFiles = Collections.synchronizedList(new ArrayList<UploadedFile>());

    public FacturaElectronicaHome() {
    }
    
    @PostConstruct
    private void init() {
        int amount = Integer.valueOf(settingService.findByName(SettingNames.DASHBOARD_RANGE).getValue());
        setEnd(Dates.now());
        setStart(Dates.addDays(getEnd(), -1 * amount));
    }

    public List<UploadedFile> getUploadedFiles() {
        return uploadedFiles;
    }

    public void setUploadedFiles(List<UploadedFile> uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public void addURL(){
        if (getUrl().isEmpty()) return;
        if (Strings.isUrl(getUrl()) && (getUrl().endsWith(".zip") || getUrl().endsWith(".xml"))){
            this.urls.add(getUrl());
        } else {
            addErrorMessage(I18nUtil.getMessages("action.fail"), I18nUtil.getMessages("add.url.invalid"));
        }
    }
    
    public void removeURL(String url){
        this.urls.remove(url);
    }
    
    /**
     * Obtener todas las facturas disponibles en el sistema
     *
     * @return
     */
    public List<FacturaElectronica> listarFacturasElectronicasParaLineaTiempo() {
        return facturaElectronicaService.listarFacturasElectronicas(Strings.toInt(settingService.findByName("fede.dashboard.timeline.length").getValue()));
    }

    /**
     * Obtener todas las facturas disponibles en el sistema para las etiquetas
     * definidas en el controlador Por defecto carga la lista de facturas que
     * pertenecen a DefaultGroup
     *
     * @return la lista de facturas electrónicas que pertenecen a las etiquetas
     * indicadas en el controlador
     */
    public List<FacturaElectronica> listarFacturasElectronicas() {
        List<FacturaElectronica> result = new ArrayList<>();
        if (getTags() == null || getTags().isEmpty()) {
            setTags(getDefaultGroup().getCode());
        }
        for (String tag : Lists.toList(getTags())) {
            result.addAll(listarFacturasElectronicas(tag));
        }
        return result;
    }

    /**
     * Obtener todas las facturas disponibles en el sistema para el usuario
     * actual
     *
     * @param tag agrupación de facturas
     * @return lista de facturas electrónicas
     */
    public List<FacturaElectronica> listarFacturasElectronicas(String tag) {
        return facturaElectronicaService.listarFacturasElectronicas(tag, subject, getStart(), getEnd());
    }
    
    /**
     * Obtener todas las facturas disponibles en el sistema para el usuario
     * actual dados los ids de la instancia actual <tt>FacturaElectronicaHome</tt>
     *
     * @return lista de facturas electrónicas
     */
    public List<FacturaElectronica> listarFacturasElectronicasPorIds() {
        if (getKeys().isEmpty())
            return new ArrayList<>();
        
        List<Long> ids = new ArrayList<>();
        for (String s : getKeys().split(KEY_SEPARATOR)){
            ids.add(Long.valueOf(s));
        }
        return facturaElectronicaService.findByNamedQuery("BussinesEntity.findByIds", ids);
    }

    /**
     * TODO !IMPLEMENTACION TEMPORAL
     *
     * @return
     */
    public BigDecimal calcularImporteTotal(String tag) {
        BigDecimal total = new BigDecimal(0);
        for (FacturaElectronica fe : facturaElectronicaService.listarFacturasElectronicas(tag, subject, getStart(), getEnd())) {
            total = total.add(fe.getImporteTotal());
        }
        return total;
    }

    public void mostrarFormularioCargaFacturaElectronica() {
        super.openDialog(SettingNames.POPUP_SUBIR_FACTURA_ELECTRONICA, 800, 600, true);
    }
    
    public void mostrarFormularioDescargaFacturaElectronica() {
        super.openDialog(SettingNames.POPUP_DESCARGAR_FACTURA_ELECTRONICA, 800, 600, true);
    }

    public List<FacturaElectronica> importarDesdeInbox() {
        List<FacturaElectronica> result = new ArrayList<>();

        if (subject == null) {
            this.addErrorMessage(I18nUtil.getMessages("action.fail"), I18nUtil.getMessages("fede.subject.null"));
            return result;
        }
        try {
            for (FacturaReader fr : facturaElectronicaMailReader.getFacturasElectronicas(subject)) {
                try {
                    result.add(procesarFactura(fr, SourceType.EMAIL));
                } catch (FacturaXMLReadException ex) {
                    addErrorMessage(I18nUtil.getMessages("action.fail"), I18nUtil.getMessages("xml.read.error.detail"));
                    java.util.logging.Logger.getLogger(FacturaElectronicaHome.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            this.addSuccessMessage(I18nUtil.getMessages("action.sucessfully"), "Se agregarón " + result.size() + " facturas a fede desde el correo!");

        } catch (MessagingException | IOException ex) {
            addErrorMessage(I18nUtil.getMessages("action.fail"), I18nUtil.getMessages("import.email.error"));
            java.util.logging.Logger.getLogger(FacturaElectronicaHome.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public void handleFileUpload(FileUploadEvent event) {
        procesarUploadFile(event.getFile());
    }

    public void procesarUploadFile(UploadedFile file) {
        if (subject == null) {
            this.addErrorMessage(I18nUtil.getMessages("action.fail"), I18nUtil.getMessages("fede.subject.null"));
            return;
        }

        try {
            String xml = new String(file.getContents());
            procesarFactura(FacturaUtil.read(xml), xml, file.getFileName(), SourceType.FILE);
            this.addSuccessMessage(I18nUtil.getMessages("action.sucessfully"), "El archivo " + file.getFileName() + " ahora esta seguro en fede!");
        } catch (Exception e) {
            this.addErrorMessage(I18nUtil.getMessages("action.fail"), e.getMessage());
        }
    }
    
    /**
     * Cargar Facturas electrónicas a partir los urls indicados por el usuario
     * @return lista de instancias FacturaElectronica
     */
    public List<FacturaElectronica>  procesarURLs(){
        List<FacturaElectronica> result = new ArrayList<>();
        try {
            
            for (FacturaReader fr : FacturaElectronicaURLReader.getFacturasElectronicas(getUrls())){
                result.add(procesarFactura(fr, SourceType.URL));
            }
            this.addSuccessMessage(I18nUtil.getMessages("action.sucessfully"), "Se agregarón " + result.size() + " facturas a fede desde los URLs dados!");
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(FacturaElectronicaHome.class.getName()).log(Level.SEVERE, null, ex);
            addErrorMessage(I18nUtil.getMessages("action.fail"), I18nUtil.getMessages("xml.read.error.detail"));
        }
        closeDialog(null);
        return result;
    }

    public void addURLAndProcesarURLs(){
        addURL();
        procesarURLs();
        closeDialog(null);
    }

    
    @Deprecated
    /**
     * Carga los archivos en lote.
     */
    public void procesarUploadFiles() {
        boolean errors = false;
        for (UploadedFile file : this.uploadedFiles) {
            try {
                String xml = new String(file.getContents());
                procesarFactura(FacturaUtil.read(xml), xml, file.getFileName(), SourceType.FILE);
            } catch (Exception e) {
                errors = true;
                this.addErrorMessage(I18nUtil.getMessages("action.fail"), "No fue posible cargar el archivo " + file.getFileName() + " Error: " + e.getMessage() + ". Intente nuevamente!");
            }
        }
        if (!errors) {
            closeDialog(null);
        }
    }

    private FacturaElectronica procesarFactura(Factura factura, String xml, String filename, SourceType sourceType) throws FacturaXMLReadException {
        FacturaElectronica instancia = null;

        if (factura == null) {
            addErrorMessage(I18nUtil.getMessages("action.fail"), "No fue posible leer el contenido XML");
            throw new FacturaXMLReadException("No fue posible leer el contenido XML!");
        }
        
        CodeType codeType = CodeType.encode(factura.getInfoFactura().getTipoIdentificacionComprador());
        logger.info("IdentificacionComprador {}, CodeType {}", factura.getInfoFactura().getIdentificacionComprador(), codeType);
        
        if (!(factura.getInfoFactura().getIdentificacionComprador().startsWith(subject.getCode())
               || subject.getCode().startsWith(factura.getInfoFactura().getIdentificacionComprador()))) {
            addErrorMessage(I18nUtil.getMessages("xml.read.forbidden"), I18nUtil.getMessages("xml.read.forbidden.detail"));
            throw new FacturaXMLReadException(I18nUtil.getMessages("xml.read.forbidden.detail"));
        }
        
        actualizarDatosDesdeFactura(subject, factura);

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

            instancia.setSourceType(sourceType); //El tipo de importación realizado

            logger.info("Organizacion {}, CodeType {}", factura.getInfoTributaria().getRuc(), CodeType.RUC);

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

            instancia.setOrganization(organizacion);
            instancia.setOwner(subject);

            instancia = facturaElectronicaService.save(instancia);

            //Establecer grupo por defecto
            if (instancia.isPersistent() && getDefaultGroup().isPersistent()) {
                instancia.add(getDefaultGroup());
                facturaElectronicaService.save(instancia.getId(), instancia);
            }

        } else {
            this.addWarningMessage(I18nUtil.getMessages("action.warning"), "El archivo " + filename + " contiene una factura que ya existe en fede. ID: " + codigo + ".");
        }

        return instancia;
    }

    private FacturaElectronica procesarFactura(FacturaReader fr, SourceType sourceType) throws FacturaXMLReadException {
        return procesarFactura(fr.getFactura(), fr.getXml(), fr.getFileName(), sourceType);
    }

    @Override
    public void handleReturn(SelectEvent event) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Group getDefaultGroup() {
        if (defaultGroup == null) {
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

    public String getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
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

    public List<Group> getGroups() {
        return groupService.findAllByOwner(subject);
    }
    
    public List<Group> getGroupsByCodes() {
        if (getTags() == null)
            return getGroups();
        
        if (getTags().isEmpty())
            return getGroups();
 
        return groupService.findByNamedQuery("BussinesEntity.findByCodesAndOwner", tags, subject);
    }

    /**
     * Encuentra la instancia Subject para los parámetros dados. Se actualiza al
     * tipo de código desde CEDULA a RUC, si es el caso
     *
     * @param identificacionComprador
     * @param codeType
     * @return
     */
    private Subject findSubject(String identificacionComprador, CodeType codeType) {
        String cedula = identificacionComprador.substring(0, identificacionComprador.length() > 10 ? 10 : identificacionComprador.length());
        Subject subject_ = null;
        subject_ = subjectService.findUniqueByNamedQuery("BussinesEntity.findByCodeAndCodeType", identificacionComprador, codeType);
        if (subject_ == null && codeType == CodeType.RUC) {
            subject_ = subjectService.findUniqueByNamedQuery("BussinesEntity.findByCodeAndCodeType", cedula, CodeType.CEDULA);
            if (subject_ != null) {
                subject_.setCode(identificacionComprador);
                subject_.setCodeType(CodeType.RUC);
                subjectService.save(subject_.getId(), subject_); //actualizar a RUC
            }
        }
        return subject_;
    }

    /**
     * Actualiza los datos de la instancia <tt>Subject</tt>, desde la factura dada,
     * sólo si los campos estan vacios.
     * @param subject el sujeto propietarios de la factura
     * @param factura la factura origen de datos
     */
    private void actualizarDatosDesdeFactura(Subject subject_, Factura factura) {
        
        boolean changed = false;
        if (Strings.isNullOrEmpty(subject_.getDescription())){
            subject_.setDescription(factura.getInfoFactura().getDireccionComprador());
            changed = true;
        }
        
        if (factura.getInfoAdicional() != null) {
            for (Factura.InfoAdicional.CampoAdicional campoAdicional : factura.getInfoAdicional().getCampoAdicional()) {
                if ("email".equalsIgnoreCase(campoAdicional.getNombre())) {
                    if (Strings.isNullOrEmpty(subject_.getEmail())){
                        subject_.setEmail(campoAdicional.getValue());
                        changed = true;
                    }
                } else if ("dirección".equalsIgnoreCase(campoAdicional.getNombre())) {
                    //TODO
                } else if ("teléfono".equalsIgnoreCase(campoAdicional.getNombre())) {
                    if (Strings.isNullOrEmpty(subject_.getWorkPhoneNumber())){
                        subject_.setWorkPhoneNumber(campoAdicional.getValue());
                        changed = true;
                    }
                    
                } else {
                    //TODO activar estructure para almacenar todos los campos adicionales
                }
            }
        }
        
        if (changed){
            subjectService.save(subject_.getId(), subject_);
        }
    }

}
