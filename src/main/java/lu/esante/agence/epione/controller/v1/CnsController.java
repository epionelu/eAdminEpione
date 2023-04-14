package lu.esante.agence.epione.controller.v1;

import lu.esante.agence.epione.controller.V1AbstractController;
import org.openapitools.api.CnsApi;
import org.openapitools.model.DocumentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class CnsController extends V1AbstractController implements CnsApi {

    @Override
    public ResponseEntity<List<DocumentDto>> _getCNSDocumentsFromPerson(String ssn, Optional<Integer> page, Optional<Integer> size) throws Exception {
        return null;
    }
}
