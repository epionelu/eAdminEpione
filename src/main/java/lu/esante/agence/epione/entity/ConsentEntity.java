package lu.esante.agence.epione.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;

import lombok.Data;

@Entity
@Table(name = "consent")
@Data
@Audited
public class ConsentEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")

    private UUID id;

    private String author;

    private String ssn;

    @Column(name = "start_at")
    private OffsetDateTime startAt;
    @Column(name = "end_at")
    private OffsetDateTime endAt;

    @JoinColumn(name = "consent_type", referencedColumnName = "id")
    @OneToOne
    private ConsentTypeEntity consentType;
}
