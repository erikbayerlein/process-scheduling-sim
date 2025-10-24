// Algoritmos disponíveis (baseado no enum Algorithms do backend)
export type Algorithm =
  | 'FCFS'
  | 'SJF'
  | 'SRTF'
  | 'ROUND_ROBIN'
  | 'ROUND_ROBIN_PRIORITY_AGING'
  | 'PRIORITY_PREEMPTIVE'
  | 'PRIORITY_NON_PREEMPTIVE';

// Mapeamento de nomes amigáveis para valores do enum
export const ALGORITHM_MAP: Record<string, Algorithm> = {
  fcfs: 'FCFS',
  sjf: 'SJF',
  srtf: 'SRTF',
  'round-robin': 'ROUND_ROBIN',
  'round-robin-aging': 'ROUND_ROBIN_PRIORITY_AGING',
  'priority-preemptive': 'PRIORITY_PREEMPTIVE',
  'priority-non-preemptive': 'PRIORITY_NON_PREEMPTIVE',
};

// ProcessDTOMessage
export interface ProcessDTO {
  creationTime: number;
  duration: number;
  staticPriority: number;
}

// GlobalConfig
export interface GlobalConfig {
  quantum?: number;
  aging?: number;
}

// SimulationConfigMessage
export interface SimulationConfigMessage {
  algorithm: Algorithm;
  processes: ProcessDTO[];
  config: GlobalConfig;
}

// StatusUpdateEvent
export interface StatusUpdateEvent {
  time: number;
  cpuRunningPid: number | null;
  readyQueueState: ProcessQueueState[];
}

export interface ProcessQueueState {
  pid: number;
  remainingTime: number;
  dynamicPriority: number;
}

// ProcessCompleteEvent
export interface ProcessCompleteEvent {
  pid: number;
  tt: number; // turnaround time
  wt: number; // waiting time
}

// SimulationCompletedEvent
export interface SimulationCompletedEvent {
  averageTurnaroundTime: number;
  averageWaitingTime: number;
  totalContextSwitches: number;
}

// Tipos locais para UI
export interface Process {
  id: string;
  arrivalTime: number;
  duration: number;
  priority: number;
  color: string;
}

export interface GanttSegment {
  processId: number;
  start: number;
  end: number;
  color: string;
}
