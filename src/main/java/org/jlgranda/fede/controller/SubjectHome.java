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

import com.jlgranda.fede.ejb.SubjectService;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import org.jlgranda.fede.cdi.LoggedIn;
import org.jpapi.model.profile.Subject;
import org.picketlink.Identity;
import org.picketlink.idm.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlgranda
 */
@Named
@RequestScoped
public class SubjectHome implements Serializable {

    private static final long serialVersionUID = -1007161141552849702L;
    
    Logger logger = LoggerFactory.getLogger(SubjectHome.class);
    
    @Inject
    private Identity identity;

    Subject loggedIn = new Subject();

    @EJB
    SubjectService subjectService;

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
}
