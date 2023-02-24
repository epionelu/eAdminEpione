package lu.esante.agence.epione.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lu.esante.agence.epione.model.PractitionerMember;

public interface IPractitionerMemberService {
    Optional<PractitionerMember> getById(UUID id);

    List<PractitionerMember> getByEHealthId(String eHealthId);

    Optional<PractitionerMember> getByEHealthIdAndMemberId(String eHealthId, String memberId);

    PractitionerMember save(PractitionerMember practitionerMember);

    void delete(PractitionerMember practitionerMember);
}
