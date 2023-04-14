package lu.esante.agence.epione.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lu.esante.agence.epione.entity.DocumentEntity;
import lu.esante.agence.epione.entity.DocumentStatusEntity;
import lu.esante.agence.epione.entity.DocumentTypeEntity;
import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.model.DocumentStatus;
import lu.esante.agence.epione.model.DocumentType;
import lu.esante.agence.epione.repository.DocumentRepository;
import lu.esante.agence.epione.repository.DocumentStatusRepository;
import lu.esante.agence.epione.repository.DocumentTypeRepository;
import lu.esante.agence.epione.service.impl.document.DocumentServiceImpl;

@ExtendWith(SpringExtension.class)
public class DocumentServiceImplTests {

    @MockBean
    private DocumentRepository repo;

    @MockBean
    private DocumentTypeRepository typeRepo;

    @MockBean
    private DocumentStatusRepository statusRepo;

    @Test
    public void documentServiceInitializationFailureTest() {
        Stream.of(DocumentStatus.values())
                .forEach((key) -> {
                    when(statusRepo.findByCode(key.toString()))
                            .thenReturn(Optional.of(buildDocumentStatusEntity(key.toString())));
                });
        when(typeRepo.findByCode("MH"))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(IllegalStateException.class, () -> new DocumentServiceImpl(repo, typeRepo, statusRepo));
    }

    @Test
    public void documentServiceInitializationFailureTest2() {
        Stream.of(DocumentType.values())
                .forEach((key) -> {
                    when(typeRepo.findByCode(key.toString()))
                            .thenReturn(Optional.of(buildDocumentTypeEntity(key.toString())));
                });
        when(statusRepo.findByCode("SENT"))
                .thenReturn(Optional.of(buildDocumentStatusEntity("SENT")));
        Assertions.assertThrows(IllegalStateException.class, () -> new DocumentServiceImpl(repo, typeRepo, statusRepo));
    }

    @Test
    public void getByIdTest() {
        bootstrapService();
        DocumentServiceImpl service = new DocumentServiceImpl(repo, typeRepo, statusRepo);

        when(repo.findById(any()))
                .thenReturn(Optional.empty());
        Assertions.assertEquals(Optional.empty(), service.getById(UUID.randomUUID()));
    }

    @Test
    public void getDocumentBySsnAndStatusTest() {
        bootstrapService();
        DocumentServiceImpl service = new DocumentServiceImpl(repo, typeRepo, statusRepo);
        when(repo.findBySsnAndDocumentStatus(any(), any()))
                .thenReturn(List.of());
        Assertions.assertEquals(List.of(), service.getBySsnAndStatus("ssn", DocumentStatus.CANCEL_ASKED));
    }

    @Test
    public void saveTest() {
        bootstrapService();
        DocumentServiceImpl service = new DocumentServiceImpl(repo, typeRepo, statusRepo);
        DocumentEntity doc = buildDocumentEntity();
        when(repo.save(any()))
                .thenReturn(doc);

        Assertions.assertEquals(doc.getId(), service.save(service.entityToBusiness(doc)).getId());
    }

    private DocumentTypeEntity buildDocumentTypeEntity(String code) {
        DocumentTypeEntity ent = new DocumentTypeEntity();
        ent.setId(UUID.randomUUID());
        ent.setCode(code);
        ent.setDescription(code);
        return ent;
    }

    private DocumentStatusEntity buildDocumentStatusEntity(String code) {
        DocumentStatusEntity ent = new DocumentStatusEntity();
        ent.setId(UUID.randomUUID());
        ent.setCode(code);
        ent.setDescription(code);
        return ent;
    }

    private DocumentEntity buildDocumentEntity() {
        DocumentEntity document = new DocumentEntity();
        document.setId(UUID.randomUUID());
        document.setDocumentStatus(statusRepo.findByCode("SENT").get());
        document.setDocumentType(typeRepo.findByCode("MH").get());
        return document;
    }

    private void bootstrapService() {
        Stream.of(DocumentType.values())
                .forEach((key) -> {
                    when(typeRepo.findByCode(key.toString()))
                            .thenReturn(Optional.of(buildDocumentTypeEntity(key.toString())));
                });

        Stream.of(DocumentStatus.values())
                .forEach((key) -> {
                    when(statusRepo.findByCode(key.toString()))
                            .thenReturn(Optional.of(buildDocumentStatusEntity(key.toString())));
                });
    }

}
