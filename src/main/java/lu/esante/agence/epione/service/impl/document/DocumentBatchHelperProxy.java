package lu.esante.agence.epione.service.impl.document;

import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.model.DocumentStatus;
import lu.esante.agence.epione.service.IDocumentBatchHelper;
import lu.esante.agence.epione.service.ITraceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class DocumentBatchHelperProxy implements IDocumentBatchHelper {

    ITraceService trace;
    IDocumentBatchHelper service;

    @Autowired
    public DocumentBatchHelperProxy(ITraceService trace, IDocumentBatchHelper service) {
        this.trace = trace;
        this.service = service;
    }

    @Override
    public List<Document> getAll() {
        return service.getAll();
    }

    @Override
    @Transactional
    public void acknowledgeSend(Document document) {
        trace.write("DOCUMENT", "SEND", document.getId(), "EPIONE", "[EPIONE]");
        service.acknowledgeSend(document);
    }

    @Override
    @Transactional
    public void batchChangeStatus(String ssn, DocumentStatus initial, DocumentStatus target) {
        trace.write("DOCUMENT", "CNS_AUTO");
        service.batchChangeStatus(ssn, initial, target);
    }
}
