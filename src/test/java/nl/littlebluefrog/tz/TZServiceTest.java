package nl.littlebluefrog.tz;

import nl.littlebluefrog.tz.model.TerrorZone;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


class TZServiceTest {


    @Test
    void currentTZ() throws IOException {
        final TZService cut = new TZService();

        TerrorZone current =  cut.getCurrent().get();

        assertThat(current.zone().enUS()).isNotBlank();
    }

}