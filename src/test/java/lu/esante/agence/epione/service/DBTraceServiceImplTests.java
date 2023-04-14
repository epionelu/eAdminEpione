package lu.esante.agence.epione.service;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lu.esante.agence.epione.WithEpioneUser;
import lu.esante.agence.epione.service.impl.trace.DBTraceServiceImpl;

@SpringBootTest
public class DBTraceServiceImplTests {

    @Autowired
    DBTraceServiceImpl service;

    @Test
    @WithEpioneUser
    public void writeTest() {
        Assertions.assertDoesNotThrow(() -> service.write("TEST", "TEST"));
        Assertions.assertDoesNotThrow(() -> service.write("TEST", "TEST", UUID.randomUUID()));
    }
}
