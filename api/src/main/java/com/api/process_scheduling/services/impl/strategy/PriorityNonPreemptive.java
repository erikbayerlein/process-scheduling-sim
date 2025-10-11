package com.api.process_scheduling.services.impl.strategy;

import com.api.process_scheduling.entities.Process;
import com.panfutov.result.Result;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import org.jetbrains.annotations.NotNull;

/**
 * Prioridade Não Preemptiva - O processo com a maior prioridade (menor valor numérico) é
 * selecionado para execução. Se um processo está em execução, ele não pode ser interrompido por
 * outro processo com prioridade mais alta que chegue.
 */
public class PriorityNonPreemptive implements schedulingAlgorithm {

  private final PriorityQueue<Process> readyQueue;
  private Process currentProcess = null;


  /**
   * inicializa a fila de processos prontos com um comparador que ordena os processos pela
   * prioridade em ordem decrescente (maior prioridade primeiro)
   */
  PriorityNonPreemptive() {
    this.readyQueue = new PriorityQueue<>(
        Collections.reverseOrder(Comparator.comparingInt(Process::getStaticPriority)));
  }

  @Override
  public void addProcess(@NotNull Process process) {
    this.readyQueue.add(process);
  }

  @Override
  public Result<Process> selectNextProcess() {
    if (this.currentProcess != null && !this.currentProcess.isCompleted()) {
      return Result.success(this.currentProcess);
    }

    if (!this.readyQueue.isEmpty()) {
      this.currentProcess = this.readyQueue.poll();
      return Result.success(this.currentProcess);
    }

    return Result.success(null);
  }
}
