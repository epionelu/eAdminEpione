package lu.esante.agence.epione.service.impl.consent;

import lu.esante.agence.epione.model.Consent;
import lu.esante.agence.epione.model.ConsentType;
import lu.esante.agence.epione.service.IConsentService;
import lu.esante.agence.epione.service.ITraceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Proxy auditing the accessed resources
 * This proxy must be placed first in the chain
 */
public class ConsentAuditProxy implements IConsentService {

    public static final String ENTITY_TYPE = "CONSENT";

    private IConsentService service;
    private ITraceService trace;

    @Autowired
    public ConsentAuditProxy(IConsentService service, ITraceService trace) {
        this.service = service;
        this.trace = trace;
    }

    @Override
    @Transactional
    public List<Consent> getBySsn(String ssn) {
        List<Consent> res = service.getBySsn(ssn);
        trace.write(ENTITY_TYPE, "LIST BY SSN");
        return res;
    }

    @Override
    @Transactional
    public Optional<Consent> getBySsnAndConsentType(String ssn, ConsentType consentType) {
        Optional<Consent> res = service.getBySsnAndConsentType(ssn, consentType);
        if (!res.isEmpty()) {
            trace.write(ENTITY_TYPE, "GET BY SSN AND TYPE", res.get().getId());
        }
        return res;
    }

    @Override
    @Transactional
    public Consent save(Consent consent) {
        Consent res = service.save(consent);
        trace.write(ENTITY_TYPE, "SAVE", consent.getId());
        return res;
    }

    @Override
    @Transactional
    public void delete(Consent consent) {
        service.delete(consent);
        trace.write(ENTITY_TYPE, "DELETE", consent.getId());

    }

}
