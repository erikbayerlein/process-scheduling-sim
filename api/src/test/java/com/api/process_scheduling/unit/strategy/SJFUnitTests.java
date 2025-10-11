package com.api.process_scheduling.unit.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.api.process_scheduling.services.impl.strategy.SJF;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SJFUnitTests {

  private SJF sjfScheduler;
  private com.api.process_scheduling.entities.Process p1;
  private com.api.process_scheduling.entities.Process p2;

  @BeforeEach
  void setUp() {
    // Roda antes de cada teste, garantindo um estado limpo
    sjfScheduler = new SJF();
    p1 = new com.api.process_scheduling.entities.Process(1, 5, 1);
    p2 = new com.api.process_scheduling.entities.Process(2, 3, 1);
  }

  @Test
  @DisplayName("Deve adicionar processos à fila de prontos")
  void addProcess_ShouldAddProcessToQueue() {
    sjfScheduler.addProcess(p1);

    assertEquals(1, this.sjfScheduler.getReadyQueue().size());
  }

  @Test
  @DisplayName("Deve retornar null se a fila de prontos estiver vazia")
  void selectNextProcess_WithEmptyQueue_ShouldReturnFailure() {
    var result = sjfScheduler.selectNextProcess();
    assertTrue(result.isSuccess());
    assertNull(result.getObject());
  }

  @Test
  @DisplayName("Deve selecionar o processo com menor tempo de duração")
  void selectNextProcess_ShouldReturnShortestProcessInQueue() {
    sjfScheduler.addProcess(p1);
    sjfScheduler.addProcess(p2);

    var result = sjfScheduler.selectNextProcess();

    assertTrue(result.isSuccess());
    assertEquals(p2, result.getObject());
  }

  @Test
  @DisplayName("SJF não é preemptivo: deve retornar o mesmo processo até que ele termine")
  void selectNextProcess_MultipleCalls_ShouldReturnSameProcessUntilCompleted() {
    sjfScheduler.addProcess(p1);
    sjfScheduler.addProcess(p2);

    // Primeira chamada: Pega o p2
    var firstCallResult = sjfScheduler.selectNextProcess();
    assertEquals(p2, firstCallResult.getObject());

    // Segunda chamada: Como p1 não terminou, deve retornar p1 DE NOVO
    var secondCallResult = sjfScheduler.selectNextProcess();
    assertEquals(p2, secondCallResult.getObject());
  }

  @Test
  @DisplayName("Após um processo terminar, deve selecionar o próximo da fila")
  void selectNextProcess_AfterProcessCompletion_ShouldReturnNextProcess() {
    sjfScheduler.addProcess(p1);
    sjfScheduler.addProcess(p2);

    // Pega o primeiro processo
    var runningProcess = sjfScheduler.selectNextProcess().getObject();
    assertEquals(p2, runningProcess);

    // Simula o término do primeiro processo
    runningProcess.setRemainingTime(0);

    // Agora, a próxima chamada deve retornar o p1
    var nextProcess = sjfScheduler.selectNextProcess().getObject();
    assertEquals(p1, nextProcess);
  }
}
