package lu.esante.agence.epione.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;

import lombok.Data;

@Data
@Entity
@Table(name = "practitioner_member")
@Audited
public class PractitionerMemberEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "ehealth_id")
    private String eHealthId;
    @Column(name = "member_id")
    private String memberId;
}
