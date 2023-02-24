package lu.esante.agence.epione.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "histo_stat")
public class HistoStat {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "stat_file")
    @Getter
    @Setter
    private String statFile;

    @Column(name = "export_count")
    @Getter
    @Setter
    private int exportCount;

    @Column(name = "stat_type")
    @Getter
    @Setter
    private String statType;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @Getter
    @Setter
    private OffsetDateTime createdat;

    @Column(name = "end_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @Getter
    @Setter
    private OffsetDateTime endat;
}