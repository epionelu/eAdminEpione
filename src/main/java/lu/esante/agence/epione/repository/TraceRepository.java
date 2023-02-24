package lu.esante.agence.epione.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lu.esante.agence.epione.entity.TraceEntity;

public interface TraceRepository extends JpaRepository<TraceEntity, UUID> {

}
