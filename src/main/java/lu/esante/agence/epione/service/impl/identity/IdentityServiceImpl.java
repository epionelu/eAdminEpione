package lu.esante.agence.epione.service.impl.identity;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lu.esante.agence.epione.auth.EpioneAuthentication;
import lu.esante.agence.epione.service.IIdentityService;

@Service
public class IdentityServiceImpl implements IIdentityService {

    public String getName() {
        return getAuthentication().getName();
    }

    public boolean hasRole(String role) {
        return getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    public String getEHealthId() {
        return getAuthentication().getEHealthId();
    }

    public Collection<? extends GrantedAuthority> getRoles() {
        return getAuthentication().getAuthorities();
    }

    private EpioneAuthentication getAuthentication() {
        return (EpioneAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }
}
