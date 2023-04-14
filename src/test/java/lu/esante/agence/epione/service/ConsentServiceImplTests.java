package lu.esante.agence.epione.service;

import lu.esante.agence.epione.entity.ConsentEntity;
import lu.esante.agence.epione.entity.ConsentTypeEntity;
import lu.esante.agence.epione.model.Consent;
import lu.esante.agence.epione.model.ConsentType;
import lu.esante.agence.epione.repository.ConsentRepository;
import lu.esante.agence.epione.repository.ConsentTypeRepository;
import lu.esante.agence.epione.service.impl.consent.ConsentServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ConsentServiceImplTests {

    @MockBean
    private ConsentRepository repo;

    @MockBean
    private ConsentTypeRepository typeRepo;

    @Test
    public void consentServiceInitializationTest() {
        initRepos();
        Assertions.assertDoesNotThrow(() -> new ConsentServiceImpl(repo, typeRepo));
    }

    @Test
    public void consentServiceInitializationFailureTest() {
        when(typeRepo.findByCode("MH"))
                .thenReturn(Optional.of(buildConsentTypeEntity("MH")));
        Assertions.assertThrows(IllegalStateException.class, () -> new ConsentServiceImpl(repo, typeRepo));
    }

    @Test
    public void consentServiceGetBySsnTest() {
        initRepos();
        ConsentServiceImpl service = new ConsentServiceImpl(repo, typeRepo);
        List<Consent> res = service.getBySsn("test");
        assertEquals(1, res.size());
        Consent consent = res.get(0);

        assertEquals("author", consent.getAuthor());
        assertEquals("test", consent.getSsn());
        assertEquals(ConsentType.MH, consent.getConsentType());
    }

    @Test
    public void consentServiceGetBySsnAndConsentTypeTest() {
        initRepos();
        ConsentServiceImpl service = new ConsentServiceImpl(repo, typeRepo);
        Optional<Consent> res = service.getBySsnAndConsentType("test", ConsentType.MH);
        Consent consent = res.get();

        assertEquals("author", consent.getAuthor());
        assertEquals("test", consent.getSsn());
        assertEquals(ConsentType.MH, consent.getConsentType());
    }

    @Test
    public void consentServiceSaveTest() {
        initRepos();
        ConsentServiceImpl service = new ConsentServiceImpl(repo, typeRepo);
        Consent consent = service.save(new Consent());

        assertEquals("author", consent.getAuthor());
        assertEquals("test", consent.getSsn());
        assertEquals(ConsentType.MH, consent.getConsentType());
    }

    @Test
    public void consentServiceDeleteTest() {
        initRepos();
        ConsentServiceImpl service = new ConsentServiceImpl(repo, typeRepo);
        Consent c = new Consent();
        c.setId(UUID.randomUUID());
        assertDoesNotThrow(() -> service.delete(c));
        Consent d = new Consent();
        assertDoesNotThrow(() -> service.delete(d));
    }

    @Test
    public void consentServiceHasConsentTest() {
        initRepos();
        ConsentServiceImpl service = new ConsentServiceImpl(repo, typeRepo);
        assertEquals(true, service.hasConsent("test", ConsentType.MH));
        assertEquals(false, service.hasConsent("test2", ConsentType.MH));
        assertEquals(false, service.hasConsent("test", ConsentType.CNS));
    }

    private void initRepos() {
        Stream.of(ConsentType.values())
                .forEach((key) -> {
                    when(typeRepo.findByCode(key.toString()))
                            .thenReturn(Optional.of(buildConsentTypeEntity(key.toString())));
                });
        Optional<ConsentTypeEntity> type = typeRepo.findByCode("MH");
        when(repo.findBySsn("test")).thenReturn(List.of(buildConsentEntity(type.get(), "test", "author")));

        when(repo.findBySsnAndConsentType("test", type.get()))
                .thenReturn(Optional.of(buildConsentEntity(type.get(), "test", "author")));

        ConsentEntity c = buildConsentEntity(type.get(), "test", "author");
        when(repo.save(any())).thenReturn(c);
    }

    private ConsentTypeEntity buildConsentTypeEntity(String code) {
        ConsentTypeEntity entity = new ConsentTypeEntity();
        entity.setCode(code);
        entity.setDescription(code);
        entity.setId(UUID.randomUUID());
        return entity;
    }

    private ConsentEntity buildConsentEntity(ConsentTypeEntity consentType, String ssn, String author) {
        ConsentEntity entity = new ConsentEntity();
        entity.setAuthor(author);
        entity.setConsentType(consentType);
        entity.setEndAt(OffsetDateTime.now().plusHours(2));
        entity.setId(UUID.randomUUID());
        entity.setSsn(ssn);
        entity.setStartAt(OffsetDateTime.now());
        return entity;
    }
}
