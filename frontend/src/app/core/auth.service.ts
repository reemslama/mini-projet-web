import { computed, inject, Injectable, signal } from '@angular/core';
import Keycloak, { KeycloakProfile } from 'keycloak-js';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly keycloak = new Keycloak({
    url: environment.keycloak.url,
    realm: environment.keycloak.realm,
    clientId: environment.keycloak.clientId
  });

  readonly ready = signal(false);
  readonly authenticated = signal(false);
  readonly profile = signal<KeycloakProfile | null>(null);
  readonly roles = signal<string[]>([]);
  readonly initError = signal<string | null>(null);

  readonly displayName = computed(() => {
    const profile = this.profile();
    const fullName = [profile?.firstName, profile?.lastName].filter(Boolean).join(' ');
    return fullName || profile?.username || 'Invité';
  });

  readonly keycloakId = computed(() => this.profile()?.id ?? this.tokenSub());
  readonly username = computed(() => this.profile()?.username ?? 'invite');
  readonly email = computed(() => this.profile()?.email ?? undefined);

  async init(): Promise<void> {
    try {
      const authenticated = await this.keycloak.init({
        onLoad: 'check-sso',
        pkceMethod: 'S256',
        checkLoginIframe: false,
        silentCheckSsoFallback: false
      });
      this.authenticated.set(authenticated);
      if (authenticated) {
        await this.loadProfile();
      }
    } catch (error) {
      console.error('Keycloak init failed', error);
      this.initError.set(
        'Keycloak injoignable sur ' + environment.keycloak.url + '. Démarrez l’infrastructure (docker compose).'
      );
    } finally {
      this.ready.set(true);
    }
  }

  private async loadProfile(): Promise<void> {
    this.roles.set(this.keycloak.realmAccess?.roles ?? []);
    try {
      this.profile.set(await this.keycloak.loadUserProfile());
    } catch {
      this.profile.set(null);
    }
  }

  private tokenSub(): string | undefined {
    const parsed = this.keycloak.tokenParsed as { sub?: string } | undefined;
    return parsed?.sub;
  }

  hasRole(...roles: string[]): boolean {
    return roles.some((role) => this.roles().includes(role));
  }

  async token(): Promise<string | undefined> {
    if (!this.authenticated()) return undefined;
    try {
      await this.keycloak.updateToken(30);
      return this.keycloak.token;
    } catch {
      this.authenticated.set(false);
      return undefined;
    }
  }

  login(redirectPath = window.location.pathname + window.location.search): void {
    void this.keycloak.login({
      redirectUri: window.location.origin + redirectPath
    });
  }

  logout(): void {
    void this.keycloak.logout({ redirectUri: window.location.origin });
  }
}
