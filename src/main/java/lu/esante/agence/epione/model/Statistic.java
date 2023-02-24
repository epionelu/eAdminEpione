package lu.esante.agence.epione.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;


@Entity
@Table
public class Statistic {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Getter
    @Setter
    private Integer createdAtYear;
    @Getter
    @Setter
    private Integer createdAtMonth;
    @Getter
    @Setter
    private Integer sentAtYear;
    @Getter
    @Setter
    private Integer sentAtMonth;
    @Getter
    @Setter
    private String practitionerId;
    @Getter
    @Setter
    private Integer total;
    @Getter
    @Setter
    private Integer sent;
    @Getter
    @Setter
    private Integer received;
    @Getter
    @Setter
    private Integer cancelled;


    public Statistic() {
    }

    public Statistic(Integer createdAtYear,
                     Integer createdAtMonth,
                     Integer sentAtYear,
                     Integer sentAtMonth,
                     String practitionerId,
                     Integer total,
                     Integer sent,
                     Integer received,
                     Integer cancelled) {
        this.cancelled = cancelled;
        this.sent = sent;
        this.total = total;
        this.createdAtMonth = createdAtMonth;
        this.received = received;
        this.createdAtYear = createdAtYear;
        this.sentAtMonth = sentAtMonth;
        this.sentAtYear = sentAtYear;
        this.practitionerId = practitionerId;

    }


}