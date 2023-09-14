package lu.esante.agence.epione.service.impl.document;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.model.DocumentStatus;
import lu.esante.agence.epione.service.IDocumentService;
import lu.esante.agence.epione.service.ITraceService;

/**
 * Proxy auditing the accessed resources
 * This proxy must be placed first in the chain
 */
public class DocumentAuditProxy implements IDocumentService {

    public static final String ENTITY_TYPE = "DOCUMENT";

    private IDocumentService service;
    private ITraceService trace;

    @Autowired
    public DocumentAuditProxy(IDocumentService service, ITraceService trace) {
        this.service = service;
        this.trace = trace;
    }

    @Override
    @Transactional
    public Optional<Document> getById(UUID id) {
        Optional<Document> res = service.getById(id);
        if (!res.isEmpty()) {
            trace.write(ENTITY_TYPE, "GET BY ID", res.get().getId());
        }
        return res;
    }

    @Override
    @Transactional
    public Optional<Byte[]> getPdfById(UUID id) {
        Optional<Byte[]> res = service.getPdfById(id);
        if (!res.isEmpty()) {
            trace.write(ENTITY_TYPE, "GET PDF BY ID", id);
        }
        return res;
    }

    @Override
    @Transactional
    public Document save(Document document) {
        Document res = service.save(document);
        trace.write(ENTITY_TYPE, "SAVE", res.getId());
        return res;
    }

    @Override
    @Transactional
    public List<Document> getBySsnAndStatus(String ssn, DocumentStatus documentStatus) {
        List<Document> res = service.getBySsnAndStatus(ssn, documentStatus);
        trace.write(ENTITY_TYPE, "LIST BY SSN AND STATUS");
        return res;
    }

    @Override
    public Optional<Document> getByFileId(UUID fileId) {
        Optional<Document> res = service.getByFileId(fileId);
        if (!res.isEmpty()) {
            trace.write(ENTITY_TYPE, "GET BY FILE ID", res.get().getId());
        }
        return res;
    }

    @Override
    public List<Document> getAvailableDocuments(String ssn) {
        List<Document> res = service.getAvailableDocuments(ssn);
        trace.write(ENTITY_TYPE, "LIST AVAILABLE DOCS");
        return res;
    }

    @Override
    public List<Document> getAvailableDocumentsFromPractitioner(LocalDate createdFrom, LocalDate createdTo, String ehealthId) {
        List<Document> res = service.getAvailableDocumentsFromPractitioner(createdFrom, createdTo, ehealthId);
        trace.write(ENTITY_TYPE, "LIST AVAILABLE DOCS FROM PRACTITIONER");
        return res;
    }

    @Override
    @Transactional
    public void cancelDocument(Document oldDoc, Optional<Document> newDoc) {
        service.cancelDocument(oldDoc, newDoc);
        if (newDoc.isEmpty()) {
            trace.write(ENTITY_TYPE, "CANCEL", oldDoc.getId());
        } else {
            trace.write(ENTITY_TYPE, "CANCEL_REPLACE", oldDoc.getId());
        }

    }
}
