package nl.littlebluefrog.tz;


import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TZResourceTest {

    private final TZResource cut = new TZResource(new TZService());

    @Test
    void calculateTimezoneHour() {
        LocalDateTime now = LocalDateTime.now();
        final ZonedDateTime input = ZonedDateTime.of(now, ZoneId.of("Australia/Lindeman"));
        final String outcome = cut.getTZDisplayHour(input, ZoneId.of("UTC"));

        int hour = now.getHour() - 10;
        assertThat(outcome).isEqualTo("%s:00".formatted(hour));
    }
}