/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlgranda.fede.security;

import org.picketlink.annotations.PicketLink;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * 
 * @author jlgranda
 */
public class Resources {
    @SuppressWarnings("unused")
    @Produces
    @PicketLink
    @PersistenceContext(unitName = "fede")
    private static EntityManager picketLinkEntityManager;
}