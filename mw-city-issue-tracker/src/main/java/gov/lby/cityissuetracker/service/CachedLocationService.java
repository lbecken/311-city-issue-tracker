package gov.lby.cityissuetracker.service;

import gov.lby.cityissuetracker.dto.GeocodingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachedLocationService {

    private final LocationService locationService;

    /**
     * Reverse geocode with caching.
     * Results are cached for 24 hours using coordinates as the key.
     * This significantly reduces API calls to OpenEpi.io for repeated lookups.
     */
    @Cacheable(value = "geocoding", key = "#lat + ',' + #lon")
    public GeocodingResult reverseGeocode(Double lat, Double lon) {
        log.info("Cache miss for coordinates: lat={}, lon={} - calling API", lat, lon);

        // Block on the Mono since we need synchronous result for caching
        // The underlying service still has rate limiting
        return locationService.reverseGeocode(lat, lon)
                .block();
    }

    /**
     * Async version for non-cached scenarios.
     * Use this when you need reactive streams but don't need caching.
     */
    public Mono<GeocodingResult> reverseGeocodeAsync(Double lat, Double lon) {
        return locationService.reverseGeocode(lat, lon);
    }
}
