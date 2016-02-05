/*
 * Copyright (C) 2016 jlgranda
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

import com.jlgranda.fede.ejb.BalancedScoreCardService;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import org.jlgranda.fede.cdi.LoggedIn;
import org.jlgranda.fede.model.management.Organization;
import org.jpapi.model.profile.Subject;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlgranda
 */
@ManagedBean
@ViewScoped
public class BalancedScoreCardHome extends FedeController implements Serializable {

    Logger logger = LoggerFactory.getLogger(BalancedScoreCardHome.class);
    
    @Inject
    @LoggedIn
    private Subject subject;
    
    @Inject
    private SettingHome settingHome;
    
    @EJB
    private BalancedScoreCardService  balancedScoreCardService;
    
    private Organization organization;

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @Override
    public void handleReturn(SelectEvent event) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public BigDecimal countRowsByTag(String tag) {
        BigDecimal total = new BigDecimal(0);
        if ("all".equalsIgnoreCase(tag)){
            total = new BigDecimal(balancedScoreCardService.count());
        } else if ("own".equalsIgnoreCase(tag)){
            total = new BigDecimal(balancedScoreCardService.count("BalancedScoreCard.countByOwner", subject));
        } else {
            total = new BigDecimal(balancedScoreCardService.count("BalancedScoreCard.countByOwnerAndOrganization", subject, getOrganization()));
        }
        return total;
    }
}
