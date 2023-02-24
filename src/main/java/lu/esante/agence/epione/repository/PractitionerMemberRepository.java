package lu.esante.agence.epione.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lu.esante.agence.epione.entity.PractitionerMemberEntity;

public interface PractitionerMemberRepository extends JpaRepository<PractitionerMemberEntity, UUID> {

    List<PractitionerMemberEntity> findByeHealthId(String eHealthId);

    Optional<PractitionerMemberEntity> findByeHealthIdAndMemberId(String eHealthId, String memberId);
}
