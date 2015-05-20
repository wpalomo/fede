package org.jlgranda.fede.cdi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Stereotype;


//@Qualifier
//@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})

@ApplicationScoped
@Stereotype 
@Target(ElementType.TYPE) 
@Retention(RetentionPolicy.RUNTIME) 
public @interface BussinesEntityRepository {}
