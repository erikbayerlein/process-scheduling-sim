package com.api.process_scheduling.services.impl.strategy;

import com.api.process_scheduling.entities.Process;
import com.panfutov.result.Result;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import org.jetbrains.annotations.NotNull;

/**
 * Round-Robin com envelhecimento baseado em prioridade. Um processo em execução só é preemptado se
 * um processo na fila de prontos tiver prioridade estritamente maior. Todos os processos na fila de
 * prontos sofrem envelhecimento a cada seleção de processo, aumentando sua prioridade dinâmica em
 * 1.
 */
public class RoundRobinPriorityAging implements schedulingAlgorithm {

  private final PriorityQueue<Process> readyQueue;
  private Process currentProcess;

  public RoundRobinPriorityAging() {
    this.readyQueue = new PriorityQueue<>(
        Collections.reverseOrder(Comparator.comparingInt(Process::getDynamicPriority)));
  }

  /**
   * envelhecimento dos processos na fila de prontos.
   */
  public void applyAgingToReady() {
    if (this.readyQueue.isEmpty()) {
      return;
    }

    for (var process : this.readyQueue) {
      process.aging();
    }

    // Reordena a fila de prontos após o envelhecimento
    var reorderedQueue = new PriorityQueue<>(this.readyQueue);
    this.readyQueue.clear();
    this.readyQueue.addAll(reorderedQueue);
  }

  @Override
  public void addProcess(@NotNull Process process) {
    this.readyQueue.add(process);
  }

  @Override
  public Result<Process> selectNextProcess() {

    // Verifica se o processo que estava em execução terminou.
    if (this.currentProcess != null && this.currentProcess.isCompleted()) {
      this.currentProcess = null;
    }

    // Pega o candidato de maior prioridade da fila, mas sem removê-lo ainda.
    Process bestCandidate = this.readyQueue.peek();

    // Se não há candidato na fila, o processo atual (se existir) continua.
    if (bestCandidate == null) {
      return Result.success(this.currentProcess);
    }

    // não há processo em execução, então o melhor candidato assume a CPU.
    if (this.currentProcess == null) {
      this.currentProcess = this.readyQueue.poll();

      // Aplica envelhecimento aos processos na fila de prontos.
      this.applyAgingToReady();

      return Result.success(this.currentProcess);
    }

    // Se a CPU tem um processo, compara com o melhor candidato.
    // A preempção SÓ OCORRE se a prioridade do candidato for ESTRITAMENTE MAIOR.
    if (bestCandidate.getDynamicPriority() > this.currentProcess.getDynamicPriority()) {
      this.readyQueue.add(this.currentProcess);
      this.currentProcess = this.readyQueue.poll();
    }

    this.applyAgingToReady();
    return Result.success(this.currentProcess);
  }
}