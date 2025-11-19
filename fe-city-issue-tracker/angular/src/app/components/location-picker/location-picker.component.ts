import {
  Component,
  OnInit,
  OnDestroy,
  Output,
  EventEmitter,
  Input,
  AfterViewInit,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import * as L from 'leaflet';
import { LocationService, GeocodingResult } from '../../services/location.service';
import { Subject } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';

// Fix for default marker icons in Leaflet with webpack
const iconRetinaUrl = 'assets/marker-icon-2x.png';
const iconUrl = 'assets/marker-icon.png';
const shadowUrl = 'assets/marker-shadow.png';

@Component({
  selector: 'app-location-picker',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule],
  template: `
    <div class="location-picker-container">
      <div id="map" class="map-container"></div>
      <div class="location-info" *ngIf="selectedLocation">
        <div class="coordinates">
          <span><strong>Lat:</strong> {{ selectedLocation.lat.toFixed(6) }}</span>
          <span><strong>Lng:</strong> {{ selectedLocation.lng.toFixed(6) }}</span>
        </div>
        <div class="address" *ngIf="address">
          <strong>Address:</strong> {{ address }}
        </div>
        <div class="loading" *ngIf="isLoading">
          <mat-spinner diameter="16"></mat-spinner>
          <span>Looking up address...</span>
        </div>
      </div>
      <div class="instructions" *ngIf="!selectedLocation">
        Click on the map to select issue location
      </div>
    </div>
  `,
  styles: [
    `
      .location-picker-container {
        width: 100%;
        margin-bottom: 16px;
      }

      .map-container {
        height: 300px;
        width: 100%;
        border-radius: 4px;
        border: 1px solid rgba(0, 0, 0, 0.12);
      }

      .location-info {
        margin-top: 8px;
        padding: 8px 12px;
        background: #f5f5f5;
        border-radius: 4px;
        font-size: 14px;
      }

      .coordinates {
        display: flex;
        gap: 16px;
        margin-bottom: 4px;
      }

      .address {
        color: #666;
      }

      .loading {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #666;
        margin-top: 4px;
      }

      .instructions {
        text-align: center;
        color: #666;
        font-size: 14px;
        margin-top: 8px;
        font-style: italic;
      }
    `,
  ],
})
export class LocationPickerComponent implements OnInit, AfterViewInit, OnDestroy {
  @Input() initialLat = 40.7128; // Default to NYC
  @Input() initialLng = -74.006;
  @Input() initialZoom = 13;

  @Output() locationSelected = new EventEmitter<{ lat: number; lng: number }>();
  @Output() addressResolved = new EventEmitter<string>();

  private map!: L.Map;
  private marker?: L.Marker;
  private clickSubject = new Subject<{ lat: number; lng: number }>();
  private destroy$ = new Subject<void>();

  selectedLocation?: { lat: number; lng: number };
  address?: string;
  isLoading = false;

  constructor(private locationService: LocationService) {}

  ngOnInit(): void {
    // Debounce clicks to respect API rate limit (1 req/sec)
    this.clickSubject
      .pipe(debounceTime(1000), takeUntil(this.destroy$))
      .subscribe((location) => {
        this.reverseGeocode(location.lat, location.lng);
      });
  }

  ngAfterViewInit(): void {
    this.initMap();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.map) {
      this.map.remove();
    }
  }

  private initMap(): void {
    // Initialize map
    this.map = L.map('map').setView([this.initialLat, this.initialLng], this.initialZoom);

    // Add OpenStreetMap tile layer
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 19,
    }).addTo(this.map);

    // Fix marker icon path issue
    const defaultIcon = L.icon({
      iconRetinaUrl,
      iconUrl,
      shadowUrl,
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41],
    });
    L.Marker.prototype.options.icon = defaultIcon;

    // Handle map clicks
    this.map.on('click', (e: L.LeafletMouseEvent) => {
      this.onMapClick(e.latlng);
    });
  }

  private onMapClick(latlng: L.LatLng): void {
    const location = { lat: latlng.lat, lng: latlng.lng };

    // Update or create marker
    if (this.marker) {
      this.marker.setLatLng(latlng);
    } else {
      this.marker = L.marker(latlng).addTo(this.map);
    }

    // Update selected location
    this.selectedLocation = location;
    this.address = undefined;

    // Emit location immediately
    this.locationSelected.emit(location);

    // Queue geocoding request (debounced)
    this.clickSubject.next(location);
  }

  private reverseGeocode(lat: number, lng: number): void {
    this.isLoading = true;

    this.locationService.reverseGeocode(lat, lng).subscribe({
      next: (result: GeocodingResult) => {
        this.address = result.displayName;
        this.isLoading = false;

        // Emit the resolved address
        if (result.displayName) {
          this.addressResolved.emit(result.displayName);
        }

        // Update marker popup
        if (this.marker && result.displayName) {
          this.marker.bindPopup(result.displayName).openPopup();
        }
      },
      error: (error) => {
        console.error('Geocoding error:', error);
        this.isLoading = false;
        this.address = 'Unable to resolve address';
      },
    });
  }

  /**
   * Set location programmatically (e.g., from saved data)
   */
  setLocation(lat: number, lng: number): void {
    const latlng = L.latLng(lat, lng);

    if (this.map) {
      this.map.setView(latlng, this.initialZoom);
    }

    this.onMapClick(latlng);
  }
}
