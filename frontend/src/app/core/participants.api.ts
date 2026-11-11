import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, of, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  Application,
  ApplicationCreate,
  Developer,
  DeveloperCreate,
  Team,
  TeamCreate
} from './models';

@Injectable({ providedIn: 'root' })
export class ParticipantsApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.api.ms2}/api`;

  createDeveloper(body: DeveloperCreate): Observable<Developer> {
    return this.http.post<Developer>(`${this.base}/developers`, body);
  }

  /** Extension additive : profil du porteur du JWT (404 si non créé). */
  me(): Observable<Developer | null> {
    return this.http.get<Developer>(`${this.base}/developers/me`).pipe(catchError(() => of(null)));
  }

  getDeveloper(id: string): Observable<Developer> {
    return this.http.get<Developer>(`${this.base}/developers/${id}`);
  }

  /**
   * Récupère le profil courant, le crée s'il n'existe pas.
   * Le 409 (profil déjà créé) est résolu par un second appel à /me.
   */
  ensureDeveloper(body: DeveloperCreate): Observable<Developer> {
    return this.me().pipe(
      switchMap((existing) =>
        existing
          ? of(existing)
          : this.createDeveloper(body).pipe(
              catchError(() =>
                this.me().pipe(
                  switchMap((retried) => {
                    if (retried) return of(retried);
                    throw new Error('Profil développeur introuvable après création.');
                  })
                )
              )
            )
      )
    );
  }

  createTeam(body: TeamCreate): Observable<Team> {
    return this.http.post<Team>(`${this.base}/teams`, body);
  }

  getTeam(id: string): Observable<Team> {
    return this.http.get<Team>(`${this.base}/teams/${id}`);
  }

  apply(challengeId: string, body: ApplicationCreate): Observable<Application> {
    return this.http.post<Application>(`${this.base}/challenges/${challengeId}/applications`, body);
  }

  applications(challengeId: string): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.base}/challenges/${challengeId}/applications`);
  }
}
