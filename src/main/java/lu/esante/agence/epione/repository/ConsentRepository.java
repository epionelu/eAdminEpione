package lu.esante.agence.epione.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lu.esante.agence.epione.entity.ConsentEntity;
import lu.esante.agence.epione.entity.ConsentTypeEntity;

public interface ConsentRepository extends JpaRepository<ConsentEntity, UUID> {
    List<ConsentEntity> findBySsn(String ssn);

    Optional<ConsentEntity> findBySsnAndConsentType(String ssn, ConsentTypeEntity consentType);
}
