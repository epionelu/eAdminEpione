package lu.esante.agence.epione.controller.v1;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.openapitools.api.ConsentApi;
import org.openapitools.model.ConsentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import lu.esante.agence.epione.auth.Roles;
import lu.esante.agence.epione.controller.V1AbstractController;
import lu.esante.agence.epione.exception.BadRequestException;
import lu.esante.agence.epione.exception.ForbiddenException;
import lu.esante.agence.epione.exception.InvalidConsentTypeException;
import lu.esante.agence.epione.model.Consent;
import lu.esante.agence.epione.model.ConsentType;
import lu.esante.agence.epione.service.IConsentService;
import lu.esante.agence.epione.service.IIdentityService;
import org.springframework.http.HttpStatus;

@RestController
public class ConsentController extends V1AbstractController implements ConsentApi {

    private IConsentService service;
    private IIdentityService identity;

    @Autowired
    public ConsentController(IConsentService service, IIdentityService identity) {
        this.service = service;
        this.identity = identity;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('PATIENT')")
    public ResponseEntity<Void> _addConsent2Person(@Pattern(regexp = "\\d{13}") @Size(min = 13, max = 13) String ssn,
            @Valid ConsentDto consentDto) throws Exception {
        checksPatientNameEqualsSsn(ssn);
        createConsent(ssn, consentDto);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @Override
    @PreAuthorize("hasAnyAuthority('PRACTITIONER')")
    public ResponseEntity<Void> _addConsent2PersonPractitioner(
            @Pattern(regexp = "\\d{13}") @Size(min = 13, max = 13) String ssn, @Valid ConsentDto consentDto)
            throws Exception {
        if (consentDto.getType() != ConsentDto.TypeEnum.MH) {
            throw new ForbiddenException("You cannot create this kind of consent");
        }
        createConsent(ssn, consentDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('PATIENT')")
    public ResponseEntity<Void> _deleteConsentFromPerson(
            @Pattern(regexp = "\\d{13}") @Size(min = 13, max = 13) String ssn, String consentType) throws Exception {
        checksPatientNameEqualsSsn(ssn);
        Optional<Consent> opt = service.getBySsnAndConsentType(ssn, map(ConsentDto.TypeEnum.valueOf(consentType)));
        if (!opt.isEmpty()) {
            service.delete(opt.get());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('PATIENT')")
    public ResponseEntity<ConsentDto> _getConsentByTypeFromPerson(
            @Pattern(regexp = "\\d{13}") @Size(min = 13, max = 13) String ssn, String consentType) throws Exception {
        checksPatientNameEqualsSsn(ssn);
        Optional<Consent> opt = getConsentTypeByType(ssn, consentType);
        if (opt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(map(opt.get()), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('PRACTITIONER')")
    public ResponseEntity<ConsentDto> _getConsentByTypeFromPersonPractitioner(
            @Pattern(regexp = "\\d{13}") @Size(min = 13, max = 13) String ssn, String consentType) throws Exception {
        Optional<Consent> opt = getConsentTypeByType(ssn, consentType);
        if (opt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(map(opt.get()), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('PATIENT')")
    public ResponseEntity<List<ConsentDto>> _getConsentsFromPerson(
            @Pattern(regexp = "\\d{13}") @Size(min = 13, max = 13) String ssn) throws Exception {
        checksPatientNameEqualsSsn(ssn);
        List<Consent> consents = service.getBySsn(ssn);
        List<ConsentDto> res = new ArrayList<>();
        for (Consent consent : consents) {
            res.add(map(consent));
        }

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    // Check that patient accesses only his resources
    private void checksPatientNameEqualsSsn(String ssn) throws ForbiddenException {
        if (identity.hasRole(Roles.PATIENT) && !identity.getName().equals(ssn)) {
            throw new ForbiddenException("You are not allow to consult these resource");
        }
    }

    private void createConsent(String ssn, ConsentDto consentDto) throws InvalidConsentTypeException {
        Optional<Consent> opt = service.getBySsnAndConsentType(ssn, map(consentDto.getType()));
        Consent consent = null;
        if (opt.isEmpty()) {
            consent = new Consent();
            consent.setConsentType(map(consentDto.getType()));
            consent.setSsn(ssn);

        } else {
            consent = opt.get();
        }

        if (consentDto.getEndAt().isBefore(OffsetDateTime.now())) {
            throw new BadRequestException("End At must be in the future");
        }

        consent.setStartAt(OffsetDateTime.now());
        consent.setAuthor(identity.getName());
        consent.setEndAt(consentDto.getEndAt());
        service.save(consent);
    }

    private static ConsentType map(ConsentDto.TypeEnum type) throws InvalidConsentTypeException {
        switch (type) {
            case CNS:
                return ConsentType.CNS;
            case MH:
                return ConsentType.MH;
            case ESANTE:
                return ConsentType.ESANTE;
            case CNS_AUTO:
                return ConsentType.CNS_AUTO;
            default:
                throw new InvalidConsentTypeException("Invalid consent type");
        }
    }

    private static ConsentDto.TypeEnum map(ConsentType type) throws InvalidConsentTypeException {
        switch (type) {
            case CNS:
                return ConsentDto.TypeEnum.CNS;
            case MH:
                return ConsentDto.TypeEnum.MH;
            case ESANTE:
                return ConsentDto.TypeEnum.ESANTE;
            case CNS_AUTO:
                return ConsentDto.TypeEnum.CNS_AUTO;
            default:
                throw new InvalidConsentTypeException("Invalid consent type");
        }
    }

    private static ConsentDto map(Consent consent) throws InvalidConsentTypeException {
        ConsentDto res = new ConsentDto();
        res.setAuthor(consent.getAuthor());
        res.setEndAt(consent.getEndAt());
        res.setSsn(consent.getSsn());
        res.setStartAt(consent.getStartAt());
        res.setType(map(consent.getConsentType()));
        return res;
    }

    private Optional<Consent> getConsentTypeByType(String ssn, String consentType) throws InvalidConsentTypeException {
        return service.getBySsnAndConsentType(ssn, map(ConsentDto.TypeEnum.valueOf(consentType)));
    }

}
