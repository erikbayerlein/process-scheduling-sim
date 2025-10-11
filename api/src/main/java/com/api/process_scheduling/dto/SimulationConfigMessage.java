package com.api.process_scheduling.dto;

import com.api.process_scheduling.enums.Algorithms;
import java.util.List;
import org.jetbrains.annotations.NotNull;


/**
 *
 * @param algorithm algoritmo a utilizar
 * @param processes uma lista de processos
 * @param config    configuração global
 */
public record SimulationConfigMessage(Algorithms algorithm, List<ProcessDTOMessage> processes,
                                      GlobalConfig config) {

  @Override
  public @NotNull String toString() {
    return "SimulationConfigMessage{" + "algorithm=" + algorithm + ", processes=" + processes
        + ", config=" + config + '}';
  }

  /**
   * @param quantum fatia de tempo
   * @param aging   taxa de envelhecimento
   */
  public record GlobalConfig(Integer quantum, Integer aging) {

    @Override
    public @NotNull String toString() {
      return "Config{" + "quantum=" + quantum + ", aging=" + aging + '}';
    }
  }
}
