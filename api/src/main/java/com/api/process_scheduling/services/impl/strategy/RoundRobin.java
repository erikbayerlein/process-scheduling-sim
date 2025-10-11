package com.api.process_scheduling.services.impl.strategy;

import com.api.process_scheduling.entities.Process;
import com.panfutov.result.Result;
import java.util.LinkedList;
import java.util.Queue;
import org.jetbrains.annotations.NotNull;


/**
 * Round Robin non preemptive: cada processo recebe um quantum fixo de tempo para execução. Se o
 * processo não terminar dentro do quantum, ele é colocado no final da fila de prontos.
 */
public class RoundRobin implements schedulingAlgorithm {

  private final Queue<Process> readyQueue;
  private final int quantum;
  private Process currentProcess = null;
  private int currentCycleTime = 0;


  RoundRobin(int quantum) {
    if (quantum <= 0) {
      throw new IllegalArgumentException("Quantum deve ser um valor positivo.");
    }

    this.readyQueue = new LinkedList<>();
    this.quantum = quantum;
  }

  @Override
  public void addProcess(@NotNull Process process) {
    this.readyQueue.add(process);
  }

  @Override
  public Result<Process> selectNextProcess() {
    // processo atual terminou sua execução
    if (this.currentProcess != null && this.currentProcess.isCompleted()) {
      this.currentProcess = null;
      this.currentCycleTime = 0;
    }

    // processo atual esgotou tempo de quantum
    if (this.currentProcess != null && this.currentCycleTime >= quantum) {
      this.readyQueue.add(this.currentProcess);
      this.currentProcess = null;
      this.currentCycleTime = 0;
    }

    // não há processo em execução, seleciona o próximo da fila
    if (this.currentProcess == null && !this.readyQueue.isEmpty()) {
      this.currentProcess = this.readyQueue.poll();
      this.currentCycleTime = 0;
    }

    // incrementa o tempo de ciclo do processo atual
    if (this.currentProcess != null) {
      this.currentCycleTime++;
    }

    return Result.success(this.currentProcess);
  }
}
