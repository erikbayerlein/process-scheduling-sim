package com.api.process_scheduling.services.impl.strategy;

import com.api.process_scheduling.entities.Process;
import com.panfutov.result.Result;
import java.util.Comparator;
import java.util.PriorityQueue;
import org.jetbrains.annotations.NotNull;


/**
 * Shortest Remaining Time First (SRTF) scheduling algorithm - Prioriza os processos com o menor
 * tempo restante de execução. É uma versão preemptiva do SJF.
 */
public class SRTF implements schedulingAlgorithm {

  private final PriorityQueue<Process> readyQueue;
  private Process currentProcess = null;

  SRTF() {
    this.readyQueue = new PriorityQueue<>(Comparator.comparingInt(Process::getRemainingTime));
  }

  @Override
  public void addProcess(@NotNull Process process) {
    this.readyQueue.add(process);
  }

  @Override
  public Result<Process> selectNextProcess() {
    // se o processo atual terminou sua execução
    if (this.currentProcess != null && this.currentProcess.isCompleted()) {
      this.currentProcess = null;
    }

    // se não há processo em execução, seleciona o próximo da fila
    if (this.currentProcess == null && !this.readyQueue.isEmpty()) {
      this.currentProcess = this.readyQueue.poll();
    }

    // verifica se há um processo na fila com tempo restante menor que o do processo atual
    if (this.currentProcess != null && !this.readyQueue.isEmpty()) {
      Process nextProcess = this.readyQueue.peek();
      if (nextProcess.getRemainingTime() < this.currentProcess.getRemainingTime()) {
        this.readyQueue.add(this.currentProcess);
        this.currentProcess = this.readyQueue.poll();
      }
    }

    return Result.success(this.currentProcess);
  }
}
