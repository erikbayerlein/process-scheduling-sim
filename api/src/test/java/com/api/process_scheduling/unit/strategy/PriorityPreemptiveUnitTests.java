package com.api.process_scheduling.unit.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.api.process_scheduling.entities.Process;
import com.api.process_scheduling.services.impl.strategy.PriorityPreemptive;
import com.panfutov.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PriorityPreemptiveUnitTests {

  private PriorityPreemptive scheduler;
  private Process p_low, p_mid, p_high;

  @BeforeEach
  void setUp() {
    scheduler = new PriorityPreemptive();

    p_low = new Process(1, 10, 5);  // Prioridade Baixa
    p_mid = new Process(2, 5, 10);  // Prioridade Média
    p_high = new Process(3, 3, 15); // Prioridade Alta
  }

  @Test
  @DisplayName("Deve retornar nulo se a fila estiver vazia e não houver processo corrente")
  void selectNextProcess_WithEmptyQueue_ShouldReturnNull() {
    Result<Process> result = scheduler.selectNextProcess();
    assertTrue(result.isSuccess());
    assertNull(result.getObject());
  }

  @Test
  @DisplayName("Com CPU ociosa, deve selecionar o processo de maior prioridade")
  void selectNextProcess_WhenCPUIsIdle_ShouldSelectHighestPriority() {
    scheduler.addProcess(p_low);
    scheduler.addProcess(p_high);
    scheduler.addProcess(p_mid);

    Process selected = scheduler.selectNextProcess().getObject();
    assertEquals(p_high, selected, "Deveria selecionar o processo de maior prioridade (p_high)");
  }

  @Test
  @DisplayName("PREEMPÇÃO: Deve interromper o processo atual se um de maior prioridade chegar")
  void preemption_WhenHigherPriorityProcessArrives_ShouldSwitchProcess() {
    // 1. Começa com o processo de prioridade média
    scheduler.addProcess(p_mid);
    Process current = scheduler.selectNextProcess().getObject();
    assertEquals(p_mid, current, "Deveria ter iniciado com p_mid");

    // 2. Agora, um processo de prioridade ALTA chega na fila
    scheduler.addProcess(p_high);

    // 3. Na próxima seleção, o escalonador DEVE INTERROMPER p_mid e selecionar p_high
    Process next = scheduler.selectNextProcess().getObject();
    assertEquals(p_high, next, "DEVERIA ter preemptado p_mid para executar p_high");
  }

  @Test
  @DisplayName("NÃO PREEMPÇÃO: Deve continuar se a prioridade do novo for igual ou menor")
  void noPreemption_WhenEqualOrLowerPriorityArrives() {
    // Inicia com o processo de prioridade média
    scheduler.addProcess(p_mid);
    Process current = scheduler.selectNextProcess().getObject();
    assertEquals(p_mid, current);

    // Chega um processo de prioridade menor e um de prioridade igual
    Process p_mid_2 = new Process(4, 10, 10);
    scheduler.addProcess(p_low);
    scheduler.addProcess(p_mid_2);

    // Na próxima seleção, deve continuar com o processo original
    Process next = scheduler.selectNextProcess().getObject();
    assertEquals(p_mid, next, "Não deveria preemptar por prioridade igual ou menor");
  }

  @Test
  @DisplayName("Após um processo terminar, deve selecionar o próximo de maior prioridade")
  void selectNextProcess_AfterProcessCompletion_ShouldSelectNextHighestPriority() {
    scheduler.addProcess(p_low);
    scheduler.addProcess(p_mid);

    // Primeira chamada: p_mid é selecionado
    Process current = scheduler.selectNextProcess().getObject();
    assertEquals(p_mid, current);

    // Simula o término de p_mid
    current.setRemainingTime(0);

    // Próxima chamada: deve selecionar o próximo mais prioritário (p_low)
    Process next = scheduler.selectNextProcess().getObject();
    assertEquals(p_low, next, "Após p_mid terminar, deveria selecionar p_low");
  }
}