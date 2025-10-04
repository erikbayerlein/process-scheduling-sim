package com.api.process_scheduling.controllers.impl;

import com.api.process_scheduling.controllers.ProcessSchedulerController;
import com.api.process_scheduling.dto.SimulationConfigMessage;
import com.api.process_scheduling.services.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class ProcessSchedulerControllerImpl implements ProcessSchedulerController {

  private final SchedulerService schedulerService;
  private final SimpMessagingTemplate messagingTemplate;

  @Override
  @MessageMapping("/start")
  public void startSimulation(@RequestBody SimulationConfigMessage request) {
    System.out.println("Received START_SIMULATION request:");
    System.out.println("Received simulation start request: " + request);

    schedulerService.runSimulation(request, messagingTemplate);

    messagingTemplate.convertAndSend("/process-scheduler/status",
        "Simulation initialization received.");
  }

}
