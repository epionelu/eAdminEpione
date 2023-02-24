package lu.esante.agence.epione.service;

import lu.esante.agence.epione.model.ConsentType;

public interface IConsentVerifier {
    boolean hasConsent(String ssn, ConsentType consentType);
}
