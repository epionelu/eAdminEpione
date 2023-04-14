package lu.esante.agence.epione.service;

import lu.esante.agence.epione.model.Consent;
import lu.esante.agence.epione.model.ConsentType;
import lu.esante.agence.epione.service.impl.consent.ConsentAuditProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ConsentAuditProxyTests {

    ConsentAuditProxy proxy;

    @MockBean
    private IConsentService service;

    @MockBean
    private ITraceService trace;

    @BeforeEach
    public void init() {
        proxy = new ConsentAuditProxy(service, trace);
    }

    @Test
    public void getBySsnTest() {
        Consent consent = buildConsent();

        when(service.getBySsn(any())).thenReturn(List.of());
        when(service.getBySsn(consent.getSsn())).thenReturn(List.of(consent));
        List<Consent> response = proxy.getBySsn("ssn");
        assertEquals(1, response.size());
        assertEquals(consent, response.get(0));

        response = proxy.getBySsn("123");
        assertEquals(0, response.size());
    }

    @Test
    public void getBySsnAndConsentTypeTest() {
        Consent consent = buildConsent();
        when(service.getBySsnAndConsentType(any(), any())).thenReturn(Optional.empty());
        when(service.getBySsnAndConsentType(consent.getSsn(), consent.getConsentType()))
                .thenReturn(Optional.of(consent));
        Optional<Consent> response = proxy.getBySsnAndConsentType("ssn", ConsentType.MH);
        assertEquals(false, response.isEmpty());
        assertEquals(consent, response.get());

        response = proxy.getBySsnAndConsentType("ssn", ConsentType.CNS);
        assertEquals(true, response.isEmpty());
    }

    @Test
    public void saveTest() {
        Consent consent = buildConsent();
        when(service.save(consent)).thenReturn(consent);
        Consent response = proxy.save(consent);
        assertEquals(consent, response);

    }

    @Test
    public void deleteTest() {
        Consent consent = buildConsent();
        assertDoesNotThrow(() -> proxy.delete(consent));
    }

    private Consent buildConsent() {
        Consent consent = new Consent();
        consent.setId(UUID.randomUUID());
        consent.setAuthor("author");
        consent.setConsentType(ConsentType.MH);
        consent.setEndAt(OffsetDateTime.MAX);
        consent.setSsn("ssn");
        consent.setStartAt(OffsetDateTime.MIN);
        return consent;
    }

}
