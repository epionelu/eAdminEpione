package lu.esante.agence.epione.model;

import java.util.UUID;

import lombok.Data;

@Data
public class PractitionerMember {

    private UUID id;

    private String eHealthId;

    private String memberId;
}
