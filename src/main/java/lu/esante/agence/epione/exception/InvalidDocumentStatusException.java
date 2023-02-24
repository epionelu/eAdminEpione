package lu.esante.agence.epione.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidDocumentStatusException extends RuntimeException {
    public InvalidDocumentStatusException(String message) {
        super(message);
    }
}
