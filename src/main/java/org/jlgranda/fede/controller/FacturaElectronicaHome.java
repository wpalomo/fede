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
import com.jlgranda.fede.ejb.OrganizationService;
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
import org.jpapi.model.profile.Subject;
import org.jpapi.util.Dates;
import org.primefaces.event.FileUploadEvent;
import com.jlgranda.fede.SettingNames;
import com.jlgranda.fede.ejb.GroupService;
import com.jlgranda.fede.ejb.url.reader.FacturaElectronicaURLReader;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.jlgranda.fede.cdi.LoggedIn;
import org.jlgranda.fede.ui.model.LazyFacturaElectronicaDataModel;
import org.jpapi.model.BussinesEntity;
import org.jpapi.model.SourceType;
import org.jpapi.util.I18nUtil;
import org.jpapi.util.Lists;
import org.jpapi.util.Strings;
import org.picketlink.idm.credential.Password;
import org.primefaces.component.api.UIColumn;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

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
            
    //@EJB
    //private SettingService settingService;
    @Inject
    private SettingHome settingHome;

    @EJB
    private GroupService groupService;

    @EJB
    private FacturaElectronicaService facturaElectronicaService;

    @EJB
    private FacturaElectronicaMailReader facturaElectronicaMailReader;

    @EJB
    private OrganizationService organizacionService;

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
    
    private LazyFacturaElectronicaDataModel lazyDataModel; 
    
    private List<BussinesEntity> selectedBussinesEntities;
    
    private Map<String, String> selectedTriStateGroups = new LinkedHashMap<String, String>();
    
    private String keyword;
    
    private List<Group> groups = new ArrayList<>();

    public FacturaElectronicaHome() {
    }
    
    @PostConstruct
    private void init() {
        int amount = 0;
        try {
            //amount = Integer.valueOf(settingService.findByName(SettingNames.DASHBOARD_RANGE).getValue());
            amount = Integer.valueOf(settingHome.getValue(SettingNames.DASHBOARD_RANGE, "360"));
        } catch (java.lang.NumberFormatException nfe){
            nfe.printStackTrace();
            amount = 30;
        }
        
        setEnd(Dates.now());
        setStart(Dates.addDays(getEnd(), -1 * amount));
    }

    public List<BussinesEntity> getSelectedBussinesEntities() {
        return selectedBussinesEntities;
    }

    public void setSelectedBussinesEntities(List<BussinesEntity> selectedBussinesEntities) {
        this.selectedBussinesEntities = selectedBussinesEntities;
    }

    public Map<String, String> getSelectedTriStateGroups() {
        return selectedTriStateGroups;
    }

    public void setSelectedTriStateGroups(Map<String, String> selectedTriStateGroups) {
        this.selectedTriStateGroups = selectedTriStateGroups;
    }

    public List<UploadedFile> getUploadedFiles() {
        return uploadedFiles;
    }

    public void setUploadedFiles(List<UploadedFile> uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }

    public LazyFacturaElectronicaDataModel getLazyDataModel() {
        

        filter();
    
        return lazyDataModel;
    }

    public void setLazyDataModel(LazyFacturaElectronicaDataModel lazyDataModel) {
        this.lazyDataModel = lazyDataModel;
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

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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
     * Obtener todas las facturas disponibles en el sistema para el usuario
     * actual dados los ids de la instancia actual <tt>FacturaElectronicaHome</tt>
     * Se usa para mostrar los RIDE
     * @return lista de facturas electrónicas
     */
    public List<FacturaElectronica> listarFacturasElectronicasPorIds() {
        if (getKeys().isEmpty())
            return new ArrayList<>();
        
        List<Long> ids = new ArrayList<>();
        for (String s : getKeys().split(KEY_SEPARATOR)){
            ids.add(Long.valueOf(s.trim()));
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
    
    public BigDecimal countRowsByTag(String tag) {
        BigDecimal total = new BigDecimal(0);
        if ("all".equalsIgnoreCase(tag)){
            total = new BigDecimal(facturaElectronicaService.count());
        } else if ("own".equalsIgnoreCase(tag)){
            total = new BigDecimal(facturaElectronicaService.count("FacturaElectronica.countBussinesEntityByOwner", subject));
        } else {
            total = new BigDecimal(facturaElectronicaService.count("FacturaElectronica.countBussinesEntityByTagAndOwner", tag, subject));
        }
        return total;
    }

    public boolean mostrarFormularioCargaFacturaElectronica() {
        String width = settingHome.getValue(SettingNames.POPUP_WIDTH, "550");
        String height = settingHome.getValue(SettingNames.POPUP_HEIGHT, "480");
        super.openDialog(SettingNames.POPUP_SUBIR_FACTURA_ELECTRONICA, width, height, true);
        return true;
    }
    
    public boolean mostrarFormularioDescargaFacturaElectronica() {
        String width = settingHome.getValue(SettingNames.POPUP_WIDTH, "550");
        String height = settingHome.getValue(SettingNames.POPUP_HEIGHT, "480");
        super.openDialog(SettingNames.POPUP_DESCARGAR_FACTURA_ELECTRONICA, width, height, true);
        return true;
    }
    
    public boolean mostrarFormularioNuevaEtiqueta() {
        String width = settingHome.getValue(SettingNames.POPUP_SMALL_WIDTH, "400");
        String height = settingHome.getValue(SettingNames.POPUP_SMALL_HEIGHT, "240");
        super.openDialog(SettingNames.POPUP_NUEVA_ETIQUETA, width, height, true);
        return true;
    }
    

    public List<FacturaElectronica> importarDesdeInbox() {
        List<FacturaElectronica> result = new ArrayList<>();

        if (subject == null) {
            this.addErrorMessage(I18nUtil.getMessages("action.fail"), I18nUtil.getMessages("fede.subject.null"));
            return result;
        }
        try {
            for (FacturaReader fr : facturaElectronicaMailReader.read(subject, "inbox")) {
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
        
        if (file == null){
            this.addErrorMessage(I18nUtil.getMessages("action.fail"), I18nUtil.getMessages("fede.file.null"));
            return;
        }

        if (subject == null) {
            this.addErrorMessage(I18nUtil.getMessages("action.fail"), I18nUtil.getMessages("fede.subject.null"));
            return;
        }
        String xml = null;
        try {
            if (file.getFileName().endsWith(".xml")){
                byte[] content = IOUtils.toByteArray(file.getInputstream());
                 xml = new String(content);
                procesarFactura(FacturaUtil.read(xml), xml, file.getFileName(), SourceType.FILE);
                this.addSuccessMessage(I18nUtil.getMessages("action.sucessfully"), "Su factura electrónica " + file.getFileName() + " ahora empieza a generar valor para ud!");
                IOUtils.closeQuietly(file.getInputstream());
            } else if (file.getFileName().endsWith(".zip")){
                ZipInputStream zis = new ZipInputStream(file.getInputstream());
                try {
                    ZipEntry entry = null;
                    ByteArrayOutputStream fout = null;
                    while ((entry = zis.getNextEntry()) != null) {
                        if (entry.getName().endsWith(".xml")) {
                            //logger.debug("Unzipping {}", entry.getFilename());
                            fout = new ByteArrayOutputStream();
                            for (int c = zis.read(); c != -1; c = zis.read()) {
                                fout.write(c);
                            }

                            xml = new String(fout.toByteArray(), Charset.defaultCharset());
                            procesarFactura(FacturaUtil.read(xml), xml, file.getFileName(), SourceType.FILE);
                            this.addSuccessMessage(I18nUtil.getMessages("action.sucessfully"), "Su factura electrónica " + entry.getName() + " ahora empieza a generar valor para ud!");
                            fout.close();
                        }
                        zis.closeEntry();
                    }
                    zis.close();

                } finally {
                    IOUtils.closeQuietly(file.getInputstream());
                    IOUtils.closeQuietly(zis);
                }
            }
           
        } catch (Exception e) {
            e.printStackTrace();
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
        
        //actualizarDatosDesdeFactura(subject, factura);

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
            String tag = settingHome.getValue(SettingNames.TAG_FECHA_AUTORIZACION, "<fechaAutorizacion></fechaAutorizacion>");
            instancia.setFechaAutorizacion(Dates.toDate(FacturaUtil.read(xml, tag)));
            instancia.setNumeroAutorizacion(FacturaUtil.read(xml, tag));

            instancia.setSourceType(sourceType); //El tipo de importación realizado

            logger.info("Author {}, CodeType {}", factura.getInfoTributaria().getRuc(), CodeType.RUC);
            Subject author = null;
            if ((author = subjectService.findUniqueByNamedQuery("BussinesEntity.findByCodeAndCodeType", factura.getInfoTributaria().getRuc(), CodeType.RUC)) == null) {
                author = subjectService.createInstance();
                author.setCode(factura.getInfoTributaria().getRuc());
                author.setName(factura.getInfoTributaria().getRazonSocial());
                author.setFirstname(factura.getInfoTributaria().getRazonSocial());
                author.setInitials((factura.getInfoTributaria().getNombreComercial() != null && !factura.getInfoTributaria().getNombreComercial().isEmpty()) ? factura.getInfoTributaria().getNombreComercial() : factura.getInfoTributaria().getRazonSocial());
                //Todo guardar la dirección como html o xml para uso posterior
                author.setDescription(factura.getInfoTributaria().getDirMatriz());
                author.setSubjectType(Subject.Type.PRIVATE);
                author.setCodeType(CodeType.RUC);
                author.setRuc(factura.getInfoTributaria().getRuc());
                author.setNumeroContribuyenteEspecial(factura.getInfoFactura().getContribuyenteEspecial());
                author.setEmail(author.getCode()+"@dummy.com");
                author.setUsername(author.getEmail());
                author.setPassword((new org.apache.commons.codec.digest.Crypt().crypt("dummy")));
                author.setActive(Boolean.FALSE);

                subjectService.save(author);
            
            }

            instancia.setAuthor(author);
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
            return groupService.findByCode(settingHome.getValue(SettingNames.DEFAULT_INVOICES_GROUP_NAME, "fede"));
        }
        return defaultGroup;
    }

    public void setDefaultGroup(Group defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    public String getTags() {
        
        if (this.tags == null || tags.isEmpty()) {
            setTags(getDefaultGroup().getCode());
        }
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
    
    public String getSelectedKeys(){
        String _keys = "";
        if (getSelectedBussinesEntities() != null && !getSelectedBussinesEntities().isEmpty())
            _keys = Lists.toString(getSelectedBussinesEntities());
        return _keys;
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
        if (groups.isEmpty()){
            groups = groupService.findAllByOwner(subject);
        }
        
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    
    
    public List<String> getGroupNames() {
        List<String> names = new ArrayList<>();
                
        for (Group g : getGroups()){
            names.add(g.getName());
        }
        
        return names;
    }
    
    public List<Group> getGroupsByCodes() {
        if (getTags() == null)
            return getGroups();
        
        if (getTags().isEmpty())
            return getGroups();
 
        return groupService.findByNamedQuery("BussinesEntity.findByCodesAndOwner", tags, subject);
    }
    
    
    public void filter() {
        if (lazyDataModel == null ){
            lazyDataModel = new LazyFacturaElectronicaDataModel(facturaElectronicaService);
        }
        
        lazyDataModel.setOwner(subject);
        lazyDataModel.setStart(getStart());
        lazyDataModel.setEnd(getEnd());
            
        if (getKeyword()!= null && getKeyword().startsWith("label:")){
            String parts[] = getKeyword().split(":");
            if (parts.length > 1){
                lazyDataModel.setTags(parts[1]);
            }
            lazyDataModel.setFilterValue(null);//No buscar por keyword
        } else {
            lazyDataModel.setTags(getTags());
            lazyDataModel.setFilterValue(getKeyword());
        }
    }


    /**
     * Encuentra la instancia Subject para los parámetros dados. Se actualiza al
     * tipo de código desde CEDULA a RUC, si es el caso
     *
     * @param identificacionComprador
     * @param codeType
     * @return
     */
    @Deprecated
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
    
    public void applySelectedGroups() {
        String status = "";
        Group group = null;
        //FacturaElectronica fe = null;
        for (BussinesEntity fe : getSelectedBussinesEntities()) {
            //fe = facturaElectronicaService.find(be.getId());
            for (String key : selectedTriStateGroups.keySet()) {
                group = findGroup(key);
                if (key.equalsIgnoreCase(group.getName())) {
                    group = groupService.findByCode(group.getCode()); //Attached entity
                    status = selectedTriStateGroups.get(key);
                    if ("0".equalsIgnoreCase(status)) {
                        if (fe.containsGroup(key)) {
                            fe.remove(group);
                        }
                    } else if ("1".equalsIgnoreCase(status)) {
                        if (!fe.containsGroup(key)) {
                            fe.add(group);
                        }
                    } else if ("2".equalsIgnoreCase(status)) {
                        if (!fe.containsGroup(key)) {
                            fe.add(group);
                        }
                    }
                }
            }
            facturaElectronicaService.save(fe.getId(), (FacturaElectronica) fe);
        }

        this.addSuccessMessage("Las facturas se agregaron a " + selectedTriStateGroups.keySet(), "");

    }
    
    public void onRowSelect(SelectEvent event) {
        try {
            //Redireccionar a RIDE de objeto seleccionado
            if (event != null && event.getObject() != null){
                redirectTo("/pages/fede/ride.jsf?key=" + ((BussinesEntity) event.getObject()).getId());
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(FacturaElectronicaHome.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage(I18nUtil.getMessages("BussinesEntity") + " " + I18nUtil.getMessages("common.unselected"), ((BussinesEntity) event.getObject()).getName());

        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.selectedBussinesEntities.remove((FacturaElectronica) event.getObject());
         logger.info(I18nUtil.getMessages("BussinesEntity") + " " + I18nUtil.getMessages("common.unselected"), ((BussinesEntity) event.getObject()).getName());
    }

    private Group findGroup(String key) {
        for (Group g: getGroups()){
            if (g.getName().equalsIgnoreCase(key)){
                return g;
            }
        }
        return new Group("null", "null");
    }

}
