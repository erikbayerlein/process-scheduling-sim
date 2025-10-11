package com.api.process_scheduling.dto;

import com.api.process_scheduling.enums.Algorithms;
import java.util.List;


/**
 *
 * @param algorithm algoritmo a utilizar
 * @param processes uma lista de processos
 * @param config    configuração global
 */
public record SimulationConfigMessage(Algorithms algorithm, List<ProcessDTOMessage> processes,
                                      GlobalConfig config) {

  /**
   * @param quantum fatia de tempo
   * @param aging   taxa de envelhecimento
   */
  public record GlobalConfig(Integer quantum, Integer aging) {

  }


}
