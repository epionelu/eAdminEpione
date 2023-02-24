package lu.esante.agence.epione.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidConsentTypeException extends Exception {
    public InvalidConsentTypeException(String message) {
        super(message);
    }
}
