import { Component, inject } from '@angular/core';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-toasts',
  template: `
    <div class="toasts" role="status" aria-live="polite">
      @for (toast of toast.toasts(); track toast.id) {
        <div class="toast" [attr.data-kind]="toast.kind">
          <span>{{ toast.message }}</span>
          <button type="button" (click)="toast.dismiss(toast.id)" aria-label="Fermer">✕</button>
        </div>
      }
    </div>
  `
})
export class ToastsComponent {
  protected readonly toast = inject(ToastService);
}
