import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, of } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  Evaluation,
  EvaluationCreate,
  Expert,
  ExpertCreate,
  Jury,
  LeaderboardEntry,
  Rubric,
  RubricCreate,
  Winner
} from './models';

@Injectable({ providedIn: 'root' })
export class EvaluationApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.api.ms4}/api`;

  createExpert(body: ExpertCreate): Observable<Expert> {
    return this.http.post<Expert>(`${this.base}/experts`, body);
  }

  /** Extension additive : profil expert du porteur du JWT (404 si non créé). */
  me(): Observable<Expert | null> {
    return this.http.get<Expert>(`${this.base}/experts/me`).pipe(catchError(() => of(null)));
  }

  jury(challengeId: string): Observable<Jury> {
    return this.http.get<Jury>(`${this.base}/challenges/${challengeId}/jury`);
  }

  setJury(challengeId: string, expertIds: string[]): Observable<Jury> {
    return this.http.post<Jury>(`${this.base}/challenges/${challengeId}/jury`, { expertIds });
  }

  rubrics(challengeId: string): Observable<Rubric[]> {
    return this.http.get<Rubric[]>(`${this.base}/rubrics`, {
      params: new HttpParams().set('challengeId', challengeId)
    });
  }

  createRubric(body: RubricCreate): Observable<Rubric> {
    return this.http.post<Rubric>(`${this.base}/rubrics`, body);
  }

  evaluate(body: EvaluationCreate): Observable<Evaluation> {
    return this.http.post<Evaluation>(`${this.base}/evaluations`, body);
  }

  leaderboard(challengeId: string): Observable<LeaderboardEntry[]> {
    return this.http.get<LeaderboardEntry[]>(`${this.base}/challenges/${challengeId}/leaderboard`);
  }

  declareWinners(challengeId: string): Observable<Winner[]> {
    return this.http.post<Winner[]>(`${this.base}/challenges/${challengeId}/declare-winners`, {});
  }
}
