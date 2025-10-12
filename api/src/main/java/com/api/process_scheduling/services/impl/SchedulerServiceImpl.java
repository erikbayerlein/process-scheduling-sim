package com.api.process_scheduling.services.impl;

import com.api.process_scheduling.dto.ProcessCompleteEvent;
import com.api.process_scheduling.dto.SimulationCompletedEvent;
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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Data
@Slf4j
public class SchedulerServiceImpl implements SchedulerService {

  final static int MAX_TIME_SAFETY_LIMIT = 100; // tempo máximo de simulação em unidades de tempo
  private int nextProcessIndex = 0;
  private List<Process> processQueue = new LinkedList<>();
  private schedulingAlgorithm algorithm;
  private SimpMessagingTemplate messagingTemplate;
  private int numOfContextSwitches = 0;

  private int completedProcesses = 0;
  private int totalProcesses = 0;

  @Override
  public Result<Void> setupSimulation(SimulationConfigMessage message,
      SimpMessagingTemplate template) {
    this.completedProcesses = 0;
    this.totalProcesses = message.processes().size();
    return this.configureSimulation(template, message);
  }

  public void cleanUp() {
    try {
      this.nextProcessIndex = 0;
      this.algorithm = null;
      this.messagingTemplate = null;
      this.numOfContextSwitches = 0;
      this.completedProcesses = 0;
      this.totalProcesses = 0;
    } catch (Exception e) {
      log.error("Error during cleanup: {}", e.getMessage());
      this.sendError("Error during cleanup: " + e.getMessage());
    }
  }

  @Override
  public void runSimulation() {
    log.info("Starting simulation");
    int time = 0;

    Process previousProcess = null;

    // verificar se posso incrementar o tempo aqui time++
    while (this.completedProcesses < this.totalProcesses && time <= MAX_TIME_SAFETY_LIMIT) {
      // 1. processos a serem criados neste ciclo
      var createProcessResult = this.createProcesses(time);
      if (createProcessResult.isFailure()) {
        log.error("Failed to create processes: {}", createProcessResult.getErrors());
        this.sendError(createProcessResult.getErrors());
        return;
      }

      // 2. selecionar processo a ser executado
      var nextProcessResult = this.algorithm.selectNextProcess();
      if (nextProcessResult.isFailure()) {
        log.error("Failed to select next process: {}", nextProcessResult.getErrors());
        this.sendError(nextProcessResult.getErrors());
        return;
      }
      var nextProcess = nextProcessResult.getObject();

      if (Objects.isNull(nextProcess)) {
        if (Objects.nonNull(previousProcess)) {
          log.debug("context switch from process {} to idle",
              previousProcess.getPid());
          this.numOfContextSwitches++;
        }

        previousProcess = null;
        time++;
        continue;
      }

      // 3. verificar se houve troca de contexto
      if (Objects.isNull(previousProcess) || !Objects.equals(
          previousProcess.getPid(), nextProcess.getPid())) {
        log.debug("context switch from process {} to process {}",
            Objects.isNull(previousProcess) ? "null" : previousProcess.getPid(),
            nextProcess.getPid());
        this.numOfContextSwitches++;
      }

      // 4. executar o processo selecionado
      nextProcess.execute();

      // 5. atualizar o estado dos processos (finalizados, prontos, etc)
      // e metadados (waiting time, turnaround time, etc)

      this.updateAllProcessStatuses(nextProcess);

      // se o processo atual terminou, calcula o turnaround time

      if (nextProcess.getStatus() == ProcessStatus.TERMINATED) {
        completedProcesses++;
        nextProcess.setTurnaroundTime(time + 1 - nextProcess.getCreationTime());
        nextProcess.setWaitingTime(nextProcess.getTurnaroundTime() - nextProcess.getDuration());
        log.info("Process {} terminated at time {}", nextProcess.getPid(), time + 1);

        // envio de evento de término
        this.sendCompleteEvent(nextProcess);
      }

      // 6. enviar atualização de status via WebSocket
      this.sendStatusUpdateEvent(
          time,
          nextProcess.getPid(),
          this.processQueue.stream().filter(p -> p.getStatus() == ProcessStatus.READY).toList()
      );

      // 7. incrementar o tempo
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      previousProcess = nextProcess;
      time++;
    }

    if (time > MAX_TIME_SAFETY_LIMIT) {
      log.warn("Max simulation time reached, terminating simulation");
    }

    // calcular métricas finais
    double averageWaitingTime = this.processQueue.stream()
        .mapToInt(Process::getWaitingTime)
        .average().orElse(0.0);
    double averageTurnaroundTime = this.processQueue.stream()
        .mapToInt(Process::getTurnaroundTime)
        .average().orElse(0.0);

    this.sendSimulationCompleteEvent(averageTurnaroundTime, averageWaitingTime);

    this.cleanUp();
  }


