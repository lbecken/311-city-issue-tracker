package gov.lby.cityissuetracker.controller;

import gov.lby.cityissuetracker.dto.GeocodingResult;
import gov.lby.cityissuetracker.service.CachedLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/location")
@RequiredArgsConstructor
@Tag(name = "Location", description = "Geocoding and location services")
public class LocationController {

    private final CachedLocationService cachedLocationService;

    @GetMapping("/reverse")
    @Operation(
            summary = "Reverse geocode coordinates",
            description = "Convert latitude/longitude coordinates to a human-readable address using OpenEpi.io. Results are cached for 24 hours."
    )
    @ApiResponse(responseCode = "200", description = "Successfully geocoded coordinates")
    @ApiResponse(responseCode = "400", description = "Invalid coordinates")
    public ResponseEntity<GeocodingResult> reverseGeocode(
            @Parameter(description = "Latitude coordinate", example = "40.7128")
            @RequestParam Double lat,
            @Parameter(description = "Longitude coordinate", example = "-74.0060")
            @RequestParam Double lon) {

        // Validate coordinates
        if (lat < -90 || lat > 90) {
            return ResponseEntity.badRequest().build();
        }
        if (lon < -180 || lon > 180) {
            return ResponseEntity.badRequest().build();
        }

        GeocodingResult result = cachedLocationService.reverseGeocode(lat, lon);
        return ResponseEntity.ok(result);
    }
}
