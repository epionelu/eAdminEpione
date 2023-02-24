package lu.esante.agence.epione.repository;

import lu.esante.agence.epione.model.HistoStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.UUID;

@Repository
public interface HistoStatRepository extends JpaRepository<HistoStat, UUID> {

    @Query(value = "select max(created_at) from histo_stat where stat_type=?", nativeQuery = true)
    Timestamp getMaxCreatedAt(String statType);
}
