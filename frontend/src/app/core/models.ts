export type ChallengeStatus =
  | 'DRAFT'
  | 'PUBLISHED'
  | 'IN_PROGRESS'
  | 'JUDGING'
  | 'CLOSED'
  | 'CANCELLED';

export interface ChallengeCreate {
  title: string;
  description: string;
  enterpriseId: string;
  deadline: string;
  tags: string[];
}

export interface Challenge extends ChallengeCreate {
  id: string;
  status: ChallengeStatus;
  createdAt: string;
  publishedAt: string | null;
}

export interface AcceptanceCriterion {
  id: string;
  description: string;
  weight: number;
  testCommand?: string;
}

export interface Prize {
  rank: number;
  amount: number;
  currency: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface DeveloperCreate {
  keycloakId: string;
  displayName: string;
  email?: string;
  skills?: string[];
}

export interface Developer extends DeveloperCreate {
  id: string;
  createdAt: string;
}

export interface TeamCreate {
  name: string;
  ownerId: string;
  memberIds?: string[];
}

export interface Team extends TeamCreate {
  id: string;
}

export type ApplicantType = 'DEVELOPER' | 'TEAM';
export type ApplicationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'WITHDRAWN';

export interface ApplicationCreate {
  applicantType: ApplicantType;
  applicantId: string;
}

export interface Application extends ApplicationCreate {
  id: string;
  challengeId: string;
  status: ApplicationStatus;
  registeredAt: string;
}

export type SubmissionStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'VERIFYING'
  | 'PASSED'
  | 'REJECTED'
  | 'WITHDRAWN';

export interface SubmissionCreate {
  challengeId: string;
  applicantId: string;
  repoUrl: string;
  branch: string;
}

export interface Submission extends SubmissionCreate {
  id: string;
  status: SubmissionStatus;
  commitSha: string | null;
  submittedAt: string | null;
}

export interface Verdict {
  submissionId: string;
  result: 'PASS' | 'FAIL';
  passed: number;
  failed: number;
  total: number;
  coverage: number | null;
  logsUrl: string;
  verifiedAt: string;
}

export interface ExpertCreate {
  keycloakId: string;
  displayName: string;
  domains?: string[];
}

export interface Expert extends ExpertCreate {
  id: string;
}

export interface Jury {
  challengeId: string;
  expertIds: string[];
}

export interface RubricCriterion {
  label: string;
  weight: number;
  maxScore: number;
}

export interface RubricCreate {
  challengeId: string;
  name: string;
  criteria: RubricCriterion[];
}

export interface Rubric extends RubricCreate {
  id: string;
}

export interface ScoreItem {
  criterionLabel: string;
  score: number;
}

export interface EvaluationCreate {
  submissionId: string;
  expertId: string;
  rubricId: string;
  scores: ScoreItem[];
  comment?: string;
}

export interface Evaluation extends EvaluationCreate {
  id: string;
  weightedScore: number;
  evaluatedAt: string;
}

export interface LeaderboardEntry {
  rank: number;
  submissionId: string;
  applicantId: string;
  averageScore: number;
  evaluationsCount: number;
}

export interface Winner {
  rank: number;
  submissionId: string;
  applicantId: string;
  score: number;
  prizeAmount: number;
}

export interface VerificationRequest {
  submissionId: string;
  challengeId?: string;
  repoUrl?: string;
  commitSha?: string;
  testCommand?: string;
}

export interface Verification {
  id: string;
  submissionId: string;
  status: 'QUEUED' | 'RUNNING' | 'COMPLETED' | 'ERROR';
  result: 'PASS' | 'FAIL' | null;
  passed: number;
  failed: number;
  total: number;
  coverage: number | null;
  durationMs: number;
  sandboxImage: string;
  startedAt: string;
  finishedAt: string | null;
}

export interface SimilarityMatch {
  otherSubmissionId: string;
  similarity: number;
}

export interface SimilarityReport {
  submissionId: string;
  challengeId: string;
  maxSimilarity: number;
  flagged: boolean;
  matches: SimilarityMatch[];
  analyzedAt: string;
}

export interface ReputationProfile {
  developerId: string;
  score: number;
  challengesWon: number;
  submissionsPassed: number;
  skills: string[];
  updatedAt: string;
}

export interface Badge {
  id: string;
  code: string;
  label: string;
  awardedAt: string;
}

export interface RankingEntry {
  rank: number;
  developerId: string;
  displayName: string;
  score: number;
}

export const STATUS_LABELS: Record<ChallengeStatus, string> = {
  DRAFT: 'Brouillon',
  PUBLISHED: 'Publié',
  IN_PROGRESS: 'En cours',
  JUDGING: 'Jury',
  CLOSED: 'Clôturé',
  CANCELLED: 'Annulé'
};

export const SUBMISSION_LABELS: Record<SubmissionStatus, string> = {
  DRAFT: 'Brouillon',
  SUBMITTED: 'Soumis',
  VERIFYING: 'Vérification',
  PASSED: 'Validé',
  REJECTED: 'Refusé',
  WITHDRAWN: 'Retiré'
};
