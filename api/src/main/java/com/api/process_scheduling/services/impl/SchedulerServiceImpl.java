package com.api.process_scheduling.services.impl;

import com.api.process_scheduling.dto.SimulationConfigMessage;
import com.api.process_scheduling.dto.StatusUpdateEvent;
import com.api.process_scheduling.entities.Process;
import com.api.process_scheduling.services.SchedulerService;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SchedulerServiceImpl implements SchedulerService {

  @Override
  public String schedule(Process process) {
    return "Scheduled Process: " + process.getPid();
  }

  @Override
  public void runSimulation(SimulationConfigMessage message,
      SimpMessagingTemplate messagingTemplate) {
    Integer time = 0;

    while (time < 10) {
      var event = new StatusUpdateEvent(time, 0L,
          List.of(new StatusUpdateEvent.ProcessQueueState(
              0L, 1, 0
          )), false,
          new StatusUpdateEvent.GanttSegment(0L, time, 1));

      messagingTemplate.convertAndSend("/process-scheduler/updates", event);

      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      time++;
    }

  }
}
