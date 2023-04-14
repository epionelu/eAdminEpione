package lu.esante.agence.epione.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lu.esante.agence.epione.WithEpioneUser;
import lu.esante.agence.epione.auth.Roles;
import lu.esante.agence.epione.exception.ForbiddenException;
import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.model.DocumentStatus;
import lu.esante.agence.epione.service.impl.document.DocumentConsentCheckProxy;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class DocumentConsentCheckProxyTests {

    DocumentConsentCheckProxy proxy;

    @MockBean
    IConsentVerifier verifier;

    @MockBean
    IDocumentService service;

    @Autowired
    IIdentityService identity;

    @BeforeEach
    public void init() {
        proxy = new DocumentConsentCheckProxy(service, verifier, identity);
        when(service.getById(any())).thenReturn(Optional.empty());
        when(service.save(any())).thenReturn(new Document());
        when(service.getBySsnAndStatus(any(), any())).thenReturn(List.of());
    }

    @Test
    @WithEpioneUser(name = "name", autority = Roles.PATIENT)
    public void getByIdTPatientTest() {
        Document doc = new Document();
        doc.setId(UUID.randomUUID());
        when(verifier.hasConsent(any(), any())).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> proxy.getById(doc.getId()));
        when(service.getById(doc.getId())).thenReturn(Optional.of(doc));
        when(verifier.hasConsent(any(), any())).thenReturn(true);
        assertDoesNotThrow(() -> proxy.getById(doc.getId()));
    }

    @Test
    @WithEpioneUser(name = "name", autority = Roles.PRACTITIONER, eHealthId = "ehealthid")
    public void getByIdPRTest() {
        Document doc = new Document();
        doc.setId(UUID.randomUUID());
        doc.setEHealthId("ehealthid");
        when(service.getById(any())).thenReturn(Optional.of(doc));
        when(verifier.hasConsent(any(), any())).thenReturn(true);
        assertDoesNotThrow(() -> proxy.getById(doc.getId()));
    }

    @Test
    @WithEpioneUser(name = "name", autority = Roles.CNS)
    public void getByIdCnsTest() {
        assertDoesNotThrow(() -> proxy.getById(UUID.randomUUID()));
    }

    @Test
    @WithEpioneUser(name = "name", autority = Roles.ADMIN)
    public void getByIdAdminTest() {
        assertThrows(ForbiddenException.class, () -> proxy.getById(UUID.randomUUID()));
    }

    @Test
    @WithEpioneUser(name = "name", autority = Roles.PRACTITIONER, eHealthId = "ehealthid")
    public void saveTest() {
        Document doc = new Document();
        doc.setEHealthId("xxxxxx");
        when(verifier.hasConsent(any(), any())).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> proxy.save(doc));
        when(verifier.hasConsent(any(), any())).thenReturn(true);
        assertThrows(ForbiddenException.class, () -> proxy.save(doc));
        doc.setEHealthId("ehealthid");
        assertDoesNotThrow(() -> proxy.save(doc));
    }

    @Test
    @WithEpioneUser(name = "name", autority = Roles.PATIENT)
    public void getDocumentBySsnAndStatusTest() {
        when(verifier.hasConsent(any(), any())).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> proxy.getBySsnAndStatus("Hello", DocumentStatus.CANCEL_ASKED));
        when(verifier.hasConsent(any(), any())).thenReturn(true);
        assertThrows(ForbiddenException.class, () -> proxy.getBySsnAndStatus("Hello", DocumentStatus.CANCEL_ASKED));
        assertDoesNotThrow(() -> proxy.getBySsnAndStatus("name", DocumentStatus.CANCEL_ASKED));
    }

}
