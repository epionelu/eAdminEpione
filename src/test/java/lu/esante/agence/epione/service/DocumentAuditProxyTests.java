package lu.esante.agence.epione.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.model.DocumentStatus;
import lu.esante.agence.epione.service.impl.document.DocumentAuditProxy;

@ExtendWith(SpringExtension.class)
public class DocumentAuditProxyTests {

    DocumentAuditProxy proxy;

    @MockBean
    private IDocumentService service;

    @MockBean
    private ITraceService trace;

    @BeforeEach
    public void init() {
        proxy = new DocumentAuditProxy(service, trace);
    }

    @Test
    public void getByIdTest() {
        Document doc = buildDocument();

        when(service.getById(any())).thenReturn(Optional.empty());
        when(service.getById(doc.getId())).thenReturn(Optional.of(doc));
        Optional<Document> response = proxy.getById(doc.getId());
        assertEquals(false, response.isEmpty());
        assertEquals(doc, response.get());

        response = proxy.getById(UUID.randomUUID());
        assertEquals(true, response.isEmpty());
    }

    @Test
    public void getDocumentBySsnAndStatusTest() {
        Document doc = buildDocument();
        when(service.getBySsnAndStatus(any(), any())).thenReturn(List.of());
        when(service.getBySsnAndStatus(doc.getSsn(), doc.getDocumentStatus()))
                .thenReturn(List.of(doc));
        List<Document> response = proxy.getBySsnAndStatus(doc.getSsn(), doc.getDocumentStatus());
        assertEquals(1, response.size());
        assertEquals(doc, response.get(0));

        response = proxy.getBySsnAndStatus("1234", DocumentStatus.CANCEL_ASKED);
        assertEquals(true, response.isEmpty());
    }

    @Test
    public void saveTest() {
        Document member = buildDocument();
        when(service.save(member)).thenReturn(member);
        Document response = proxy.save(member);
        assertEquals(member, response);

    }

    private Document buildDocument() {
        Document doc = new Document();
        doc.setId(UUID.randomUUID());
        doc.setSsn("ssn");
        doc.setDocumentStatus(DocumentStatus.CANCEL_ASKED);
        return doc;
    }

}
