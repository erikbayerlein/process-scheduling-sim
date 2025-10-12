package com.api.process_scheduling.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.api.process_scheduling.dto.ProcessCompleteEvent;
import com.api.process_scheduling.dto.ProcessDTOMessage;
import com.api.process_scheduling.dto.SimulationCompletedEvent;
import com.api.process_scheduling.dto.SimulationConfigMessage;
import com.api.process_scheduling.entities.Process;
import com.api.process_scheduling.enums.Algorithms;
import com.api.process_scheduling.services.impl.SchedulerServiceImpl;
import com.api.process_scheduling.services.impl.strategy.schedulingAlgorithm;
import com.panfutov.result.Result;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

// Usa a extensão do Mockito para inicializar os mocks automaticamente
@ExtendWith(MockitoExtension.class)
class SchedulerServiceImplTests {

  // Cria uma instância real de SchedulerServiceImpl e injeta os mocks abaixo nela
  @InjectMocks
  private SchedulerServiceImpl schedulerService;

  // Cria um mock da nossa dependência de mensagens
  @Mock
  private SimpMessagingTemplate messagingTemplate;

  // Cria um mock do nosso algoritmo para podermos controlar seu comportamento
  @Mock
  private schedulingAlgorithm algorithm;

  // NOTA: Para este teste funcionar, a classe SchedulerServiceImpl precisa de uma forma
  // de injetar o mock do algoritmo. Uma maneira é ter um setter package-private
  // ou modificar o SchedulingAlgorithmFactory para poder retornar um mock.
  // Por simplicidade, assumiremos que podemos injetar o mock.

  @Test
  @DisplayName("Deve executar uma simulação FCFS simples e calcular as métricas corretamente")
  void runSimulation_SimpleFCFSScenario_ShouldCalculateMetricsCorrectly() {
    // Arrange

    // Define os processos de entrada
    var p1Dto = new ProcessDTOMessage(0, 3, 1); // Chega em t=0, dura 3
    var p2Dto = new ProcessDTOMessage(1, 2, 1); // Chega em t=1, dura 2

    var config = new SimulationConfigMessage(
        Algorithms.FCFS,
        List.of(p1Dto, p2Dto),
        new SimulationConfigMessage.GlobalConfig(0, 0) // Quantum não é usado
    );

    schedulerService.setupSimulation(config, messagingTemplate);

    // Nossos objetos de processo reais que serão usados na simulação
    List<Process> processosReais = (List<Process>) ReflectionTestUtils.getField(schedulerService,
        "processQueue");

    Process p1 = processosReais.stream().filter(p -> p.getCreationTime() == 0).findFirst()
        .orElseThrow(
            () -> new AssertionError("Processo p1 (creationTime 0) não encontrado na fila")
        );
    Process p2 = processosReais.stream().filter(p -> p.getCreationTime() == 1).findFirst()
        .orElseThrow(
            () -> new AssertionError("Processo p2 (creationTime 1) não encontrado na fila")
        );

    // ** O "Roteiro" para o nosso algoritmo mockado **
    // Faremos o mock se comportar como um FCFS.
    when(algorithm.selectNextProcess())
        .thenReturn(Result.success(p1)) // t=0: seleciona p1
        .thenReturn(Result.success(p1)) // t=1: continua com p1
        .thenReturn(Result.success(p1)) // t=2: continua com p1
        .thenReturn(Result.success(p2)) // t=3: p1 terminou, seleciona p2
        .thenReturn(Result.success(p2)); // t=4: continua com p2

    // Configura o serviço para usar nosso algoritmo mockado
    // (Isso pode exigir um método setter ou o uso de ReflectionTestUtils do Spring)
    schedulerService.setAlgorithm(algorithm); // Supondo que este método exista para o teste

    ArgumentCaptor<ProcessCompleteEvent> processCompleteCaptor = ArgumentCaptor.forClass(
        ProcessCompleteEvent.class);
    ArgumentCaptor<SimulationCompletedEvent> simulationCompleteCaptor = ArgumentCaptor.forClass(
        SimulationCompletedEvent.class);

    // ACT
    schedulerService.runSimulation();

    // ASSERT

    // Verificamos que o método foi chamado 2 vezes e capturamos TODOS os argumentos
    verify(messagingTemplate, times(2)).convertAndSend(
        eq("/process-scheduler/process/completed"),
        processCompleteCaptor.capture()
    );

    // Verificamos o evento final e capturamos o argumento
    verify(messagingTemplate, times(1)).convertAndSend(
        eq("/process-scheduler/simulation/completed"),
        simulationCompleteCaptor.capture()
    );

    var completedEvents = processCompleteCaptor.getAllValues();
    var p1Event = completedEvents.stream().filter(e -> e.pid().equals(p1.getPid()))
        .findFirst().orElseThrow(
            () -> new AssertionError("Evento de conclusão do processo p1 não encontrado")
        );
    var p2Event = completedEvents.stream().filter(e -> e.pid().equals(p2.getPid()))
        .findFirst().orElseThrow(
            () -> new AssertionError("Evento de conclusão do processo p2 não encontrado")
        );

    // Asserções nos dados dos eventos
    assertEquals(3, p1Event.tt(), "P1 Turnaround Time no evento");
    assertEquals(0, p1Event.wt(), "P1 Waiting Time no evento");

    assertEquals(4, p2Event.tt(), "P2 Turnaround Time no evento");
    assertEquals(2, p2Event.wt(), "P2 Waiting Time no evento");

    // Asserções no evento final
    SimulationCompletedEvent finalEvent = simulationCompleteCaptor.getValue();
    assertEquals(2, finalEvent.totalContextSwitches());
    assertEquals(3.5, finalEvent.averageTurnaroundTime()); // (3+4)/2
    assertEquals(1.0, finalEvent.averageWaitingTime());    // (0+2)/2
  }


