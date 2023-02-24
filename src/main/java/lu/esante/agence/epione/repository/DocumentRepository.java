package lu.esante.agence.epione.repository;

import lu.esante.agence.epione.entity.DocumentEntity;
import lu.esante.agence.epione.entity.DocumentStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

    List<DocumentEntity> findBySsnAndDocumentStatus(String ssn, DocumentStatusEntity documentStatus);

    @Query(value = "select document.* from document inner join consent on consent.ssn = document.ssn inner join consent_type on consent.consent_type = consent_type.id inner join document_status on document.document_status = document_status.id where consent_type.code = 'CNS' and document_status.code = 'TO_SEND' and document.paid = true and consent.end_at > now() and consent.start_at < now();", nativeQuery = true)
    List<DocumentEntity> getDocumentToSend();

    Optional<DocumentEntity> getByFileId(UUID fileId);

    @Query(value = "select * from document inner join document_status on document.document_status=document_status.id where (document_status.code = 'RECEIVED' or (document.created_at < now() + interval '1 year' and document_status.code <> 'RECEIVED' )) and document.ssn=:ssn", nativeQuery = true)
    List<DocumentEntity> getAvailableDocuments(@Param("ssn") String ssn);

    @Modifying
    @Query(value = "update document set document_status=:target where ssn=:ssn and document_status=:initial and paid=true", nativeQuery = true)
    void batchChangeStatus(@Param("ssn") String ssn, @Param("initial") UUID initial, @Param("target") UUID target);
}