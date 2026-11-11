import { Injectable, signal } from '@angular/core';
import { apiError } from './format';

export type ToastKind = 'info' | 'success' | 'error';

export interface Toast {
  id: string;
  message: string;
  kind: ToastKind;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);

  show(message: string, kind: ToastKind = 'info', ttl = 5000): void {
    const id = crypto.randomUUID();
    this.toasts.update((toasts) => [...toasts, { id, message, kind }]);
    setTimeout(() => this.dismiss(id), ttl);
  }

  success(message: string): void {
    this.show(message, 'success');
  }

  error(error: unknown): void {
    this.show(apiError(error), 'error', 7000);
  }

  dismiss(id: string): void {
    this.toasts.update((toasts) => toasts.filter((toast) => toast.id !== id));
  }
}
