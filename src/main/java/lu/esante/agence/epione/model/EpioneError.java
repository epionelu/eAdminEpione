package lu.esante.agence.epione.model;

import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
@Builder
public class EpioneError {
    private String message;
    private String code;
    private OffsetDateTime timestamp;
}
