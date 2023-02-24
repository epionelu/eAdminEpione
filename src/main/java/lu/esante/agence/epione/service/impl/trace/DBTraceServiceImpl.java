package lu.esante.agence.epione.service.impl.trace;

import lu.esante.agence.epione.entity.TraceEntity;
import lu.esante.agence.epione.repository.TraceRepository;
import lu.esante.agence.epione.service.IIdentityService;
import lu.esante.agence.epione.service.ITraceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class DBTraceServiceImpl implements ITraceService {

    TraceRepository repo;
    IIdentityService identity;

    @Autowired
    public DBTraceServiceImpl(TraceRepository repo, IIdentityService identity) {
        this.repo = repo;
        this.identity = identity;
    }

    public void write(String entityType, String action) {
        write(entityType, action, null);

    }

    public void write(String entityType, String action, UUID referenceId) {
        TraceEntity trace = new TraceEntity();
        trace.setEntityType(entityType);
        trace.setCreatedAt(OffsetDateTime.now());
        trace.setAction(action);
        trace.setReferenceId(referenceId);

        trace.setAuthor(identity.getName());
        trace.setAuthorRoles(identity.getRoles().toString());
        repo.save(trace);
    }


    public void write(String entityType, String action, UUID referenceId, String author, String authorRole) {
        TraceEntity trace = new TraceEntity();
        trace.setEntityType(entityType);
        trace.setCreatedAt(OffsetDateTime.now());
        trace.setAction(action);
        trace.setReferenceId(referenceId);

        trace.setAuthor(author);
        trace.setAuthorRoles(authorRole);
        repo.save(trace);
    }
}
