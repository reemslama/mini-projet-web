import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  AcceptanceCriterion,
  Challenge,
  ChallengeCreate,
  ChallengeStatus,
  PageResponse,
  Prize
} from './models';

export interface ChallengeFilters {
  tag?: string;
  status?: ChallengeStatus | '';
  minPrize?: number | null;
  page?: number;
  size?: number;
  sort?: string;
}

@Injectable({ providedIn: 'root' })
export class ChallengesApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.api.ms1}/api/challenges`;

  list(filters: ChallengeFilters = {}): Observable<PageResponse<Challenge>> {
    let params = new HttpParams()
      .set('page', filters.page ?? 0)
      .set('size', filters.size ?? 20)
      .set('sort', filters.sort ?? 'createdAt,desc');
    if (filters.tag) params = params.set('tag', filters.tag);
    if (filters.status) params = params.set('status', filters.status);
    if (filters.minPrize != null) params = params.set('minPrize', filters.minPrize);
    return this.http.get<PageResponse<Challenge>>(this.base, { params });
  }

  get(id: string): Observable<Challenge> {
    return this.http.get<Challenge>(`${this.base}/${id}`);
  }

  create(body: ChallengeCreate): Observable<Challenge> {
    return this.http.post<Challenge>(this.base, body);
  }

  update(id: string, body: ChallengeCreate): Observable<Challenge> {
    return this.http.put<Challenge>(`${this.base}/${id}`, body);
  }

  publish(id: string): Observable<Challenge> {
    return this.http.post<Challenge>(`${this.base}/${id}/publish`, {});
  }

  criteria(id: string): Observable<AcceptanceCriterion[]> {
    return this.http.get<AcceptanceCriterion[]>(`${this.base}/${id}/acceptance-criteria`);
  }

  saveCriteria(id: string, criteria: AcceptanceCriterion[]): Observable<AcceptanceCriterion[]> {
    return this.http.put<AcceptanceCriterion[]>(`${this.base}/${id}/acceptance-criteria`, criteria);
  }

  prizes(id: string): Observable<Prize[]> {
    return this.http.get<Prize[]>(`${this.base}/${id}/prizes`);
  }

  savePrizes(id: string, prizes: Prize[]): Observable<Prize[]> {
    return this.http.put<Prize[]>(`${this.base}/${id}/prizes`, prizes);
  }
}
