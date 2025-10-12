package com.api.process_scheduling.services;

import com.api.process_scheduling.dto.SimulationConfigMessage;
import com.panfutov.result.Result;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public interface SchedulerService {

  Result<Void> setupSimulation(SimulationConfigMessage message,
      SimpMessagingTemplate messagingTemplate);

  void runSimulation();
}

