package com.api.process_scheduling.controllers.impl;

import com.api.process_scheduling.controllers.ProcessSchedulerController;
import com.api.process_scheduling.dto.SimulationConfigMessage;
import com.api.process_scheduling.services.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ProcessSchedulerControllerImpl implements ProcessSchedulerController {

  private final SchedulerService schedulerService;
  private final SimpMessagingTemplate messagingTemplate;

  @Override
  @MessageMapping("/start")
  public void startSimulation(@RequestBody SimulationConfigMessage request) {
    log.debug("Received START_SIMULATION request:");

    var setupResult = schedulerService.setupSimulation(request, messagingTemplate);
    if (setupResult.isFailure()) {
      log.error("Error during simulation setup: {}", setupResult.getErrors());
      messagingTemplate.convertAndSend("/process-scheduler/errors",
          "Error during simulation setup: " + setupResult.getErrors());
      return;
    }

    schedulerService.runSimulation();
    log.info("Simulation completed successfully.");
  }
}
