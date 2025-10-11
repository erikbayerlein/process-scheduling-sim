package com.api.process_scheduling.services.impl.strategy;

import com.api.process_scheduling.entities.Process;
import com.panfutov.result.Result;
import java.util.List;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * First Come, First Served algoritmo de escalonamento - O processo que chega primeiro é o primeiro
 * a ser atendido.
 */
@Data
public class FCFS implements schedulingAlgorithm {

  private final List<Process> readyQueue;
  private Process currentProcess = null;

  FCFS() {
    this.readyQueue = new java.util.LinkedList<>();
  }

  @Override
  public void addProcess(@NotNull Process process) {
    readyQueue.add(process);
  }

  @Override
  public Result<Process> selectNextProcess() {
    if (readyQueue.isEmpty()) {
      return Result.failure("Ready queue is empty");
    }
    this.currentProcess = readyQueue.getFirst();

    readyQueue.removeFirst();

    return Result.success(this.currentProcess);
  }
}
