package lu.esante.agence.epione.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "extractData")
public class ExtractData {

    public ExtractData() {
    }

    public ExtractData(String documentId, String ehealthid, String gpCodeInvoice, String patientSSN,
            String documentType, String documentLogicalId, String sent, String status) {
        this.documentId = documentId;
        this.sent = sent;
        this.documentLogicalId = documentLogicalId;
        this.ehealthid = ehealthid;
        this.documentType = documentType;
        this.gpCodeInvoice = gpCodeInvoice;
        this.patientSsn = patientSSN;
        this.status = status;
    }

    @Id
    @GeneratedValue
    private Long id;

    @Getter
    @Setter
    private String documentId;

    @Getter
    @Setter
    private String ehealthid;

    @Getter
    @Setter
    private String gpCodeInvoice;

    @Getter
    @Setter
    private String patientSsn;

    @Getter
    @Setter
    private String documentType;

    @Getter
    @Setter
    private String documentLogicalId;

    @Getter
    @Setter
    private String sent;

    @Getter
    @Setter
    private String status;

}