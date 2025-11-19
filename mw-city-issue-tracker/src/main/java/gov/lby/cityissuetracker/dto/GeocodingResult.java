package gov.lby.cityissuetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeocodingResult {
    private Double lat;
    private Double lon;
    private String displayName;
    private String street;
    private String houseNumber;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    // For OpenEpi.io reverse geocoding response
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpenEpiResponse {
        private List<Feature> features;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Feature {
            private Properties properties;
            private Geometry geometry;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Properties {
                private String name;
                private String street;
                private String housenumber;
                private String city;
                private String state;
                private String country;
                private String postcode;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Geometry {
                private List<Double> coordinates;
            }
        }
    }
}
