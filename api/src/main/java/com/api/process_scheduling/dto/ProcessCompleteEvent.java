package com.api.process_scheduling.dto;

/**
 * @param pid     process id do processo completado
 * @param metrics métricas do processo
 */
public record ProcessCompleteEvent(Long pid, ProcessMetrics metrics) {

  /**
   * @param tt turnaround time (tempo total de execução)
   * @param wt waiting time (tempo de espera)
   *
   */
  public record ProcessMetrics(Integer tt, Integer wt) {

  }

}
