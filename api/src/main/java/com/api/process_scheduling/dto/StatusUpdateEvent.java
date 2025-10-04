package com.api.process_scheduling.dto;

import java.util.List;
import org.jetbrains.annotations.NotNull;


/**
 * @param time            o tempo atual da simulação
 * @param cpuRunningPid   o ID do processo atual na CPU
 * @param readyQueueState o estado atual da fila de processos prontos
 * @param contextSwitch   true se uma troca de contexto ocorreu
 * @param ganttSegment    dados para atualizar o gráfico
 */
public record StatusUpdateEvent(Integer time, Long cpuRunningPid,
                                List<ProcessQueueState> readyQueueState,

                                Boolean contextSwitch, @NotNull GanttSegment ganttSegment) {

  /**
   * @param pid             ID do processo
   * @param remainingTime   tempo restante
   * @param dynamicPriority prioridade dinamica
   */
  public record ProcessQueueState(Long pid, Integer remainingTime, Integer dynamicPriority

  ) {

  }

  /**
   * @param pid       ID do processo
   * @param startTime tempo de início
   * @param duration  duração
   */
  public record GanttSegment(Long pid, Integer startTime, Integer duration) {

  }
}