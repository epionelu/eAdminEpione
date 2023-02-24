package lu.esante.agence.epione.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lu.esante.agence.epione.entity.DocumentTypeEntity;

public interface DocumentTypeRepository extends JpaRepository<DocumentTypeEntity, UUID> {

    Optional<DocumentTypeEntity> findByCode(String code);
}
