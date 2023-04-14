package lu.esante.agence.epione;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

import lu.esante.agence.epione.auth.Roles;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithEpioneUserSecurityContextFactory.class)
public @interface WithEpioneUser {

    String name() default "212345736125371";

    String eHealthId() default "";

    String autority() default Roles.PATIENT;
}
