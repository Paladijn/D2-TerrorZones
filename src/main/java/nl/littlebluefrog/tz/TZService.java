package nl.littlebluefrog.tz;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;
import nl.littlebluefrog.tz.model.TerrorZone;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TZService {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final List<TerrorZone> terrorZones;

    public TZService() throws IOException {
        terrorZones = objectMapper.readValue(Paths.get("src/main/resources/tz-2023-localized.json").toFile(), new TypeReference<List<TerrorZone>>(){});
    }

    public Optional<TerrorZone> getCurrent() {
        final ZonedDateTime currentHour = ZonedDateTime.now().minusHours(1);
        return terrorZones.stream()
                .dropWhile(tz -> tz.dateTime().isBefore(currentHour))
                .findFirst();
    }

    public List<TerrorZone> getCurrentAndUpcoming(int amount) {
        final ZonedDateTime currentHour = ZonedDateTime.now().minusHours(1);
        return terrorZones.stream()
                .dropWhile(tz -> tz.dateTime().isBefore(currentHour))
                .limit(amount)
                .toList();
    }
}
