package lu.esante.agence.epione.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class AbstractMapperTests {

    Mapper mapper = new Mapper();

    @Test
    public void abstractMapperTest() {
        assertEquals("value", mapper.entityToBusiness("value"));
        assertEquals(Optional.empty(), mapper.entityToBusiness(Optional.empty()));
        assertEquals(Optional.of("value"), mapper.entityToBusiness(Optional.of("value")));
        assertEquals(List.of("value"), mapper.entityToBusiness(List.of("value")));
        assertEquals("value", mapper.businessToEntity("value"));
        assertEquals(Optional.empty(), mapper.businessToEntity(Optional.empty()));
        assertEquals(Optional.of("value"), mapper.businessToEntity(Optional.of("value")));
        assertEquals(List.of("value"), mapper.businessToEntity(List.of("value")));
    }
}
