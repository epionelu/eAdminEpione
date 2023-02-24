package lu.esante.agence.epione.service;

import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.model.DocumentStatus;

import java.util.List;

public interface IDocumentBatchHelper {

    List<Document> getAll();

    void acknowledgeSend(Document document);

    void batchChangeStatus(String ssn, DocumentStatus initial, DocumentStatus target);
}
