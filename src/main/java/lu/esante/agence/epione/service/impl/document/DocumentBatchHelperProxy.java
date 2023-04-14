package lu.esante.agence.epione.service.impl.document;

import lu.esante.agence.epione.exception.IllegalOperationException;
import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.model.DocumentStatus;
import lu.esante.agence.epione.service.IDocumentBatchHelper;
import lu.esante.agence.epione.service.ITraceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public class DocumentBatchHelperProxy implements IDocumentBatchHelper {

    ITraceService trace;
    IDocumentBatchHelper service;

    @Autowired
    public DocumentBatchHelperProxy(ITraceService trace, IDocumentBatchHelper service) {
        this.trace = trace;
        this.service = service;
    }

    @Override
    @Transactional
    public Document acknowledgeSend(Document document, String mySecuId) throws IllegalOperationException {
        trace.write("DOCUMENT", "SEND", document.getId(), "EPIONE", "[EPIONE]");
        return service.acknowledgeSend(document, mySecuId);

    }

    @Override
    public List<Document> getAllReceived() {
        return service.getAllReceived();
    }

    @Override
    public List<Document> getAllReimbursementAsked() {
        return service.getAllReimbursementAsked();
    }

    @Override
    public List<Document> getAllCancelationAsked() {
        return service.getAllCancelationAsked();
    }

    @Override
    public void updateCnsAuto() {
        trace.write("DOCUMENT", "UPDATE CNS_AUTO");
        service.updateCnsAuto();

    }

    @Override
    public void updateMySecuIdOnly(UUID documentid, String mySecuId) {
        service.updateMySecuIdOnly(documentid, mySecuId);

    }

    @Override
    public void updateIfStatusEquals(Document document, DocumentStatus oldStatus, DocumentStatus newStatus) {
        service.updateIfStatusEquals(document, oldStatus, newStatus);

    }

    @Override
    public Document saveHelper(Document document) {
        return service.saveHelper(document);
    }

}
