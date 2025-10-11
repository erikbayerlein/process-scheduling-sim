package com.api.process_scheduling.services.impl;

import com.api.process_scheduling.dto.SimulationConfigMessage;
import com.api.process_scheduling.dto.StatusUpdateEvent;
import com.api.process_scheduling.entities.Process;
import com.api.process_scheduling.enums.ProcessStatus;
import com.api.process_scheduling.services.SchedulerService;
import com.api.process_scheduling.services.impl.strategy.SchedulingAlgorithmFactory;
import com.api.process_scheduling.services.impl.strategy.schedulingAlgorithm;
import com.panfutov.result.Result;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SchedulerServiceImpl implements SchedulerService {

  private final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);
  private Integer nextProcessIndex = 0;
  private List<Process> processQueue = new LinkedList<>();
  private schedulingAlgorithm algorithm;
  private SimpMessagingTemplate messagingTemplate;

  private int numOfcontextSwitches = 0;

  private Result<Void> configureSimulation(SimpMessagingTemplate messageTemplate,
      SimulationConfigMessage message) {
    this.logger.debug("Configuring simulation with message: {}", message);

    this.messagingTemplate = messageTemplate;

    var algorithmResult = SchedulingAlgorithmFactory.getAlgorithm(message.algorithm(),
        message.config().quantum());
    if (algorithmResult.isFailure()) {
      return Result.failure(algorithmResult.getErrors());
    }
    this.algorithm = algorithmResult.getObject();
    this.logger.info("Simulation configured with algorithm: {}", message.algorithm());

    this.logger.debug("initializing processes");

    this.processQueue = message.processes().stream()
        .map(p -> new Process(p.creationTime(), p.duration(), p.staticPriority()))
        .sorted(Comparator.comparingInt(Process::getCreationTime)).toList();

    this.logger.info("Initialized {} processes", this.processQueue.size());
    return Result.success(null);
  }


  private Result<Void> createProcesses(int currentTime) {
    if (this.algorithm == null) {
      return Result.failure("Algorithm not configured");
    }

    while (this.nextProcessIndex < this.processQueue.size()
        && this.processQueue.get(this.nextProcessIndex).getCreationTime() == currentTime) {
      var process = this.processQueue.get(this.nextProcessIndex);
      process.setStatus(ProcessStatus.READY);
      this.algorithm.addProcess(process);
      this.nextProcessIndex++;
    }

    return Result.success(null);
  }

  @Override
  public void runSimulation(SimulationConfigMessage message,
      SimpMessagingTemplate messagingTemplate) {
    this.logger.info("Starting simulation with config: {}", message);
    int time = 0;

    var configResult = this.configureSimulation(messagingTemplate, message);
    if (configResult.isFailure()) {
      this.logger.error("Failed to configure simulation: {}", configResult.getErrors());
      this.sendError(configResult.getErrors());
      return;
    }
    Process previousProcess = null;

    // TODO: replace 10 with a dynamic value
    while (time < 10) {
      var creationResult = this.createProcesses(time);
      if (creationResult.isFailure()) {
        this.logger.error("Failed to turn processes ready: {}", creationResult.getErrors());
        this.sendError(creationResult.getErrors());
        return;
      }

      var nextProcessResult = this.algorithm.selectNextProcess();
      if (nextProcessResult.isFailure()) {
        this.logger.error("Failed to select next process: {}", nextProcessResult.getErrors());
        this.sendError(nextProcessResult.getErrors());
        return;
      }
      var nextProcess = nextProcessResult.getObject();

      if (previousProcess != null && !Objects.equals(previousProcess.getPid(),
          nextProcess.getPid())) {
        this.numOfcontextSwitches++;
      }

      var event = new StatusUpdateEvent(time, nextProcess.getPid(),
          List.of(new StatusUpdateEvent.ProcessQueueState(0L, 1, 0)), false,
          new StatusUpdateEvent.GanttSegment(0L, time, 1));

      this.sendUpdate(event);

      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      previousProcess = nextProcess;
      time++;
    }

  }

  private void sendUpdate(StatusUpdateEvent event) {
    messagingTemplate.convertAndSend("/process-scheduler/updates", event);
  }

  private void sendError(Object errorMessage) {
    messagingTemplate.convertAndSend("/process-scheduler/errors", errorMessage);
  }
}
