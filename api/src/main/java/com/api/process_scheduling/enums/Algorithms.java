package com.api.process_scheduling.enums;

/**
 * Enum representing different process scheduling algorithms.
 * <ul>
 *   <li>FCFS: First Come, First Served</li>
 *   <li>SJF: Shortest Job First</li>
 *   <li>SRTF: Shortest Remaining Time First</li>
 *   <li>PRIORITY_NON_PREEMPTIVE: Priority-based, non-preemptive</li>
 *   <li>PRIORITY_PREEMPTIVE: Priority-based, preemptive</li>
 *   <li>ROUND_ROBIN: Round-Robin with quantum, no priority</li>
 *   <li>ROUND_ROBIN_PRIORITY_AGING: Round-Robin with priority and aging</li>
 * </ul>
 */
public enum Algorithms {
  FCFS,
  SJF,
  SRTF,
  PRIORITY_NON_PREEMPTIVE,
  PRIORITY_PREEMPTIVE,
  ROUND_ROBIN,
  ROUND_ROBIN_PRIORITY_AGING,
}