  @Test
  @DisplayName("Deve lidar com ciclos ociosos e calcular as métricas corretamente")
  void runSimulation_WithIdleCycles_ShouldCalculateMetricsCorrectly() {
    // Arrange

    // Cenário: P1 termina antes de P2 chegar, criando um "buraco"
    var p1Dto = new ProcessDTOMessage(0, 2, 1); // Chega em t=0, dura 2
    var p2Dto = new ProcessDTOMessage(4, 1, 1); // Chega em t=4, dura 1

    var config = new SimulationConfigMessage(
        Algorithms.FCFS, // O algoritmo não importa, pois estamos a controlar o mock
        List.of(p1Dto, p2Dto),
        new SimulationConfigMessage.GlobalConfig(0, 0)
    );

    schedulerService.setupSimulation(config, messagingTemplate);

    // Pegamos referências para os processos REAIS que o serviço criou
    @SuppressWarnings("unchecked")
    List<Process> processosReais = (List<Process>) ReflectionTestUtils.getField(schedulerService, "processQueue");
    assertNotNull(processosReais);
    Process p1 = processosReais.stream().filter(p -> p.getCreationTime() == 0).findFirst()
        .orElseThrow(() -> new AssertionError("Processo p1 não encontrado"));
    Process p2 = processosReais.stream().filter(p -> p.getCreationTime() == 4).findFirst()
        .orElseThrow(() -> new AssertionError("Processo p2 não encontrado"));

    // ** O "Roteiro" para o nosso algoritmo mockado com ciclos ociosos **
    when(algorithm.selectNextProcess())
        .thenReturn(Result.success(p1))   // t=0: P1 é selecionado
        .thenReturn(Result.success(p1))   // t=1: P1 continua
        .thenReturn(Result.success(null)) // t=2: P1 terminou. Fila vazia. CPU OCIOSA.
        .thenReturn(Result.success(null)) // t=3: P2 ainda não chegou. CPU OCIOSA.
        .thenReturn(Result.success(p2));  // t=4: P2 chega e é selecionado

    schedulerService.setAlgorithm(algorithm);

    // Criamos os "capturadores" de argumentos
    ArgumentCaptor<ProcessCompleteEvent> processCompleteCaptor = ArgumentCaptor.forClass(ProcessCompleteEvent.class);
    ArgumentCaptor<SimulationCompletedEvent> simulationCompleteCaptor = ArgumentCaptor.forClass(SimulationCompletedEvent.class);

    // ===================================
    // 2. EXECUÇÃO (Act)
    // ===================================
    schedulerService.runSimulation();

    // ===================================
    // 3. VERIFICAÇÃO (Assert)
    // ===================================

    verify(messagingTemplate, times(2)).convertAndSend(
        eq("/process-scheduler/process/completed"),
        processCompleteCaptor.capture()
    );
    verify(messagingTemplate, times(1)).convertAndSend(
        eq("/process-scheduler/simulation/completed"),
        simulationCompleteCaptor.capture()
    );

    var completedEvents = processCompleteCaptor.getAllValues();
    var p1Event = completedEvents.stream().filter(e -> e.pid().equals(p1.getPid())).findFirst().orElseThrow();
    var p2Event = completedEvents.stream().filter(e -> e.pid().equals(p2.getPid())).findFirst().orElseThrow();

    // Asserções para P1
    // Chegou em t=0, durou 2 (executou em t=0, t=1), terminou no fim de t=1 (tempo de conclusão = 2)
    assertEquals(2, p1Event.tt(), "P1 Turnaround Time"); // TT = 2 - 0 = 2
    assertEquals(0, p1Event.wt(), "P1 Waiting Time");    // WT = 2 - 2 = 0

    // Asserções para P2
    // Chegou em t=4, durou 1 (executou em t=4), terminou no fim de t=4 (tempo de conclusão = 5)
    assertEquals(1, p2Event.tt(), "P2 Turnaround Time"); // TT = 5 - 4 = 1
    assertEquals(0, p2Event.wt(), "P2 Waiting Time");    // WT = 1 - 1 = 0

    // Asserções no evento final
    SimulationCompletedEvent finalEvent = simulationCompleteCaptor.getValue();
    // Trocas de contexto: (null -> P1), (P1 -> null), (null -> P2) = 3 trocas
    assertEquals(3, finalEvent.totalContextSwitches());
    assertEquals(1.5, finalEvent.averageTurnaroundTime()); // (2+1)/2
    assertEquals(0.0, finalEvent.averageWaitingTime());    // (0+0)/2
  }
}