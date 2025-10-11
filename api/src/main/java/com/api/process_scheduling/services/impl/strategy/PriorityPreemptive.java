package com.api.process_scheduling.services.impl.strategy;

import com.api.process_scheduling.entities.Process;
import com.panfutov.result.Result;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import org.jetbrains.annotations.NotNull;

/**
 * Priority Preemptive scheduling algorithm - Prioriza os processos com maior prioridade. Se um
 * processo está em execução, ele pode ser interrompido por outro processo com prioridade mais alta
 * que chegue.
 */
public class PriorityPreemptive implements schedulingAlgorithm {

  private final PriorityQueue<Process> readyQueue;
  private Process currentProcess = null;

  PriorityPreemptive() {

    this.readyQueue = new PriorityQueue<>(
        Collections.reverseOrder(Comparator.comparingInt(Process::getStaticPriority)));
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

    // há processo atual e verifica se há processo em execução com prioridade maior
    if (this.currentProcess != null && !this.readyQueue.isEmpty()) {
      var nextProcess = this.readyQueue.peek();
      if (nextProcess.getStaticPriority() > this.currentProcess.getStaticPriority()) {
        this.readyQueue.add(this.currentProcess);
        this.currentProcess = this.readyQueue.poll();
      }

      return Result.success(this.currentProcess);
    }

    // se não há processo em execução, seleciona o próximo da fila
    if (this.currentProcess == null && !this.readyQueue.isEmpty()) {
      this.currentProcess = this.readyQueue.poll();
    }

    return Result.success(this.currentProcess);
  }
}
