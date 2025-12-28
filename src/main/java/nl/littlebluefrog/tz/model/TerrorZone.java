package nl.littlebluefrog.tz.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.List;

public record TerrorZone(
        @JsonProperty("datetime")
        ZonedDateTime dateTime,
        Zone zone,
        List<String> immunities,
        List<Integer> numBossPacks,
        @JsonProperty("superuniques")
        List<String> superUniques
        ) {
}
