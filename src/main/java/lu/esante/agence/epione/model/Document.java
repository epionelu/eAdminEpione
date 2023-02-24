package lu.esante.agence.epione.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class Document {

    private UUID id;

    private String author;

    private String gpCode;

    private String eHealthId;

    private UUID replacedBy;

    private String ssn;

    private boolean paid;

    private UUID fileId;

    private String mhNumber;

    private OffsetDateTime sentAt;

    private OffsetDateTime createdAt;

    private DocumentType documentType;

    private DocumentStatus documentStatus;

    private byte[] file;

    private float price;
    private String practitionerFirstname;
    private String practitionerLastname;
    private OffsetDateTime memoireDate;
}
