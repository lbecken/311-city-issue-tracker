import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { IssueService, CreateIssueRequest, Issue } from '../../services/issue.service';
import { ImageService } from '../../services/image.service';
import { LocationPickerComponent } from '../location-picker/location-picker.component';
import { ImageUploadComponent } from '../image-upload/image-upload.component';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-issue-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    LocationPickerComponent,
    ImageUploadComponent,
  ],
  templateUrl: './issue-form.component.html',
  styleUrls: ['./issue-form.component.scss'],
})
export class IssueFormComponent {
  @ViewChild(ImageUploadComponent) imageUpload!: ImageUploadComponent;

  // Define form group with validation rules
  issueForm: FormGroup;

  // State
  isSubmitting = false;
  selectedImages: File[] = [];

  // Categories for the dropdown
  categories = [
    { value: 'POTHOLE', label: 'Pothole' },
    { value: 'STREETLIGHT', label: 'Street Light' },
    { value: 'GRAFFITI', label: 'Graffiti' },
    { value: 'TRASH', label: 'Trash/Illegal Dumping' },
    { value: 'NOISE', label: 'Noise Complaint' },
    { value: 'OTHER', label: 'Other' },
  ];

  constructor(
    private fb: FormBuilder,
    private issueService: IssueService,
    private imageService: ImageService
  ) {
    // Initialize the form with fields and validators
    this.issueForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', Validators.maxLength(2000)],
      category: ['', Validators.required],
      latitude: ['', [Validators.required, Validators.min(-90), Validators.max(90)]],
      longitude: ['', [Validators.required, Validators.min(-180), Validators.max(180)]],
      address: [''],
      reportedBy: ['', Validators.required],
      reporterEmail: ['', [Validators.required, Validators.email]],
    });
  }

  // Convenience getter for form fields (makes template cleaner)
  get f() {
    return this.issueForm.controls;
  }

  // Handle location selection from map
  onLocationSelected(location: { lat: number; lng: number }): void {
    this.issueForm.patchValue({
      latitude: location.lat,
      longitude: location.lng,
    });
  }

  // Handle address resolution from geocoding
  onAddressResolved(address: string): void {
    this.issueForm.patchValue({
      address: address,
    });
  }

  // Handle image file selection
  onImagesChanged(files: File[]): void {
    this.selectedImages = files;
  }

  // Handle form submission
  onSubmit(): void {
    // Mark all fields as touched to show validation errors
    if (this.issueForm.invalid) {
      this.issueForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;

    // Convert form value to CreateIssueRequest
    const request: CreateIssueRequest = this.issueForm.value;

    // Call the service to create issue
    this.issueService.createIssue(request).subscribe({
      next: (issue: Issue) => {
        console.log('Issue created:', issue);

        // If there are images, upload them
        if (this.selectedImages.length > 0) {
          this.uploadImages(issue.id);
        } else {
          this.onSubmitSuccess(issue.id);
        }
      },
      error: (error) => {
        console.error('Error creating issue:', error);
        this.isSubmitting = false;
        alert('Failed to create issue. Please try again.');
      },
    });
  }

  private uploadImages(issueId: string): void {
    const uploads = this.selectedImages.map((file) =>
      this.imageService.uploadImage(issueId, file)
    );

    forkJoin(uploads).subscribe({
      next: (results) => {
        console.log('Images uploaded:', results);
        this.onSubmitSuccess(issueId);
      },
      error: (error) => {
        console.error('Error uploading images:', error);
        this.isSubmitting = false;
        // Issue was created but images failed
        alert(
          `Issue reported successfully (ID: ${issueId}), but some images failed to upload. Please try uploading them again.`
        );
        this.resetForm();
      },
    });
  }

  private onSubmitSuccess(issueId: string): void {
    this.isSubmitting = false;
    alert(`Issue reported successfully! ID: ${issueId}`);
    this.resetForm();
  }

  private resetForm(): void {
    this.issueForm.reset();
    this.selectedImages = [];
    if (this.imageUpload) {
      this.imageUpload.clear();
    }
  }

  // Reset form button
  onReset(): void {
    this.resetForm();
  }
}
