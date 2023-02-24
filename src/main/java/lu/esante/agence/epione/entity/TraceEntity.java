package lu.esante.agence.epione.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;

@Entity
@Table(name = "trace")
@Data
public class TraceEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")

    private UUID id;

    private String action;
    private String author;
    @Column(name = "entity_type")
    private String entityType;
    @Column(name = "reference_id")
    private UUID referenceId;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "author_roles")
    private String authorRoles;
}
