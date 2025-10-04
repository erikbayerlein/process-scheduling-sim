package com.api.process_scheduling.dto;

import java.util.List;


/**
 *
 * @param algorithm algoritmo a utilizar
 * @param processes uma lista de processos
 * @param config    configuração global
 */
public record SimulationConfigMessage(String algorithm, List<ProcessDTOMessage> processes,
                                      GlobalConfig config) {

  /**
   * @param quantum fatia de tempo
   * @param aging   taxa de envelhecimento
   */
  public record GlobalConfig(Integer quantum, Integer aging) {

  }


}
