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
package org.jlgranda.fede.faces.validator;

import java.util.regex.Pattern;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import org.jpapi.util.I18nUtil;
import org.jpapi.util.Interpolator;


/**
 *
 * @author jlgranda
 */
@FacesValidator("ciValidator")
@RequestScoped
public class CIValidator implements Validator {

    private String message;

    @Override
    public void validate(final FacesContext context, final UIComponent component, final Object value) throws ValidatorException {
        if (value != null) {
            String nid = value.toString().trim();
            if (nid.length() <= 10) {
                this.validateNationalIdentityDocument(nid);
            } else {
                this.validateTaxpayerDocument(nid);
            }
        } else {
            message = Interpolator.interpolate(
                    I18nUtil.getMessages("validation.requiredIdentificationNumber"),
                    new Object[0]);
            throw new ValidatorException(new FacesMessage(message));
        }

    }

    private void verifyNationalIdentityDocument(String nid) {
        String wced = nid.substring(0, 9);
        String verif = nid.substring(9, 10);
        double wd = 0;
        double wc = 0;
        double wa = 0;
        double wb = 0;

        for (int i = 0; i <= wced.length() - 1; i = i + 2) {
            wa = Double.parseDouble(wced.substring(i, i + 1));
            wb = wa * 2;
            wc = wb;
            if (wb > 9) {
                wc = wb - 9;
            }
            wd = wd + wc;
        }

        for (int i = 1; i <= wced.length() - 1; i = i + 2) {
            wa = Double.parseDouble(wced.substring(i, i + 1));
            wd = wd + wa;
        }
        double wn;
        wn = wd / 10;
        double wn2 = Math.ceil(wn);
        wn2 = wn2 * 10;
        double digit = wn2 - wd;

        if (digit != Double.parseDouble(verif)) {
            message = Interpolator.interpolate(
                    I18nUtil.getMessages("validation.invalidIdentificationNumber"),
                    new Object[0]);
            throw new ValidatorException(new FacesMessage(message));
        }
    }

    private void validateNationalIdentityDocument(String nid) {
        System.out.println("eqaula --> validateNationalIdentityDocument" + nid);
        message = Interpolator.interpolate(
                I18nUtil.getMessages("validation.lengthIdentificationNumber"),
                new Object[0]);
        if (nid.length() < 10) {
            throw new ValidatorException(new FacesMessage(message));
        }
        String spatron = "[0-9]{10}";// \\d{10}
        if (!Pattern.matches(spatron, nid)) {
            message = Interpolator.interpolate(
                    I18nUtil.getMessages("validation.wrongIdentificationNumber"),
                    new Object[0]);
            throw new ValidatorException(new FacesMessage(message));
        }
        this.verifyNationalIdentityDocument(nid);
    }

    private void verifyTaxPayerPrivate(String nid) {
        int[] coeficientes = {4, 3, 2, 7, 6, 5, 4, 3, 2};
        int module = 11;
        String wced = nid.substring(0, 9);
        String verif = nid.substring(9, 10);
        int sum_coef = 0;
        int wa;
        for (int i = 0; i < coeficientes.length; i++) {
            wa = Integer.parseInt(wced.substring(i, i + 1));
            sum_coef = sum_coef + (wa * coeficientes[i]);
        }
        int residue = sum_coef % module;
        int digit_verify = residue == 0 ? residue : module - residue;
        if (digit_verify != Integer.parseInt(verif)) {
            message = Interpolator.interpolate(
                     I18nUtil.getMessages("validation.invalidIdentificationNumber"),
                    new Object[0]);
            throw new ValidatorException(new FacesMessage(message));
        }
        String _main = nid.substring(10, nid.length());
        if (!_main.matches("[0-9]{2}[0-9&&[^0]]")) {
            message = Interpolator.interpolate(
                    I18nUtil.getMessages("validation.invalidIdentificationNumberFinished"),
                    new Object[0]);
            throw new ValidatorException(new FacesMessage(message));
        }

    }

    private void verifyTaxPayerPublic(String nid) {
        int[] coeficientes = {3, 2, 7, 6, 5, 4, 3, 2};
        int module = 11;
        String wced = nid.trim().substring(0, 8);
        String verif = nid.trim().substring(8, 9);
        int sum_coef = 0;
        int wa;
        for (int i = 0; i < coeficientes.length; i++) {
            wa = Integer.parseInt(wced.substring(i, i + 1));
            sum_coef = sum_coef + (wa * coeficientes[i]);
        }
        int residue = sum_coef % module;
        int digit_verify = residue == 0 ? residue : module - residue;
        if (digit_verify != Integer.parseInt(verif)) {
            message = Interpolator.interpolate(
                    I18nUtil.getMessages("validation.invalidIdentificationNumber"),
                    new Object[0]);
            throw new ValidatorException(new FacesMessage(message));
        }
        String _main = nid.substring(9, nid.length());
        if (!_main.matches("[0-9]{3}[0-9&&[^0]]")) {
            message = Interpolator.interpolate(
                    I18nUtil.getMessages("validation.invalidIdentificationNumberFinished"),
                    new Object[0]);
            throw new ValidatorException(new FacesMessage(message));
        }
    }

    private void validateTaxpayerDocument(String nid) {
        message = Interpolator.interpolate(
                I18nUtil.getMessages("validation.lengthIdentificationNumberLegalEntity"),
                new Object[0]);
        if (nid.length() < 13) {
            throw new ValidatorException(new FacesMessage(message));
        }
        String spatron = "[0-9]{13}";// \\d{10}
        if (!Pattern.matches(spatron, nid)) {
            message = Interpolator.interpolate(
                    I18nUtil.getMessages("validation.wrongIdentificationNumberLegalEntity"),
                    new Object[0]);
            throw new ValidatorException(new FacesMessage(message));
        }
        // TODO Chequeo de RUC
        /**
         * Extraer tercer digito para saber si es: 9 para sociedades privadas y
         * extranjeros 6 para sociedades publicas menor que 6 (0,1,2,3,4,5) para
         * personas naturales
         */
        nid = nid.trim();
        char typeRucChar = nid.charAt(2);
        int typeRuc = Integer.parseInt(typeRucChar + "");
        if (typeRuc < 6) {
            this.verifyNationalIdentityDocument(nid);
            String _main = nid.substring(10, nid.length());
            if (!_main.matches("[0-9]{2}[0-9&&[^0]]")) {
                message = Interpolator.interpolate(
                        I18nUtil.getMessages("validation.invalidIdentificationNumberFinished"),
                        new Object[0]);
                throw new ValidatorException(new FacesMessage(message));
            }
        } else if (typeRuc == 6) {
            verifyTaxPayerPublic(nid);
        } else if (typeRuc == 9) {
            this.verifyTaxPayerPrivate(nid);
        } else {
            message = Interpolator.interpolate(
                    I18nUtil.getMessages("validation.wrongTypeIdentificationNumberLegalEntity"),
                    new Object[0]);
            throw new ValidatorException(new FacesMessage(message));
        }
        String _main = nid.substring(9, nid.length());
        if (!_main.matches("[0-9]{3}[1-9]")) {
            message = Interpolator.interpolate(
                    I18nUtil.getMessages("validation.invalidIdentificationNumberFinished"),
                    new Object[0]);
            throw new ValidatorException(new FacesMessage(message));
        }
        

    }
}