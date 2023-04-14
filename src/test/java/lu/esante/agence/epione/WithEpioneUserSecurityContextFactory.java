package lu.esante.agence.epione;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import lu.esante.agence.epione.auth.EpioneAuthentication;

public class WithEpioneUserSecurityContextFactory
        implements WithSecurityContextFactory<WithEpioneUser> {
    @Override
    public SecurityContext createSecurityContext(WithEpioneUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        EpioneAuthentication auth = new EpioneAuthentication(customUser.name(), true,
                new SimpleGrantedAuthority(customUser.autority()));

        if (!customUser.eHealthId().isBlank()) {
            auth.setEHealthId(customUser.eHealthId());
        }
        context.setAuthentication(auth);
        return context;
    }
}