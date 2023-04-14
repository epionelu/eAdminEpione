package lu.esante.agence.epione.auth;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lu.esante.agence.epione.auth.exception.AuthenticationException;
import lu.esante.agence.epione.config.EpioneSettings;
import lu.esante.agence.epione.service.IMpiService;
import lu.esante.agence.epione.service.ITeamVerifier;

@Component
@Order(1)
public class AuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    EpioneSettings settings;

    @Autowired
    IMpiService mpi;

    @Autowired
    ITeamVerifier verifier;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Prepare Data
        JwtAuthenticationToken token = null;
        String eHealthId = null;
        String header = request.getHeader(settings.getSecurity().getCertificateHeaderName());
        if (header != null) {
            Optional<String> value = extractValueFromCertificateHeader(header,
                    settings.getSecurity().getCertificateKey());
            if (!value.isEmpty()) {
                eHealthId = value.get();
            }
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken) {
            token = (JwtAuthenticationToken) auth;
        }

        try {
            // Setup Authentication
            EpioneAuthentication epioneAuth = defineUserProfile(eHealthId, token);
            SecurityContextHolder.getContext().setAuthentication(epioneAuth);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No authentication method found");
        }

    }

    private static Optional<String> extractValueFromCertificateHeader(String header, String key) {
        if (header == null || key == null) {
            return Optional.empty();
        }

        return Arrays.stream(header.split(","))
                .map(s -> s.split("="))
                .filter(s -> s[0].equals(key))
                .map(s -> s[1])
                .findFirst();
    }

    private EpioneAuthentication defineUserProfile(String eHealthId, JwtAuthenticationToken token) {
        EpioneAuthentication epioneAuth;
        if (eHealthId != null && token != null) {
            if (!eHealthId.equals(token.getName()) && !verifier.isTeamMember(token.getName(), eHealthId)) {
                throw new AuthenticationException("User not member of the practitioner team");
            }
            epioneAuth = new EpioneAuthentication(token.getName(), true, new SimpleGrantedAuthority("PRACTITIONER"));
            epioneAuth.setEHealthId(eHealthId);
        } else if (token != null && mpi.isPatient(token.getName())) {
            String ssn = mpi.getSsn(token.getName());
            if (ssn == null || ssn.isEmpty()) {
                throw new AuthenticationException("User not found in MPI");
            }
            epioneAuth = new EpioneAuthentication(ssn, true, new SimpleGrantedAuthority("PATIENT"));
        } else if (eHealthId != null && Arrays.asList(settings.getSecurity().getCnsEHealthIds()).contains(eHealthId)) {
            epioneAuth = new EpioneAuthentication("CNS", true, new SimpleGrantedAuthority("CNS"));
            epioneAuth.setEHealthId(eHealthId);
        } else if (eHealthId != null
                && Arrays.asList(settings.getSecurity().getAdminEHealthIds()).contains(eHealthId)) {
            epioneAuth = new EpioneAuthentication("ADMIN", true, new SimpleGrantedAuthority("ADMIN"));
            epioneAuth.setEHealthId(eHealthId);
        } else {
            throw new AuthenticationException("No available authentication method found");
        }
        return epioneAuth;
    }

}
