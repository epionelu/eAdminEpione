package lu.esante.agence.epione.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hl7.fhir.dstu2.model.Practitioner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lu.esante.agence.epione.entity.PractitionerMemberEntity;
import lu.esante.agence.epione.model.PractitionerMember;
import lu.esante.agence.epione.repository.PractitionerMemberRepository;
import lu.esante.agence.epione.service.impl.member.PractitionerMemberServiceImpl;

@ExtendWith(SpringExtension.class)
public class PractitionerMemberServiceImplTests {

    @MockBean
    private PractitionerMemberRepository repo;

    private PractitionerMemberServiceImpl service;

    @BeforeEach
    public void init() {
        service = new PractitionerMemberServiceImpl(repo);
    }

    @Test
    public void getByIdTest() {

        PractitionerMemberEntity m = buildPractitionerMemberEntity("eHealthId", "memberId");
        when(repo.findById(any())).thenReturn(Optional.of(m));
        Optional<PractitionerMember> res = service.getById(UUID.randomUUID());

        Assertions.assertEquals(m.getId(), res.get().getId());
        Assertions.assertEquals(m.getEHealthId(), res.get().getEHealthId());
        Assertions.assertEquals(m.getMemberId(), res.get().getMemberId());
    }

    @Test
    public void getByIdEHealthIdTest() {
        List<PractitionerMember> res = service.getByEHealthId("TEST");
        Assertions.assertEquals(0, res.size());
    }

    @Test
    public void saveTest() {
        when(repo.save(any())).thenReturn(buildPractitionerMemberEntity("eHealthId", "memberId"));
        PractitionerMember res = service.save(new PractitionerMember());
        Assertions.assertEquals("eHealthId", res.getEHealthId());
        Assertions.assertEquals("memberId", res.getMemberId());
    }

    @Test
    public void deleteTest() {
        PractitionerMember m = buildPractitionerMember("eHealthId", "memberId");
        Assertions.assertDoesNotThrow(() -> service.delete(m));
    }

    @Test
    public void isTeamMemberTest() {
        when(repo.findByeHealthIdAndMemberId("eHealthId", "identifier")).thenReturn(Optional.empty());
        Assertions.assertEquals(false, service.isTeamMember("identifier", "eHealthId"));
        when(repo.findByeHealthIdAndMemberId("eHealthId", "identifier"))
                .thenReturn(Optional.of(buildPractitionerMemberEntity("eHealthId", "memberId")));
        Assertions.assertEquals(true, service.isTeamMember("identifier", "eHealthId"));
    }

    private PractitionerMember buildPractitionerMember(String eHealthId, String memberId) {
        PractitionerMember m = new PractitionerMember();
        m.setId(UUID.randomUUID());
        m.setEHealthId(eHealthId);
        m.setMemberId(memberId);
        return m;
    }

    private PractitionerMemberEntity buildPractitionerMemberEntity(String eHealthId, String memberId) {
        PractitionerMemberEntity m = new PractitionerMemberEntity();
        m.setId(UUID.randomUUID());
        m.setEHealthId(eHealthId);
        m.setMemberId(memberId);
        return m;
    }

}
