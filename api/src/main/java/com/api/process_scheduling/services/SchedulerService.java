package com.api.process_scheduling.services;

import com.api.process_scheduling.dto.SimulationConfigMessage;
import com.api.process_scheduling.entities.Process;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public interface SchedulerService {

  String schedule(Process process);

  void runSimulation(SimulationConfigMessage message, SimpMessagingTemplate messagingTemplate);
}
