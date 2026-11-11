import { Component, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterOutlet, RouterLinkActive } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { initials } from '../core/format';
import { ToastService } from '../core/toast.service';

interface NavItem {
  label: string;
  path: string;
  roles?: string[];
}

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.html',
  styleUrl: './shell.css'
})
export class Shell {
  protected readonly auth = inject(AuthService);
  protected readonly toast = inject(ToastService);
  protected readonly router = inject(Router);
  protected readonly menuOpen = signal(false);

  protected readonly nav: NavItem[] = [
    { label: 'Défis', path: '/' },
    { label: 'Classements', path: '/rankings' },
    { label: 'Mon espace', path: '/dashboard', roles: ['DEVELOPER'] },
    { label: 'Publier un défi', path: '/enterprise/new', roles: ['ENTERPRISE', 'ADMIN'] },
    { label: 'Jury', path: '/evaluate', roles: ['EXPERT', 'ADMIN'] },
    { label: 'Réputation', path: '/reputation', roles: ['DEVELOPER'] }
  ];

  protected readonly primaryRole = computed<string>(() => {
    const roles = this.auth.roles();
    const order = ['ADMIN', 'ENTERPRISE', 'EXPERT', 'DEVELOPER'];
    for (const role of order) {
      if (roles.includes(role)) {
        return role;
      }
    }
    return 'MEMBRE';
  });

  protected readonly isAuthenticated = this.auth.authenticated;

  onLogout(): void {
    this.toast.info('Déconnexion…');
    this.auth.logout();
  }
}
