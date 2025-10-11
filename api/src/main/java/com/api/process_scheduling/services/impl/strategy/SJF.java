package com.api.process_scheduling.services.impl.strategy;

import com.api.process_scheduling.entities.Process;
import com.panfutov.result.Result;
import java.util.Comparator;
import java.util.PriorityQueue;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Shortest Job First (SJF) scheduling algorithm - Prioriza os processos com o menor tempo de
 * duração.
 */
@Getter
public class SJF implements schedulingAlgorithm {

  private final PriorityQueue<Process> readyQueue;
  private Process currentProcess = null;

  public SJF() {
    this.readyQueue = new PriorityQueue<>(Comparator.comparingInt(Process::getDuration));
  }

  @Override
  public void addProcess(@NotNull Process process) {
    readyQueue.add(process);
  }

  @Override
  public Result<Process> selectNextProcess() {
    if (this.currentProcess != null && !this.currentProcess.isCompleted()) {
      // se o processo atual nao terminou, ele continua em execucao
      return Result.success(this.currentProcess);
    }

    if (readyQueue.isEmpty()) {
      return Result.success(null);
    }

    this.currentProcess = readyQueue.poll();

    return Result.success(this.currentProcess);
  }
}
