package lu.esante.agence.epione.service;

import java.util.List;
import java.util.Optional;

import lu.esante.agence.epione.model.Consent;
import lu.esante.agence.epione.model.ConsentType;

public interface IConsentService {
    List<Consent> getBySsn(String ssn);

    Optional<Consent> getBySsnAndConsentType(String ssn, ConsentType consentType);

    Consent save(Consent consent);

    void delete(Consent consent);
}
