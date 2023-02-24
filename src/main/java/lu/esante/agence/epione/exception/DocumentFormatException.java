package lu.esante.agence.epione.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class DocumentFormatException extends RuntimeException {
    public DocumentFormatException(String message) {
        super(message);
    }
}
