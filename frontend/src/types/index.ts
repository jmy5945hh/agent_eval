export type RunStatus = 'queued' | 'running' | 'success' | 'failed' | 'cancelled';
export type TaskStatus = 'running' | 'completed' | 'cancelled';
export type ScoreStatus = 'idle' | 'scoring' | 'scored' | 'confirmed';

export interface AgentProduct {
  id: string;
  name: string;
  version: string;
  vendor: string;
  desc: string;
  status: 'enabled' | 'disabled';
}

export interface AgentVersion {
  id: string;
  agentId: string;
  version: string;
  releasedAt: string;
  notes: string;
  recommended?: boolean;
  enabled: boolean;
}

export interface ModelConfig {
  id: string;
  name: string;
  tier: string;
  version: string;
  provider: string;
  enabled: boolean;
  scoring: boolean;
  desc: string;
}

export interface CaseFile {
  path: string;
  content: string;
}

export interface EvaluationCase {
  id: string;
  code: string;
  name: string;
  prompt: string;
  repo: string;
  branch: string;
  category: string;
  difficulty: string;
  importance: string;
  version: number;
  remark: string;
  createdAt: string;
  standardAnswer: CaseFile[];
}

export interface ScoringDimension {
  key: string;
  label: string;
  weight: number;
  desc: string;
}

export interface ScoringStandard {
  id: string;
  version: string;
  current?: boolean;
  updatedAt: string;
  note: string;
  dimensions: ScoringDimension[];
}

export interface TrajectoryEntry {
  role: 'user' | 'agent' | 'tool';
  kind?: string;
  time: string;
  title?: string;
  content: string;
}

export interface ErrorInfo {
  category: string;
  log: string;
}

export interface RunScore {
  dims: Record<string, number>;
  comments: Record<string, string>;
  analysis: string;
  note: string;
  edited: boolean;
  model: string;
  standardVersion: string;
}

export interface CaseRun {
  caseId: string;
  status: RunStatus;
  attempts?: number;
  removed?: boolean;
  removeReason?: string;
  rounds?: number;
  tokensIn?: number;
  tokensOut?: number;
  durationMs?: number;
  error?: ErrorInfo | null;
  trajectory?: TrajectoryEntry[];
  score?: RunScore | null;
}

export interface EvaluationTask {
  id: string;
  name: string;
  agentId: string;
  agentVersionId?: string;
  modelId: string;
  creator: string;
  createdAt: string;
  status: TaskStatus;
  phase?: string;
  scoringModelId?: string | null;
  standardVersion?: string | null;
  scoringStatus: ScoreStatus;
  runs: CaseRun[];
}

export interface CreateTaskPayload {
  agentId: string;
  agentVersionId: string;
  modelId: string;
  selectedCases: string[];
  taskName: string;
}
