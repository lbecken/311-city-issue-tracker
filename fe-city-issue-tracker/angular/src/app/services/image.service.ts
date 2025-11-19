import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ImageResponse {
  id: string;
  issueId: string;
  filename: string;
  originalFilename: string;
  contentType: string;
  fileSize: number;
  url: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root',
})
export class ImageService {
  private apiUrl = 'http://localhost:8080/api/v1';

  constructor(private http: HttpClient) {}

  /**
   * Upload an image for a specific issue.
   */
  uploadImage(issueId: string, file: File): Observable<ImageResponse> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<ImageResponse>(`${this.apiUrl}/issues/${issueId}/images`, formData);
  }

  /**
   * Get all images for a specific issue.
   */
  getImagesForIssue(issueId: string): Observable<ImageResponse[]> {
    return this.http.get<ImageResponse[]>(`${this.apiUrl}/issues/${issueId}/images`);
  }

  /**
   * Get a single image's metadata.
   */
  getImage(imageId: string): Observable<ImageResponse> {
    return this.http.get<ImageResponse>(`${this.apiUrl}/images/${imageId}`);
  }

  /**
   * Delete an image.
   */
  deleteImage(imageId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/images/${imageId}`);
  }

  /**
   * Get the full URL for an image file.
   */
  getImageFileUrl(imageId: string): string {
    return `${this.apiUrl}/images/${imageId}/file`;
  }
}
