package nl.littlebluefrog.tz.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class TerrorZoneTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void readCrystalline() throws IOException {
        final List<TerrorZone> terrorZones = objectMapper.readValue(Paths.get("src/test/resources/crystalline.json").toFile(), new TypeReference<List<TerrorZone>>(){});

        assertThat(terrorZones).hasSize(1);
    }
}
