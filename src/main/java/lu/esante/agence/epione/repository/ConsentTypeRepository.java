package lu.esante.agence.epione.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lu.esante.agence.epione.entity.ConsentTypeEntity;

public interface ConsentTypeRepository extends JpaRepository<ConsentTypeEntity, UUID> {

    Optional<ConsentTypeEntity> findByCode(String code);

}
