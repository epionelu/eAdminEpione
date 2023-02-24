package lu.esante.agence.epione.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lu.esante.agence.epione.entity.DocumentStatusEntity;

public interface DocumentStatusRepository extends JpaRepository<DocumentStatusEntity, UUID> {

    Optional<DocumentStatusEntity> findByCode(String code);
}
