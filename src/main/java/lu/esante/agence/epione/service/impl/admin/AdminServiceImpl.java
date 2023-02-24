package lu.esante.agence.epione.service.impl.admin;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.extern.slf4j.Slf4j;
import lu.esante.agence.epione.config.EpioneSettings;
import lu.esante.agence.epione.model.ExtractData;
import lu.esante.agence.epione.model.HistoStat;
import lu.esante.agence.epione.model.Statistic;
import lu.esante.agence.epione.repository.HistoStatRepository;
import lu.esante.agence.epione.repository.StatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AdminServiceImpl {
    @Autowired
    private StatisticRepository statisticRepository;

    @Autowired
    private HistoStatRepository histoStatRepository;

    @Autowired
    private EpioneSettings epioneSettings;

    @Scheduled(cron = "${cns.stat.generate.delay}")
    public void exportCnsStat() {
        log.info("Starting CNS stat export");
        List<Statistic> stats = statisticRepository.findStatistic();
        if (stats == null) {
            log.info("Statistics - No data");
        }
        try {
            OffsetDateTime offsetdatetime = OffsetDateTime.now();
            UUID uuid = UUID.randomUUID();
            if (stats.size() > 0) {
                Writer writer = Files.newBufferedWriter(
                        Paths.get(epioneSettings.getCnsFileDirectory() + "/" + uuid.toString() + ".csv"));

                ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
                mappingStrategy.setType(lu.esante.agence.epione.model.Statistic.class);
                String[] columns = new String[]{"createdAtYear", "createdAtMonth", "sentAtYear", "sentAtMonth",
                        "practitionerId", "total", "sent", "received", "cancelled"};
                mappingStrategy.setColumnMapping(columns);
                StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer)
                        .withMappingStrategy(mappingStrategy)
                        .withSeparator(';')
                        .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                        .build();

                beanToCsv.write(stats);
                writer.close();
            }
            OffsetDateTime endOffsetdatetime = OffsetDateTime.now();
            HistoStat histoStat = new HistoStat();
            histoStat.setStatFile(uuid.toString());
            histoStat.setCreatedat(offsetdatetime);
            histoStat.setEndat(endOffsetdatetime);
            histoStat.setStatType("STAT");
            histoStat.setExportCount(stats.size());
            histoStatRepository.save(histoStat);
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            log.error("", e);
        }
    }

    @Scheduled(cron = "${cns.extract.generate.delay}")
    public void exportCnsData() {
        log.info("Starting CNS data export");
        try {
            Timestamp lastTimestamp = histoStatRepository.getMaxCreatedAt("EXPORT");
            if (lastTimestamp == null) {
                lastTimestamp = Timestamp.valueOf("2022-01-01 00:00:00");
            }
            List<ExtractData> extractedData = statisticRepository.extractData(lastTimestamp);
            OffsetDateTime offsetdatetime = OffsetDateTime.now();
            UUID uuid = UUID.randomUUID();
            if (extractedData == null) {
                log.info("Statistics - No data");
            }
            if (extractedData.size() > 0) {
                Writer writer;
                writer = Files.newBufferedWriter(
                        Paths.get(epioneSettings.getCnsFileDirectory() + "/" + uuid.toString() + ".csv"));


                ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
                mappingStrategy.setType(ExtractData.class);
                String[] columns = new String[]{"documentId", "ehealthid", "gpCodeInvoice", "patientSSN", "documentType",
                        "documentLogicalId", "sent", "status"};
                mappingStrategy.generateHeader(columns);

                mappingStrategy.setColumnMapping(columns);

                StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer)
                        .withMappingStrategy(mappingStrategy)
                        .withSeparator(';')
                        .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                        .build();

                beanToCsv.write(extractedData);
                writer.close();
            }
            OffsetDateTime endOffsetdatetime = OffsetDateTime.now();
            HistoStat histoStat = new HistoStat();
            histoStat.setStatFile(uuid.toString());
            histoStat.setCreatedat(offsetdatetime);
            histoStat.setEndat(endOffsetdatetime);
            histoStat.setStatType("EXPORT");
            histoStat.setExportCount(extractedData.size());
            histoStatRepository.save(histoStat);
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            log.error("", e);
        }

    }
}
