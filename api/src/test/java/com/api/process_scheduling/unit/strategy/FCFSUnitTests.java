package com.api.process_scheduling.unit.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.api.process_scheduling.entities.Process;
import com.api.process_scheduling.services.impl.strategy.FCFS;
import com.panfutov.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FCFSUnitTests {

  private FCFS fcfsScheduler;
  private Process p1;
  private Process p2;

  @BeforeEach
  void setUp() {
    // Roda antes de cada teste, garantindo um estado limpo
    fcfsScheduler = new FCFS();
    p1 = new Process(1, 5, 1);
    p2 = new Process(2, 3, 1);
  }

  @Test
  @DisplayName("Deve adicionar processos à fila de prontos")
  void addProcess_ShouldAddProcessToQueue() {
    fcfsScheduler.addProcess(p1);

    assertEquals(1, this.fcfsScheduler.getReadyQueue().size());
  }

  @Test
  @DisplayName("Deve retornar falha se a fila de prontos estiver vazia")
  void selectNextProcess_WithEmptyQueue_ShouldReturnFailure() {
    Result<Process> result = fcfsScheduler.selectNextProcess();
    assertTrue(result.isSuccess());
    assertNull(result.getObject(), "Expected null when no processes are in the queue");
  }

  @Test
  @DisplayName("Deve selecionar o primeiro processo que chegou")
  void selectNextProcess_ShouldReturnFirstProcessAdded() {
    fcfsScheduler.addProcess(p1);
    fcfsScheduler.addProcess(p2);

    Result<Process> result = fcfsScheduler.selectNextProcess();

    assertTrue(result.isSuccess());
    assertEquals(p1, result.getObject());
  }

  @Test
  @DisplayName("FCFS não é preemptivo: deve retornar o mesmo processo até que ele termine")
  void selectNextProcess_MultipleCalls_ShouldReturnSameProcessUntilCompleted() {
    fcfsScheduler.addProcess(p1);

    // Primeira chamada: Pega o p1
    Result<Process> firstCallResult = fcfsScheduler.selectNextProcess();
    assertEquals(p1, firstCallResult.getObject());

    // Segunda chamada: Como p1 não terminou, deve retornar p1 DE NOVO
    Result<Process> secondCallResult = fcfsScheduler.selectNextProcess();
    assertEquals(p1, secondCallResult.getObject());
  }

  @Test
  @DisplayName("Após um processo terminar, deve selecionar o próximo da fila")
  void selectNextProcess_AfterProcessCompletion_ShouldReturnNextProcess() {
    fcfsScheduler.addProcess(p1);
    fcfsScheduler.addProcess(p2);

    // Pega o primeiro processo
    Process runningProcess = fcfsScheduler.selectNextProcess().getObject();
    assertEquals(p1, runningProcess);

    // Simula o término do primeiro processo
    runningProcess.setRemainingTime(0);

    // Agora, a próxima chamada deve retornar o p2
    Process nextProcess = fcfsScheduler.selectNextProcess().getObject();
    assertEquals(p2, nextProcess);
  }
}
