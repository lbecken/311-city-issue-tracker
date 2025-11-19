package gov.lby.cityissuetracker.service;

import gov.lby.cityissuetracker.dto.GeocodingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final WebClient webClient;

    /**
     * Reverse geocode coordinates to address using OpenEpi.io API.
     * Includes a 1-second delay to respect the rate limit of 1 request per second.
     */
    public Mono<GeocodingResult> reverseGeocode(Double lat, Double lon) {
        log.info("Reverse geocoding coordinates: lat={}, lon={}", lat, lon);

        // Add delay to respect rate limit (max 1 request per second)
        return Mono.delay(Duration.ofMillis(1000))
                .then(webClient.get()
                        .uri("/geocoding/reverse?lat={lat}&lon={lon}", lat, lon)
                        .retrieve()
                        .bodyToMono(GeocodingResult.OpenEpiResponse.class)
                        .map(this::mapToGeocodingResult)
                        .doOnSuccess(result -> log.info("Geocoding result: {}", result.getDisplayName()))
                        .doOnError(error -> log.error("Geocoding failed: {}", error.getMessage())));
    }

    private GeocodingResult mapToGeocodingResult(GeocodingResult.OpenEpiResponse response) {
        if (response == null || response.getFeatures() == null || response.getFeatures().isEmpty()) {
            return GeocodingResult.builder()
                    .displayName("Unknown location")
                    .build();
        }

        GeocodingResult.OpenEpiResponse.Feature feature = response.getFeatures().get(0);
        GeocodingResult.OpenEpiResponse.Feature.Properties props = feature.getProperties();
        List<Double> coords = feature.getGeometry() != null ? feature.getGeometry().getCoordinates() : null;

        // Build display name from components
        StringBuilder displayName = new StringBuilder();
        if (props.getHousenumber() != null) {
            displayName.append(props.getHousenumber()).append(" ");
        }
        if (props.getStreet() != null) {
            displayName.append(props.getStreet());
        }
        if (props.getCity() != null) {
            if (displayName.length() > 0) displayName.append(", ");
            displayName.append(props.getCity());
        }
        if (props.getState() != null) {
            if (displayName.length() > 0) displayName.append(", ");
            displayName.append(props.getState());
        }
        if (props.getCountry() != null) {
            if (displayName.length() > 0) displayName.append(", ");
            displayName.append(props.getCountry());
        }

        return GeocodingResult.builder()
                .lat(coords != null && coords.size() > 1 ? coords.get(1) : null)
                .lon(coords != null && !coords.isEmpty() ? coords.get(0) : null)
                .displayName(displayName.length() > 0 ? displayName.toString() : props.getName())
                .street(props.getStreet())
                .houseNumber(props.getHousenumber())
                .city(props.getCity())
                .state(props.getState())
                .country(props.getCountry())
                .postalCode(props.getPostcode())
                .build();
    }
}
