import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Badge, RankingEntry, ReputationProfile } from './models';

@Injectable({ providedIn: 'root' })
export class ReputationApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.api.ms6}/api`;

  profile(developerId: string): Observable<ReputationProfile> {
    return this.http.get<ReputationProfile>(`${this.base}/developers/${developerId}/reputation`);
  }

  badges(developerId: string): Observable<Badge[]> {
    return this.http.get<Badge[]>(`${this.base}/developers/${developerId}/badges`);
  }

  rankings(skill?: string, page = 0, size = 20): Observable<RankingEntry[]> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (skill) params = params.set('skill', skill);
    return this.http.get<RankingEntry[]>(`${this.base}/rankings`, { params });
  }
}
