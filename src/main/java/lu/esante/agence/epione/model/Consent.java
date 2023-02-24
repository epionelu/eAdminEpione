package lu.esante.agence.epione.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class Consent {

    private UUID id;

    private String author;

    private String ssn;

    private OffsetDateTime startAt;

    private OffsetDateTime endAt;

    private ConsentType consentType;
}
