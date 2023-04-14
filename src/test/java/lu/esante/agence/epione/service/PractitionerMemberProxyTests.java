package lu.esante.agence.epione.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lu.esante.agence.epione.model.PractitionerMember;
import lu.esante.agence.epione.service.impl.member.PractitionerMemberAuditProxy;

@ExtendWith(SpringExtension.class)
public class PractitionerMemberProxyTests {

    PractitionerMemberAuditProxy proxy;

    @MockBean
    private IPractitionerMemberService service;

    @MockBean
    private ITraceService trace;

    @BeforeEach
    public void init() {
        proxy = new PractitionerMemberAuditProxy(service, trace);
    }

    @Test
    public void getByIdTest() {
        PractitionerMember member = buildMember();

        when(service.getById(any())).thenReturn(Optional.empty());
        when(service.getById(member.getId())).thenReturn(Optional.of(member));
        Optional<PractitionerMember> response = proxy.getById(member.getId());
        assertEquals(false, response.isEmpty());
        assertEquals(member, response.get());

        response = proxy.getById(UUID.randomUUID());
        assertEquals(true, response.isEmpty());
    }

    @Test
    public void getByEHealthIdTest() {
        PractitionerMember member = buildMember();
        when(service.getByEHealthId(any())).thenReturn(List.of());
        when(service.getByEHealthId(member.getEHealthId()))
                .thenReturn(List.of(member));
        List<PractitionerMember> response = proxy.getByEHealthId(member.getEHealthId());
        assertEquals(1, response.size());
        assertEquals(member, response.get(0));

        response = proxy.getByEHealthId("1234");
        assertEquals(true, response.isEmpty());
    }

    @Test
    public void saveTest() {
        PractitionerMember member = buildMember();
        when(service.save(member)).thenReturn(member);
        PractitionerMember response = proxy.save(member);
        assertEquals(member, response);

    }

    @Test
    public void deleteTest() {
        PractitionerMember member = buildMember();
        assertDoesNotThrow(() -> proxy.delete(member));
    }

    private PractitionerMember buildMember() {
        PractitionerMember member = new PractitionerMember();
        member.setId(UUID.randomUUID());
        member.setEHealthId("eHealthId");
        member.setMemberId("memberId");
        return member;
    }

}
