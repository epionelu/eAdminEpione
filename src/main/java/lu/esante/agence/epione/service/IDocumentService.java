package lu.esante.agence.epione.service;

import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.model.DocumentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IDocumentService {
    Optional<Document> getById(UUID id);

    Optional<Byte[]> getPdfById(UUID id);

    Document save(Document document);

    List<Document> getBySsnAndStatus(String ssn, DocumentStatus documentStatus);

    Optional<Document> getByFileId(UUID fileId);

    List<Document> getAvailableDocuments(String ssn);

    /**
     * Put a document in the correct cancelation state *
     * 
     * @param oldDoc: The document to replace
     * @param newDoc: An optional taking a document as replacement
     */
    void cancelDocument(Document oldDoc, Optional<Document> newDoc);
}
