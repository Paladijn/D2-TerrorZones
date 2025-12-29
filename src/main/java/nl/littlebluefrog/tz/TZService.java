package nl.littlebluefrog.tz;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;
import nl.littlebluefrog.tz.model.TerrorZone;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class TZService {
    private static final Logger log = getLogger(TZService.class);

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private List<TerrorZone> terrorZones;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    protected String tzURL = "https://www.d2emu.com/data/tz-2023-localized.json";

    private final Path dataFile = Paths.get("data/tz-cached.json");

    public TZService() {
        try {
            // create the directory in case it doesn't exist
            Files.createDirectories(Paths.get("data"));
        } catch (IOException e) {
            log.error("Unable to create data directory due to {}", e.getMessage(), e);
            System.exit(1);
        }

        readTZsFromLocalFile();

        if (terrorZones.size() <= 24) { // have at least a day of TZ data available
            updateList();
        }
    }

    private void readTZsFromLocalFile() {
        try {
            terrorZones = objectMapper.readValue(Paths.get("data/tz-cached.json").toFile(), new TypeReference<List<TerrorZone>>(){});
            return;
        } catch (FileNotFoundException _) {
            log.warn("Locally cached tz json doesn't exist, it will be downloaded");
        } catch (IOException ioe) {
            log.warn("unable to read terrorzones from local json file", ioe);
        }
        terrorZones = List.of();
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

    private void updateList() {
        log.info("retrieving latest terrorzone data from {}", tzURL);
        HttpRequest request = HttpRequest.newBuilder(URI.create(tzURL)).GET().build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                final List<TerrorZone> downloadedTZs = objectMapper.readValue(response.body(), new TypeReference<List<TerrorZone>>() {});
                log.info("terror zones in original file: {}", downloadedTZs.size());

                final List<TerrorZone> upcomingTZs = filterOutdatedTZs(downloadedTZs);

                updateLocalCache(upcomingTZs);
                terrorZones = upcomingTZs;
            } else {
                log.error("Call to download TZ json returned response code {} on URL {}", response.statusCode(), tzURL);
            }
        } catch (IOException e) {
            log.error("Issue downloading, or reading the latest tz json", e);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    private void updateLocalCache(List<TerrorZone> upcomingTZs) {
        try {
            Files.writeString(dataFile, objectMapper.writeValueAsString(upcomingTZs), StandardCharsets.UTF_8);
        } catch (IOException ioe) {
            log.error("There was an issue updating the locally cached tz.json", ioe);
        }
    }

    protected List<TerrorZone> filterOutdatedTZs(List<TerrorZone> downloadedTZs) {
        final ZonedDateTime currentHour = ZonedDateTime.now().minusHours(1);
        final List<TerrorZone> upcomingTZs = downloadedTZs.stream()
                .dropWhile(tz -> tz.dateTime().isBefore(currentHour))
                .toList();
        log.info("filtered outdated zones, amount left: {}, latest: {}", upcomingTZs.size(), upcomingTZs.getLast().dateTime());
        return upcomingTZs;
    }
}
