package com.api.process_scheduling.services;

import com.api.process_scheduling.dto.SimulationConfigMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public interface SchedulerService {

  void runSimulation(SimulationConfigMessage message, SimpMessagingTemplate messagingTemplate);
}

