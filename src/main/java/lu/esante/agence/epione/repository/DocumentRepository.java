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

    @Query(value = "select document.* from document inner join document_status on document.document_status = document_status.id where document_status.code = 'RECEIVED';", nativeQuery = true)
    List<DocumentEntity> getDocumentToSend();

    Optional<DocumentEntity> getByFileId(UUID fileId);

    @Query(value = "select * from document inner join document_status on document.document_status=document_status.id where (document_status.code = 'RECEIVED' or (document.created_at < now() + interval '1 year' and document_status.code <> 'RECEIVED' )) and document.ssn=:ssn", nativeQuery = true)
    List<DocumentEntity> getAvailableDocuments(@Param("ssn") String ssn);

    @Query(value = "select document.* from document inner join document_status on document.document_status = document_status.id where document_status.code = 'REIMBURSEMENT_ASKED';", nativeQuery = true)
    List<DocumentEntity> getReimbursementAskedDocuments();

    @Query(value = "select document.* from document inner join document_status on document.document_status = document_status.id where document_status.code = 'CANCEL_ASKED' or document_status.code = 'CANCEL_REPLACED';", nativeQuery = true)
    List<DocumentEntity> getCancelationAskedDocuments();

    @Modifying
    @Query(value = "update document set document_status=:target where ssn=:ssn and document_status=:initial", nativeQuery = true)
    void batchChangeStatus(@Param("ssn") String ssn, @Param("initial") UUID initial, @Param("target") UUID target);

    @Modifying
    @Query(value = "update document set document_status = ds2.id from document as d inner join consent as c on d.ssn = c.ssn inner join consent_type as ct on c.consent_type = ct.id inner join document_status as ds on d.document_status = ds.id inner join document_status as ds2 on ds2.code = 'REIMBURSEMENT_ASKED' where ct.code = 'CNS_AUTO' and ds.code = 'SENT' and c.start_at < now() and c.end_at > now();", nativeQuery = true)
    void cnsAutoApply();

    @Modifying
    @Query(value = "update document set my_secu_id = :mySecuId where id = :docId", nativeQuery = true)
    void updateMySecuIdOnly(@Param("mySecuId") String mySecuId, @Param("docId") UUID docId);

    @Modifying
    @Query(value = "update document set document_status = :newStatusId where id = :docId and document_status = :oldStatusId", nativeQuery = true)
    void updateIfStatusEquals(@Param("docId") UUID docId, @Param("oldStatusId") UUID oldStatusId,
            @Param("newStatusId") UUID newStatusId);

}