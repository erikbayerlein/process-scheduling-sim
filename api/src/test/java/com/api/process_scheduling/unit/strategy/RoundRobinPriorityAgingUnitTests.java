package com.api.process_scheduling.unit.strategy;


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.api.process_scheduling.entities.Process;
import com.api.process_scheduling.services.impl.strategy.RoundRobinPriorityAging;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoundRobinPriorityAgingUnitTests {

  private RoundRobinPriorityAging scheduler;
  private Process p_low, p_mid, p_high;

  @BeforeEach
  void setUp() {
    scheduler = new RoundRobinPriorityAging();

    p_low = new Process(1, 10, 5);  // Prioridade Baixa
    p_mid = new Process(2, 10, 10); // Prioridade Média
    p_high = new Process(3, 10, 15); // Prioridade Alta
  }

  @Test
  @DisplayName("Com CPU ociosa, deve selecionar o processo de maior prioridade")
  void selectNextProcess_WhenCPUIsIdle_ShouldSelectHighestPriority() {
    scheduler.addProcess(p_low);
    scheduler.addProcess(p_high);
    scheduler.addProcess(p_mid);

    Process selected = scheduler.selectNextProcess().getObject();
    assertEquals(p_high, selected);
  }

  @Test
  @DisplayName("PREEMPÇÃO: Deve trocar para um novo processo se a prioridade for estritamente maior")
  void preemption_WhenHigherPriorityProcessArrives_ShouldSwitch() {
    // Inicia com o processo de prioridade média
    scheduler.addProcess(p_mid);
    Process current = scheduler.selectNextProcess().getObject();
    assertEquals(p_mid, current);

    // Chega um processo de prioridade alta
    scheduler.addProcess(p_high);

    // Na próxima seleção, deve ocorrer a preempção
    Process next = scheduler.selectNextProcess().getObject();
    assertEquals(p_high, next, "Deveria ter preemptado p_mid e selecionado p_high");
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
  @DisplayName("AGING: Deve preemptar um processo após o 'aging' aumentar a prioridade de outro")
  void aging_ShouldInfluenceSchedulingAndCausePreemption() {
    // p_mid (Prio 10) começa a executar
    scheduler.addProcess(p_mid);
    Process current = scheduler.selectNextProcess().getObject();
    assertEquals(p_mid, current);

    // p_low_aging (Prio 9) chega e fica na fila
    Process p_low_aging = new Process(4, 10, 9);
    scheduler.addProcess(p_low_aging);

    // simula várias chamadas para aplicar aging
    scheduler.applyAgingToReady();
    scheduler.applyAgingToReady();

    // Próxima chamada: o aging deve ocorrer e p_low_aging deve tomar o lugar de p_mid
    Process next = scheduler.selectNextProcess().getObject();

    assertEquals(p_low_aging, next,
        "O aging deveria aumentar a prioridade de p_low_aging e causar a preempção");
  }

  @Test
  @DisplayName("AGING: Deve resetar a prioridade ao ser selecionado")
  void aging_ShouldResetPriorityWhenSelected() {
    // p_low (Prio 5) começa a executar
    scheduler.addProcess(p_low);
    scheduler.addProcess(p_mid);
    p_mid.setRemainingTime(1);
    Process current = scheduler.selectNextProcess().getObject();
    assertEquals(p_mid, current);

    // Simula várias chamadas para aplicar aging
    for (int i = 0; i < 5; i++) {
      scheduler.applyAgingToReady();
    }
    assertEquals(11, p_low.getDynamicPriority(), "A prioridade de p_low deveria ter aumentado para 11 devido ao aging");

    // Na próxima seleção, o aging deve ter aumentado a prioridade de p_low
    Process next = scheduler.selectNextProcess().getObject();
    assertEquals(p_low, next, "p_low deveria ser selecionado novamente");

    // Verifica se a prioridade foi resetada
    assertEquals(5, p_low.getDynamicPriority(), "A prioridade de p_low deveria ter sido resetada para 5");
  }
}
