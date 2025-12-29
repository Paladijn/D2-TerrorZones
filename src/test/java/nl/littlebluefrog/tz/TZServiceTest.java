package nl.littlebluefrog.tz;

import nl.littlebluefrog.tz.model.TerrorZone;
import nl.littlebluefrog.tz.model.Zone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class TZServiceTest {

    private final TZService cut = new TZService();

    @BeforeEach
    void init() {
        // FIXME: we don't want to download the file each time we run a test on an empty data directory.
        cut.tzURL = "https://www.d2emu.com/data/tz-2023-localized.json";
    }

    @Test
    void currentTZ() {
        TerrorZone current =  cut.getCurrent().get();

        assertThat(current.zone().enUS()).isNotBlank();
    }

    @Test
    void verifyFilteredPastTZ() {
        final List<TerrorZone> input = List.of(
                new TerrorZone(ZonedDateTime.of(2021, 11, 23, 0, 0, 0, 0, ZoneId.of("UTC")),
                        new Zone("deleteMe", "", "", "", "", "", "", "", "", "", "", "", ""), List.of(), List.of(), List.of()),
                new TerrorZone(ZonedDateTime.of(2099, 11, 23, 0, 0, 0, 0, ZoneId.of("UTC")),
                        new Zone("IShouldRemain", "", "", "", "", "", "", "", "", "", "", "", ""), List.of(), List.of(), List.of())
        );

        final List<TerrorZone> output = cut.filterOutdatedTZs(input);

        assertThat(output)
                .hasSize(1)
                .extracting("zone.enUS")
                .containsExactly("IShouldRemain");
    }

}