package lu.esante.agence.epione.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lu.esante.agence.epione.WithEpioneUser;
import lu.esante.agence.epione.auth.Roles;
import lu.esante.agence.epione.service.impl.identity.IdentityServiceImpl;

@SpringBootTest
public class IdentityServiceImplTests {

    @Autowired
    IdentityServiceImpl service;

    @Test
    @WithEpioneUser(name = "TEST", autority = "PRACTITIONER", eHealthId = "EHEALTHID")
    public void identityServiceTest() {
        Assertions.assertEquals("TEST", service.getName());
        Assertions.assertEquals(false, service.hasRole(Roles.CNS));
        Assertions.assertEquals(false, service.hasRole(Roles.ADMIN));
        Assertions.assertEquals(true, service.hasRole(Roles.PRACTITIONER));
        Assertions.assertEquals(false, service.hasRole(Roles.PATIENT));
        Assertions.assertEquals("EHEALTHID", service.getEHealthId());
        Assertions.assertEquals(1, service.getRoles().size());
    }

    @Test
    @WithEpioneUser(name = "HELLO", autority = "PATIENT")
    public void identityServiceTestNoEHealthId() {
        Assertions.assertEquals("HELLO", service.getName());
        Assertions.assertEquals(false, service.hasRole(Roles.CNS));
        Assertions.assertEquals(false, service.hasRole(Roles.ADMIN));
        Assertions.assertEquals(false, service.hasRole(Roles.PRACTITIONER));
        Assertions.assertEquals(true, service.hasRole(Roles.PATIENT));
        Assertions.assertEquals(null, service.getEHealthId());
        Assertions.assertEquals(1, service.getRoles().size());
    }
}
