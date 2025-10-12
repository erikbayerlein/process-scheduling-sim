package com.api.process_scheduling.dto;

import lombok.Builder;

/**
 * @param averageTurnaroundTime tempo médio de retorno
 * @param averageWaitingTime    tempo médio de espera
 * @param totalContextSwitches  total de trocas de contexto
 */
@Builder
public record SimulationCompletedEvent(
    Double averageTurnaroundTime, Double averageWaitingTime,
    Integer totalContextSwitches
) {

}
