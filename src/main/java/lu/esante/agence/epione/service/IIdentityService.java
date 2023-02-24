package lu.esante.agence.epione.service;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public interface IIdentityService {
    String getName();

    boolean hasRole(String role);

    String getEHealthId();

    Collection<? extends GrantedAuthority> getRoles();

}
