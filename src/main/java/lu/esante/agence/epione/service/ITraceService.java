package lu.esante.agence.epione.service;

import java.util.UUID;

public interface ITraceService {
    void write(String entityType, String action);

    void write(String entityType, String action, UUID referenceId);

    void write(String entityType, String action, UUID referenceId, String author, String authorRole);
}
