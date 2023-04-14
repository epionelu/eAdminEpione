package lu.esante.agence.epione.service.impl.member;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lu.esante.agence.epione.model.PractitionerMember;
import lu.esante.agence.epione.service.IPractitionerMemberService;
import lu.esante.agence.epione.service.ITraceService;

/**
 * Proxy auditing the accessed resources
 * This proxy must be placed first in the chain
 */
public class PractitionerMemberAuditProxy implements IPractitionerMemberService {
    public static final String ENTITY_TYPE = "MEMBER";

    private IPractitionerMemberService service;
    private ITraceService trace;

    @Autowired
    public PractitionerMemberAuditProxy(IPractitionerMemberService service, ITraceService trace) {
        this.service = service;
        this.trace = trace;
    }

    @Override
    @Transactional
    public Optional<PractitionerMember> getById(UUID id) {
        Optional<PractitionerMember> res = service.getById(id);
        if (!res.isEmpty()) {
            trace.write(ENTITY_TYPE, "GET BY ID", res.get().getId());
        }
        return res;
    }

    @Override
    @Transactional
    public List<PractitionerMember> getByEHealthId(String eHealthId) {
        List<PractitionerMember> res = service.getByEHealthId(eHealthId);
        trace.write(ENTITY_TYPE, "LIST BY EHEALTHID");
        return res;
    }

    @Override
    @Transactional
    public PractitionerMember save(PractitionerMember practitionerMember) {
        PractitionerMember res = service.save(practitionerMember);
        trace.write(ENTITY_TYPE, "SAVE", res.getId());
        return res;
    }

    @Override
    public Optional<PractitionerMember> getByEHealthIdAndMemberId(String eHealthId, String memberId) {
        Optional<PractitionerMember> res = service.getByEHealthIdAndMemberId(eHealthId, memberId);
        trace.write(ENTITY_TYPE, "GET ONE");
        return res;
    }

    @Override
    @Transactional
    public void delete(PractitionerMember practitionerMember) {
        trace.write(ENTITY_TYPE, "DELETE", practitionerMember.getId());
        service.delete(practitionerMember);
    }
}
