package lu.esante.agence.epione.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "document")
@Data
@Audited
public class DocumentEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    private String author;
    @Column(name = "gp_code")
    private String gpCode;

    @Column(name = "ehealth_id")
    private String eHealthId;

    @Column(name = "replaced_by")
    private UUID replacedBy;
    private String ssn;
    private boolean paid;
    @Column(name = "file_id")
    private UUID fileId;
    @Column(name = "mh_number")
    private String mhNumber;
    @Column(name = "sent_at")
    private OffsetDateTime sentAt;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "my_secu_id")
    private String mySecuId;
    @JoinColumn(name = "document_type", referencedColumnName = "id")
    @OneToOne
    private DocumentTypeEntity documentType;
    @JoinColumn(name = "document_status", referencedColumnName = "id")
    @OneToOne
    private DocumentStatusEntity documentStatus;

    private float price;
    @Column(name = "practitioner_firstname")
    private String practitionerFirstname;
    @Column(name = "practitioner_lastname")
    private String practitionerLastname;

    private byte[] file;

    @Column(name = "memoire_date")
    private OffsetDateTime memoireDate;
}
