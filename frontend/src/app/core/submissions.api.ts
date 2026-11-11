import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Submission, SubmissionCreate, SubmissionStatus, Verdict } from './models';

@Injectable({ providedIn: 'root' })
export class SubmissionsApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.api.ms3}/api/submissions`;

  create(body: SubmissionCreate): Observable<Submission> {
    return this.http.post<Submission>(this.base, body);
  }

  list(challengeId: string, status?: SubmissionStatus): Observable<Submission[]> {
    let params = new HttpParams().set('challengeId', challengeId);
    if (status) params = params.set('status', status);
    return this.http.get<Submission[]>(this.base, { params });
  }

  get(id: string): Observable<Submission> {
    return this.http.get<Submission>(`${this.base}/${id}`);
  }

  submit(id: string): Observable<Submission> {
    return this.http.post<Submission>(`${this.base}/${id}/submit`, {});
  }

  verdict(id: string): Observable<Verdict> {
    return this.http.get<Verdict>(`${this.base}/${id}/verdict`);
  }
}
