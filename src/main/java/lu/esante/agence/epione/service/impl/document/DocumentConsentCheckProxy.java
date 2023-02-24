package lu.esante.agence.epione.service.impl.document;

import lu.esante.agence.epione.auth.Roles;
import lu.esante.agence.epione.exception.ForbiddenException;
import lu.esante.agence.epione.model.ConsentType;
import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.model.DocumentStatus;
import lu.esante.agence.epione.service.IConsentVerifier;
import lu.esante.agence.epione.service.IDocumentService;
import lu.esante.agence.epione.service.IIdentityService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This class acts as a proxy before accessing document
 * It cheks that the user have a valid consent before processing the request
 */
public class DocumentConsentCheckProxy implements IDocumentService {

    IDocumentService service;

    IConsentVerifier verifier;

    IIdentityService identity;

    @Autowired
    public DocumentConsentCheckProxy(IDocumentService service,
            IConsentVerifier verifier, IIdentityService identity) {
        this.service = service;
        this.verifier = verifier;
        this.identity = identity;
    }

    @Override
    /**
     * Checks:
     * - Patient: Must have eSante Consent
     * - Admin: denied
     * - CNS: Always OK
     * - Practitioner: ssn have MH consent and eHealthId of document = certificate
     * eHealthId
     */
    public Optional<Document> getById(UUID id) {
        if ((identity.hasRole(Roles.PATIENT) && !verifier.hasConsent(identity.getName(), ConsentType.ESANTE)) ||
                identity.hasRole(Roles.ADMIN)) {
            throw new ForbiddenException("Missing consent to access this document");
        }

        Optional<Document> res = service.getById(id);

        if (identity.hasRole(Roles.PRACTITIONER) && !res.isEmpty()
                && !isPractitionerAccessValid(res.get())) {
            throw new ForbiddenException("Missing consent to access this document");
        }

        return res;
    }

    @Override
    /**
     * Checks:
     * - Patient: Must have eSante Consent
     * - Admin: denied
     * - CNS: Always OK
     * - Practitioner: ssn have MH consent and eHealthId of document = certificate
     * eHealthId
     */
    public Optional<Byte[]> getPdfById(UUID id) {
        if ((identity.hasRole(Roles.PATIENT) && !verifier.hasConsent(identity.getName(), ConsentType.ESANTE)) ||
                identity.hasRole(Roles.ADMIN)) {
            throw new ForbiddenException("Missing consent to access this document");
        }
        Optional<Byte[]> res = service.getPdfById(id);
        return res;
    }

    /**
     * Checks:
     * - Patient: Must have MH Consent
     * - Admin: denied
     * - CNS: Always OK
     * - Practitioner: ssn have MH consent and eHealthId of document = certificate
     * eHealthId
     */
    @Override
    public Document save(Document document) {
        if ((identity.hasRole(Roles.PRACTITIONER) && !verifier.hasConsent(document.getSsn(), ConsentType.MH)) ||
                (identity.hasRole(Roles.PRACTITIONER) && !isPractitionerAccessValid(document)) ||
                (identity.hasRole(Roles.ADMIN))) {
            throw new ForbiddenException("Missing consent to create this document");
        }
        if (identity.hasRole(Roles.PATIENT) && !verifier.hasConsent(identity.getName(), ConsentType.CNS)) {
            throw new ForbiddenException("Missing CNS consent to send document");
        }
        if (verifier.hasConsent(document.getSsn(), ConsentType.CNS_AUTO)) {
            document.setDocumentStatus(DocumentStatus.TO_SEND);
        }
        return service.save(document);
    }

    /**
     * Checks:
     * - Patient: Must have MH Consent and SSN = self
     * - Practitioner: deny
     * - CNS: deny
     * - Admin: deny
     */
    @Override
    public List<Document> getBySsnAndStatus(String ssn, DocumentStatus documentStatus) {
        if (identity.hasRole(Roles.PATIENT) && ssn.equals(identity.getName())
                && verifier.hasConsent(ssn, ConsentType.ESANTE)) {
            return service.getBySsnAndStatus(ssn, documentStatus);

        }
        throw new ForbiddenException("Missing consent to get these documents");

    }

    @Override
    public Optional<Document> getByFileId(UUID fileId) {
        if ((identity.hasRole(Roles.PATIENT) && !verifier.hasConsent(identity.getName(), ConsentType.ESANTE)) ||
                identity.hasRole(Roles.ADMIN)) {
            throw new ForbiddenException("Missing consent to access this document");
        }

        Optional<Document> res = service.getByFileId(fileId);

        if (identity.hasRole(Roles.PRACTITIONER) && !res.isEmpty()
                && !isPractitionerAccessValid(res.get())) {
            throw new ForbiddenException("Missing consent to access this document");
        }

        return res;

    }

    private boolean isPractitionerAccessValid(Document document) {

        return verifier.hasConsent(document.getSsn(), ConsentType.MH)
                && document.getEHealthId().equals(identity.getEHealthId());
    }

    @Override
    public List<Document> getAvailableDocuments(String ssn) {
        if (identity.hasRole(Roles.PATIENT) && ssn.equals(identity.getName())
                && verifier.hasConsent(ssn, ConsentType.ESANTE)) {
            return service.getAvailableDocuments(ssn);

        }
        throw new ForbiddenException("Missing consent to read these documents");
    }

    @Override
    public void cancelDocument(Document oldDoc, Optional<Document> newDoc) {
        service.cancelDocument(oldDoc, newDoc);
    }

}
