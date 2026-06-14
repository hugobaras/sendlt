export type UserRole = 'CLIMBER' | 'SETTER_ADMIN';
export type AttemptType = 'FLASH' | 'TOP_AFTER_TRIES' | 'FAIL';
export type SessionStatus = 'ACTIVE' | 'CLOSED';
export type BoulderColor =
  | 'JAUNE'
  | 'VERT'
  | 'BLEU'
  | 'VIOLET'
  | 'ROUGE'
  | 'BLANC'
  | 'NOIR';
export type ScoringMode = 'FONT' | 'COLOR';

export interface UserResponse {
  id: string;
  email: string;
  displayName: string;
  role: UserRole;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: UserResponse;
}

export interface GymResponse {
  id: string;
  name: string;
  city: string | null;
  scoringMode: ScoringMode;
  createdAt: string;
}

export interface SectorResponse {
  id: string;
  gymId: string;
  name: string;
  createdAt: string;
}

export interface BoulderResponse {
  id: string;
  sectorId: string;
  color: BoulderColor;
  gradeFont: string | null;
  gradeVScale: string | null;
  holdStyle: string;
  openedAt: string;
}

export interface SessionResponse {
  id: string;
  roomCode: string;
  status: SessionStatus;
  gymId: string;
  createdAt: string;
  closedAt: string | null;
}

export interface SessionParticipantResponse {
  id: string;
  sessionId: string;
  userId: string;
  joinedAt: string;
}

export interface AttemptResponse {
  id: string;
  boulderId: string;
  userId: string;
  sessionId: string;
  type: AttemptType;
  triesCount: number;
  createdAt: string;
}

export interface VolumeStatsResponse {
  successfulBoulders: number;
  from: string;
  to: string;
}

export interface GradePyramidEntry {
  grade: string;
  count: number;
}

export interface GradePyramidResponse {
  entries: GradePyramidEntry[];
}

export interface SessionActivityMessage {
  eventType: 'ATTEMPT_CREATED' | 'PARTICIPANT_JOINED';
  sessionId: string;
  timestamp: string;
  attempt?: {
    attemptId: string;
    userId: string;
    userDisplayName: string;
    boulderId: string;
    boulderColor: BoulderColor;
    gradeFont: string | null;
    gradeVScale: string | null;
    type: AttemptType;
    triesCount: number;
  };
  participant?: {
    userId: string;
    userDisplayName: string;
    joinedAt: string;
  };
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  violations?: { field: string; message: string }[];
}
