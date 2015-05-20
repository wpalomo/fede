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

import java.util.Set;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.security.api.authorization.AbstractAccessDecisionVoter;
import org.apache.deltaspike.security.api.authorization.AccessDecisionVoterContext;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;

/**
 *
 * @author jlgranda
 */
@SessionScoped //or @WindowScoped
public class AdminAccessDecisionVoter extends AbstractAccessDecisionVoter {

    @Override
    protected void checkPermission(AccessDecisionVoterContext advc, Set<SecurityViolation> set) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

//    @Inject
//    private ViewConfigResolver viewConfigResolver;
//
//    private Class<? extends ViewConfig> deniedPage = Pages.Home.class;
//
//    @Override
//    protected void checkPermission(AccessDecisionVoterContext context, Set<SecurityViolation> violations) {
//
//        AuthorizationChecker authorizationChecker = BeanProvider.getContextualReference(AuthorizationChecker.class);
//        boolean loggedIn = authorizationChecker.isLoggedIn();
//
//        if(loggedIn) {
//            //...
//        } else {
//            violations.add(/*...*/);
//            deniedPage = viewConfigResolver.getViewConfigDescriptor(FacesContext.getCurrentInstance().getViewRoot().getViewId()).getConfigClass();
//        }
//    }
//
//    public Class<? extends ViewConfig> getDeniedPage() {
//        try {
//            return deniedPage;
//        } finally {
//            deniedPage = Pages.Home.class;
//        }
//    }
}