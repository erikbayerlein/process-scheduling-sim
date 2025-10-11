package com.api.process_scheduling.services.impl.strategy;

import com.api.process_scheduling.entities.Process;
import com.panfutov.result.Result;
import org.jetbrains.annotations.NotNull;

public interface schedulingAlgorithm {

  /**
   * Adiciona um processo na fila de prontos.
   * <br>
   * fila de prontos pode ser uma lista, fila ou qualquer outra estrutura de dados, dependendo do
   * algoritmo
   *
   * @param process O processo a ser adicionado na fila de prontos
   */
  void addProcess(@NotNull Process process);

  /**
   * O método principal que decide qual processo deve ser executado no próximo ciclo. A lógica de
   * seleção (Round Robin, Prioridade, etc.) está aqui dentro.
   *
   * @return O processo selecionado para execução, ou null se não houver nenhum.
   */
  Result<Process> selectNextProcess();
}
