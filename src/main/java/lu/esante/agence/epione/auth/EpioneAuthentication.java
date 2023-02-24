package lu.esante.agence.epione.auth;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;

public class EpioneAuthentication implements Authentication {

    private String name;
    private boolean authenticated;

    @Getter
    private String eHealthId = null;
    private Collection<? extends GrantedAuthority> authorities;

    public EpioneAuthentication(String name, boolean authenticated, GrantedAuthority... authorities) {
        this.name = name;
        this.authenticated = authenticated;
        this.authorities = Collections.unmodifiableList(Arrays.asList(authorities));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return getName();
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        authenticated = isAuthenticated;

    }

    public void setEHealthId(String eHealthId) {
        if (this.eHealthId == null) {
            this.eHealthId = eHealthId;
        }
    }

}
