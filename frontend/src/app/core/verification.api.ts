import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { SimilarityReport, Verification, VerificationRequest } from './models';

@Injectable({ providedIn: 'root' })
export class VerificationApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.api.ms5}/api`;

  start(body: VerificationRequest): Observable<Verification> {
    return this.http.post<Verification>(`${this.base}/verifications`, body);
  }

  get(id: string): Observable<Verification> {
    return this.http.get<Verification>(`${this.base}/verifications/${id}`);
  }

  logs(id: string): Observable<string> {
    return this.http.get(`${this.base}/verifications/${id}/logs`, { responseType: 'text' });
  }

  analyze(submissionId: string, challengeId: string): Observable<SimilarityReport> {
    return this.http.post<SimilarityReport>(`${this.base}/similarity/analyze`, {
      submissionId,
      challengeId
    });
  }

  similarity(submissionId: string): Observable<SimilarityReport> {
    return this.http.get<SimilarityReport>(`${this.base}/similarity/${submissionId}`);
  }

  summarize(submissionId: string): Observable<{
    submissionId: string;
    summary: string;
    model: string;
    generatedAt: string;
  }> {
    return this.http.post<{ submissionId: string; summary: string; model: string; generatedAt: string }>(
      `${this.base}/ai/summarize/${submissionId}`,
      {}
    );
  }
}
