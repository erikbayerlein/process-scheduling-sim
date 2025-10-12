package com.api.process_scheduling.dto;

import java.util.List;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;


/**
 * @param time            o tempo atual da simulação
 * @param cpuRunningPid   o ID do processo atual na CPU
 * @param readyQueueState o estado atual da fila de processos prontos
 */
@Builder
public record StatusUpdateEvent(Integer time, Long cpuRunningPid,
                                @NotNull List<ProcessQueueState> readyQueueState) {

  /**
   * @param pid             ID do processo
   * @param remainingTime   tempo restante
   * @param dynamicPriority prioridade dinamica
   */
  @Builder
  public record ProcessQueueState(Long pid, Integer remainingTime, Integer dynamicPriority

  ) {

  }
}