  private Result<Void> configureSimulation(SimpMessagingTemplate messageTemplate,
      SimulationConfigMessage message) {
    log.debug("Configuring simulation with message: {}", message);

    this.messagingTemplate = messageTemplate;

    var algorithmResult = SchedulingAlgorithmFactory.getAlgorithm(message.algorithm(),
        message.config().quantum());
    if (algorithmResult.isFailure()) {
      return Result.failure(algorithmResult.getErrors());
    }
    this.algorithm = algorithmResult.getObject();
    log.info("Simulation configured with algorithm: {}", message.algorithm());

    log.debug("initializing processes");

    this.processQueue = message.processes().stream()
        .map(p -> new Process(p.creationTime(), p.duration(), p.staticPriority()))
        .sorted(Comparator.comparingInt(Process::getCreationTime)).toList();

    log.info("Initialized {} processes", this.processQueue.size());
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

  private void updateAllProcessStatuses(Process currentProcess) {
    this.processQueue.forEach(p -> {
      // Se um processo já está terminado, seu estado é final. ignora
      if (p.getStatus() == ProcessStatus.TERMINATED) {
        return;
      }

      // Se o processo acabou de completar NESTE ciclo, ele se torna TERMINATED.
      // Esta é a transição para o estado final.
      if (p.isCompleted()) {
        p.setStatus(ProcessStatus.TERMINATED);
        return;
      }

      // Se o processo é o que foi selecionado para este ciclo, ele está RUNNING.
      if (Objects.nonNull(currentProcess) && p.getPid().equals(currentProcess.getPid())) {
        p.setStatus(ProcessStatus.RUNNING);
      }
      // Se o processo NÃO está executando e NÃO é novo, ele só pode estar READY.
      // Isso cobre tanto os processos que já estavam na fila quanto aquele que
      // estava executando e foi preemptado.
      else if (p.getStatus() == ProcessStatus.NEW) {
        p.setStatus(ProcessStatus.READY);
      }
    });
  }

  private void sendSimulationCompleteEvent(double averageTurnaroundTime,
      double averageWaitingTime) {
    var event = SimulationCompletedEvent.builder()
        .totalContextSwitches(this.numOfContextSwitches)
        .averageTurnaroundTime(averageTurnaroundTime)
        .averageWaitingTime(averageWaitingTime)
        .build();

    this.messagingTemplate.convertAndSend("/process-scheduler/simulation/completed", event);
  }

  private void sendCompleteEvent(Process process) {
    var event = ProcessCompleteEvent.builder()
        .pid(process.getPid())
        .tt(process.getTurnaroundTime())
        .wt(process.getWaitingTime())
        .build();

    messagingTemplate.convertAndSend("/process-scheduler/process/completed", event);

  }

  private void sendStatusUpdateEvent(int time, Long cpuRunningPid, List<Process> readyQueue) {
    var event = StatusUpdateEvent.builder()
        .time(time)
        .cpuRunningPid(cpuRunningPid)
        .readyQueueState(
            readyQueue.stream().map(
                p -> StatusUpdateEvent.ProcessQueueState.builder()
                    .pid(p.getPid())
                    .remainingTime(p.getRemainingTime())
                    .dynamicPriority(p.getDynamicPriority())
                    .build()
            ).toList())
        .build();
    messagingTemplate.convertAndSend("/process-scheduler/simulation/update", event);
  }

  private void sendError(Object errorMessage) {
    messagingTemplate.convertAndSend("/process-scheduler/errors", errorMessage);
  }
}
