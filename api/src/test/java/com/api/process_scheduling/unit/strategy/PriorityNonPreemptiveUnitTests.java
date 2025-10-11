package com.api.process_scheduling.unit.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.api.process_scheduling.entities.Process;
import com.api.process_scheduling.services.impl.strategy.PriorityNonPreemptive;
import com.panfutov.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PriorityNonPreemptiveUnitTests {

  private PriorityNonPreemptive scheduler;
  private Process p_low, p_mid, p_high;

  @BeforeEach
  void setUp() {
    scheduler = new PriorityNonPreemptive();

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
  @DisplayName("NÃO PREEMPÇÃO: Deve continuar com o processo atual mesmo que um de maior prioridade chegue")
  void nonPreemption_ShouldContinueWithCurrentProcess_EvenIfHigherPriorityArrives() {
    // 1. Começa com o processo de baixa prioridade na fila
    scheduler.addProcess(p_low);

    // 2. A primeira seleção pega o p_low, pois é o único
    Process current = scheduler.selectNextProcess().getObject();
    assertEquals(p_low, current, "Deveria ter iniciado com p_low");

    // 3. Agora, um processo de prioridade MUITO ALTA chega na fila
    scheduler.addProcess(p_high);

    // 4. Na próxima seleção, o escalonador DEVE IGNORAR p_high e continuar com p_low
    Process next = scheduler.selectNextProcess().getObject();
    assertEquals(p_low, next, "NÃO deveria ter preemptado! Tinha que continuar com p_low");
  }

  @Test
  @DisplayName("Após um processo terminar, deve selecionar o próximo de maior prioridade da fila")
  void selectNextProcess_AfterProcessCompletion_ShouldSelectNextHighestPriority() {
    scheduler.addProcess(p_mid);
    scheduler.addProcess(p_low);
    scheduler.addProcess(p_high);

    // Primeira chamada: seleciona p_high
    Process current = scheduler.selectNextProcess().getObject();
    assertEquals(p_high, current);

    // Simula o término de p_high
    current.setRemainingTime(0);

    // Próxima chamada: deve selecionar o próximo mais prioritário (p_mid)
    Process next = scheduler.selectNextProcess().getObject();
    assertEquals(p_mid, next, "Após p_high terminar, deveria selecionar p_mid");

    // Simula o término de p_mid
    next.setRemainingTime(0);

    // Próxima chamada: deve selecionar o último que restou (p_low)
    Process last = scheduler.selectNextProcess().getObject();
    assertEquals(p_low, last, "Após p_mid terminar, deveria selecionar p_low");
  }
}