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
import com.jlgranda.fede.ejb.SubjectService;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.jlgranda.fede.cdi.LoggedIn;
import org.jpapi.model.CodeType;
import org.jpapi.model.profile.Subject;
import org.jpapi.util.Dates;
import org.jpapi.util.I18nUtil;
import org.jpapi.util.Strings;
import org.picketlink.Identity;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.BasicModel;
import static org.picketlink.idm.model.basic.BasicModel.addToGroup;
import static org.picketlink.idm.model.basic.BasicModel.grantGroupRole;
import static org.picketlink.idm.model.basic.BasicModel.grantRole;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador de entidades Subject: signup, profile
 * @author jlgranda
 */
@Named
@RequestScoped
public class SubjectHome extends FedeController implements Serializable {

    private static final long serialVersionUID = -1007161141552849702L;
    
    Logger logger = LoggerFactory.getLogger(SubjectHome.class);
    
    @Inject
    private Identity identity;

    Subject loggedIn = new Subject();
    
    Subject signup = null;

    @EJB
    SubjectService subjectService;
    
    @EJB
    SettingService settingService;
    
    @Inject
    GroupHome groupHome;
    
    @Inject
    private PartitionManager partitionManager;
    
    @Resource
    private UserTransaction userTransaction; //https://issues.jboss.org/browse/PLINK-332
    
    IdentityManager identityManager = null;

    @Produces
    @LoggedIn
    @Named("subject")
    public Subject getLoggedIn() {
        if (identity.isLoggedIn() && !loggedIn.isPersistent()) {
            try {
                Account account = identity.getAccount();
                loggedIn = subjectService.findUniqueByNamedQuery("Subject.findUserByUUID", account.getId());
                if (loggedIn != null)
                    loggedIn.setLoggedIn(true);
            } catch (NoResultException e) {
                throw e;
            }
        } else if (!identity.isLoggedIn()) {
        }
        return loggedIn;
    }

    public boolean isLoggedIn() {
        return loggedIn != null && loggedIn.getId() != null;
    }
    
    public void save(Subject subject){
        subjectService.save(subject.getId(), subject);
        addSuccessMessage(I18nUtil.getMessages("action.sucessfully"), I18nUtil.getMessages("action.sucessfully.detail"));
    }

    @Override
    public void handleReturn(SelectEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Subject getSignup() {
        if (signup == null){
            signup = subjectService.createInstance();
        }
        return signup;
    }

    public void setSignup(Subject signup) {
        this.signup = signup;
    }
    
    /**
     * Procesa la creación de una cuenta en fede
     */
    public void processSignup(){
        
        identityManager = partitionManager.createIdentityManager();
        
        logger.info("Procesar signup para {} ", signup);
        if (signup != null){
            //Crear la identidad para acceso al sistema
            try {

                //Prepare password
                Password password = new Password(signup.getPassword());
                //separar nombres
                List<String> names = Strings.splitNamesAt(signup.getFirstname());

                if (names.size() > 1){
                    signup.setFirstname(names.get(0));
                    signup.setSurname(names.get(1));
                } 
                signup.setUsername(signup.getEmail());
                
                this.userTransaction.begin();
                User user = new User(signup.getUsername());
                user.setFirstName(signup.getFirstname());
                user.setLastName(signup.getSurname());
                user.setEmail(signup.getEmail());
                user.setCreatedDate(Dates.now());
                identityManager.add(user);

                identityManager.updateCredential(user, password);

                // Create application role "superuser"
                Role superuser = BasicModel.getRole(identityManager, "superuser");

                Group group = BasicModel.getGroup(identityManager, "fede");

                RelationshipManager relationshipManager = partitionManager.createRelationshipManager();
                // Make john a member of the "sales" group
                addToGroup(relationshipManager, user, group);
                // Make mary a manager of the "sales" group
                grantGroupRole(relationshipManager, user, superuser, group);
                // Grant the "superuser" application role to jane
                grantRole(relationshipManager, user, superuser);
                
                this.userTransaction.commit();
                
                //Conectar con el user auth
                String passwrod_ = new BasicPasswordEncryptor().encryptPassword(new String(password.getValue()));
                signup.setUsername(signup.getEmail());
                signup.setCodeType(CodeType.CEDULA);
                signup.setPassword(passwrod_);
                signup.setUsernameConfirmed(true);
            
                //Set fede email
                signup.setFedeEmail(signup.getCode().concat("@").concat(settingService.findByName("mail.imap.host").getValue()));
                signup.setFedeEmailPassword(passwrod_);
                
                //Finalmente crear en fede
                signup.setUuid(user.getId());
                signup.setSubjectType(Subject.Type.NATURAL);
                subjectService.save(signup);
                
                //Crear grupos por defecto para el subject
                groupHome.createDefaultGroups(signup);
                
            } catch (NotSupportedException | SystemException | IdentityManagementException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException e) {
                try {
                    this.userTransaction.rollback();
                } catch (SystemException ignore) {
                }
                throw new RuntimeException("Could not create default security entities.", e);
            }
        }

    }
}
