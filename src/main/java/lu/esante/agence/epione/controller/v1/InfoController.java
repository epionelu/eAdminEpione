package lu.esante.agence.epione.controller.v1;

import org.openapitools.api.InfoApi;
import org.openapitools.model.PersonInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import lu.esante.agence.epione.auth.Roles;
import lu.esante.agence.epione.client.mpi.MpiClient;
import lu.esante.agence.epione.client.mpi.model.MpiPerson;
import lu.esante.agence.epione.controller.V1AbstractController;
import lu.esante.agence.epione.exception.ForbiddenException;
import lu.esante.agence.epione.service.IIdentityService;

@RestController
public class InfoController extends V1AbstractController implements InfoApi {

    @Autowired
    private MpiClient mpi;

    @Autowired
    private IIdentityService identity;

    @Override
    @PreAuthorize("hasAnyAuthority('PATIENT')")
    public ResponseEntity<PersonInfoDto> _getPersonInfo() throws Exception {

        if (identity.hasRole(Roles.PATIENT)) {
            MpiPerson patient = mpi.getPatientBySsn(identity.getName(), identity.getName());

            PersonInfoDto dto = new PersonInfoDto();
            dto.setSsn(identity.getName());

            if (patient != null) {
                dto.setFirstname(patient.getFirstname());
                dto.setLastname(patient.getFamilyname());
            }

            return new ResponseEntity<>(dto, HttpStatus.OK);

        } else {
            throw new ForbiddenException("Only patient are allowed to retrieve additional information");
        }
    }

}
