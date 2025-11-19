import { Component, Output, EventEmitter, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';

export interface SelectedFile {
  file: File;
  preview: string;
  name: string;
  size: number;
}

@Component({
  selector: 'app-image-upload',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatProgressBarModule],
  template: `
    <div class="image-upload-container">
      <div class="upload-area" (click)="fileInput.click()" (dragover)="onDragOver($event)"
           (drop)="onDrop($event)" [class.dragover]="isDragOver">
        <mat-icon>cloud_upload</mat-icon>
        <p>Click or drag images here</p>
        <p class="hint">Supported: JPEG, PNG, GIF, WebP (max 10MB each)</p>
        <input
          #fileInput
          type="file"
          accept="image/jpeg,image/png,image/gif,image/webp"
          multiple
          (change)="onFileSelected($event)"
          style="display: none"
        />
      </div>

      <div class="preview-container" *ngIf="selectedFiles.length > 0">
        <div class="preview-item" *ngFor="let file of selectedFiles; let i = index">
          <img [src]="file.preview" [alt]="file.name" />
          <div class="file-info">
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">{{ formatFileSize(file.size) }}</span>
          </div>
          <button mat-icon-button color="warn" (click)="removeFile(i)" type="button">
            <mat-icon>delete</mat-icon>
          </button>
        </div>
      </div>

      <div class="error-message" *ngIf="errorMessage">
        {{ errorMessage }}
      </div>
    </div>
  `,
  styles: [
    `
      .image-upload-container {
        width: 100%;
        margin-bottom: 16px;
      }

      .upload-area {
        border: 2px dashed rgba(0, 0, 0, 0.24);
        border-radius: 8px;
        padding: 32px;
        text-align: center;
        cursor: pointer;
        transition: all 0.2s ease;
        background: #fafafa;
      }

      .upload-area:hover,
      .upload-area.dragover {
        border-color: #3f51b5;
        background: #e8eaf6;
      }

      .upload-area mat-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
        color: #666;
        margin-bottom: 8px;
      }

      .upload-area p {
        margin: 0;
        color: #666;
      }

      .upload-area .hint {
        font-size: 12px;
        margin-top: 8px;
        color: #999;
      }

      .preview-container {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
        gap: 12px;
        margin-top: 16px;
      }

      .preview-item {
        position: relative;
        border: 1px solid rgba(0, 0, 0, 0.12);
        border-radius: 4px;
        overflow: hidden;
        background: white;
      }

      .preview-item img {
        width: 100%;
        height: 100px;
        object-fit: cover;
      }

      .file-info {
        padding: 8px;
        font-size: 12px;
      }

      .file-name {
        display: block;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        font-weight: 500;
      }

      .file-size {
        color: #666;
      }

      .preview-item button {
        position: absolute;
        top: 4px;
        right: 4px;
        background: rgba(255, 255, 255, 0.9);
      }

      .error-message {
        color: #f44336;
        font-size: 12px;
        margin-top: 8px;
        padding: 8px;
        background: #ffebee;
        border-radius: 4px;
      }
    `,
  ],
})
export class ImageUploadComponent {
  @Input() maxFiles = 5;
  @Input() maxFileSize = 10 * 1024 * 1024; // 10MB

  @Output() filesChanged = new EventEmitter<File[]>();

  selectedFiles: SelectedFile[] = [];
  isDragOver = false;
  errorMessage = '';

  private allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.addFiles(Array.from(input.files));
    }
    // Reset input so same file can be selected again
    input.value = '';
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = true;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;

    if (event.dataTransfer?.files) {
      this.addFiles(Array.from(event.dataTransfer.files));
    }
  }

  private addFiles(files: File[]): void {
    this.errorMessage = '';

    for (const file of files) {
      // Check max files
      if (this.selectedFiles.length >= this.maxFiles) {
        this.errorMessage = `Maximum ${this.maxFiles} files allowed`;
        break;
      }

      // Validate file type
      if (!this.allowedTypes.includes(file.type)) {
        this.errorMessage = `Invalid file type: ${file.name}. Allowed: JPEG, PNG, GIF, WebP`;
        continue;
      }

      // Validate file size
      if (file.size > this.maxFileSize) {
        this.errorMessage = `File too large: ${file.name}. Maximum size is 10MB`;
        continue;
      }

      // Create preview
      const reader = new FileReader();
      reader.onload = (e: ProgressEvent<FileReader>) => {
        this.selectedFiles.push({
          file,
          preview: e.target?.result as string,
          name: file.name,
          size: file.size,
        });
        this.emitFiles();
      };
      reader.readAsDataURL(file);
    }
  }

  removeFile(index: number): void {
    this.selectedFiles.splice(index, 1);
    this.errorMessage = '';
    this.emitFiles();
  }

  private emitFiles(): void {
    this.filesChanged.emit(this.selectedFiles.map((sf) => sf.file));
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  }

  /**
   * Clear all selected files (called after successful submission)
   */
  clear(): void {
    this.selectedFiles = [];
    this.errorMessage = '';
    this.emitFiles();
  }

  /**
   * Get files for upload
   */
  getFiles(): File[] {
    return this.selectedFiles.map((sf) => sf.file);
  }
}
