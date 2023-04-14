package lu.esante.agence.epione.service.impl.document;

import java.io.UnsupportedEncodingException;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import lombok.extern.slf4j.Slf4j;
import lu.esante.agence.epione.entity.DocumentEntity;
import lu.esante.agence.epione.entity.DocumentStatusEntity;
import lu.esante.agence.epione.entity.DocumentTypeEntity;
import lu.esante.agence.epione.exception.DocumentFormatException;
import lu.esante.agence.epione.exception.ForbiddenException;
import lu.esante.agence.epione.exception.IllegalOperationException;
import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.model.DocumentStatus;
import lu.esante.agence.epione.model.DocumentType;
import lu.esante.agence.epione.repository.DocumentRepository;
import lu.esante.agence.epione.repository.DocumentStatusRepository;
import lu.esante.agence.epione.repository.DocumentTypeRepository;
import lu.esante.agence.epione.service.IDocumentBatchHelper;
import lu.esante.agence.epione.service.IDocumentService;
import lu.esante.agence.epione.structure.AbstractMapper;
import lu.esante.agence.epione.util.DocumentUtils;

@Slf4j
public class DocumentServiceImpl extends AbstractMapper<Document, DocumentEntity>
        implements IDocumentService, IDocumentBatchHelper {

    private DocumentRepository repo;

    protected EnumMap<DocumentType, DocumentTypeEntity> documentTypes = new EnumMap<>(DocumentType.class);
    protected EnumMap<DocumentStatus, DocumentStatusEntity> documentStatuses = new EnumMap<>(DocumentStatus.class);

    @Autowired
    EntityManager entityManager;

    @Autowired
    public DocumentServiceImpl(DocumentRepository repo, DocumentTypeRepository typeRepo,
            DocumentStatusRepository statusRepo) {
        this.repo = repo;
        Stream.of(DocumentType.values())
                .forEach((key) -> {
                    Optional<DocumentTypeEntity> t = typeRepo.findByCode(key.toString());
                    if (t.isEmpty()) {
                        throw new IllegalStateException("Missing document type for " + key);
                    }
                    documentTypes.put(key, t.get());
                });

        Stream.of(DocumentStatus.values())
                .forEach((key) -> {
                    Optional<DocumentStatusEntity> t = statusRepo.findByCode(key.toString());
                    if (t.isEmpty()) {
                        throw new IllegalStateException("Missing document status for " + key);
                    }
                    documentStatuses.put(key, t.get());
                });

    }

    @Override
    public Optional<Document> getById(UUID id) {
        Optional<DocumentEntity> res = repo.findById(id);
        return entityToBusiness(res);
    }

    @Override
    public Optional<Byte[]> getPdfById(UUID id) {
        Optional<Document> res = getByFileId(id);
        if (res.isEmpty()) {
            return Optional.empty();
        }
        try {
            return DocumentUtils.getPdpFromDocument(res.get());
        } catch (JAXBException | UnsupportedEncodingException | SAXException e) {
            throw new DocumentFormatException("Invalid document format");
        }
    }

    @Override
    public Document save(Document document) {
        DocumentEntity res = repo.save(businessToEntity(document));
        return entityToBusiness(res);
    }

    @Override
    public List<Document> getBySsnAndStatus(String ssn, DocumentStatus documentStatus) {
        List<DocumentEntity> res = repo.findBySsnAndDocumentStatus(ssn, documentStatuses.get(documentStatus));
        return entityToBusiness(res);
    }

    @Override
    public List<Document> getAvailableDocuments(String ssn) {

        List<DocumentEntity> res = repo.getAvailableDocuments(ssn);
        return entityToBusiness(res);
    }

    @Override
    public Optional<Document> getByFileId(UUID fileId) {
        Optional<DocumentEntity> res = repo.getByFileId(fileId);
        return entityToBusiness(res);
    }

    @Override
    public Document entityToBusiness(DocumentEntity b) {
        Document doc = new Document();
        doc.setAuthor(b.getAuthor());
        doc.setCreatedAt(b.getCreatedAt());
        doc.setReplacedBy(b.getReplacedBy());
        doc.setDocumentStatus(DocumentStatus.valueOf(b.getDocumentStatus().getCode()));
        doc.setDocumentType(DocumentType.valueOf(b.getDocumentType().getCode()));
        doc.setEHealthId(b.getEHealthId());
        doc.setFile(b.getFile());
        doc.setFileId(b.getFileId());
        doc.setGpCode(b.getGpCode());
        doc.setId(b.getId());
        doc.setMhNumber(b.getMhNumber());
        doc.setPaid(b.isPaid());
        doc.setSsn(b.getSsn());
        doc.setSentAt(b.getSentAt());
        doc.setPractitionerFirstname(b.getPractitionerFirstname());
        doc.setPractitionerLastname(b.getPractitionerLastname());
        doc.setPrice(b.getPrice());
        doc.setMemoireDate(b.getMemoireDate());
        doc.setMySecuId(b.getMySecuId());
        return doc;
    }

    @Override
    public DocumentEntity businessToEntity(Document a) {
        DocumentEntity doc = new DocumentEntity();
        doc.setAuthor(a.getAuthor());
        doc.setCreatedAt(a.getCreatedAt());
        doc.setReplacedBy(a.getReplacedBy());
        doc.setDocumentStatus(documentStatuses.get(a.getDocumentStatus()));
        doc.setDocumentType(documentTypes.get(a.getDocumentType()));
        doc.setEHealthId(a.getEHealthId());
        doc.setFile(a.getFile());
        doc.setFileId(a.getFileId());
        doc.setGpCode(a.getGpCode());
        doc.setId(a.getId());
        doc.setMhNumber(a.getMhNumber());
        doc.setPaid(a.isPaid());
        doc.setSsn(a.getSsn());
        doc.setSentAt(a.getSentAt());
        doc.setPractitionerFirstname(a.getPractitionerFirstname());
        doc.setPractitionerLastname(a.getPractitionerLastname());
        doc.setPrice(a.getPrice());
        doc.setMemoireDate(a.getMemoireDate());
        doc.setMySecuId(a.getMySecuId());

        return doc;
    }

    @Override
    public Document acknowledgeSend(Document document, String mySecuId) throws IllegalOperationException {
        if (document.getMySecuId() != null) {
            log.error("Trying to override document mysecu id for document %s", document.getId());
            throw new IllegalOperationException("Trying to override document mysecu id");
        }
        updateMySecuIdOnly(document.getId(), mySecuId);
        updateIfStatusEquals(document, document.getDocumentStatus(), DocumentStatus.SENT);

        return document;
    }

    @Override
    public void cancelDocument(Document oldDoc, Optional<Document> newDoc) {
        // Save the new document if it exists
        if (!newDoc.isEmpty()) {
            Document replacer = save(newDoc.get());
            oldDoc.setReplacedBy(replacer.getId());
        }

        // Update the status
        List<DocumentStatus> allowedStatus = List.of(DocumentStatus.RECEIVED, DocumentStatus.SENT,
                DocumentStatus.REIMBURSEMENT_ASKED, DocumentStatus.REIMBURSEMENT_SENT);
        if (allowedStatus.contains(oldDoc.getDocumentStatus())) {
            oldDoc.setDocumentStatus(DocumentStatus.CANCEL_ASKED);
        } else {
            throw new ForbiddenException("This document haven't been acknowledged by the CNS yet. Retry later.");
        }
        // Persist the old document state
        save(oldDoc);
    }

    @Override
    public List<Document> getAllReceived() {
        return entityToBusiness(repo.getDocumentToSend());
    }

    @Override
    public List<Document> getAllReimbursementAsked() {
        return entityToBusiness(repo.getReimbursementAskedDocuments());
    }

    @Override
    public List<Document> getAllCancelationAsked() {
        return entityToBusiness(repo.getCancelationAskedDocuments());
    }

    @Override
    public void updateCnsAuto() {
        repo.cnsAutoApply();

    }

    @Override
    public void updateMySecuIdOnly(UUID documentId, String mySecuId) {
        if (mySecuId != null) {
            repo.updateMySecuIdOnly(mySecuId, documentId);
        }
    }

    @Override
    public void updateIfStatusEquals(Document document, DocumentStatus oldStatus, DocumentStatus newStatus) {
        UUID oldStatusId = documentStatuses.get(oldStatus).getId();
        UUID newStatusId = documentStatuses.get(newStatus).getId();

        repo.updateIfStatusEquals(document.getId(), oldStatusId, newStatusId);

    }

    @Override
    public Document saveHelper(Document document) {
        return save(document);
    }

}
