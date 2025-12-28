package nl.littlebluefrog.tz;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import nl.littlebluefrog.tz.model.TerrorZone;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Path("list")
public class TZResource {
    private static final Logger log = getLogger(TZResource.class);

    private final String outputTemplate;

    TZService tzService;

    public TZResource(TZService tzService) {
        this.tzService = tzService;
        outputTemplate = readFileContents("templates/default.html");
    }

    @GET
    @Path("{amount}")
    @Produces(MediaType.TEXT_HTML)
    public String retrieveHTML(@PathParam("amount") int amount) {
        log.info("amount = {}", amount);
        final List<TerrorZone> terrorZones = getTerrorZones(amount);

        StringBuilder rows = new StringBuilder();
        for(int i = 0; i < terrorZones.size(); i++) {
            rows.append(getRowForTZ(terrorZones.get(i), i == 0));
        }

        return outputTemplate.replace("${tz-data}", rows.toString());
    }

    @GET
    @Path("json/{amount}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TerrorZone> retrieveList(@PathParam("amount") int amount) {
        return getTerrorZones(amount);
    }

    private List<TerrorZone> getTerrorZones(int amount) {
        if (amount < 2) {
            amount = 2;
        }
        return tzService.getCurrentAndUpcoming(amount);
    }

    private String readFileContents(String location) {
        try {
            return Files.readString(java.nio.file.Path.of(location), StandardCharsets.UTF_8);
        } catch (IOException ioe) {
            log.error("Issue reading file {}", location, ioe);
            throw new RuntimeException(ioe);
        }
    }

    private String getRowForTZ(TerrorZone tz, boolean isCurrent) {
        String timeLeft = isCurrent
                ? "Current"
                : getRemainingHoursAndMinutes(tz.dateTime());
        String zoneStyle = isCurrent
                ? "current"
                : "upcoming";
        return "<tr class=\"%s\">".formatted(zoneStyle) +
                "<td>%s</td>".formatted(timeLeft) +
                "<td>%s</td>".formatted(tz.zone().enUS()) +
//                "<td>%s</td>".formatted(String.join(",", tz.immunities())) +
                "</tz>";
    }

    private String getRemainingHoursAndMinutes(ZonedDateTime startTime) {
        Duration duration = Duration.between(ZonedDateTime.now(), startTime);
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        if (duration.toSecondsPart() > 0) {
            minutes++;
        }
        if (duration.toMinutesPart() == 60) {
            hours++;
        }
        if (hours == 1) {
            return "%s hour".formatted(hours);
        } else if (hours > 1) {

            return "%s hours".formatted(hours);
        }
        return "%d minute".formatted(minutes);
    }
}
