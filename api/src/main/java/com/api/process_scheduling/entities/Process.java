package com.api.process_scheduling.entities;

import com.api.process_scheduling.enums.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Process {

  static Long nextId = 0L;

  final Long pid;

  final int creationTime;
  final int duration;
  final int staticPriority;

  int dynamicPriority;
  int remainingTime;
  ProcessStatus status;
  int waitingTime;
  int turnaroundTime;

  public Process(int creationTime, int duration, int staticPriority) {
    this.pid = nextId++;

    this.creationTime = creationTime;
    this.duration = duration;
    this.staticPriority = staticPriority;

    this.dynamicPriority = staticPriority;
    this.remainingTime = duration;
    this.status = ProcessStatus.READY;
    this.waitingTime = 0;
    this.turnaroundTime = 0;
  }


  /**
   * Simula a execução do processo por um ciclo de tempo.
   */
  public void execute() {
    if (this.remainingTime > 0) {
      this.remainingTime--;
    }
  }


  public boolean isCompleted() {
    return this.remainingTime == 0;
  }

  /**
   * aumenta a prioridade dinamica, simulando o envelhecimento do processo.
   */
  public void aging() {
    this.dynamicPriority++;
  }

  /**
   * reseta a prioridade dinamica para a prioridade estatica. para quando o processo é selecionado
   * para execução.
   */
  public void resetDynamicPriority() {
    this.dynamicPriority = this.staticPriority;
  }

  @Override
  public String toString() {
    return "Processo[PID=" + pid + ", Criação=" + creationTime + ", Duração=" + duration
        + ", Restante=" + remainingTime + ", Prio=" + staticPriority + "]";
  }


}
