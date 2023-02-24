package lu.esante.agence.epione.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;

import lu.esante.agence.epione.repository.ConsentRepository;
import lu.esante.agence.epione.repository.ConsentTypeRepository;
import lu.esante.agence.epione.repository.DocumentRepository;
import lu.esante.agence.epione.repository.DocumentStatusRepository;
import lu.esante.agence.epione.repository.DocumentTypeRepository;
import lu.esante.agence.epione.repository.PractitionerMemberRepository;
import lu.esante.agence.epione.service.IConsentService;
import lu.esante.agence.epione.service.IConsentVerifier;
import lu.esante.agence.epione.service.IDocumentBatchHelper;
import lu.esante.agence.epione.service.IDocumentService;
import lu.esante.agence.epione.service.IIdentityService;
import lu.esante.agence.epione.service.IPractitionerMemberService;
import lu.esante.agence.epione.service.ITeamVerifier;
import lu.esante.agence.epione.service.ITraceService;
import lu.esante.agence.epione.service.impl.consent.ConsentAuditProxy;
import lu.esante.agence.epione.service.impl.consent.ConsentServiceImpl;
import lu.esante.agence.epione.service.impl.document.DocumentAuditProxy;
import lu.esante.agence.epione.service.impl.document.DocumentBatchHelperProxy;
import lu.esante.agence.epione.service.impl.document.DocumentConsentCheckProxy;
import lu.esante.agence.epione.service.impl.document.DocumentServiceImpl;
import lu.esante.agence.epione.service.impl.member.PractitionerMemberAuditProxy;
import lu.esante.agence.epione.service.impl.member.PractitionerMemberServiceImpl;

@Configuration
@EnableScheduling
public class EpioneConfig {

    @Bean
    @Autowired
    public IConsentService configureConsentService(IIdentityService identity, ITraceService trace,
            ConsentRepository consentRepository, ConsentTypeRepository consentTypeRepository) {
        ConsentServiceImpl impl = new ConsentServiceImpl(consentRepository, consentTypeRepository);
        return new ConsentAuditProxy(impl, trace);
    }

    @Bean
    @Autowired
    public IConsentVerifier configureConsentVerifier(ConsentRepository consentRepository,
            ConsentTypeRepository consentTypeRepository) {
        return new ConsentServiceImpl(consentRepository, consentTypeRepository);
    }

    @Bean
    @Autowired
    public IDocumentBatchHelper configureDocumentBatchHelper(DocumentRepository repo, DocumentTypeRepository typeRepo,
            DocumentStatusRepository statusRepo, ITraceService trace) {
        DocumentServiceImpl documentService = new DocumentServiceImpl(repo, typeRepo, statusRepo);
        return new DocumentBatchHelperProxy(trace, documentService);
    }

    @Bean
    @Autowired
    @Primary
    public IDocumentService configureDocumentService(DocumentRepository repo, DocumentTypeRepository typeRepo,
            DocumentStatusRepository statusRepo, IConsentVerifier verifier, IIdentityService identity,
            ITraceService trace) {
        DocumentServiceImpl impl = new DocumentServiceImpl(repo, typeRepo, statusRepo);
        DocumentConsentCheckProxy proxy1 = new DocumentConsentCheckProxy(impl, verifier, identity);
        return new DocumentAuditProxy(proxy1, trace);
    }

    @Bean
    @Autowired
    public ITeamVerifier configureTeamVerifier(PractitionerMemberRepository repo) {
        return new PractitionerMemberServiceImpl(repo);
    }

    @Bean
    @Autowired
    @Primary
    public IPractitionerMemberService configurePractitionerMemberService(PractitionerMemberRepository repo,
            ITraceService trace) {
        PractitionerMemberServiceImpl impl = new PractitionerMemberServiceImpl(repo);
        return new PractitionerMemberAuditProxy(impl, trace);
    }
}
