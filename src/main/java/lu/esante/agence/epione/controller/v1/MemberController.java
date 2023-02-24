package lu.esante.agence.epione.controller.v1;

import java.util.List;
import java.util.Optional;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.openapitools.api.MemberApi;
import org.openapitools.model.MemberDto;
import org.openapitools.model.PractitionerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import lu.esante.agence.epione.controller.V1AbstractController;
import lu.esante.agence.epione.exception.ForbiddenException;
import lu.esante.agence.epione.model.PractitionerMember;
import lu.esante.agence.epione.service.IIdentityService;
import lu.esante.agence.epione.service.IPractitionerMemberService;

@RestController
public class MemberController extends V1AbstractController implements MemberApi {

    IPractitionerMemberService service;
    IIdentityService identity;

    @Autowired
    public MemberController(IPractitionerMemberService service, IIdentityService identity) {
        this.service = service;
        this.identity = identity;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('PRACTITIONER')")
    public ResponseEntity<Void> _addMember2Practitioner(
            @Pattern(regexp = "[0-9]{10}") @Size(min = 10, max = 10) String eHealthId, String identifier)
            throws Exception {

        if (!this.identity.getName().equals(eHealthId)) {
            throw new ForbiddenException("You cannot access this resource");
        }

        Optional<PractitionerMember> opt = service.getByEHealthIdAndMemberId(eHealthId, identifier);
        if (!opt.isEmpty()) {
            // TODO: Throw exception
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        PractitionerMember member = new PractitionerMember();
        member.setEHealthId(eHealthId);
        member.setMemberId(identifier);
        service.save(member);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('PRACTITIONER')")
    public ResponseEntity<Void> _deleteMemberFromPractitioner(
            @Pattern(regexp = "[0-9]{10}") @Size(min = 10, max = 10) String eHealthId, String identifier)
            throws Exception {
        if (!this.identity.getName().equals(eHealthId)) {
            throw new ForbiddenException("You cannot access this resource");
        }

        Optional<PractitionerMember> opt = service.getByEHealthIdAndMemberId(eHealthId, identifier);
        if (!opt.isEmpty()) {
            service.delete(opt.get());
        }

        return new ResponseEntity<>(HttpStatus.OK);

    }

    @Override
    @PreAuthorize("hasAnyAuthority('PRACTITIONER')")
    public ResponseEntity<Void> _getMemberFromPractitioner(
            @Pattern(regexp = "[0-9]{10}") @Size(min = 10, max = 10) String eHealthId, String identifier)
            throws Exception {
        if (!this.identity.getName().equals(eHealthId)) {
            throw new ForbiddenException("You cannot access this resource");
        }

        Optional<PractitionerMember> opt = service.getByEHealthIdAndMemberId(eHealthId, identifier);
        if (opt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('PRACTITIONER')")
    public ResponseEntity<PractitionerDto> _getMembersFromPractitioner(
            @Pattern(regexp = "[0-9]{10}") @Size(min = 10, max = 10) String eHealthId) throws Exception {
        if (!this.identity.getName().equals(eHealthId)) {
            throw new ForbiddenException("You cannot access this resource");
        }

        List<PractitionerMember> list = service.getByEHealthId(eHealthId);
        return new ResponseEntity<>(businessToDto(list, eHealthId), HttpStatus.OK);
    }

    private PractitionerDto businessToDto(List<PractitionerMember> member, String eHealthId) {
        PractitionerDto dto = new PractitionerDto();
        dto.seteHealthId(eHealthId);
        for (PractitionerMember practitionerMember : member) {
            MemberDto d = new MemberDto();
            d.setIdentifier(practitionerMember.getMemberId());
            dto.addMembersItem(d);
        }
        return dto;
    }

}
