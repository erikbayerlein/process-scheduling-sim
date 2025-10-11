package com.api.process_scheduling.services.impl.strategy;

import com.api.process_scheduling.dto.SimulationConfigMessage;
import com.api.process_scheduling.enums.Algorithms;
import com.panfutov.result.Result;

public class SchedulingAlgorithmFactory {

  public static Result<schedulingAlgorithm> getAlgorithm(Algorithms algorithmName,
      SimulationConfigMessage.GlobalConfig config) {
    try {
      return switch (algorithmName) {
        case FCFS -> Result.success(new FCFS());
        case SJF -> Result.success(new SJF());
        case SRTF -> Result.success(new SRTF());
        case PRIORITY_NON_PREEMPTIVE -> Result.success(new PriorityNonPreemptive());
        case PRIORITY_PREEMPTIVE -> Result.success(new PriorityPreemptive());
        case ROUND_ROBIN -> Result.success(new RoundRobin(config.quantum()));
        case ROUND_ROBIN_PRIORITY_AGING -> Result.success(new RoundRobinPriorityAging());
      };
    } catch (Exception e) {
      return Result.failure("Error creating scheduling algorithm: " + e.getMessage());
    }
  }
}
