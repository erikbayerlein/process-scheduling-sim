package com.api.process_scheduling.dto;

/**
 * @param algorithm    algoritmo utilizado na simulação
 * @param finalMetrics métricas finais da simulação
 */
public record SimulationCompletedEvent(String algorithm, FinalMetrics finalMetrics) {

  /**
   * @param averageTurnaroundTime tempo médio de retorno
   * @param averageWaitingTime    tempo médio de espera
   * @param totalContextSwitches  total de trocas de contexto
   */
  public record FinalMetrics(Double averageTurnaroundTime, Double averageWaitingTime,
                             Integer totalContextSwitches) {

  }
}
