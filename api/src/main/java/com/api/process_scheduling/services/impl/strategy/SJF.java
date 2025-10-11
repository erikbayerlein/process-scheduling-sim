package com.api.process_scheduling.services.impl.strategy;

import com.api.process_scheduling.entities.Process;
import com.panfutov.result.Result;
import java.util.Comparator;
import java.util.PriorityQueue;
import org.jetbrains.annotations.NotNull;

/**
 * Shortest Job First (SJF) scheduling algorithm - Prioriza os processos com o menor tempo de
 * duração.
 */
public class SJF implements schedulingAlgorithm {

  private final PriorityQueue<Process> queue;
  private Process currentProcess = null;

  SJF() {
    this.queue = new PriorityQueue<>(Comparator.comparingInt(Process::getDuration));
  }

  @Override
  public void addProcess(@NotNull Process process) {
    queue.add(process);
  }

  @Override
  public Result<Process> selectNextProcess() {
    if (queue.isEmpty()) {
      return Result.success(null);
    }

    this.currentProcess = queue.poll();

    return Result.success(this.currentProcess);
  }
}
