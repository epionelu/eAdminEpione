package lu.esante.agence.epione.service.impl.mpi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import lu.esante.agence.epione.client.mpi.MpiClient;
import lu.esante.agence.epione.client.mpi.exception.MpiCriticalException;
import lu.esante.agence.epione.client.mpi.exception.MpiErrorException;
import lu.esante.agence.epione.service.IMpiService;

@Service
@Slf4j
public class MpiServiceImpl implements IMpiService {

    @Autowired
    MpiClient client;

    @Override
    @Cacheable("patients")
    public String getSsn(String key) {
        try {
            return client.getSsn(key);
        } catch (MpiCriticalException | MpiErrorException e) {
            log.error("Impossible to retrieve the user from MPI");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean isPatient(String key) {
        return client.isPatient(key);
    }

    @CacheEvict(allEntries = true, cacheNames = { "patients" })
    @Scheduled(fixedDelay = 3600000)
    public void cacheEvict() {
        // Nothing to do
    }

}
