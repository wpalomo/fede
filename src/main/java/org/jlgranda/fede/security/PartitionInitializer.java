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
package org.jlgranda.fede.security;

import org.picketlink.event.PartitionManagerCreateEvent;
import org.picketlink.idm.PartitionManager;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.event.Observes;
import javax.transaction.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Realm;

/**
 * Initializes the partition manager for use under GlassFish. Ensures that there
 * is a transaction before adding the default partition.
 *
 * @based on https://github.com/pedroigor/picketlink-quickstarts/blob/a8ed0d00f4638dc7d2c596e9c440db78088b1c0d/picketlink-authorization-idm-jpa/src/main/java/org/jboss/as/quickstarts/picketlink/authorization/idm/jpa/PartitionInitializer.java
 * 
*/
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class PartitionInitializer {
    
    private static Logger log = LoggerFactory.getLogger(PartitionInitializer.class);

    @Resource
    private UserTransaction userTransaction;

    public void initPartition(@Observes PartitionManagerCreateEvent event) {
        PartitionManager partitionManager = event.getPartitionManager();

        try {
            if (partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM) == null) {
                this.userTransaction.begin();

                partitionManager.add(new Realm(Realm.DEFAULT_REALM));

                this.userTransaction.commit();
            }
        } catch (Exception e) {
            try {
                this.userTransaction.rollback();
            } catch (SystemException ignore) {
            }
            throw new RuntimeException("Could not create default partition.", e);
        }
    }
}
