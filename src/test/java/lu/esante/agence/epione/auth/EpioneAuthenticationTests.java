package lu.esante.agence.epione.auth;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class EpioneAuthenticationTests {

    @Test
    public void instanciationTest() {
        EpioneAuthentication auth = new EpioneAuthentication("name", true,
                new SimpleGrantedAuthority(Roles.PRACTITIONER));
        Assertions.assertEquals("name", auth.getPrincipal());
        Assertions.assertEquals("name", auth.getName());

        Assertions.assertEquals(true, auth.isAuthenticated());
        auth.setAuthenticated(false);
        Assertions.assertEquals(false, auth.isAuthenticated());

        Assertions.assertEquals(Arrays.asList(new SimpleGrantedAuthority("PRACTITIONER")), auth.getAuthorities());

        auth.setEHealthId("eHealthId");
        Assertions.assertEquals("eHealthId", auth.getEHealthId());
        auth.setEHealthId("TODO");
        Assertions.assertEquals("eHealthId", auth.getEHealthId());

        Assertions.assertEquals(null, auth.getCredentials());
        Assertions.assertEquals(null, auth.getDetails());

    }
}
