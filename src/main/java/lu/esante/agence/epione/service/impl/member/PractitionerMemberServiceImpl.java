package lu.esante.agence.epione.service.impl.member;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import lu.esante.agence.epione.entity.PractitionerMemberEntity;
import lu.esante.agence.epione.model.PractitionerMember;
import lu.esante.agence.epione.repository.PractitionerMemberRepository;
import lu.esante.agence.epione.service.IPractitionerMemberService;
import lu.esante.agence.epione.service.ITeamVerifier;
import lu.esante.agence.epione.structure.AbstractMapper;

public class PractitionerMemberServiceImpl extends AbstractMapper<PractitionerMember, PractitionerMemberEntity>
        implements IPractitionerMemberService, ITeamVerifier {

    PractitionerMemberRepository repo;

    @Autowired
    public PractitionerMemberServiceImpl(PractitionerMemberRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<PractitionerMember> getById(UUID id) {
        Optional<PractitionerMemberEntity> res = repo.findById(id);
        return entityToBusiness(res);
    }

    @Override
    public List<PractitionerMember> getByEHealthId(String eHealthId) {
        List<PractitionerMemberEntity> res = repo.findByeHealthId(eHealthId);
        return entityToBusiness(res);
    }

    @Override
    public PractitionerMember save(PractitionerMember practitionerMember) {
        PractitionerMemberEntity res = repo.save(businessToEntity(practitionerMember));
        return entityToBusiness(res);
    }

    @Override
    public void delete(PractitionerMember practitionerMember) {
        repo.delete(businessToEntity(practitionerMember));

    }

    @Override
    public Optional<PractitionerMember> getByEHealthIdAndMemberId(String eHealthId, String memberId) {
        Optional<PractitionerMemberEntity> opt = repo.findByeHealthIdAndMemberId(eHealthId, memberId);
        return entityToBusiness(opt);
    }

    @Override
    public boolean isTeamMember(String identifier, String eHealthId) {
        return !getByEHealthIdAndMemberId(eHealthId, identifier).isEmpty();
    }

    @Override
    public PractitionerMember entityToBusiness(PractitionerMemberEntity b) {
        PractitionerMember member = new PractitionerMember();
        member.setId(b.getId());
        member.setEHealthId(b.getEHealthId());
        member.setMemberId(b.getMemberId());

        return member;
    }

    @Override
    public PractitionerMemberEntity businessToEntity(PractitionerMember a) {
        PractitionerMemberEntity member = new PractitionerMemberEntity();
        member.setId(a.getId());
        member.setEHealthId(a.getEHealthId());
        member.setMemberId(a.getMemberId());

        return member;
    }

}
