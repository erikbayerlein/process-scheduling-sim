package com.api.process_scheduling.unit.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.api.process_scheduling.entities.Process;
import com.api.process_scheduling.services.impl.strategy.SRTF;
import com.panfutov.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SRTFUnitTests {

  private SRTF srtfScheduler;
  private Process p1, p2, p3;

  @BeforeEach
  void setUp() {
    srtfScheduler = new SRTF();

    p1 = new Process(1, 10, 1); // Processo longo
    p2 = new Process(2, 3, 1);  // Processo curto
    p3 = new Process(3, 5, 1);  // Processo médio
  }

  @Test
  @DisplayName("Deve retornar sucesso com 'null' se a CPU estiver ociosa e a fila vazia")
  void selectNextProcess_WithEmptyQueue_ShouldReturnSuccessWithNull() {
    Result<Process> result = srtfScheduler.selectNextProcess();
    assertTrue(result.isSuccess());
    assertNull(result.getObject());
  }

  @Test
  @DisplayName("Com CPU ociosa, deve selecionar o processo com menor tempo restante")
  void selectNextProcess_WhenCPUIsIdle_ShouldSelectShortestProcess() {
    srtfScheduler.addProcess(p1); // 10 de tempo
    srtfScheduler.addProcess(p2); // 3 de tempo

    Process selected = srtfScheduler.selectNextProcess().getObject();
    assertEquals(p2, selected, "Deveria ter selecionado o processo mais curto (p2)");
  }

  @Test
  @DisplayName("Deve continuar com o processo atual se nenhum processo mais curto chegar")
  void selectNextProcess_ShouldContinueWithCurrentProcess_IfNotPreempted() {
    srtfScheduler.addProcess(p3); // 5 de tempo

    // Primeira chamada: seleciona p3
    Process firstSelected = srtfScheduler.selectNextProcess().getObject();
    assertEquals(p3, firstSelected);

    // Simula a execução de p3 por 2 ciclos
    firstSelected.setRemainingTime(3);

    // Adiciona um processo mais longo
    srtfScheduler.addProcess(p1); // 10 de tempo

    // Segunda chamada: deve continuar com p3
    Process secondSelected = srtfScheduler.selectNextProcess().getObject();
    assertEquals(p3, secondSelected, "Deveria continuar com p3, pois p1 é mais longo");
  }

  @Test
  @DisplayName("PREEMPÇÃO: Deve trocar para um novo processo se ele for mais curto")
  void preemption_WhenShorterProcessArrives_ShouldSwitchProcess() {
    srtfScheduler.addProcess(p1); // 10 de tempo

    // Primeira chamada: p1 começa a executar
    Process current = srtfScheduler.selectNextProcess().getObject();
    assertEquals(p1, current);

    // Simula p1 executando por 3 ciclos
    current.setRemainingTime(7);

    // Chega um processo novo e mais curto!
    srtfScheduler.addProcess(p2); // 3 de tempo

    // Próxima chamada: Deve ocorrer a preempção e p2 deve ser selecionado
    Process next = srtfScheduler.selectNextProcess().getObject();
    assertEquals(p2, next, "Deveria ter preemptado p1 e selecionado p2");
  }

  @Test
  @DisplayName("Após um processo terminar, deve selecionar o próximo mais curto da fila")
  void selectNextProcess_AfterProcessCompletion_ShouldSelectNextShortest() {
    srtfScheduler.addProcess(p1); // 10
    srtfScheduler.addProcess(p3); // 5

    // Primeira chamada: p3 é selecionado
    Process current = srtfScheduler.selectNextProcess().getObject();
    assertEquals(p3, current);

    // Simula o término de p3
    current.setRemainingTime(0);

    // Próxima chamada: deve selecionar p1, pois é o único que restou
    Process next = srtfScheduler.selectNextProcess().getObject();
    assertEquals(p1, next);
  }

  @Test
  @DisplayName("Em caso de empate no tempo restante, deve favorecer o processo atual")
  void tieBreaking_ShouldFavorCurrentProcess() {
    srtfScheduler.addProcess(p1); // 10 de tempo

    // p1 começa a executar
    Process current = srtfScheduler.selectNextProcess().getObject();

    // Simula p1 executando até ter 5 de tempo restante
    current.setRemainingTime(5);

    // Chega p3, que também tem 5 de tempo restante
    srtfScheduler.addProcess(p3);

    // Próxima chamada: NÃO deve haver preempção, pois p3 não é estritamente mais curto
    Process next = srtfScheduler.selectNextProcess().getObject();
    assertEquals(p1, next, "Não deveria preemptar em caso de empate no tempo restante");
  }
}
