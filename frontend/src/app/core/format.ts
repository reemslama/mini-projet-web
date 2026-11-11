import { HttpErrorResponse } from '@angular/common/http';

/** Extrait le message de l'enveloppe ErrorResponse, avec repli lisible. */
export function apiError(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    const body = error.error as { message?: string; error?: string } | null;
    if (body?.message) return body.message;
    if (error.status === 0) {
      return 'Passerelle injoignable. Vérifiez que docker compose est démarré.';
    }
    if (error.status === 401) return 'Session expirée. Reconnectez-vous.';
    if (error.status === 403) return "Vous n'avez pas les droits pour cette action.";
    if (error.status === 404) return 'Ressource introuvable.';
    if (error.status === 409) return 'Conflit : cette action est déjà réalisée ou incomplète.';
    return body?.error ?? `Erreur ${error.status}`;
  }
  return error instanceof Error ? error.message : 'Erreur inattendue.';
}

export function formatDeadline(iso: string): string {
  return new Intl.DateTimeFormat('fr-FR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(iso));
}

export function daysLeft(iso: string): number {
  return Math.ceil((new Date(iso).getTime() - Date.now()) / 86_400_000);
}

export function formatMoney(amount: number, currency = 'EUR'): string {
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency,
    maximumFractionDigits: 0
  }).format(amount);
}

export function initials(name: string | undefined): string {
  if (!name) return 'EN';
  return name
    .split(/[\s._-]+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('');
}

export function toIsoLocal(value: string): string {
  return new Date(value).toISOString();
}

export function defaultDeadline(): string {
  const date = new Date(Date.now() + 30 * 86_400_000);
  date.setHours(23, 59, 0, 0);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}
