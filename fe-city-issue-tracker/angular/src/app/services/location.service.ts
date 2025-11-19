import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GeocodingResult {
  lat: number;
  lon: number;
  displayName: string;
  street?: string;
  houseNumber?: string;
  city?: string;
  state?: string;
  country?: string;
  postalCode?: string;
}

@Injectable({
  providedIn: 'root',
})
export class LocationService {
  private apiUrl = 'http://localhost:8080/api/v1/location';

  constructor(private http: HttpClient) {}

  /**
   * Reverse geocode coordinates to get address information.
   * Results are cached on the backend for 24 hours.
   */
  reverseGeocode(lat: number, lon: number): Observable<GeocodingResult> {
    return this.http.get<GeocodingResult>(`${this.apiUrl}/reverse`, {
      params: {
        lat: lat.toString(),
        lon: lon.toString(),
      },
    });
  }
}
