package lu.esante.agence.epione.service;

import java.util.List;
import java.util.UUID;

import lu.esante.agence.epione.exception.IllegalOperationException;
import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.model.DocumentStatus;

public interface IDocumentBatchHelper {

    List<Document> getAllReceived();

    List<Document> getAllReimbursementAsked();

    List<Document> getAllCancelationAsked();

    Document saveHelper(Document document);

    Document acknowledgeSend(Document document, String mySecuId) throws IllegalOperationException;

    void updateCnsAuto();

    void updateMySecuIdOnly(UUID documentid, String mySecuId);

    void updateIfStatusEquals(Document document, DocumentStatus oldStatus, DocumentStatus newStatus);

}
