package lu.esante.agence.epione.service.impl.consent;

import lu.esante.agence.epione.entity.ConsentEntity;
import lu.esante.agence.epione.entity.ConsentTypeEntity;
import lu.esante.agence.epione.model.Consent;
import lu.esante.agence.epione.model.ConsentType;
import lu.esante.agence.epione.repository.ConsentRepository;
import lu.esante.agence.epione.repository.ConsentTypeRepository;
import lu.esante.agence.epione.service.IConsentService;
import lu.esante.agence.epione.service.IConsentVerifier;
import lu.esante.agence.epione.structure.AbstractMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ConsentServiceImpl extends AbstractMapper<Consent, ConsentEntity>
        implements IConsentService, IConsentVerifier {

    private ConsentRepository repo;

    protected EnumMap<ConsentType, ConsentTypeEntity> consentTypes = new EnumMap<>(ConsentType.class);

    @Autowired
    public ConsentServiceImpl(ConsentRepository repo, ConsentTypeRepository typeRepo) {
        this.repo = repo;
        Stream.of(ConsentType.values())
                .forEach((key) -> {
                    Optional<ConsentTypeEntity> t = typeRepo.findByCode(key.toString());
                    if (t.isEmpty()) {
                        throw new IllegalStateException("Missing consent for " + key);
                    }
                    consentTypes.put(key, t.get());
                });

    }

    @Override
    public boolean hasConsent(String ssn, ConsentType consentType) {
        Optional<Consent> consent = getBySsnAndConsentType(ssn, consentType);
        if (consent.isEmpty()) {
            return false;
        }
        return consent.get().getEndAt().isAfter(OffsetDateTime.now());
    }

    @Override
    public List<Consent> getBySsn(String ssn) {
        List<ConsentEntity> res = repo.findBySsn(ssn);
        return entityToBusiness(res);
    }

    @Override
    public Optional<Consent> getBySsnAndConsentType(String ssn, ConsentType consentType) {
        ConsentTypeEntity ent = consentTypes.get(consentType);
        Optional<ConsentEntity> res = repo.findBySsnAndConsentType(ssn, ent);
        return entityToBusiness(res);
    }

    @Override
    public Consent save(Consent consent) {
        ConsentEntity res = repo.save(businessToEntity(consent));
        return entityToBusiness(res);
    }

    @Override
    public void delete(Consent consent) {
        if (consent.getId() != null) {
            repo.delete(businessToEntity(consent));
        }

    }

    @Override
    public Consent entityToBusiness(ConsentEntity b) {
        Consent consent = new Consent();
        consent.setId(b.getId());
        consent.setAuthor(b.getAuthor());
        consent.setConsentType(ConsentType.valueOf(b.getConsentType().getCode()));
        consent.setEndAt(b.getEndAt());
        consent.setSsn(b.getSsn());
        consent.setStartAt(b.getStartAt());

        return consent;
    }

    @Override
    public ConsentEntity businessToEntity(Consent a) {
        ConsentEntity consent = new ConsentEntity();
        consent.setId(a.getId());
        consent.setAuthor(a.getAuthor());
        consent.setConsentType(consentTypes.get(a.getConsentType()));
        consent.setEndAt(a.getEndAt());
        consent.setSsn(a.getSsn());
        consent.setStartAt(a.getStartAt());

        return consent;
    }

}
