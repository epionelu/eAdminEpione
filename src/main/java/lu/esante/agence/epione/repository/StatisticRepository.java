package lu.esante.agence.epione.repository;

import lu.esante.agence.epione.model.ExtractData;
import lu.esante.agence.epione.model.Statistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Repository
public interface StatisticRepository extends JpaRepository<Statistic, UUID> {
    @Query(nativeQuery = true)
    List<Statistic> findStatistic();

    @Query(nativeQuery = true)
    List<ExtractData> extractData(@Param("startDate") Timestamp startDate);
}
