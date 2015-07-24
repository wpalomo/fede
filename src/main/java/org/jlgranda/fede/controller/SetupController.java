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
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.jlgranda.fede.database.SetupService;
import org.jpapi.util.Dates;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.BasicModel;
import static org.picketlink.idm.model.basic.BasicModel.addToGroup;
import static org.picketlink.idm.model.basic.BasicModel.grantGroupRole;
import static org.picketlink.idm.model.basic.BasicModel.grantRole;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador de verificación de arranque de la aplicación
 *
 * @author jlgranda
 */
@Singleton
@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class SetupController implements Serializable {

    private static Logger log = LoggerFactory.getLogger(SetupController.class);

    @EJB
    private SetupService service;

    @Inject
    private PartitionManager partitionManager;

    @Resource
    private UserTransaction userTransaction; //https://issues.jboss.org/browse/PLINK-332

    public SetupController() {
    }

    public void validate(@Observes final HttpSession session) {
        log.info("Start validate setup database");
        service.validate();
        log.info("End validate setup database");
    }

    @PostConstruct
    public void init() {

        IdentityManager identityManager = null;
        List<Realm> realms = partitionManager.getPartitions(Realm.class);
        if (realms.isEmpty()) {
            Realm realm = new Realm(Realm.DEFAULT_REALM);
            partitionManager.add(realm);
            identityManager = partitionManager.createIdentityManager(realm);
            log.info("### Partition removed?");
        } else {
            identityManager = partitionManager.createIdentityManager();
        }
        if (BasicModel.getUser(identityManager, "admin") == null) {
            try {

                this.userTransaction.begin();
                User user = new User("admin");
                user.setFirstName("Administrador");
                user.setLastName("fede");
                user.setEmail("admin@fede.com");
                user.setCreatedDate(Dates.now());
                identityManager.add(user);

                identityManager.updateCredential(user, new Password("f3d3"));

                // Create application role "superuser"
                Role superuser = new Role("superuser");
                identityManager.add(superuser);

                Group group = new Group("fede");
                identityManager.add(group);

                Role admin = new Role("admin");

                identityManager.add(admin);

                RelationshipManager relationshipManager = partitionManager.createRelationshipManager();
                // Make john a member of the "sales" group
                addToGroup(relationshipManager, user, group);
                // Make mary a manager of the "sales" group
                grantGroupRole(relationshipManager, user, superuser, group);
                // Grant the "superuser" application role to jane
                grantRole(relationshipManager, user, superuser);
                log.info("Creador usuario " + user);
                this.userTransaction.commit();
            } catch (NotSupportedException | SystemException | IdentityManagementException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException e) {
                try {
                    this.userTransaction.rollback();
                } catch (SystemException ignore) {
                }
                throw new RuntimeException("Could not create default security entities.", e);
            }
        }

    }

    private static final long serialVersionUID = -2202084526171728773L;
}

