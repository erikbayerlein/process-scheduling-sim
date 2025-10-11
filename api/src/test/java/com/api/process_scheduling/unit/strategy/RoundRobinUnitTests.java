package com.api.process_scheduling.unit.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.api.process_scheduling.entities.Process;
import com.api.process_scheduling.services.impl.strategy.RoundRobin;
import com.panfutov.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoundRobinUnitTests {

  private final int QUANTUM = 3;
  private RoundRobin rrScheduler;
  private Process p1, p2, p3;

  @BeforeEach
  void setUp() {
    rrScheduler = new RoundRobin(QUANTUM);
    p1 = new Process(1, 10, 1); // Processo longo
    p2 = new Process(2, 2, 1);  // Processo curto
    p3 = new Process(3, 5, 1);  // Processo médio
  }

  @Test
  @DisplayName("Deve lançar exceção se o quantum for inválido")
  void constructor_WithInvalidQuantum_ShouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> new RoundRobin(0));
    assertThrows(IllegalArgumentException.class, () -> new RoundRobin(-1));
  }

  @Test
  @DisplayName("Deve retornar nulo se a CPU estiver ociosa e a fila vazia")
  void selectNextProcess_WithEmptyQueue_ShouldReturnNull() {
    Result<Process> result = rrScheduler.selectNextProcess();
    assertTrue(result.isSuccess());
    assertNull(result.getObject());
  }

  @Test
  @DisplayName("Deve continuar executando o mesmo processo DENTRO do quantum")
  void selectNextProcess_ShouldContinueProcess_WithinQuantum() {
    rrScheduler.addProcess(p1);

    // Ciclo 1: Pega p1
    assertEquals(p1, rrScheduler.selectNextProcess().getObject());
    // Ciclo 2: Continua com p1
    assertEquals(p1, rrScheduler.selectNextProcess().getObject());
  }

  @Test
  @DisplayName("EXPIRAÇÃO DO QUANTUM: Deve trocar para o próximo processo")
  void quantumExpiration_ShouldSwitchToNextProcess() {
    rrScheduler.addProcess(p1);
    rrScheduler.addProcess(p2);

    // Executa p1 por todo o seu quantum
    assertEquals(p1, rrScheduler.selectNextProcess().getObject(), "Ciclo 1: P1");
    assertEquals(p1, rrScheduler.selectNextProcess().getObject(), "Ciclo 2: P1");
    assertEquals(p1, rrScheduler.selectNextProcess().getObject(), "Ciclo 3: P1");

    // Ciclo 4: O quantum de P1 expirou, deve trocar para P2
    assertEquals(p2, rrScheduler.selectNextProcess().getObject(), "Ciclo 4: Deveria ser P2");
  }

  @Test
  @DisplayName("TÉRMINO ANTECIPADO: Deve trocar para o próximo processo imediatamente")
  void processCompletion_BeforeQuantumExpires_ShouldSwitchImmediately() {
    rrScheduler.addProcess(p2); // p2 dura apenas 2 ciclos
    rrScheduler.addProcess(p3);

    // Ciclo 1: Executa p2
    Process current = rrScheduler.selectNextProcess().getObject();
    assertEquals(p2, current);
    current.setRemainingTime(1); // Simula 1 ciclo de execução

    // Ciclo 2: Executa p2
    current = rrScheduler.selectNextProcess().getObject();
    assertEquals(p2, current);
    current.setRemainingTime(0); // Simula o término de p2

    // Ciclo 3: Como p2 terminou, deve selecionar p3 IMEDIATAMENTE, sem esperar o quantum.
    Process next = rrScheduler.selectNextProcess().getObject();
    assertEquals(p3, next, "Deveria selecionar P3 pois P2 terminou");
  }

  @Test
  @DisplayName("CIRCULAÇÃO: Deve retornar ao primeiro processo após todos executarem sua vez")
  void queueCirculation_ShouldReturnToFirstProcess() {
    // Quantum é 3
    rrScheduler.addProcess(p1); // p1 (dura 10)
    rrScheduler.addProcess(p3); // p3 (dura 5)

    // Gasta o quantum de p1 (3 ciclos)
    for (int i = 0; i < QUANTUM; i++) {
      rrScheduler.selectNextProcess();
    }

    // Gasta o quantum de p3 (3 ciclos)
    for (int i = 0; i < QUANTUM; i++) {
      assertEquals(p3, rrScheduler.selectNextProcess().getObject());
    }

    // Agora, p1 deve ter voltado para o início da fila
    assertEquals(p1, rrScheduler.selectNextProcess().getObject(), "Deveria voltar para P1");
  }
}