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

/**
 *
 * @author jlgranda
 */
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import org.jpapi.util.I18nUtil;
import org.jpapi.util.StringValidations;


@FacesValidator("safeTextUTF8Validator")
@RequestScoped
public class SafeTextUTF8Validator implements Validator
{
   @Override
   public void validate(final FacesContext context, final UIComponent component, final Object value)
            throws ValidatorException
   {
      String field = value.toString();
      if (!StringValidations.isPunctuatedTextUTF8(field))
      {
         FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, I18nUtil.getMessages("validation.badUTF8Input"), I18nUtil.getMessages("validation.badUTF8Input.detail"));
         throw new ValidatorException(msg);
      }
   }
}
